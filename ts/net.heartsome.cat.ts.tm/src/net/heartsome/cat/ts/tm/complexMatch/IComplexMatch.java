/**
 * IComplexMatch.java
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
package net.heartsome.cat.ts.tm.complexMatch;

import java.util.Vector;

import org.eclipse.core.resources.IProject;

import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.bean.TransUnitBean;

/**
 * 复杂翻译，用于实现各种翻译相关的计算等，如快速翻译
 * @author  jason
 * @version 
 * @since   JDK1.6
 */
public interface IComplexMatch {
	
	/**
	 * 执行翻译
	 * @return 返回翻译结果 ;
	 */
	Vector<AltTransBean> executeTranslation(TransUnitBean transUnitBean,IProject currentProject);	
		
	/**
	 * 获取当前匹配器的toolId
	 * @return ;
	 */
	String getToolId();
	
	/**
	 * 获取匹配的类型简称，如快速翻译QT
	 * @return ;
	 */
	String getMatcherType();
	
	/**
	 * 获取匹配的来源
	 * @return ;
	 */
	String getMathcerOrigin();
}
