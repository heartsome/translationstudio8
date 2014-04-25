#!/usr/bin/env python3

import os
import os.path
import shutil
import logging


WS_PATH = '/Users/felix_lu/Documents/r8space'
COPYTO = '/Users/felix_lu/Desktop/R8L10N-zh'
COPYFROM = '/Users/felix_lu/Desktop/R8L10N-en'
PLUGIN_PREFIX = 'net.heartsome.cat.'
l10n_en = []
l10n_zh = []
l10n = []


class PropFile():
    '''处理每个资源文件的文件名映射、复制进/出工作空间的逻辑'''

    def __init__(self, zhPath):
        '''用中文版资源文件的绝对路径初始化对象'''
        assert(os.path.isabs(zhPath))
        self.zhPath = zhPath
        # 直接替换语言后缀，得到英文版和默认资源文件路径
        self.enPath = zhPath.replace('_zh', '_en')
        self.defaultPath = zhPath.replace('_zh', '')
        # 从路径中截取插件名称
        self.pluginName = zhPath.replace(WS_PATH, '').split(os.sep)[1]

    def mapName(self):
        '''文件名映射：去掉插件名称的前缀后、作为新文件名前缀，再与原文件名拼接'''
        prefix = self.pluginName.replace(PLUGIN_PREFIX, '')
        name = os.path.split(self.zhPath)[-1]
        return prefix + '.' + name

    def copyOut(self):
        '''将资源文件从工作空间复制到指定目录，并按上述映射规则改名'''
        if not os.path.exists(COPYTO):
            os.makedirs(COPYTO)
        targetPath = os.path.join(COPYTO, self.mapName())
        logging.info('复制 {0}\n为 {1}'.format(
                self.zhPath.replace(WS_PATH, ''),
                targetPath.replace(COPYTO, '')))
        shutil.copyfile(self.zhPath, targetPath)

    def copyEnIn(self):
        '''将资源文件从指定的目录复制到工作空间，并按映射规则反向改名，且修改为 _en 后缀'''
        sourcePath = os.path.join(COPYFROM, self.mapName())
        logging.info('复制 {0}\n为 {1}'.format(
                sourcePath.replace(COPYFROM, ''),
                self.enPath.replace(WS_PATH, '')))
        shutil.copyfile(sourcePath, self.enPath)

    def copyDefaultIn(self):
        '''将资源文件从指定的目录复制到工作空间，并按映射规则反向改名，且去掉后缀'''
        sourcePath = os.path.join(COPYFROM, self.mapName())
        logging.info('复制 {0}\n为 {1}'.format(
                sourcePath.replace(COPYFROM, ''),
                self.defaultPath.replace(WS_PATH, '')))
        shutil.copyfile(sourcePath, self.defaultPath)


def findWsPropFiles():
    '''列出工作空间中的所有中文版资源文件，保存在 List 对象中'''
    for root, dirs, files in os.walk(WS_PATH):
        # 去掉隐藏目录和 bin 目录
        if not root.startswith(WS_PATH + os.sep + '.') and (
                not (os.sep + 'bin' + os.sep) in root):
            for name in files:
                # 过滤出资源文件后缀名，并排除无用的资源文件
                if ('.properties' in name) and (name != 'build.properties')\
                        and (name != 'log4j.properties'):
                    path = os.path.join(root, name)
                    # 仅按文件名是否有 _zh 后缀来判断
                    if '_zh' in name:
                        l10n_zh.append(path)


def getPropFiles():
    '''列出指定目录中的所有资源文件，保存在 List 中返回'''
    result = []
    for root, dirs, files in os.walk(COPYFROM):
        # 去掉隐藏目录和 bin 目录
        if not root.startswith(COPYFROM + os.sep + '.') and (
                not (os.sep + 'bin' + os.sep) in root):
            for name in files:
                # 过滤出资源文件后缀名，并排除无用的资源文件
                if ('.properties' in name) and (name != 'build.properties')\
                        and (name != 'log4j.properties'):
                    path = os.path.join(root, name)
                    result.append(path)
    return result


def copyZhOut():
    '''完整的复制工作空间中的中文版资源文件到外部目录的流程'''
    logging.info('从工作空间 {0} 复制资源文件到 {1} ...'.format(WS_PATH, COPYTO))
    print('从工作空间 {0} 复制资源文件到 {1} ...'.format(WS_PATH, COPYTO))
    findWsPropFiles()
    copyCount = 0
    # 逐个处理资源文件
    for path in l10n_zh:
        prop = PropFile(path)
        prop.copyOut()
        copyCount += 1
    if copyCount:
        logging.info('复制完成！共复制了 {0} 个资源文件。\n'.format(copyCount))
        print('复制完成！共复制了 {0} 个资源文件。\n'.format(copyCount))
    else:
        print('没有匹配的资源文件可复制。\n')


def copyEnIn():
    '''完整的复制外部目录中的英文版资源文件到工作空间的流程'''
    logging.info('从 {0} 复制资源文件到工作空间 {1} ...'.format(COPYFROM, WS_PATH))
    print('从 {0} 复制资源文件到工作空间 {1} ...'.format(COPYFROM, WS_PATH))
    findWsPropFiles()
    propFiles = getPropFiles()
    copied = []
    # 从工作空间反过来找资源文件
    for path in l10n_zh:
        tgtProp = PropFile(path)
        for srcProp in propFiles:
            srcPropName = os.path.split(srcProp)[-1]
            # 当前文件名与工作空间映射出来的文件名一致时，认为是同一个资源文件对象
            if (srcPropName not in copied) \
                    and (srcPropName == tgtProp.mapName()):
                # tgtProp.copyDefaultIn()
                tgtProp.copyEnIn()
                copied.append(srcPropName)
    if copied:
        logging.info('复制完成！共复制了 {0} 个资源文件。\n'.format(len(copied)))
        print('复制完成！共复制了 {0} 个资源文件。\n'.format(len(copied)))
    else:
        print('没有匹配的资源文件可复制。\n')


def main():
    logging.basicConfig(filename='CopyResources.log', format='%(asctime)s %(message)s',
            level=logging.INFO)
    # copyZhOut()
    copyEnIn()


if __name__ == '__main__':
    main()
