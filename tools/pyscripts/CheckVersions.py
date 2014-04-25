#!/usr/bin/env python3

"""自动取插件、feature、product 的代码的最后更新日期，并按指定的规则更新相应文件中的版本号。"""

import os
import sys
import subprocess
import re
import logging
from xml.dom.minidom import parse

import hsconfig


#################### 请修改以下参数 ####################
# 本次的检查对象、及每个检查对象所对应的更新类型
# 可用的检查对象值：'PLUGIN', 'FEATURE', 'PRODUCT'
CHECK_TYPES = hsconfig.check_types

# 可用的更新类型值：'MAJOR', 'MINOR', 'SERVICE', 'BUILD'，依次对应版本号中的 x.y.z.date 四个值
UPDATE_TYPES = hsconfig.update_types

# 工作空间路径
WS_PATH = hsconfig.workspace_path

###### Git 专用参数 ######
# Git 仓库地址
GIT_REPO_PATH = hsconfig.git_repo_path

# 检查是否有更新的基准版本，可以是 Tag 或 commit ID
LAST_BUILD_TAG = hsconfig.git_last_version_tag

# Git 版本分支
VCS_BRANCH = hsconfig.git_branch


###### CVS 专用参数 ######
# 上次打包日期，用以减少输出的 Log 长度
LAST_BUILD_DATE = '2013/3/1'

# CVS 服务器类型
CVS_TYPE = 'pserver'
# CVS 用户名
CVS_USER = 'felix'
# CVS 密码
CVS_PASSWORD = ''
# CVS 服务器地址
CVS_HOST = '192.168.0.13'
# CVS 服务器路径
CVS_PATH = '/data/cvs'


#################### 以下参数若有必要时可修改 ####################
# 目前使用的 VCS 类型
VCS = 'git'

# 插件的版本号与 Build 号分隔符
VER_SEPARATOR = 'R8b_v'


#################### ！！！以下参数不要随便修改！！！ ####################
# 支持的版本控制系统类型
SUPPORTED_VCS = ('cvs', 'git')

# 插件、feature、product 中保存版本的文件
VERSION_FILES = {
    'PLUGIN': 'META-INF/MANIFEST.MF',
    'FEATURE': 'feature.xml',
    'PRODUCT': '.product'}

# 上述版本文件中的版本标识前缀
VERSION_PREFIXS = {
    'PLUGIN': 'Bundle-Version:',
    'FEATURE': 'version=',
    'PRODUCT': 'version='}


#################### 运行时使用 ####################
# 项目路径
project_pathes = []

# 已检查的缓存
CHECKED = {
    'PLUGIN': [],
    'FEATURE': [],
    'PRODUCT': []}

# 已更新的缓存
UPDATED = {
    'PLUGIN': [],
    'FEATURE': [],
    'PRODUCT': []}

# 出错信息
error_msgs = []


class Vcs():
    """版本控制系统对象，提供取得更新历史、版本差异输出的功能"""
    def __init__(self, vcs_type=VCS, branch=VCS_BRANCH):
        """初始化 VCS 类型、认证信息、输出日志的版本及日期前缀等"""
        if vcs_type in SUPPORTED_VCS:
            self.type = vcs_type
            self.path = WS_PATH
            self.since = LAST_BUILD_DATE
            if vcs_type == 'cvs':
                # CVS 登录验证参数及其值
                self.cvs_auth_1 = '-d'
                self.cvs_auth_2 = ':{0}:{1}:{2}@{3}:{4}'.format(
                    CVS_TYPE, CVS_USER, CVS_PASSWORD, CVS_HOST, CVS_PATH)
                # CVS Log 内容中的日期前缀
                self.date_prefix = 'date:'
                # CVS Log 内容中的版本前缀
                self.ver_prefix = 'revision 1.'
            else:
                self.path = GIT_REPO_PATH
                self.since = LAST_BUILD_TAG
                self.branch = branch
                self.date_prefix = 'Date:'
                self.ver_prefix = 'commit'
            print('\n检查 {0} 中的插件和 Feature 自 {1} 以来的更新版本...\n'.format(
                self.path, self.since))
            logging.info('检查 {0} 中的插件和 Feature 自 {1} 以来的更新版本...\n'.format(
                self.path, self.since))
        else:
            raise Exception('不支持该 VCS 类型：{0}'.format(vcs_type))

    def get_log(self, file_list, until=None):
        """取得 VCS 的更新历史信息，直接输出 readlines() 的结果。
        如果使用相对路径要十分小心！注意父目录是否正确，否则无法得到预期的结果。"""
        cwd = os.getcwd()
        os.chdir(self.path)
        if self.type == 'cvs':
            # 完整的 CVS Log 命令
            log_cmd = [
                'cvs', self.cvs_auth_1, self.cvs_auth_2,
                'log', '-S', '-d',
                '{}<now'.format(self.since)]
            if file_list:
                log_cmd += file_list
        elif self.type == 'git':
            # 判断是否在指定的分支，且没有未提交的修改
            status_cmd = ['git', 'status']
            status_result = subprocess.Popen(
                status_cmd, stdout=subprocess.PIPE).stdout.readlines()
            is_branch_right = False
            for line in status_result:
                try:
                    line = line.decode('utf-8').replace('\n', '').strip()
                except UnicodeDecodeError as e:
                    logging.warning(
                        '{1}\n已放弃对上一行 Git 状态信息的解析。异常信息如下：\n{0}'.format(
                            e, line))
                    continue
                if line == '# On branch {}'.format(self.branch):
                    is_branch_right = True
                    break
            if not is_branch_right:
                raise Exception(
                    '请将 Git 仓库切换到 {} 分支，然后再运行此脚本。'
                    .format(self.branch))

            if not file_list:
                file_list = ['.', ]
            if not until:
                until = ''
            is_abs_path = True
            for path in file_list:
                if not os.path.isabs(path):
                    is_abs_path = False
                    break
            if not is_abs_path:
                logging.warning('使用了相对路径，可能导致提交历史信息不准确！')

            # 完整的 Git Log 命令
            log_cmd = [
                'git', 'log', '-1',
                '{}..{}'.format(self.since, until),
                '--date=iso',
                '--'] + file_list
        log_result = subprocess.Popen(
            log_cmd, stdout=subprocess.PIPE).stdout.readlines()
        # 返回之前的目录，避免给其他方法造成麻烦
        os.chdir(cwd)
        logging.debug('命令 {0} 的执行结果：\n{1}'.format(log_cmd, log_result))
        return log_result

    def get_latest_ver_and_date(self, file_list=None):
        """从 VCS 更新历史中解析出指定文件的最新版本及日期，返回字典。"""
        vcs_log = self.get_log(file_list)
        result = {}
        vcs_date = '0'
        vcs_ver = 0

        if self.type == 'cvs':
            for line in vcs_log:
                try:
                    line = line.decode('utf-8')
                except UnicodeDecodeError as e:
                    logging.warning(
                        '{1}\n已放弃对上一行 CVS 提交历史的解析。异常信息如下：\n{0}'.format(
                            e, line))
                    continue
                # 取 CVS 提交历史中的日期
                if line.startswith(self.date_prefix):
                    date = line[6:16].strip().replace('-', '')
                    # 只取最大日期
                    if int(date) > int(vcs_date):
                        vcs_date = date
                    continue
                # 取 CVS 提交历史中的版本
                elif line.startswith(self.ver_prefix):
                    version = int(line.strip().replace(
                        self.ver_prefix, '').split('.')[0])
                    # 只取最大的版本号
                    if version > vcs_ver:
                        vcs_ver = version

        elif self.type == 'git':
            for line in vcs_log:
                try:
                    line = line.decode('utf-8')
                except UnicodeDecodeError as e:
                    logging.warning(
                        '{1}\n已放弃对上一行 Git 提交历史的解析。异常信息如下：\n{0}'.format(
                            e, line))
                    continue
                if line.startswith(self.ver_prefix):
                    ver = line.replace(self.ver_prefix, '').strip()
                    continue
                elif line.startswith(self.date_prefix):
                    line = line.replace(self.date_prefix, '').strip()
                    # 由于 Git 不再有数字形式的版本号，故把日期与时间拼起来转为数字比较大小，以判断新旧
                    datetime = line[:10].replace(
                        '-', '') + line[11:19].replace(':', '')
                    if int(datetime) > int(vcs_date):
                        vcs_date = datetime
                        # 因 commit 总是在日期之前被重新取值，所以取到的日期总是与 commit 对应
                        vcs_ver = ver

            vcs_date = vcs_date[:8]

        if vcs_date != '0' and vcs_ver != 0:
            result['date'] = vcs_date[:8]
            result['ver'] = vcs_ver
            return result

        return None

    def get_latest_ver_and_date_except_identifier(
            self, file_list, identifier_file, identifier_text, since=None):
        """取得指定的文件的最新版本和日期，排除指定的文件中的指定修改内容。"""
        ver_info_left = self.get_latest_ver_and_date(file_list)
        ver_info_right = self.get_latest_ver_and_date([identifier_file])
        if ver_info_right:
            if not ver_info_left or (
                    ver_info_left and
                    int(ver_info_right['date']) > int(ver_info_left['date'])):
                # 左边的文件无更新、或有更新且日期比右边早，则看右边更新的内容是不是只有版本号
                if not self.is_diff_only_changed_identifier(
                        ver_info_right['ver'],
                        identifier_file,
                        identifier_text,
                        since):
                    return ver_info_right
        return ver_info_left

    def get_diff(self, ver_left, ver_right, file_list):
        """取得 VCS 的版本差异信息，直接输出 readlines() 的结果"""
        cwd = os.getcwd()
        os.chdir(self.path)
        if self.type == 'cvs':
            # cvs diff 命令
            diff_cmd = [
                'cvs', self.cvs_auth_1, self.cvs_auth_2,
                'diff', '-r', '1.{}'.format(ver_left),
                '-r', '1.{}'.format(ver_right),
                file_list]
            if file_list:
                diff_cmd += file_list
        elif self.type == 'git':
            if not file_list:
                file_list = ['.', ]
            is_abs_path = True
            for path in file_list:
                if not os.path.isabs(path):
                    is_abs_path = False
                    break
            if not is_abs_path:
                logging.warning('使用了相对路径，可能导致提交历史信息不准确！')

            diff_cmd = [
                'git', 'diff',
                ver_left, ver_right,
                '--date=iso',
                '--'] + file_list

        diff_result = subprocess.Popen(
            diff_cmd, stdout=subprocess.PIPE).stdout.readlines()
        os.chdir(cwd)
        logging.debug('命令 {0} 的执行结果：\n{1}'.format(diff_cmd, diff_result))
        return diff_result

    def is_diff_only_changed_identifier(
            self, ver_right, identifier_file, identifier_text, ver_left=None):
        """通过解析 VCS 的版本差异信息，判断是否只修改了指定的内容。"""
        # 若不指定对比的基础版本，则默认以上一次打包的版本为基础
        if not ver_left:
            ver_left = self.since
        vcs_diff = self.get_diff(ver_left, ver_right, [identifier_file, ])
        changed_left = []
        changed_right = []
        if self.type == 'cvs':
            for line in vcs_diff:
                try:
                    line = line.decode('utf-8')
                except UnicodeDecodeError as e:
                    logging.info(
                        '{1}\n已放弃对上一行 CVS 差异信息的解析。异常信息如下：\n{0}'.format(
                            e, line))
                    continue
                if line.startswith('< '):
                    changed_left.append(line.strip())
                    continue
                elif line.startswith('> '):
                    changed_right.append(line.strip())
        elif self.type == 'git':
            for line in vcs_diff:
                try:
                    line = line.decode('utf-8')
                except UnicodeDecodeError as e:
                    logging.info(
                        '{1}\n已放弃对上一行 Git 差异信息的解析。异常信息如下：\n{0}'.format(
                            e, line))
                    continue
                # TODO：待优化，可以解析以 ---/+++ 开头的行中的文件名以提高准确性
                if not line.startswith('---') and not line.startswith('+++'):
                    if line.startswith('-'):
                        changed_left.append(line.strip())
                        continue
                    elif line.startswith('+'):
                        changed_right.append(line.strip())
        if changed_left and changed_right:
            # 说明新旧版本的唯一区别就是版本，应忽略此文件的更新记录，即保持使用原来的版本号
            if len(changed_left) == 1 and len(changed_right) == 1 \
                    and identifier_text in changed_left[0] \
                    and identifier_text in changed_right[0]:
                logging.info('版本文件中仅更新了版本号，忽略。')
                return True
        return False

    def get_project_pathes(self):
        """根据 VCS 类型取得相应的项目路径"""
        project_pathes = []

        if self.type == 'cvs':
            # CVS 直接取工作空间下的非隐藏目录即可
            for name in os.listdir(self.path):
                path = os.path.join(self.path, name)
                if os.path.isdir(path) and not name.startswith('.'):
                    project_pathes.append(path)

        elif self.type == 'git':
            # 初始化要用到的信息
            project_dir_path_list = [
                '.metadata',
                '.plugins',
                'org.eclipse.core.resources',
                '.projects']
            project_dir_path = os.path.join(
                WS_PATH, os.path.sep.join(project_dir_path_list))
            location_file = '.location'
            path_prefix = 'file:'

            global error_msgs
            for project_name in os.listdir(project_dir_path):
                if not project_name.startswith('.'):
                    location_file_path = os.path.join(
                        project_dir_path, project_name, location_file)
                    try:
                        lfile = open(location_file_path, 'rb')
                        # 该文件只有一行内容，将其转为 str 方便后面用正则表达式
                        location_content = str(lfile.readlines()[0])
                        lfile.close()
                    except:
                        msg = '未在 Eclipse 工作空间目录 {} 中找到项目 {} 的 Git 路径，已忽略此项目。这可能是工作空间中有多余的不必要项目、或有些已删除的项目在工作空间中有残留文件。如果不想看到此信息，可以试试换个全新的、干净的工作空间。'.format(
                            WS_PATH, project_name)
                        error_msgs.append(msg)
                        logging.info(msg)
                        continue

                    # 用正则表达式来查找，注意：项目路径不能有除大小写字母、数字、/、.、-、_、\ 之外的特殊字符（例如中文、空格）
                    m = re.search('(?<={0})[A-Za-z0-9/\.\-_\\:]+{1}'.format(
                        path_prefix, project_name.replace('.', '\\.')), location_content)
                    if m:
                        project_path = m.group(0)
                    else:
                        msg = '未在 Eclipse 工作空间目录 {} 中找到项目 {} 的 Git 路径，已忽略此项目。这可能是 Eclipse 中的项目名称与 Git 中的目录名不一致，若该项目是必须的，请将它们改为一致。'.format(
                            WS_PATH, project_name)
                        error_msgs.append(msg)
                        logging.info(msg)
                        continue

                    if sys.platform == 'win32':
                        # Windows 下要去掉路径的起始 / 并替换所有 / 为 \\
                        project_path = project_path.replace('/', '\\')[1:]

                    if os.path.isdir(project_path):
                        project_pathes.append(project_path)
                    else:
                        msg = '从 Eclipse workspace 解析到的项目 {} 路径不是有效存在的目录，已忽略。\n该无效路径为：{}\n'.format(project_name, project_path)
                        error_msgs.append(msg)
                        logging.info(msg)

        # 结果简单处理
        if project_pathes:
            return project_pathes
        else:
            return None


class Bundle():
    """一个 PLUGIN、FEATURE 或 PRODUCT 对象，提供检查新版本及更新版本号的操作"""
    def __init__(self, vcs, bundlePath, bundleTypeExpected):
        '''类初始化：切换到目录，确定类型、读取当前版本信息。'''
        self.vcs = vcs
        self.bundlePath = bundlePath
        self.bundleTypeExpected = bundleTypeExpected
        self.bundleName = os.path.split(bundlePath)[-1]
        os.chdir(self.bundlePath)
        if self.isTypeExpected():
            self.readCurVersion()
            self.set_update_type()

    def set_update_type(self):
        """根据 Bundle 类型确定自身的版本更新类型。"""
        for bundle_type in CHECK_TYPES:
            if bundle_type == self.bundleTypeExpected:
                self.updateType = UPDATE_TYPES[bundle_type]

    def isTypeExpected(self):
        '''根据目录下是否存在特定的文件来判断该目录属于哪种类型。
            由于 Product 文件通常保存在 Feature 目录中，所以需要额外的 isProduct 参数来判断。'''
        if self.bundleTypeExpected == 'PLUGIN':
            if self.isPlugin():
                self.bundleType = self.bundleTypeExpected
                self.versionFile = VERSION_FILES['PLUGIN']
                self.versionPrefix = VERSION_PREFIXS['PLUGIN']
                return True
        elif self.bundleTypeExpected == 'FEATURE':
            if self.isFeature():
                self.bundleType = self.bundleTypeExpected
                self.versionFile = VERSION_FILES['FEATURE']
                self.versionPrefix = VERSION_PREFIXS['FEATURE']
                return True
        elif self.bundleTypeExpected == 'PRODUCT':
            if self.isProduct():
                self.bundleType = self.bundleTypeExpected
                self.versionPrefix = VERSION_PREFIXS['PRODUCT']
                return True
        return False

    def isPlugin(self):
        return os.path.isfile(VERSION_FILES['PLUGIN'])

    def isFeature(self):
        return os.path.isfile(VERSION_FILES['FEATURE'])

    def isProduct(self):
        for name in os.listdir('.'):
            if (not name.startswith('.')) and os.path.isfile(name) and \
                    (VERSION_FILES['PRODUCT'] == os.path.splitext(name)[-1]):
                self.versionFile = name  # 因文件名不固定，一旦找到就先赋值
                return True
        return False

    def readCurVersion(self):
        '''从文件中读取当前版本，并赋值给 curVersionList 列表'''
        f = open(self.versionFile, encoding='utf-8')
        curVersionStr = ''
        if self.bundleType == 'PLUGIN':
            for line in f:
                if line.strip().startswith(self.versionPrefix):
                    # 字符串格式的完整版本号
                    self.curVersionRaw = line.strip().replace(
                        self.versionPrefix, '').strip()
                    # 兼容 1.0.0.qualifier 这种形式
                    curVersionStr = self.curVersionRaw.replace(
                        'qualifier', '0')
                    break
        elif self.bundleType == 'FEATURE' or \
                self.bundleType == 'PRODUCT':
            self.curVersionRaw = parse(f).documentElement.getAttribute(
                'version').strip()
            curVersionStr = self.curVersionRaw.replace('qualifier', '0')
        else:
            pass
        f.close()
        self.curVersionList = self.versionStr2List(curVersionStr)
        if len(self.curVersionList) != 4:
            raise Exception('{} 的当前版本号 {} 不正确，请确认为 ["8", "0", "0", "20121201"] 的形式。'.format(self.bundleName, self.curVersionList))

    def writeNewVersion(self):
        '''将新的版本号写入文件'''
        if self.needsUpdate:
            if len(self.curVersionList) != 4:
                raise Exception('{} 的当前版本号 {} 不正确，请确认为 ["8", "0", "0", "20121201"] 的形式。'.format(self.bundleName, self.curVersionList))
            if len(self.newVersionList) != 4:
                raise Exception('{} 的新版本号 {} 不正确，请确认为 ["8", "0", "0", "20121201"] 的形式。'.format(self.bundleName, self.newVersionList))
            os.chdir(self.bundlePath)
            f = open(self.versionFile, 'r', encoding='utf-8')
            content = ''
            newVer = self.versionList2Str(self.newVersionList)
            for line in f.readlines():
                if self.versionPrefix in line:
                    # 直接用未经替换 qualifier 的原始版本号来替换
                    line = line.replace(self.curVersionRaw, newVer)
                # 对产品运行参数中的版本号进行替换，暂不兼容 qualifier 形式！
                if self.bundleType == 'PRODUCT':
                    if '-Dversion=' in line:
                        line = line.replace('.'.join(self.curVersionList[:-1]),
                            '.'.join(self.newVersionList[:-1]))
                    elif '-Ddate=' in line:
                        line = line.replace(
                            self.curVersionList[-1], self.newVersionList[-1])
                content += line
            f.close()
            f = open(self.versionFile, 'w', encoding='utf-8', newline='\n')
            f.write(content)
            f.close()

    def versionStr2List(self, strVersion):
        '''将版本号从字符串转换为列表：去除 R8b_v 并按 . 分割'''
        return strVersion.replace(VER_SEPARATOR, '').split('.')

    def versionList2Str(self, listVersion):
        '''将版本号列表转换为字符串：插件加上 R8b_v 并用 . 连接'''
        if listVersion:
            if len(listVersion) != 4:
                raise Exception('{} 的版本号 {} 不正确，请确认为 ["8", "0", "0", "20121201"] 的形式。'.format(self.bundleName, listVersion))
            # 避免将原参数修改
            listVer = listVersion[:]
            # 插件需要加上 R8b_v 分隔
            if self.bundleType == 'PLUGIN':
                listVer[-1] = VER_SEPARATOR + listVer[-1]
            return '.'.join(listVer)
        else:
            return None

    def getFileListExceptVersionFile(self):
        '''获取当前目录下，除 META-INF/MENIFEST.MF（插件）、feature.xml（功能特性）
        及 bin 之外的文件列表。'''
        cwd = os.getcwd()
        os.chdir(self.bundlePath)
        fileList = []
        versionFileDir, versionFileName = os.path.split(self.versionFile)
        # 要排除的一些文件
        ignoreFiles = ('bin', '.git', '.gitignore', 'CVS', '.DS_Store', 'Thumb.db')

        for filename in os.listdir('.'):
            if filename not in ignoreFiles:
                # 如果是版本文件所在目录，再进去列出其中的文件，以进一步排除版本文件
                if filename == versionFileDir:
                    for subfilename in os.listdir(versionFileDir):
                        if subfilename not in ignoreFiles and \
                                subfilename != versionFileName:
                            # 转为绝对路径来处理，避免 Git 因为路径不一致导致无法正确取得更新历史
                            fileList.append(os.path.normpath(
                                os.path.join(
                                    self.bundlePath,
                                    versionFileDir,
                                    versionFileName)))
                # 只要不是版本文件或其父目录，直接取所有第一级文件或子文件夹，不用展开更深层级下的子文件夹
                elif filename != versionFileName:
                    fileList.append(os.path.normpath(
                        os.path.join(self.bundlePath, filename)))
        os.chdir(cwd)
        return fileList

    def checkNewVersion(self):
        '''检查新版本（主要通过提交日期判断）'''
        print('检查 {0}：{1}'.format(self.bundleType, self.bundleName), end='\t')
        logging.info('检查 {0}：{1}'.format(self.bundleType, self.bundleName))
        if self.bundleType == 'PLUGIN':
            self.checkPlugin()
        elif self.bundleType == 'FEATURE':
            self.checkFeature()
        elif self.bundleType == 'PRODUCT':
            self.checkProduct()
        else:
            raise Exception('无效的类型：{0}'.format(self.bundleType))

    def checkPlugin(self):
        '''检查插件的新版本'''
        self.needsUpdate = False
        self.newVersionList = []
        fileList = self.getFileListExceptVersionFile()
        verInfo = self.vcs.get_latest_ver_and_date_except_identifier(
            fileList,
            os.path.normpath(os.path.join(self.bundlePath, self.versionFile)),
            self.versionPrefix)
        if verInfo:
            if int(verInfo['date']) > int(self.curVersionList[-1]):
                self.needsUpdate = True
                self.newVersionList = self.curVersionList[:]
                # 直接更新 Build 号
                self.newVersionList[-1] = verInfo['date']
            else:
                logging.info('当前版本与代码库一致。')
        else:
            logging.info('代码库暂无更新，当前已是最新版本。')
        if self.bundleName not in CHECKED[self.bundleType]:
            CHECKED[self.bundleType].append(self.bundleName)

    def getFeatureIncludes(self):
        '''得到包含的 feature id 并以列表形式返回'''
        featureIncludes = []
        for child in parse(self.versionFile).documentElement.childNodes:
            if child.nodeName == 'includes':
                featureIncludes.append(child.getAttribute('id'))
        return featureIncludes

    def getFeaturePlugins(self):
        '''得到包含的 plugin id 并以列表形式返回'''
        featurePlugins = []
        for child in parse(self.versionFile).documentElement.childNodes:
            if child.nodeName == 'plugin':
                featurePlugins.append(child.getAttribute('id'))
        return featurePlugins

    def checkFeature(self):
        '''检查 Feature 的新版本，并返回 List 形式的新版本'''
        self.needsUpdate = False

        global CHECKED
        for cache in CHECKED[self.bundleType]:
            if self.bundleName in cache.keys():
                self.newVersionList = cache[self.bundleName]
                logging.info('{0} 已缓存最新版本：{1}\t{2}'.format(
                    self.bundleName,
                    self.curVersionList,
                    self.newVersionList))
                if int(self.newVersionList[-1]) > int(self.curVersionList[-1]):
                    self.needsUpdate = True
                return self.newVersionList

        self.newVersionList = self.curVersionList[:]
        featureIncludes = self.getFeatureIncludes()
        featurePlugins = self.getFeaturePlugins()
        # 有下级 Feature
        global project_pathes
        if featureIncludes:
            for feature in featureIncludes:
                for project_path in project_pathes:
                    if feature == os.path.split(project_path)[-1]:
                        # 递归找到 feature Build 号
                        logging.info('{0}: 向下递归至 {1} ...'.format(
                            self.bundleName, feature))
                        subFeature = Bundle(
                            self.vcs,
                            project_path,
                            'FEATURE')
                        subBuild = subFeature.checkFeature()[-1]
                        if int(subBuild) > int(self.newVersionList[-1]):
                            self.needsUpdate = True
                            self.newVersionList[-1] = subBuild
                        break
        # 有下级插件
        if featurePlugins:
            for plugin in featurePlugins:
                for project_path in project_pathes:
                    if plugin == os.path.split(project_path)[-1]:
                        # 直接读取本地文件，始终假定本地已更新代码
                        subPlugin = Bundle(
                            self.vcs,
                            project_path,
                            'PLUGIN')
                        subBuild = subPlugin.curVersionList[-1]
                        if int(subBuild) > int(self.newVersionList[-1]):
                            self.needsUpdate = True
                            self.newVersionList[-1] = subBuild
                        break

        # 再检查 feature 自身的其他文件有没有更新
        fileList = self.getFileListExceptVersionFile()
        verInfo = self.vcs.get_latest_ver_and_date_except_identifier(
            fileList,
            os.path.normpath(os.path.join(self.bundlePath, self.versionFile)),
            self.versionPrefix)
        if verInfo:
            if int(verInfo['date']) > int(self.newVersionList[-1]):
                self.needsUpdate = True
                self.newVersionList[-1] = verInfo['date']

        # 将检查过的 Feature 名称缓存起来，以减少重复检查
        if self.bundleName not in CHECKED[self.bundleType]:
            CHECKED[self.bundleType].append(
                {self.bundleName: self.newVersionList})
        if self.needsUpdate:
            logging.info('{0}: 在下级 Feature 或插件中找到更新的 Build 号：{1}'.format(
                self.bundleName, self.newVersionList[-1]))
            return self.newVersionList
        else:
            logging.info('{0}: 无下级 Feature 或插件，或其中无更新的 Build 号。'.format(
                self.bundleName))
            return self.curVersionList

    def checkProduct(self):
        '''检查产品的新版本'''
        if not self.isFeature():
            raise Exception('{} 不在 feature 中。'.format(self.bundleName))
        if not self.isProduct():
            raise Exception('{} 不是产品。'.format(self.bundleName))
        self.needsUpdate = False
        bundle = Bundle(
            self.vcs,
            self.bundlePath,
            'FEATURE')
        self.newVersionList = bundle.checkFeature()
        if self.newVersionList[-1] > self.curVersionList[-1]:
            self.needsUpdate = True
        if self.bundleName not in CHECKED[self.bundleType]:
            CHECKED[self.bundleType].append(self.bundleName)

    def changeVersion(self):
        '''根据修改类型，更新主次版本号'''
        if self.needsUpdate:
            if len(self.newVersionList) != 4:
                raise Exception('{} 的新版本号 {} 不正确，请确认为 ["8", "0", "0", "20121201"] 的形式。'.format(self.bundleName, self.newVersionList))
            if self.updateType == 'SERVICE':
                self.verPlus(2)
            elif self.updateType == 'MINOR':
                self.verPlus(1)
                self.verReset(2)
            elif self.updateType == 'MAJOR':
                self.verPlus(0)
                self.verReset(1)
                self.verReset(2)
            else:
                pass
            curVersionStr = self.versionList2Str(self.curVersionList)
            newVersionStr = self.versionList2Str(self.newVersionList)
            UPDATED[self.bundleType].append(
                [self.bundleName, curVersionStr, newVersionStr])
        else:
            self.newVersionList = []

    def verPlus(self, verIndex):
        oldVer = int(self.curVersionList[verIndex])
        self.newVersionList[verIndex] = str(oldVer + 1)

    def verReset(self, verIndex):
        self.newVersionList[verIndex] = '0'


def main():
    script_path, script_name = os.path.split(os.path.realpath(sys.argv[0]))
    logging.basicConfig(
        filename='{}.log'.format(os.path.splitext(script_name)[0]),
        format='%(asctime)s %(message)s',
        level=logging.INFO)
    vcs = Vcs()
    global project_pathes
    project_pathes = vcs.get_project_pathes()
    for bundle_type in CHECK_TYPES:
        for path in project_pathes:
            bundle = Bundle(
                vcs,
                path,
                bundle_type)
            if bundle.isTypeExpected():
                bundle.checkNewVersion()
                bundle.changeVersion()
                bundle.writeNewVersion()
                newVer = bundle.versionList2Str(bundle.newVersionList)
                print(bundle.curVersionRaw, newVer, '\n-', sep='\t')
                logging.info('{0}\t{1}\n-'.format(bundle.curVersionRaw, newVer))
        print('=')
    logging.info('检查完成！\n')
    print('检查完成！\n')

    global error_msgs
    if error_msgs:
        for msg in error_msgs:
            print(msg)
            logging.info(msg)

    for bundle_type in CHECK_TYPES:
        print('更新了如下 {0} 个 {1}：'.format(
            len(UPDATED[bundle_type]), bundle_type))
        logging.info('更新了如下 {0} 个 {1}：'.format(
            len(UPDATED[bundle_type]), bundle_type))
        for bundle in UPDATED[bundle_type]:
            print(bundle[0], bundle[1], bundle[2], sep='\t')
            logging.info(' '.join(bundle))
        print()
    print('本次检查的日志已保存在 {}.log 文件中。'.format(
        os.path.join(script_path, os.path.splitext(script_name)[0])))
    logging.info(
        '================================================================\n\n')


if __name__ == '__main__':
    main()
