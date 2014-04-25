package net.heartsome.cat.ts;

import net.heartsome.cat.ts.ui.preferencepage.SystemPreferencePage;
import net.heartsome.cat.ts.ui.preferencepage.colors.ColorsPreferencePage;
import net.heartsome.cat.ts.ui.preferencepage.languagecode.LanguageCodesPreferencePage;
import net.heartsome.cat.ts.ui.preferencepage.translation.TranslationPreferencePage;

import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IComparableContribution;

/**
 * 用户对首选项菜单进行排序的类
 * @author peason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class PreferencesComparator extends ContributionComparator {
	public int category(IComparableContribution c) {
		if (c instanceof WorkbenchPreferenceNode) {
			String id = ((WorkbenchPreferenceNode) c).getId();
			if (SystemPreferencePage.ID.equals(id)) {
				// 系统菜单
				return 2;
			} else if (LanguageCodesPreferencePage.ID.equals(id)) {
				// 系统 > 语言代码菜单
				return 3;
			} else if (ColorsPreferencePage.ID.equals(id)) {
				// 系统 > 颜色菜单
				return 4;
			} else if ("org.eclipse.ui.preferencePages.Keys".equals(id)) {
				// 系统 > 快捷键菜单
				return 5;
			} else if ("org.eclipse.ui.net.proxy_preference_page_context".equals(id)) {
				// 网络连接
				return 6;
			} else if ("net.heartsome.cat.ts.ui.qa.preference.QAPage".equals(id)) {
				// 品质检查菜单
				return 7;
			} else if ("net.heartsome.cat.ts.ui.qa.preference.QAInstalPage".equals(id)) {
				// 品质检查 > 批量检查设置菜单
				return 8;
			} else if ("net.heartsome.cat.ts.ui.qa.preference.NonTranslationQAPage".equals(id)) {
				// 品质检查 > 非译元素菜单
				return 9;
			}
			else if ("net.heartsome.cat.ts.ui.qa.preference.SpellPage".equals(id)) {
				// 品质检查 > 拼写检查配置
				return 11;
			} else if ("net.heartsome.cat.ts.ui.qa.preference.FileAnalysisInstalPage".equals(id)) {
				// 文件分析
				return 12;
			} else if ("net.heartsome.cat.ts.ui.qa.preference.EquivalentPage".equals(id)) {
				// 文件分析 －－＞ 加权系数设置
				return 13;
			} else if (TranslationPreferencePage.ID.equals(id)) {
				// 翻译菜单
				return 14;
			} else if ("net.heartsome.cat.database.ui.tm.preference.tmpage".equals(id)) {
				// 记忆库
				return 15;
			} else if ("net.heartsome.cat.database.ui.tb.preference.tbpage".equals(id)) {
				// 术语库菜单
				return 16;
			} else if ("net.heartsome.cat.ts.pretranslation.preferencepage".equals(id)) {
				// 预翻译
				return 17;
			
			} else if ("net.heartsome.cat.ts.machinetranslation.prefrence.MachineTranslationPreferencePage".equals(id)) {
				// 修改google翻译的位置为机器翻译
				return 18;
			} else if ("net.heartsome.cat.ts.websearch.ui.preference.WebSearchPreferencePage".equals(id)) {
				// bing
				return 19;
			} else if ("net.heartsome.cat.convert.ui.preference.FileTypePreferencePage".equals(id)) {
				// 文件类型
				return 20;
			} else if ("net.heartsome.cat.converter.msexcel2007.preference.ExcelPreferencePage".equals(id)) {
				// Microsoft Excel 2007
				return 21;
			} else if ("net.heartsome.cat.converter.pptx.preference.PPTXPreferencePage".equals(id)) {
				// Microsoft PowerPoint 2007
				return 22;
			} else if ("net.heartsome.cat.converter.mif.preference.FrameMakerPreferencePage".equals(id)) {
				// Adobe FrameMaker
				return 23;
			} else if ("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage".equals(id)) {
				// 项目属性
				return 24;
			} else {
				return super.category(c);
			}
		} else {
			return super.category(c);
		}
	}

}
