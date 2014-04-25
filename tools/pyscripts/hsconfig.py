#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
所有 Python 脚本共用的配置
'''

import getpass


# Eclipse 的工作空间路径
# 用于如下脚本：
#            CheckProperties.py
#            CheckVersions.py
workspace_pathes = {
                    'felix_lu': '/Volumes/iMac-User/Documents/eclipse_ws/ts8',
                    'kevin': 'D:\\development\\workspace_git',
                    'Mac': '/Workspaces/tsPackForIndigo',
                    }

# 需检查的对象
# 可用的检查对象值：'PLUGIN', 'FEATURE', 'PRODUCT'
# 用于如下脚本：
#            CheckVersions.py
check_types = ('PLUGIN', 'FEATURE', 'PRODUCT')

# 每个检查对象所对应的更新类型
# 可用的更新类型值：'MAJOR', 'MINOR', 'SERVICE', 'BUILD'，依次对应版本号中的 x.y.z.date 四个值
# 用于如下脚本：
#            CheckVersions.py
update_types = {
                'PLUGIN': 'SERVICE',
                'FEATURE': 'MINOR',
                'PRODUCT': 'MINOR'
                }

# Git 代码仓库的路径
# ！！！注意：仓库路径不能有除 大小写字母 数字 / . - _ \ 之外的特殊字符（例如中文、空格），否则会导致无法正确解析项目路径、而不能正常更新版本号
# 用于如下脚本：
#            CheckProperties.py
#            CheckVersions.py
git_repo_pathes = {
                   'felix_lu': '/Volumes/iMac-User/Projects/hsgit/translation-studio-ts',
                   'kevin': 'D:\\development\\git\\translation-studio',
                   'Mac': '/Users/Mac/r8PackGit/translation-studio',
                   }

# 上一个对外发布版本的 Git 版本标签
# 用于如下脚本：
#            CheckProperties.py
#            CheckVersions.py
git_last_version_tag = 'v8.3.0'

# 要打包的 Git 代码分支
# 用于如下脚本：
#            CheckProperties.py
#            CheckVersions.py
git_branch = 'ts-free'


# 打包的输出路径
# 用于如下脚本：
#            GeneratePackages.py
#            MergeRepos.py
build_pathes = {
                'felix_lu': '/Users/felix_lu/Desktop/r8builds',
                'kevin': 'D:\\HS_Product',
                'Mac': '/Users/Mac/Desktop/r8_pack'
                }

# 打包的版本父目录前缀，推荐用 4 位数日期
# 用于如下脚本：
#            GeneratePackages.py
#            MergeRepos.py
build_prefix = '0109'

# 对外发布的版本号，用来重命名目录和安装包
# 用于如下脚本：
#            GeneratePackages.py
build_version = '8_3_1'

# 要复制到安装包中的额外文件
# ！！！注意--->>>冒号前的 key 不能重复<<<---注意！！！
# 用于如下脚本：
#            GeneratePackages.py
build_copy_pathes = {
                    'felix_lu': {
                                 'all': ('/Volumes/iMac-User/Projects/HSStudio_R8/Translation Studio/Build/build_files', '.'),
                                 'Win_x86_JRE': ('/Volumes/iMac-User/Projects/HSStudio_R8/Translation Studio/Build/jre6u37_x86', '.'),
                                 'Win_x64_JRE': ('/Volumes/iMac-User/Projects/HSStudio_R8/Translation Studio/Build/jre6u37_x64', '.')
                                 },
                    'kevin': {
                              'all': ('D:\\HS_Product\\build_files\\documents', '.'),
                              'Win_x86_JRE': ('D:\\HS_Product\\build_files\\jre6u37_x86', '.'),
                              'Win_x64_JRE': ('D:\\HS_Product\\build_files\\jre6u37_x64', '.')
                              },
                    'Mac': {
                    		   'all': ('/Document/pack_resources/build_files/documents', '.'),
                    		   'Win_x86_JRE': ('/Document/pack_resources/build_files/jre6u37_x86', '.'),
                    		   'Win_x64_JRE': ('/Document/pack_resources/build_files/jre6u37_x64', '.')
                    		},
                    }

# 目标平台路径
# 用于如下脚本：
#            FindJarResource.py
target_platform_pathes = {
                          'felix_lu': (
                                       '/Volumes/iMac-User/Documents/eclipse_tp/targetPlatformBabel', 
                                       ),
						  'Mac':('/Document/pack_resources/targetPlatform'),
                          }

# 临时文件夹路径
# 用于如下脚本：
#            FindJarResource.py
temp_pathes = {
               'felix_lu': '/Users/felix_lu/tmp/findJarResource',
               'Mac': '/Document/pack_resources/temp/findJarResource'
               }

# 根据当前系统用户名取对应的路径
username = getpass.getuser()
workspace_path = workspace_pathes.get(username)
git_repo_path = git_repo_pathes.get(username)
build_path = build_pathes.get(username)
build_copy_files = build_copy_pathes.get(username)
target_platform_path = target_platform_pathes.get(username)
temp_path = temp_pathes.get(username)

if __name__ == '__main__':
    print('此脚本仅供配置多个脚本的共用参数，没有实际功能。请在相应的脚本中 import 后使用！')
