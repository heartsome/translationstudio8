/**
 * ITermMatch.java
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
package net.heartsome.cat.ts.tb.match.extension;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.resources.IProject;

/**
 * 术语匹配
 * @author  jason
 * @version 
 * @since   JDK1.6
 */
public interface ITbMatch {
	
	/**
	 * 查找当前翻译单元的术语
	 * @return ;
	 */
	Vector<Hashtable<String, String>> getTransUnitTerms();
	
	void clearResources();
	
	/**
	 * 设置当前项目
	 * @param project ;
	 */
	void setProject(IProject project);
	
	/**
	 * 针对当前项目检查当前匹配器是否可用
	 * @param project
	 *            当前项目
	 * @return ;
	 */
	boolean checkTbMatcher(IProject project);
	
	/**
	 * 设置TransUnit的源文纯文本
	 * @param srcPureText ;
	 */
	void setTuSrcPureText(String srcPureText);
	
	/**
	 * 设置TransUnit的源语言编码
	 * @param lang 语言编码,如:en-Us;
	 */
	void setTuSrcLanguage(String lang);
	
	/**
	 * 设置TransUnit的目标语言编码
	 * @param lang 语言编码,如:en-US;
	 */
	void setTuTgtlanguage(String lang);
	
	/**
	 * 是否排序查询是结果
	 * @param isSort ;
	 */
	void setIsSortResult(boolean isSort);
}
