/**
 * TermMatcher.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.tb.match;

import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.cat.ts.tb.match.extension.ITbMatch;
import net.heartsome.cat.ts.tb.resource.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 术语匹配
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TbMatcher {
	private final Logger logger = LoggerFactory.getLogger(TbMatcher.class);
	private final String TERMMATCH_EXTENSION_ID = "net.heartsome.cat.ts.tb.match.extension";
	private ITbMatch termMatch;	
	
	public TbMatcher() {
		runExtension();
	}	

	public Vector<Hashtable<String, String>> serachTransUnitTerms(String pureText, String srcLang, String tgtLang, boolean isSort) {
		Vector<Hashtable<String, String>> terms = new Vector<Hashtable<String, String>>();
		if (termMatch != null && pureText != null) {
			termMatch.setTuSrcPureText(pureText);
			termMatch.setTuSrcLanguage(srcLang);
			termMatch.setTuTgtlanguage(tgtLang);
			termMatch.setIsSortResult(isSort);
			terms.addAll(termMatch.getTransUnitTerms());
		}

		return terms;
	}

	public void setCurrentProject(IProject project) {
		if (termMatch != null) {
			termMatch.setProject(project);
		}
	}
	
	/**
	 * 针对项目检查当前导入器是否可用，主要判断是否存在数据模块和当前项目是否设置了术语库
	 * @param project
	 *            当前项目
	 * @return true可用，false不可用
	 */
	public boolean checkTbMatcher(IProject project) {
		if (termMatch == null) {
			return false;
		}
		return termMatch.checkTbMatcher(project);
	}

	/**
	 * 释放术语匹配所需要的所有资源
	 *  ;
	 */
	public void clearResources() {
		if(termMatch != null){
			termMatch.clearResources();
		}
	}
	
	/**
	 * 加载记忆库匹配实现 ;
	 */
	private void runExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				TERMMATCH_EXTENSION_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ITbMatch) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("match.TbMatcher.logger1"), exception);
						}

						public void run() throws Exception {
							termMatch = (ITbMatch) o;
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("match.TbMatcher.logger1"), ex);
		}
	}

}
