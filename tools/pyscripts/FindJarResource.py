#!/usr/bin/env python3

'''
将指定目录下的 JAR 包解压到指定目录，并查找其中的 .properties 文件中是否有指定内容。
'''

import os.path
import shutil
import sys

import hsconfig


# 要查找的 JAR 包所在目录，通常为目标平台目录，若有多个请用逗号隔开
# -修改我-
FIND_PATHES = hsconfig.target_platform_path

# 解压 JAR 包的临时目录，如果不存在会自动创建，若无权限会报错
# -修改我-
TMP_PATH = hsconfig.temp_path


def getJarPathes(sourcePathes):
    '''取得指定目录下的所有 JAR 包路径，放在列表中返回'''
    jars = []
    for sourcePath in sourcePathes:
        if not os.path.isdir(sourcePath):
            raise Exception('目录 {} 不存在，请打开上述文件夹确认！'.format(sourcePath))
        for root, dirs, files in os.walk(sourcePath):
            for name in files:
                path = os.path.join(root, name)
                if '.jar' == os.path.splitext(path)[-1]:
                    jars.append(path)
    if not jars:
        raise Exception('未找到 jar 包。')
    print('找到 {} 个 JAR 文件'.format(len(jars)))
    return jars


def unzipJars(jarPathes):
    '''解压所有 JAR 包到临时目录'''
    print('正在解压 JAR 文件...')
    if not os.path.exists(TMP_PATH):
        os.mkdir(TMP_PATH)
    tmpPathes = []
    for jar in jarPathes:
        jarName = os.path.basename(jar)
        tmpPath = os.path.join(TMP_PATH, jarName.replace('.jar', ''))
        if not os.path.exists(tmpPath):
            shutil.unpack_archive(jar, tmpPath, 'zip')
        tmpPathes.append(tmpPath)
    if not tmpPathes:
        raise Exception('未能将 jar 包解压到临时目录。')
    return tmpPathes


def findString(findPathes, strToFind):
    '''找到所有 .properties 文件中的指定内容，并打印出来'''
    print('正在资源文件中查找 {}...'.format(strToFind))
    findCount = 0
    fileCount = 0
    resultCount = 0
    for findPath in findPathes:
        for root, dirs, files in os.walk(findPath):
            for name in files:
                path = os.path.join(root, name)
                if '.properties' == os.path.splitext(path)[-1]:
                    findCount += 1
                    found = False
                    prop = open(path)
                    for line in prop:
                        if strToFind in line:
                            found = True
                            print('在文件 {} 中找到：\n{}'.format(path.replace(TMP_PATH, ''), line))
                            resultCount += 1
                    if found:
                        fileCount += 1
    print('查找结束。共查找了 {} 个资源文件，在其中的 {} 个文件中找到 {} 个结果。'.format(findCount, fileCount, resultCount))


def main():
    if len(sys.argv) > 1:
        jarPathes = getJarPathes(FIND_PATHES)
        tmpPathes = unzipJars(jarPathes)
        strToFind = ascii(' '.join(sys.argv[1:])).replace("'", '')
        findString(tmpPathes, strToFind)
    else:
        print("请输入要查找的关键字（支持中文）并用单/双引号包围，举例：\n>>> {} '快捷键'".format(os.path.basename(sys.argv[0])))


if __name__ == '__main__':
    main()
