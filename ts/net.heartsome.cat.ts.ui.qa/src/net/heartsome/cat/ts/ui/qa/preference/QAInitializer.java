package net.heartsome.cat.ts.ui.qa.preference;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 当本产品一打开，初始化品质检查插件中缓存中的值
 * @author robert	2011-12-05
 */
public class QAInitializer extends AbstractPreferenceInitializer {
	private boolean isUltimate = CommonFunction.checkEdition("U");

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
	//-------------------------------QAPage中的默认项--------------------
		//将不包含上下文匹配的按钮设成默认选中状态
		if (isUltimate) {
			preferenceStore.setDefault(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE, true);
		} else {
			preferenceStore.setDefault(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE, false);
		}
		preferenceStore.setDefault(QAConstant.QA_PREF_LOCKED_NOTINCLUDE, true);
		
		//将相同源文不同译文与相同译文不同源文下的忽略大小写与忽略标记全部勾选
		preferenceStore.setDefault(QAConstant.QA_PREF_PARA_SAMESOURCE, true);
		preferenceStore.setDefault(QAConstant.QA_PREF_PARA_SRC_IGNORCECASE, true);
		preferenceStore.setDefault(QAConstant.QA_PREF_PARA_SRC_IGNORCETAG, true);
		preferenceStore.setDefault(QAConstant.QA_PREF_PARA_SAMETARGET, true);
		preferenceStore.setDefault(QAConstant.QA_PREF_PARA_TAR_IGNORCECASE, true);
		preferenceStore.setDefault(QAConstant.QA_PREF_PARA_TAR_IGNORCETAG, true);
		
		//设置目标文本段长度限制检查的默认值
		preferenceStore.setDefault(QAConstant.QA_PREF_isCheckTgtMinLength, false);
		preferenceStore.setDefault(QAConstant.QA_PREF_isCheckTgtMaxLength, false);
		preferenceStore.setDefault(QAConstant.QA_PREF_tgtMinLength, "0");
		preferenceStore.setDefault(QAConstant.QA_PREF_tgtMaxLength, "0");
		
	//-------------------------------QAPage中的默认项--------------------
		//默认情况下所有的品质检查项全部选中，因此先将每个检查项的标识符用","组装起来。(备注：由于现在只开发了五个检查项，因此这里只加了五个，以后每开发完成一个，就加到这里)
		String defaultItems = QAConstant.QA_PARAGRAPH + "," + QAConstant.QA_NUMBER + ","
				+ QAConstant.QA_TAG + "," + QAConstant.QA_NONTRANSLATION + ","
				+ QAConstant.QA_SPACEOFPARACHECK + "," + QAConstant.QA_PARACOMPLETENESS + ","
				+ QAConstant.QA_SPELL;
		String autoDefaultItems = QAConstant.QA_NUMBER + "," + QAConstant.QA_TAG + "," + QAConstant.QA_SPELL;

		//默认情况下所有的品质检查项全部选中，因此先将每个检查项的标识符用","组装起来。
		preferenceStore.setDefault(QAConstant.QA_PREF_BATCH_QAITEMS, defaultItems);
		preferenceStore.setDefault(QAConstant.QA_PREF_AUTO_QAITEMS, autoDefaultItems);
		//默认为入库时执行
		preferenceStore.setDefault(QAConstant.QA_PREF_AUTO_QARUNTIME, QAConstant.QA_FIRST);
		
		// 设置默认错误级别
		preferenceStore.setDefault(QAConstant.QA_PREF_term_TIPLEVEL, 1);
		preferenceStore.setDefault(QAConstant.QA_PREF_para_TIPLEVEL, 1);
		preferenceStore.setDefault(QAConstant.QA_PREF_number_TIPLEVEL, 0);
		preferenceStore.setDefault(QAConstant.QA_PREF_tag_TIPLEVEL, 0);
		preferenceStore.setDefault(QAConstant.QA_PREF_nonTrans_TIPLEVEL, 1);
		preferenceStore.setDefault(QAConstant.QA_PREF_spaceOfPara_TIPLEVEL, 1);
		preferenceStore.setDefault(QAConstant.QA_PREF_paraComplete_TIPLEVEL, 1);
		preferenceStore.setDefault(QAConstant.QA_PREF_tgtLengthLimit_TIPLEVEL, 0);
		preferenceStore.setDefault(QAConstant.QA_PREF_spell_TIPLEVEL, 1);
		
	//-------------------------------SpellPage 中的默认项--------------------
		preferenceStore.setDefault(QAConstant.QA_PREF_isHunspell, true);
		preferenceStore.setDefault(QAConstant.QA_PREF_realTimeSpell, true);
		
		preferenceStore.setDefault(QAConstant.QA_PREF_ignoreNontrans, false);
		preferenceStore.setDefault(QAConstant.QA_PREF_ignoreDigitalFirst, false);
		preferenceStore.setDefault(QAConstant.QA_PREF_ignoreUpperCaseFirst, false);
		preferenceStore.setDefault(QAConstant.QA_PREF_ignoreAllUpperCase, true);
		
		
	//-------------------------------文件分析首选项值的初始化---------------
		preferenceStore.setDefault(QAConstant.FA_PREF_ignoreCase, true);
		preferenceStore.setDefault(QAConstant.FA_PREF_ignoreTag, true);
		if (isUltimate) {
			preferenceStore.setDefault(QAConstant.FA_PREF_contextNum, 1);
		} else {
			preferenceStore.setDefault(QAConstant.FA_PREF_contextNum, 0);
		}
		preferenceStore.setDefault(QAConstant.FA_PREF_tagPenalty, 2);
		preferenceStore.setDefault(QAConstant.FA_PREF_interRepeate, true);
		preferenceStore.setDefault(QAConstant.FA_PREF_interMatch, false);
		
		
		// 等效系数的设置，初始化值，roebrt 2011-12-21
		if (isUltimate) {
			preferenceStore.setDefault(QAConstant.FA_PREF_equivalent,
				"internalRepeat:0.50;external101:0.50;external100:0.50;95-99:0.60;85-94:0.70;75-84:0.80;50-74:0.90;");
		}else {
			preferenceStore.setDefault(QAConstant.FA_PREF_equivalent,
				"internalRepeat:0.50;external100:0.50;95-99:0.60;85-94:0.70;75-84:0.80;50-74:0.90;");
		}
		
	}

}
