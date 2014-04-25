/**
 * ITmTranslation.java
 *
 * Version information :
 *
 * Date:2012-4-27
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.tm.match.extension;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;

import org.eclipse.core.resources.IProject;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public interface ITmMatch {

	/**
	 * 查找匹配
	 * @return ;
	 */
	Vector<Hashtable<String, String>> findMatch();

	List<FuzzySearchResult> fuzzySearch();
	
	List<TranslationUnitAnalysisResult> translationUnitAnalysis();
	
	/**
	 * 针对当前项目检查当前匹配器是否可用
	 * @param project
	 *            当前项目
	 * @return ;
	 */
	boolean checkTmMatcher(IProject project);

	/**
	 * 设置自定义参数
	 * @param maxMatchSize
	 * @param isIgnoreTag
	 * @param minSimilarity
	 * @param isCaseSensitive
	 * @param contextSize
	 *            ;
	 */
	void setCustomeMatchParameters(int maxMatchSize, boolean isIgnoreTag, int minSimilarity, boolean isCaseSensitive,
			int contextSize, int tagPenalty);

	/**
	 * 设置自定义参数
	 * @param maxMatchSize
	 * @param minSimilarity
	 *            ;
	 */
	void setCustomeMatchParameters(int maxMatchSize, int minSimilarity);

	/**
	 * 设置翻译单元数据
	 * @param tuInfoBean
	 *            ;
	 */
	void setTransUnitInfo(TransUnitInfo2TranslationBean tuInfoBean);

	/**
	 * 设置当前处理的项目
	 * @param project
	 *            ;
	 */
	void setProject(IProject project);

	/**
	 * 获取最大匹配个数
	 * @return ;
	 */
	int getMaxMatchSize();

	/**
	 * 获取上下文个数
	 * @return ;
	 */
	int getContextSize();
	
	/**
	 * 获取最小的匹配率
	 * @return ;
	 */
	int getMinMatchQuality();
	
	/**
	 * 获取罚分
	 * @return ;
	 */
	int getTagPenalty();
	
	/** 匹配时,是否忽略标记 */
	boolean isIgnoreTag();
	
	/**
	 * 清除当前所有资源 ;
	 */
	void clearResource();
	
	/**
	 * 只清除当前数据库资源
	 * 另参考：{@link #clearResource()}
	 *  ;
	 */
	public void clearDbResource();
	
	void deleteFuzzyResult(FuzzySearchResult fr) throws Exception;
	
	void updateFuzzyResult(FuzzySearchResult fr) throws Exception;
}
