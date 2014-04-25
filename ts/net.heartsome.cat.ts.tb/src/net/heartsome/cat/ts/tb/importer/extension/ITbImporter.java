/**
 * ITbImporter.java
 *
 * Version information :
 *
 * Date:2012-5-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.tb.importer.extension;

import net.heartsome.cat.common.core.exception.ImportException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 将符合TBX标准的字符串导入到术语库中
 * @author jason
 * @version
 * @since JDK1.6
 */
public interface ITbImporter {

	/** 导入成功 */
	int IMPORT_STATE_SUCCESSED = 0;

	/** 导入失败 */
	int IMPORT_STATE_FAILED = 1;
	
	/** 当前无可用的库 */
	int IMPORT_STATE_NODB = 2;

	/**
	 * 设置当前项目，用于获取项目配置信息
	 * @param project
	 *            ;
	 */
	void setProject(IProject project);

	/**
	 * 执行导入
	 * @param tbxStr
	 *            符合TBX标准的字符串
	 * @param srcLang
	 *            TBX源语言
	 * @return 导入标志（成功，失败...）;
	 */
	int executeImport(String tbxStr, String srcLang,IProgressMonitor monitor) throws ImportException;
	
	/**
	 * 检查导入器是否可用，主要针对当前系统是否有库相关插件或者是否设置了默认库 
	 * @return ;
	 */
	boolean checkImporter();
	
	/**
	 * 释放所使用的资源
	 *  ;
	 */
	void clearResources();

}
