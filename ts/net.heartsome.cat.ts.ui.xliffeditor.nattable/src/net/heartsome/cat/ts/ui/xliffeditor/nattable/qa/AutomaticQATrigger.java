package net.heartsome.cat.ts.ui.xliffeditor.nattable.qa;

import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自动品质检查的扩展触发类
 * @author robert	2012-??-??
 */
public class AutomaticQATrigger {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticQATrigger.class);
	
	private IAutomaticQA autoQA;
	private static final String CONSTANT_automaticQA_EXTENSION_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.extension.automaticQA";

	public AutomaticQATrigger(XLFHandler handler) {
		runExtension();
		if (checkAutoQA()) {
			autoQA.setInitData(handler);
		}
	}

	/**
	 * 开始进行自动品质检查
	 * 
	 * @param isAddToDb
	 *            若为true,则是入库操作，若false,则为批准操作
	 */
	public String beginAutoQa(boolean isAddToDb, String rowId, boolean needInitQAResultViewer) {
		if (!checkAutoQA()) {
			return "";
		}
		return autoQA.beginAutoQa(isAddToDb, rowId, needInitQAResultViewer);
	}

	public boolean checkAutoQA() {
		if (autoQA == null) {
			return false;
		}
		return true;
	}
	
	public void bringQAResultViewerToTop(){
		autoQA.bringQAResultViewerToTop();
	}
	
	public void informQAEndFlag(){
		autoQA.informQAEndFlag();
	}

	/**
	 * 加载自动品质检查的扩展
	 */
	private void runExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CONSTANT_automaticQA_EXTENSION_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof IAutomaticQA) {
					ISafeRunnable runnable = new ISafeRunnable() {
						public void handleException(Throwable exception) {
							exception.printStackTrace();
						}
						public void run() throws Exception {
							autoQA = (IAutomaticQA) o;
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

}
