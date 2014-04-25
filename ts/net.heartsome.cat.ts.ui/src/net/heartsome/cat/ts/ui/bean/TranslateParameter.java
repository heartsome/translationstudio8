/**
 * TranslateParameter.java
 *
 * Version information :
 *
 * Date:2012-6-19
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.bean;

import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.preferencepage.translation.ITranslationPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * 对应首选项的翻译参数配置<br>
 * 单例模式，监听首选项配置的改变
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TranslateParameter implements IPropertyChangeListener {

	private static TranslateParameter instance;

	public static TranslateParameter getInstance() {
		if (instance == null) {
			instance = new TranslateParameter();
		}
		return instance;
	}

	private TranslateParameter() {
		ps = Activator.getDefault().getPreferenceStore();
		if (ps != null) {
			ps.addPropertyChangeListener(this);
			loadPreferenceValues();
		}
	}

	private IPreferenceStore ps;

	private boolean isAdjustSpacePosition;
	private boolean isApplyTmMatch;
	private boolean isApplySource;
	private boolean isSkipNoTransSegment;
	private boolean isAutoQuickTrans;
	
	private IPreferenceStore machinePs;

	public void propertyChange(PropertyChangeEvent event) {
		if (ps != null) {
			loadPreferenceValues();
		}
	}

	private void loadPreferenceValues() {
		isAdjustSpacePosition = ps.getBoolean(ITranslationPreferenceConstants.AUTO_ADAPT_SPACE_POSITION);
		isApplyTmMatch = ps.getBoolean(ITranslationPreferenceConstants.AUTO_APPLY_TM_MATCH);
		isApplySource = ps.getBoolean(ITranslationPreferenceConstants.COPY_SOURCE_TO_TARGET);
		isSkipNoTransSegment = ps.getBoolean(ITranslationPreferenceConstants.SKIP_NOT_TRANSLATE_TEXT);
		isAutoQuickTrans = ps.getBoolean(ITranslationPreferenceConstants.AUTO_QUICK_TRANSLATION);
	}

	public void setMachinePs(IPreferenceStore machinePs){
		this.machinePs=machinePs;
	}
	
	public  boolean isIgnoreExactMatch(){
		if(machinePs== null){
			return false;
		}
		return machinePs.getBoolean("net.heartsome.cat.ts.machineTranslate.ignoreexactmatch");
	}
	
	public boolean isIgnoreLock(){
		if(machinePs== null){
			return false;
		}
		return machinePs.getBoolean("net.heartsome.cat.ts.machineTranslate.ignorelock");
	}
	/**
	 * 接受翻译时是否调整空格位置
	 * @return the isAdjustSpacePosition
	 */
	public boolean isAdjustSpacePosition() {
		return isAdjustSpacePosition;
	}

	/**
	 * 无译文时自动应用最高记忆库匹配
	 * @return the isApplyTmMatch
	 */
	public boolean isApplyTmMatch() {
		return isApplyTmMatch;
	}

	/**
	 * 无匹配时是否复制源文本到目标
	 * @return the isApplySource
	 */
	public boolean isApplySource() {
		return isApplySource;
	}

	/**
	 * 是否跳过不可翻译的文本段
	 * @return the isSkipNoTransSegment
	 */
	public boolean isSkipNoTransSegment() {
		return isSkipNoTransSegment;
	}

	/**
	 * 自动快速翻译
	 * @return the isAutoQuickTrans
	 */
	public boolean isAutoQuickTrans() {
		return isAutoQuickTrans;
	}

}
