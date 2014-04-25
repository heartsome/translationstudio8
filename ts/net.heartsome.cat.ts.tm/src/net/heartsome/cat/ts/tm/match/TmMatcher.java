/**
 * TmMatcher.java
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
package net.heartsome.cat.ts.tm.match;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.tm.match.extension.ITmMatch;
import net.heartsome.cat.ts.tm.resource.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记忆库匹配
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmMatcher {
	public static final Logger logger = LoggerFactory.getLogger(TmMatcher.class);
	private static final String TMMATCH_EXTENSION_ID = "net.heartsome.cat.ts.tm.match.extension";
	private ITmMatch tmTranslation;

	/**
	 * 构造方法
	 */
	public TmMatcher() {
		runExtension();
	}

	/**
	 * 针对项目检查当前导入器是否可用，主要判断是否存在数据模块和当前项目是否设置了记忆库
	 * @param project
	 *            当前项目
	 * @return true可用，false不可用
	 */
	public boolean checkTmMatcher(IProject project) {
		if (tmTranslation == null) {
			return false;
		}
		return tmTranslation.checkTmMatcher(project);
	}

	/**
	 * 查找匹配
	 * @param project
	 * @param tuInfoBean
	 * @return ;
	 */
	public Vector<Hashtable<String, String>> executeSearch(IProject project, TransUnitInfo2TranslationBean tuInfoBean) {
		Vector<Hashtable<String, String>> dbMatchsVector = new Vector<Hashtable<String, String>>();
		if (tmTranslation != null) {
			tmTranslation.setProject(project);
			tmTranslation.setTransUnitInfo(tuInfoBean);
			dbMatchsVector.addAll(tmTranslation.findMatch());
		}
		return dbMatchsVector;
	}

	/**
	 * 使用当前项目的记忆库分析翻译单元
	 * @param project
	 *            当前项目
	 * @param tuInfoBean
	 *            翻译单元
	 * @return ;
	 */
	public List<TranslationUnitAnalysisResult> analysTranslationUnit(IProject project,
			TransUnitInfo2TranslationBean tuInfoBean) {
		if (tmTranslation != null) {
			tmTranslation.setProject(project);
			tmTranslation.setTransUnitInfo(tuInfoBean);
			return tmTranslation.translationUnitAnalysis();
		}
		return new ArrayList<TranslationUnitAnalysisResult>();
	}

	public List<FuzzySearchResult> executeFuzzySearch(IProject project, TransUnitInfo2TranslationBean tuInfoBean) {
		if (tmTranslation != null) {
			tmTranslation.setProject(project);
			tmTranslation.setTransUnitInfo(tuInfoBean);
			return tmTranslation.fuzzySearch();
		}
		return new ArrayList<FuzzySearchResult>();
	}

	/**
	 * 设置自定义参数,一日设置将不会从首选项中读取关于记忆库的参数
	 * @param maxMatchSize
	 *            最大匹配个数
	 * @param isIgnoreTag
	 *            是否忽略标记
	 * @param minSimilarity
	 *            最小匹配率
	 * @param isCaseSensitive
	 *            是否区分大小写
	 * @param contextSize
	 *            上下文个数 ;
	 */
	public void setCustomeMatchParameters(int maxMatchSize, boolean isIgnoreTag, int minSimilarity,
			boolean isCaseSensitive, int contextSize, int tagPenalty) {
		if (tmTranslation != null) {
			tmTranslation.setCustomeMatchParameters(maxMatchSize, isIgnoreTag, minSimilarity, isCaseSensitive,
					contextSize, tagPenalty);
		}
	}

	/**
	 * 设置自定义参数，其他参数将默认取记忆库首选项中的配置
	 * @param maxMatchSize
	 *            最大匹配个数
	 * @param minSimilarity
	 *            最大匹配率;
	 */
	public void setCustomeMatchParameters(int maxMatchSize, int minSimilarity) {
		if (tmTranslation != null) {
			tmTranslation.setCustomeMatchParameters(maxMatchSize, minSimilarity);
		}
	}

	/**
	 * 获取最大匹配个数,如果当前版本不存在数据模块则返回0
	 * @return ;
	 */
	public int getMaxMatchSize() {
		if (tmTranslation != null) {
			return tmTranslation.getMaxMatchSize();
		}
		return 0;
	}

	
	public int getTagPenalty() {
		if (tmTranslation != null) {
			return tmTranslation.getTagPenalty();
		} else {
			return 2;//默认罚分为2
		}
	}
	
	/**
	 * 获取最低匹配率
	 * @return ;
	 */
	public int getMinMatchQuality() {
		if (tmTranslation != null) {
			return tmTranslation.getMinMatchQuality();
		}
		return 0;
	}

	/**
	 * 查询匹配时,是否忽略标记
	 * @return ;
	 */
	public boolean isIgnoreTag() {
		if (tmTranslation != null) {
			return tmTranslation.isIgnoreTag();
		}
		return false;
	}

	/**
	 * 获取匹配时检查上下文个数,如果当前版本不存在数据模块则返回0
	 * @return ;
	 */
	public int getContextSize() {
		if (tmTranslation != null) {
			return tmTranslation.getContextSize();
		}
		return 0;
	}
	
	/**
	 * 加哉记忆库匹配实现 ;
	 */
	private void runExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				TMMATCH_EXTENSION_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ITmMatch) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("match.TmMatcher.logger1"), exception);
						}

						public void run() throws Exception {
							tmTranslation = (ITmMatch) o;
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("match.TmMatcher.logger1"), ex);
		}
	}

	/**
	 * 清理当前翻译实例中的所有资源
	 * @return ;
	 */
	public void clearResources() {
		if (tmTranslation != null) {
			tmTranslation.clearResource();
		}
	}

	/**
	 * 清除当前翻译实例中的数据库资源 ;
	 */
	public void clearDbResources() {
		if (tmTranslation != null) {
			tmTranslation.clearDbResource();
		}
	}

	/**
	 * 删除当前匹配面板中显示的数据库及时匹配。<br>
	 * 如果 TU 是多语言对，则只删除当前这个语言的TUV，如果是单语言对则直接删除当前 TU 在记忆库中所有内容
	 * @param fr 从记忆库中查询出来的匹配结果
	 * @throws Exception
	 *             ;
	 */
	public void deleteFuzzyResult(FuzzySearchResult fr) throws Exception {
		tmTranslation.deleteFuzzyResult(fr);
	}

	/**
	 * 更新当前匹配面板中显示数据库及时匹配，只修改源文译文。
	 * @param fr 从记忆库中查询出来的匹配结果
	 * @throws Exception
	 *             ;
	 */
	public void updateFuzzResult(FuzzySearchResult fr) throws Exception {
		tmTranslation.updateFuzzyResult(fr);
	}
}
