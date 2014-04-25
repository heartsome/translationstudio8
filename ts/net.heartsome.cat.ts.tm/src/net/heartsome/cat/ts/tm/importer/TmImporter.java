/**
 * TmImporter.java
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
package net.heartsome.cat.ts.tm.importer;

import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.ts.tm.importer.extension.ITmImporter;
import net.heartsome.cat.ts.tm.resource.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TMX导入器，用于导入符合TMX标准的字符串到当前项目的默认记忆库中<br>
 * 此类的对象可以重用，重用时需要重新设置当前项目信息{@link #setProject(IProject)}<br>
 * 当确定此类的对象不需要再用时，需要释放资源{@link #clearResources()}<br>
 * 释放的资源包括项目记忆库的连接、参数获取对象、项目配置的监听等，当释放资源后，此对象的生命周期方可视为结束
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmImporter {
	public static final Logger logger = LoggerFactory.getLogger(TmImporter.class);

	private final String TMIMPORTER_EXTENSION_ID = "net.heartsome.cat.ts.tm.importer.extension";

	private ITmImporter tmImporter;
	
	private static TmImporter instance;

	public static TmImporter getInstance(){
		if(instance == null){
			instance = new TmImporter();
		}
		return instance;
	}
	
	/**
	 * 检查当前导入器是否可用
	 * @return true 可用，false不可用
	 */
	public boolean checkImporter(){		
		if(tmImporter != null && tmImporter.checkImporter()){
			return true;
		}		
		return false;
	}
	
	/**
	 * 构造器
	 */
	private TmImporter() {
		runExtension();
	}
	
	/**
	 * 重复设置将不会产生效率问题<br>
	 * 设置当前项目，此信息将用于获取项目的配置信息
	 * @param project ;
	 */
	public void setProject(IProject project){
		if(tmImporter != null){
			tmImporter.setProject(project);
		}
	}

	public int getContextSize(){
		if(tmImporter != null){
			return tmImporter.getContextSize();
		}
		return 1;
	}
	
	/**
	 * 清除资源，结束当前导入器的生命周期
	 *  ;
	 */
	public void clearResources(){
		if(tmImporter != null){
			tmImporter.clearResources();
		}
		instance = null;
	}
	/**
	 * 执行导入
	 * @param tmxContent 符合TMX标准的字符串
	 * @param srcLang 源语言
	 * @param monitor 进度条，可以为null
	 * @return ;
	 * @throws ImportException 
	 */
	public int executeImport(String tmxContent, String srcLang, IProgressMonitor monitor) throws ImportException {
		if (tmImporter != null) {
			return tmImporter.executeImport(tmxContent, srcLang, monitor);
		}
		return ITmImporter.IMPORT_STATE_NONE;
	}

	/**
	 * 加载记忆库匹配实现 ;
	 */
	private void runExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				TMIMPORTER_EXTENSION_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ITmImporter) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("importer.TmImporter.logger1"), exception);
						}

						public void run() throws Exception {
							tmImporter = (ITmImporter) o;
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("importer.TmImporter.logger1"), ex);
		}
	}
}
