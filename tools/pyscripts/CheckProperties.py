#!/usr/bin/env python3

"""检查 properties 文件自指定版本以来的更新情况，将结果输出到屏幕和日志文件中。"""

import os
import sys
import subprocess
import re
import logging

import hsconfig


#################### 请修改以下参数 ####################
# 工作空间路径
WS_PATH = hsconfig.workspace_path

###### Git 专用参数 ######
# Git 仓库地址
GIT_REPO_PATH = hsconfig.git_repo_path
# 检查的基准版本或提交
LAST_BUILD_TAG = hsconfig.git_last_version_tag
# Git 版本分支
VCS_BRANCH = hsconfig.git_branch


def get_log(file_list, until=None):
    """取得 Git 的更新历史信息，直接输出 readlines() 的结果。
    如果使用相对路径要十分小心！注意父目录是否正确，否则无法得到预期的结果。"""
    cwd = os.getcwd()
    os.chdir(GIT_REPO_PATH)

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
        if line == '# On branch {}'.format(VCS_BRANCH):
            is_branch_right = True
            break
    if not is_branch_right:
        print(
            '请将 Git 仓库切换到 {} 分支，然后再运行此脚本。'
            .format(VCS_BRANCH))
        exit(1)

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
        '{}..{}'.format(LAST_BUILD_TAG, until),
        '--date=iso',
        '--'] + file_list
    log_result = subprocess.Popen(
        log_cmd, stdout=subprocess.PIPE).stdout.readlines()
    # 返回之前的目录，避免给其他方法造成麻烦
    os.chdir(cwd)
    logging.debug('命令 {0} 的执行结果：\n{1}'.format(log_cmd, log_result))
    return log_result


def get_project_pathes():
    """取得相应的项目路径"""
    project_pathes = []

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

    for project_name in os.listdir(project_dir_path):
        if not project_name.startswith('.'):
            location_file_path = os.path.join(
                project_dir_path, project_name, location_file)
            lfile = open(location_file_path, 'rb')
            # 该文件只有一行内容，将其转为 str 方便后面用正则表达式
            location_content = str(lfile.readlines()[0])
            lfile.close()

            # 用正则表达式来查找
            m = re.search('(?<={0}).*{1}'.format(
                path_prefix, project_name), location_content)
            project_path = m.group(0)
            if sys.platform == 'win32':
                # Windows 下要去掉路径的起始 / 并替换所有 / 为 \\
                project_path = project_path.replace('/', '\\')[1:]
            if os.path.isdir(project_path):
                project_pathes.append(project_path)

    # 结果简单处理
    if project_pathes:
        return project_pathes
    else:
        return None


def main():
    script_path, script_name = os.path.split(os.path.realpath(sys.argv[0]))
    logging.basicConfig(
        filename='{}.log'.format(os.path.splitext(script_name)[0]),
        format='%(asctime)s %(message)s',
        level=logging.INFO)

    print('检查 {} 中的插件的资源文件修改情况：\n'.format(GIT_REPO_PATH))
    logging.info('检查 {} 中的插件的资源文件修改情况：\n'.format(GIT_REPO_PATH))

    project_pathes = get_project_pathes()
    for project in project_pathes:
        plugin_name = os.path.split(project)[1]
        changed_prop_count = 0
        for root, dirs, names in os.walk(project):
            for name in names:
                if name.endswith('.properties'):
                    path = os.path.join(root, name)
                    log = get_log([path, ])
                    if log:
                        # 只要有 Git 提交历史记录，就说明该 properties 文件有更新
                        for line in log:
                            try:
                                line = line.decode('utf-8').replace('\n', '').strip()
                            except UnicodeDecodeError as e:
                                logging.warning(
                                    '{1}\n已放弃对上一行 Git 状态信息的解析。异常信息如下：\n{0}'.format(
                                        e, line))
                                continue
                            # 更精确地确认是 commit 历史，而非出错信息
                            if line.startswith('commit'):
                                if not changed_prop_count:
                                    print('插件：{}\n已修改资源文件：'.format(plugin_name))
                                    logging.info('插件 {}\n已修改资源文件：'.format(plugin_name))
                                path = path.replace(project, '')
                                print('\t{}'.format(path))
                                logging.info('\t{0}'.format(path))
                                changed_prop_count += 1
                                break
        if changed_prop_count:
            print()
            logging.info('')
    print('检查完成！本次检查的日志已保存在 {}.log 文件中。'.format(
        os.path.join(script_path, os.path.splitext(script_name)[0])))
    logging.info(
        '检查完成！\n================================================================\n\n')


if __name__ == '__main__':
    main()
