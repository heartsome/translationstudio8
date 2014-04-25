#!/usr/bin/env python3

'''
功能：
1、以已混淆的 UE 版更新站点为基础，将已混淆的 JAR 包复制到每个未混淆的安装包中覆盖同名文件以达到混淆的效果；
2、将处理好的文件夹改名；
3、将改名后的文件夹压缩为 ZIP 格式。
目录结构示例见最后面。
'''

import os
import sys
import shutil
import logging

import hsconfig

# -CHANGEME- 请根据实际打包路径修改
BASE_PATH = hsconfig.build_path
# -CHANGEME- 请根据实际打包日期修改（文件夹名称中的日期前缀）
DIR_PREFIX = hsconfig.build_prefix
# -CHANGEME- 请根据实际打包的版本号修改，用来重命名安装包文件夹
VERSION = hsconfig.build_version
# 以下内容如无特殊情况无需修改（其中的 U、F、P、L 为文件夹名称中的版本后缀，UE、PRO、PE、LITE 则用来重命名安装包文件夹）
EDITIONS = {'U': 'UE'}
# 产品名称前缀，用来重命名安装包文件夹
PKG_PREFIX = 'HSStudio'
# 更新站点目录名，使用默认的打包名称
SOURCE_DIR_NAME = 'repository'
# key 为用来重命名安装包文件夹的平台标识、value 为默认的打包目录名
BUILD_TYPES = {
    'Win_x86': 'win32.win32.x86',
    'Win_x64': 'win32.win32.x86_64',
    'Mac_x86': 'macosx.cocoa.x86',
    'Mac_x64': 'macosx.cocoa.x86_64',
    'Linux_x86': 'linux.gtk.x86',
    'Linux_x64': 'linux.gtk.x86_64'}
# 需要额外生成的安装包，与前者类似，key 为新的安装名标识、value 为要复制的源安装包标识
EXTRA_BUILDS = {
    'Win_x86_JRE': 'Win_x86',
    'Win_x64_JRE': 'Win_x64'}
# 额外需要复制的文件，key 为需要应用到的安装包类型、value 为要复制的文件路径及目标在安装包中的相对路径
EXTRA_FILES = hsconfig.build_copy_files
# 不需要的文件，如 Mac OS 下的 .DS_Store、Windows 下的 Thumb.db 等
FILES_TO_DELETE = ['.DS_Store', ]


def getRepoPath():
    '''按指定的规则生成更新站点的 plugins 子目录路径，并在断言其存在后返回'''
    path = os.path.join(BASE_PATH, DIR_PREFIX + 'U', SOURCE_DIR_NAME, 'plugins')  # DIR_PREFIX + 'U' 取决于目录结构
    if not os.path.isdir(path):
        raise Exception('目录 {} 不存在，请打开上述文件夹确认！'.format(path))
    return path


def getBuildPathes():
    '''按指定的规则生成安装包的 plugins 子目录路径，并在断言其存在后返回'''
    buildPathes = []
    for dirSuffix in EDITIONS.keys():
        dirName = DIR_PREFIX + dirSuffix  # 取决于目录结构
        for build in BUILD_TYPES.values():
            path = os.path.join(BASE_PATH, dirName, build, 'plugins')
            if not os.path.isdir(path):
                raise Exception('目录 {} 不存在，请打开上述文件夹确认！\n（打包时需要删除 Root directory 后面的 eclipse。）'.format(path))
            buildPathes.append(path)
    return buildPathes


def cpJars(repoPath, buildPathes):
    '''从指定的更新站点中复制 JAR 包到安装包目录'''
    for buildPath in buildPathes:
        print('正在复制混淆包 {}...'.format(buildPath.replace(BASE_PATH, '')))
        for name in os.listdir(buildPath):
            if name.startswith('net.heartsome') and name.endswith('.jar'):
                buildJarPath = os.path.join(buildPath, name)
                repoJarPath = os.path.join(repoPath, name)
                buildJarSize = os.path.getsize(buildJarPath)
                repoJarSize = os.path.getsize(repoJarPath)
                if buildJarSize != repoJarSize:
                    logging.info('正在覆盖 {}'.format(buildJarPath.replace(BASE_PATH, '')))
                    shutil.copyfile(repoJarPath, buildJarPath)


def renameDirs():
    '''将安装包目录名重命名为 HSStudio_8_0_0_Beta_Mac_x64 的形式'''
    for dirSuffix, editionType in EDITIONS.items():
        dirName = DIR_PREFIX + dirSuffix  # 取决于目录结构
        for buildName, buildDir in BUILD_TYPES.items():
            oldPath = os.path.join(BASE_PATH, dirName, buildDir)
            newBuildDir = '_'.join([PKG_PREFIX, editionType, VERSION, buildName])
            newPath = os.path.join(BASE_PATH, dirName, newBuildDir)
            print('正在重命名 {}...'.format(newPath.replace(BASE_PATH, '')))
            logging.info('将 {} 重命名为 {}'.format(oldPath.replace(BASE_PATH, ''), newPath.replace(BASE_PATH, '')))
            shutil.move(oldPath, newPath)


def cpExtraBuilds():
    '''根据 EXTRA_BUILDS 生成新的安装包'''
    if EXTRA_BUILDS:
        print('开始生成额外的安装包...')
        for dirSuffix in EDITIONS.keys():
            dirName = DIR_PREFIX + dirSuffix  # 取决于目录结构
            dirPath = os.path.join(BASE_PATH, dirName)  # 这是安装包所在的父目录路径
            for name in os.listdir(dirPath):
                for newBuild, oldBuild in EXTRA_BUILDS.items():
                    if name.endswith(oldBuild):  # 根据安装包目录名称后缀判断是否需要复制
                        path = os.path.join(dirPath, name)  # 源安装包路径
                        if os.path.isdir(path):
                            newName = name.replace(oldBuild, newBuild)
                            newPath = os.path.join(dirPath, newName)  # 新安装包路径
                            print('将 {} 复制为 {}...'.format(name, newName))
                            logging.info('将 {} 复制为 {}...'.format(name, newName))
                            shutil.copytree(path, newPath)
    else:
        print('无额外的安装包需要生成。')


def cpExtraFiles():
    '''根据 EXTRA_FILES 复制文件（如用户使用许可协议、帮助文档 PDF）到指定的安装包中'''
    if EXTRA_FILES:
        print('开始为安装包复制额外的文件...')
        for dirSuffix in EDITIONS.keys():
            dirName = DIR_PREFIX + dirSuffix  # 取决于目录结构
            dirPath = os.path.join(BASE_PATH, dirName)  # 这是安装包所在的父目录路径
            for name in os.listdir(dirPath):
                if name.startswith(PKG_PREFIX):
                    for targetBuild, pathes in EXTRA_FILES.items():
                        if targetBuild == 'all' or name.endswith(targetBuild):  # 根据安装包名称判断是否需要处理
                            print('为安装包 {} 复制额外文件...'.format(name))
                            logging.info('处理安装包 {} 复制额外文件...'.format(name))
                            path = os.path.join(dirPath, name)  # 安装包路径

                            if pathes[-1] == '.':  # 确定目标路径
                                tgtPath = path
                            else:
                                tgtPath = os.path.join(path, pathes[-1])

                            for source in pathes[:-1]:  # 确定源路径
                                for srcName in os.listdir(source):
                                    if not srcName.startswith('.'):
                                        srcPath = os.path.join(source, srcName)
                                        if os.path.isfile(srcPath):
                                            logging.info('复制文件 {} 到 {}...'.format(srcPath, tgtPath))
                                            shutil.copy(srcPath, tgtPath)
                                        elif os.path.isdir(srcPath):
                                            tgtDir = os.path.split(srcPath)[-1]
                                            tgtPath = os.path.join(tgtPath, tgtDir)
                                            logging.info('复制文件夹 {} 到 {}...'.format(srcPath, tgtPath))
                                            shutil.copytree(srcPath, tgtPath)

    else:
        print('无额外的文件需要复制。')


def delFiles():
    '''删除指定的文件，如 Mac OS 自动生成的 .DS_Store'''
    if FILES_TO_DELETE:
        print('开始删除不需要的文件：{}'.format('、'.join(FILES_TO_DELETE)))
        for dirSuffix in EDITIONS.keys():
            dirName = DIR_PREFIX + dirSuffix  # 取决于目录结构
            dirPath = os.path.join(BASE_PATH, dirName)  # 这是安装包所在的父目录路径
            for root, dirs, files in os.walk(dirPath):
                for name in files:
                    if name in FILES_TO_DELETE:
                        path = os.path.join(root, name)
                        logging.info('删除文件 {}...'.format(path))
                        os.remove(path)
    else:
        print('无额外的文件需要删除。')


def zipDirs():
    '''将安装包目录压缩为 ZIP 包'''
    for dirSuffix in EDITIONS.keys():
        dirName = DIR_PREFIX + dirSuffix  # 取决于目录结构
        path = os.path.join(BASE_PATH, dirName)
        os.chdir(path)
        for name in os.listdir(path):
            if name.startswith(PKG_PREFIX):
                fullPath = os.path.join(path, name)
                print('正在压缩 {}...'.format(fullPath.replace(BASE_PATH, '')))
                logging.info('压缩 {}'.format(fullPath.replace(BASE_PATH, '')))
                shutil.make_archive(name, 'zip', path, name)
                try:
                    shutil.rmtree(fullPath)
                except Exception as e:
                    logging.info('删除文件夹 {} 时出错：\n{}'.format(fullPath, e))
                    print('删除文件夹 {} 时出错，请手动删除。'.format(fullPath))


def main():
    script_path, script_name = os.path.split(os.path.realpath(sys.argv[0]))
    logging.basicConfig(
        filename='{}.log'.format(os.path.splitext(script_name)[0]),
        format='%(asctime)s %(message)s',
        level=logging.INFO)
    print('开始处理 {} 中的安装包...'.format(BASE_PATH))
    logging.info('开始处理 {} 中的安装包...'.format(BASE_PATH))

    #cpJars(getRepoPath(), getBuildPathes())
    renameDirs()
    cpExtraBuilds()
    cpExtraFiles()
    delFiles()
    zipDirs()

    print('处理完成，本次处理的日志已保存在 {}.log 文件中。'.format(
        os.path.join(script_path, os.path.splitext(script_name)[0])))
    logging.info('处理完成\n')

if __name__ == '__main__':
    main()

'''
目前的目录结构：
    <基础目录>
        |-1224U
        |   |-repository（已混淆）
        |   |-win32.win32.x86
        |   |-win32.win32.x86_64
        |   |-macosx.cocoa.x86_64
        |   |-linux.gtk.x86
        |   |-linux.gtk.x86_64
        |
        |-1224F
        |   |-repository
        |   |-win32.win32.x86
        |   |-...
        |
        |-1224P...
        |   |-repository
        |   |-win32.win32.x86
        |   |-...
        |
        |-1224L...
        |   |-repository
        |   |-win32.win32.x86
        |   |-...

处理后的目录结构：
    <基础目录>
        |-1224U
        |   |-repository（用 MergeRepos.py 合并其他三个版本的更新站点文件）
        |   |-HSStudio_UE_8_1_0_Win_x86.zip
        |   |-HSStudio_UE_8_1_0_Win_x86_JRE.zip
        |   |-HSStudio_UE_8_1_0_Win_x64.zip
        |   |-HSStudio_UE_8_1_0_Win_x64_JRE.zip
        |   |-HSStudio_UE_8_1_0_Mac_x64.zip
        |   |-HSStudio_UE_8_1_0_Linux_x86.zip
        |   |-HSStudio_UE_8_1_0_Linux_x64.zip
        |
        |-1224F
        |   |-repository
        |   |-HSStudio_PRO_8_1_0_Win_x86.zip
        |   |-...
        |
        |-1224P
        |   |-repository
        |   |-HSStudio_PE_8_1_0_Win_x86.zip
        |   |-...
        |
        |-1224L
        |   |-repository
        |   |-HSStudio_LITE_8_1_0_Win_x86.zip
        |   |-...
'''
