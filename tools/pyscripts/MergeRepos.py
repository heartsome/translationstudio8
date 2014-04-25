#!/usr/bin/env python3

'''
功能：
将几个更新站点的文件合并为一个：
1、合并 binary、features、plugins 目录中的文件，忽略同名文件；
2、合并 artifacts.jar、content.jar 文件中的相应索引信息。
'''

import os
import sys
import shutil
import logging
from zipfile import ZipFile, ZIP_DEFLATED
import xml.etree.ElementTree as etree

import hsconfig


BASE_PATH = hsconfig.build_path
DATE = hsconfig.build_prefix
SOURCE = ('F', 'P', 'L')
TARGET = 'U'
REPO = 'repository'
#TAG_ARTIFACTS = 'artifact'
#TAG_CONTENT = 'unit'
XP_ARTIFACT = './artifacts/artifact'
XP_CONTENT = './units/unit'
AFJ = 'artifacts.jar'
AFX = 'artifacts.xml'
CTJ = 'content.jar'
CTX = 'content.xml'


def cutBase(path):
    '''将基础目录去掉，主要用在日志或屏幕输出信息中，减少冗余、提高可读性'''
    return path.replace(BASE_PATH, '')


def getSourcePathes():
    '''生成要处理的来源更新站点路径，并断言其存在，最后以 list 返回'''
    sourcePathes = []
    for src in SOURCE:
        dirName = DATE + src  # # 取决于目录结构
        path = os.path.join(BASE_PATH, dirName, REPO)
        if not os.path.isdir(path):
            raise Exception('目录 {} 不存在，请打开上述文件夹确认！'.format(path))
        print('待合并的更新站点：{}'.format(cutBase(path)))
        logging.info('待合并的更新站点：{}'.format(cutBase(path)))
        sourcePathes.append(path)
    return sourcePathes


def getTargetPath():
    '''生成目标(也就是旗舰版)更新站点路径，并断言其存在'''
    path = os.path.join(BASE_PATH, DATE + TARGET, REPO)  # # 取决于目录结构
    if not os.path.isdir(path):
        raise Exception('目录 {} 不存在，请打开上述文件夹确认！'.format(path))
    print('将更新站点合并到：{}'.format(cutBase(path)))
    logging.info('将更新站点合并到：{}'.format(cutBase(path)))
    return path


def backupRepo(path, filename):
    '''备份要被修改的文件'''
    srcPath = os.path.join(path, filename)
    bakPath = os.path.join(path, filename + '.bak')
    print('将 {} 备份到 {}'.format(cutBase(srcPath), cutBase(bakPath)))
    logging.info('将 {} 备份到 {}'.format(cutBase(srcPath), cutBase(bakPath)))
    shutil.copy(srcPath, bakPath)


def mergeFiles(sourcePathes, targetPath):
    '''合并文件，将来源路径中有、而目标路径没有的文件复制到目标路径'''
    count = 0
    for srcRepo in sourcePathes:
        for root, dirs, files in os.walk(srcRepo):
            for name in files:
                path = os.path.join(root, name)
                tgtPath = path.replace(srcRepo, targetPath)
                if not os.path.exists(tgtPath):
                    print('复制文件 {}'.format(cutBase(path), cutBase(tgtPath)))
                    logging.info('复制文件 {}'.format(cutBase(path), cutBase(tgtPath)))
                    shutil.copyfile(path, tgtPath)
                    count += 1
    if count:
        print('共复制了 {} 个文件。'.format(count))
        logging.info('共复制了 {} 个文件。'.format(count))
    else:
        print('所有文件均已存在，无需复制。')
        logging.info('所有文件均已存在，无需复制。')


# def unzipJar(jarPath):
#     '''解压 JAR 文件'''
#     names = os.path.split(jarPath)
#     os.chdir(names[0])
#     jf = ZipFile(names[1])
#     jf.extractall()


def zipAsJar(path, names, jar):
    '''将指定路径下的文件以 ZIP 格式压缩为指定的 JAR 包'''
    os.chdir(path)
    jarPath = os.path.join(path, jar)
    if os.path.exists(jarPath):
        os.remove(jarPath)
    jf = ZipFile(jar, 'w', compression=ZIP_DEFLATED)
    for name in names:
        jf.write(name)
    jf.close()


#def getTargetInfos(dom, infoType):
#    '''minidom 版本：读取 artifacts.xml 或 content.xml 文件中的 artifact/unit 节点 id 及 version，以字典返回。
#    其中 infoType 应为常量 ARTIFACTS 或 CONTENT'''
#    assert(infoType in (TAG_ARTIFACTS, TAG_CONTENT))
#    results = {}
#    for result in dom.getElementsByTagName(infoType):
#        results[result.getAttribute('id')] = result.getAttribute('version')
#    return results


def getTargetInfos(root, infoType):
    '''etree 版本：读取 artifacts.xml 或 content.xml 文件中的 artifact/unit 节点 id 及 version，以字典返回。
    其中 infoType 应为常量 XP_ARTIFACT 或 XP_CONTENT'''
    if infoType not in (XP_ARTIFACT, XP_CONTENT):
        raise Exception('参数 {} 错误，应为常量 XP_ARTIFACT 或 XP_CONTENT。'.format(infoType))
    results = {}
    for result in root.findall(infoType):
        results[result.attrib['id']] = result.attrib['version']
    return results


def mergeJarIndexes(sourcePathes, targetPath):
    '''合并更新站点根目录下两个 JAR 包中的 XML 索引信息'''
    os.chdir(targetPath)

    print('正在解压 {} ...'.format(AFJ))
    logging.info('解压 {}...'.format(AFJ))
    ZipFile(AFJ).extractall()
    print('正在解压 {} ...'.format(CTJ))
    logging.info('解压 {}...'.format(CTJ))
    ZipFile(CTJ).extractall()

    tgtAFTree = etree.parse(AFX)
    targetArtifacts = getTargetInfos(tgtAFTree, XP_ARTIFACT)
    tgtAFElmt = tgtAFTree.find('./artifacts')

    tgtCTTree = etree.parse(CTX)
    targetUnits = getTargetInfos(tgtCTTree, XP_CONTENT)
    tgtCTElmt = tgtCTTree.find('./units')

    # 遍历来源更新站点
    for srcPath in sourcePathes:
        print('处理 {} ...'.format(cutBase(srcPath)))
        logging.info('处理 {} ...'.format(cutBase(srcPath)))
        os.chdir(srcPath)

        # 解压 JAR 并解析 XML 文件
        ZipFile(AFJ).extractall()
        afxTree = etree.parse(AFX)
        count = 0
        for artifact in afxTree.findall(XP_ARTIFACT):
            aid = artifact.attrib['id']
            if aid not in targetArtifacts:
                print('添加 artifact：{}'.format(aid))
                logging.info('添加 artifact：{}'.format(aid))
                # 在 XML DOM 树上添加
                tgtAFElmt.append(artifact)
                # 在缓存中添加
                targetArtifacts[aid] = artifact.attrib['version']
                count += 1
        if count:
            print('添加了 {} 个 artifact 节点'.format(count))
            logging.info('添加了 {} 个 artifact 节点'.format(count))
        else:
            print('artifact 无事可做')
            logging.info('artifact 无事可做')
        os.remove(AFX)

        # 另一个索引文件的处理同上
        ZipFile(CTJ).extractall()
        ctxTree = etree.parse(CTX)
        count = 0
        for unit in ctxTree.findall(XP_CONTENT):
            uid = unit.attrib['id']
            if uid not in targetUnits:
                print('添加 unit：{}'.format(uid))
                logging.info('添加 unit：{}'.format(uid))
                tgtCTElmt.append(unit)
                targetUnits[aid] = unit.attrib['version']
                count += 1
        if count:
            print('添加了 {} 个 unit 节点'.format(count))
            logging.info('添加了 {} 个 unit 节点'.format(count))
        else:
            print('unit 无事可做')
            logging.info('unit 无事可做')
        os.remove(CTX)

    os.chdir(targetPath)
    print('写入 {} 文件内容...'.format(os.path.join(cutBase(targetPath), AFX)))
    logging.info('写入 {} 文件内容...'.format(os.path.join(cutBase(targetPath), AFX)))
    tgtAFElmt.attrib['size'] = str(len(tgtAFTree.findall(XP_ARTIFACT)))
    tgtAFTree.write(AFX, encoding='utf-8')
    print('写入 {} 文件内容...'.format(os.path.join(cutBase(targetPath), CTX)))
    logging.info('写入 {} 文件内容...'.format(os.path.join(cutBase(targetPath), CTX)))
    tgtCTElmt.attrib['size'] = str(len(tgtCTTree.findall(XP_CONTENT)))
    tgtCTTree.write(CTX, encoding='utf-8')

    # 压缩为 JAR 并删除 XML
    zipAsJar(targetPath, [AFX, ], AFJ)
    os.remove(AFX)
    zipAsJar(targetPath, [CTX, ], CTJ)
    os.remove(CTX)


def main():
    script_path, script_name = os.path.split(os.path.realpath(sys.argv[0]))
    logging.basicConfig(
        filename='{}.log'.format(os.path.splitext(script_name)[0]),
        format='%(asctime)s %(message)s',
        level=logging.INFO)
    srcPathes = getSourcePathes()
    tgtPath = getTargetPath()
#     backupRepo(tgtPath, AFJ)
#     backupRepo(tgtPath, CTJ)
    mergeFiles(srcPathes, tgtPath)
    mergeJarIndexes(srcPathes, tgtPath)
    print('处理完成，本次处理的日志已保存在 {}.log 文件中。'.format(
        os.path.join(script_path, os.path.splitext(script_name)[0])))
    logging.info('处理完成\n')


if __name__ == '__main__':
    main()
