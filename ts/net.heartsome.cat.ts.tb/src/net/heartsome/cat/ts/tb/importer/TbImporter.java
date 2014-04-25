/**
 * TBImporter.java
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
package net.heartsome.cat.ts.tb.importer;

import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.ts.tb.importer.extension.ITbImporter;
import net.heartsome.cat.ts.tb.resource.Messages;

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
 * @author  jason
 * @version 
 * @since   JDK1.6
 */
public class TbImporter {
	private static Logger logger = LoggerFactory.getLogger(TbImporter.class);
	private final String TERMIMPORT_EXTENSION_ID = "net.heartsome.cat.ts.tb.importer.extension";	
	private ITbImporter tbImporter;
	
	private static TbImporter instance;	
	public static TbImporter getInstance(){
		if(instance == null){
			instance = new TbImporter();
		}
		return instance;
	}
	
	private TbImporter(){
		runExtension();
	}

	public boolean checkImporter(){
		if(tbImporter == null){
			return false;
		}
		return tbImporter.checkImporter();
	}
	
	/**
	 * 释放当前导入器的资源
	 *  ;
	 */
	public void clearResources(){
		instance = null;
		if(tbImporter != null){
			tbImporter.clearResources();
		}
	}
	
	
	public void setProject(IProject project){
		if(tbImporter != null){
			tbImporter.setProject(project);
		}
	}
	
	public int executeImport(String tbxStr, String srcLang,IProgressMonitor monitor) throws ImportException{
		if(tbImporter != null){
			return tbImporter.executeImport(tbxStr, srcLang, monitor);
		}
		return ITbImporter.IMPORT_STATE_FAILED;
	}
	
	/**
	 * 加载记忆库匹配实现 ;
	 */
	private void runExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				TERMIMPORT_EXTENSION_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ITbImporter) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("importer.TbImporter.logger1"), exception);
						}

						public void run() throws Exception {
							tbImporter = (ITbImporter) o;
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("importer.TbImporter.logger1"), ex);
		}
	}
}
