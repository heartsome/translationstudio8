#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import unittest

from checkVersions import Vcs


class TestVcsFunctions(unittest.TestCase):
    """注意：以下测试结果取决于代码仓库的实际提交历史，当时测试通过的数据，在代码仓库更新后很有可能会失败。"""
    def setUp(self):
        self.vcs = Vcs()

    def test_is_diff_only_changed_identifier_true(self):
        result = self.vcs.is_diff_only_changed_identifier(
            'f1db629e',
            'ts/net.heartsome.cat.ts.edition_ultimate.feature/UltimateEdition.product',
            'version=',
            'fd42c095')
        self.assertTrue(result)

    def test_is_diff_only_changed_identifier_false(self):
        result = self.vcs.is_diff_only_changed_identifier(
            '08c37857',
            'ts/net.heartsome.cat.ts.edition_ultimate.feature/UltimateEdition.product',
            'version=',
            'e32384a5')
        self.assertFalse(result)

    def test_is_diff_only_changed_identifier_v820(self):
        result = self.vcs.is_diff_only_changed_identifier(
            '08c37857',
            'ts/net.heartsome.cat.ts.edition_ultimate.feature/UltimateEdition.product',
            'version=')
        self.assertFalse(result)

    def test_get_latest_ver_and_date_ue(self):
        result = self.vcs.get_latest_ver_and_date(
            file_list=['ts/net.heartsome.cat.ts.edition_ultimate.feature', ])
        self.assertIsNotNone(result)
        # 此结果会受代码库最新的提交历史影响
        self.assertEqual(result['date'], '20130403')
        self.assertEqual(
            result['ver'], '08c37857daccac2e90616016679ca4b733da0318')

    def test_get_latest_ver_and_date_repo(self):
        result = self.vcs.get_latest_ver_and_date()
        self.assertIsNotNone(result)
        # 此结果会受代码库最新的提交历史影响
        self.assertEqual(result['date'], '20130412')
        self.assertEqual(
            result['ver'], '6d8957878f0eaa923dced3741cf370a8b08b1cec')

    def test_get_latest_ver_and_date_license(self):
        result = self.vcs.get_latest_ver_and_date(
            file_list=['base_plugins/net.heartsome.cat.ts.help', ])
        self.assertIsNotNone(result)
        # 此结果会受代码库最新的提交历史影响
        self.assertEqual(result['date'], '20130408')
        self.assertEqual(
            result['ver'], 'fa0291e77ab8fa5b3082bf564b1e9b2261afff79')

    def test_get_latest_ver_and_date_none(self):
        result = self.vcs.get_latest_ver_and_date(
            file_list=['.project', ])
        # 此结果会受代码库最新的提交历史影响
        self.assertIsNone(result)

    def test_get_project_pathes(self):
        result = self.vcs.get_project_pathes()
        self.assertIsNotNone(result)
        self.assertTrue(
            '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/base_plugins/net.heartsome.cat.ts.help' in result)
        # 这里没有一一验证所有项目是否都已找到相应的路径
        self.assertTrue(
            '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.edition_ultimate.feature' in result)

    def test_get_latest_ver_and_date_except_identifier_10_left(self):
        result = self.vcs.get_latest_ver_and_date_except_identifier(
            ['/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/.classpath', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/.project', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/build.properties', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/plugin.properties', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/plugin.xml', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/plugin_en.properties', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/plugin_zh.properties', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/.settings/org.eclipse.jdt.core.prefs', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/match-type/bing.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/match-type/google.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/match-type/others.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/match-type/pt.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/match-type/qt.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/match-type/tm.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/status/Loading.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/tm-view/accept-all.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/tm-view/accept-text.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/tm-view/delete-match.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/tm-view/edit-match.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/tm-view/tm-info.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/images/tm-view/tm_title.png', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/Activator.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/ImageConstants.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/bean/TmConstants.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/comparator/Comparator.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/comparator/TokenComparator.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/dialog/TmMatchEditDialog.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/dialog/TmMatchEditorBodyMenu.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/resource/message.properties', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/resource/message_en.properties', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/resource/message_zh.properties', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/resource/Messages.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/view/MatchViewerBodyMenu.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/view/MatchViewPart.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/view/SourceColunmCellRenderer.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/view/TargetColunmCellRenderer.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/view/TranslationTaskContainer.java', '/Volumes/iMac-User/Projects/hsgit/translation-studio_test/ts/net.heartsome.cat.ts.ui.translation/src/net/heartsome/cat/ts/ui/translation/view/TypeColunmCellRenderer.java'],
            'META-INF/MANIFEST.MF',
            'Bundle-Version:')
        self.assertIsNotNone(result)
        # 此结果会受代码库最新的提交历史影响
        self.assertEqual(result['date'], '20130411')
        self.assertEqual(result['ver'], 'f4d97306c3f75b6de18d206dcab2aee253d0ab77')


if __name__ == '__main__':
    unittest.main()
