/**
 * ITmImporter.java
 *
 * Version information :
 *
 * Date:2012-5-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.tm.importer.extension;

import net.heartsome.cat.common.core.exception.ImportException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author  jason
 * @version 
 * @since   JDK1.6
 */
public interface ITmImporter {
	
	/** 没有执行导入 */
	int IMPORT_STATE_NONE = 0;
	/** 导入成功 */
	int IMPORT_STATE_SUCCESSED=1;
	/** 导入失败 */
	int IMPORT_STATE_FAILED= 2;
	/** 没有数据库 */
	int IMPORT_STATE_NODB= 3;
		
	/**
	 * 执行导入
	 * @param tmxContent 符合TMX标准的字符
	 * @return 1 成功,2 失败;
	 */
	int executeImport(String tmxContent,String srcLang, IProgressMonitor monitor) throws ImportException;
	
	/**
	 * 设置当前项目
	 * @param project
	 */
	void setProject(IProject project);
	
	/**
	 * 释放资源
	 */
	void clearResources();
	
	/**
	 * 获取上下文个数
	 * @return ;
	 */
	int getContextSize();
	
	/**
	 * 检查导入器是否可用，主要针对当前系统是否有库相关插件或者是否设置了默认库
	 * @return ;
	 */
	boolean checkImporter();
}
