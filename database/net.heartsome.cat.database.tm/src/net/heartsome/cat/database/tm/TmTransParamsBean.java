/**
 * TmTransParamsBean.java
 *
 * Version information :
 *
 * Date:2012-4-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.tm;

import java.beans.PropertyChangeEvent;

import net.heartsome.cat.database.bean.TMPreferenceConstants;
import net.heartsome.cat.database.ui.tm.Activator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * 记忆库匹配参数
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmTransParamsBean implements IPropertyChangeListener {
	private int maxMatchSize = 0;
	private int contextSize = 0;
	private int minSimilarity = 0;
	private int tagPenalty = 2;
	private boolean isCaseSensitive = false;
	private boolean isIgnoreTag = false;
	private int tmUpdateStrategy = 0;
	private IPreferenceStore preferenceStore;

	private int matchSortStrategry = 0;

	public TmTransParamsBean() {
		preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(this);
		loadPreference();
	}

	public TmTransParamsBean(int maxMatchSize, boolean isIgnoreTag, int minSimilarity, boolean isCaseSensitive,
			int contextSize, int tagPenalty) {
		this.maxMatchSize = maxMatchSize;
		this.contextSize = contextSize;
		this.minSimilarity = minSimilarity;
		this.isCaseSensitive = isCaseSensitive;
		this.isIgnoreTag = isIgnoreTag;
		this.tagPenalty = tagPenalty;
	}

	public void propertyChange(PropertyChangeEvent event) {
		loadPreference();
	}

	private void loadPreference() {
		this.isCaseSensitive = preferenceStore.getBoolean(TMPreferenceConstants.CASE_SENSITIVE);
		this.isIgnoreTag = preferenceStore.getBoolean(TMPreferenceConstants.IGNORE_MARK);
		this.minSimilarity = preferenceStore.getInt(TMPreferenceConstants.MIN_MATCH);
		this.maxMatchSize = preferenceStore.getInt(TMPreferenceConstants.MAX_MATCH_NUMBER);
		this.contextSize = preferenceStore.getInt(TMPreferenceConstants.CONTEXT_MATCH);
		this.tmUpdateStrategy = preferenceStore.getInt(TMPreferenceConstants.TM_UPDATE);
		this.matchSortStrategry = preferenceStore.getInt(TMPreferenceConstants.MATCH_PERCENTAGE_SORT_WITH_EQUAL);
		this.tagPenalty = preferenceStore.getInt(TMPreferenceConstants.TAG_PENALTY);
	}

	/** @return the maxMatchSize */
	public int getMaxMatchSize() {
		return maxMatchSize;
	}

	/**
	 * @param maxMatchSize
	 *            the maxMatchSize to set
	 */
	public void setMaxMatchSize(int maxMatchSize) {
		this.maxMatchSize = maxMatchSize;
	}

	/** @return the contextSize */
	public int getContextSize() {
		return contextSize;
	}

	/**
	 * @param contextSize
	 *            the contextSize to set
	 */
	public void setContextSize(int contextSize) {
		this.contextSize = contextSize;
	}

	/** @return the minSimilarity */
	public int getMinSimilarity() {
		return minSimilarity;
	}

	/**
	 * @param minSimilarity
	 *            the minSimilarity to set
	 */
	public void setMinSimilarity(int minSimilarity) {
		this.minSimilarity = minSimilarity;
	}

	/** @return the isCaseSensitive */
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	/**
	 * @param isCaseSensitive
	 *            the isCaseSensitive to set
	 */
	public void setCaseSensitive(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
	}

	/** @return the isIgnoreTaget */
	public boolean isIgnoreTag() {
		return isIgnoreTag;
	}

	/**
	 * @param isIgnoreTaget
	 *            the isIgnoreTaget to set
	 */
	public void setIgnoreTaget(boolean isIgnoreTaget) {
		this.isIgnoreTag = isIgnoreTaget;
	}

	/** @return the tmUpdateStrategy */
	public int getTmUpdateStrategy() {
		return tmUpdateStrategy;
	}

	/**
	 * @param tmUpdateStrategy
	 *            the tmUpdateStrategy to set
	 */
	public void setTmUpdateStrategy(int tmUpdateStrategy) {
		this.tmUpdateStrategy = tmUpdateStrategy;
	}

	/**
	 * @param isIgnoreTag
	 *            the isIgnoreTag to set
	 */
	public void setIgnoreTag(boolean isIgnoreTag) {
		this.isIgnoreTag = isIgnoreTag;
	}

	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		loadPreference();
	}

	/** @return the matchSortStrategry */
	public int getMatchSortStrategry() {
		return matchSortStrategry;
	}

	/** @return the tagPenalty */
	public int getTagPenalty() {
		return tagPenalty;
	}

	/**
	 * @param tagPelanty
	 *            the tagPenalty to set
	 */
	public void setTagPenalty(int tagPelanty) {
		this.tagPenalty = tagPelanty;
	}

	/**
	 * @param matchSortStrategry
	 *            the matchSortStrategry to set
	 */
	public void setMatchSortStrategry(int matchSortStrategry) {
		this.matchSortStrategry = matchSortStrategry;
	}

	public void clearResources() {
		if (preferenceStore != null) {
			this.preferenceStore.removePropertyChangeListener(this);
		}
	}

}
