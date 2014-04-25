package net.heartsome.cat.ts.ui.xliffeditor.nattable.qa;

import java.util.List;

import net.heartsome.cat.ts.core.bean.SingleWord;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * nattable 编辑界面实时检查触发类
 * @author robert	2013-01-21
 */
public class RealTimeSpellCheckTrigger {
	private static RealTimeSpellCheckTrigger instance = null;
	private IRealTimeSpellCheck realTimeSpell;
	/** 这是实时检查的扩展点 id */
	private static final String CONSTANT_realTimeSpell_extentionId = "net.heartsome.cat.ts.ui.xliffeditor.nattable.extension.realTimeSpellCheck";
	private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeSpellCheckTrigger.class.getName());
	
	private RealTimeSpellCheckTrigger(){
		if (realTimeSpell == null) {
			runExtension();
		}
		if (realTimeSpell == null) {
			// TODO ...
		}
	}
	
	public static RealTimeSpellCheckTrigger getInstance(){
		if (instance == null) {
			instance = new RealTimeSpellCheckTrigger();
		}
		return instance;
	}
	
	/**
	 * 检查拼写检查是否可用，分为四种情况，<br>
	 * 1、Spell 实例是否为空，由 trigger 类控制<br>
	 * 2、系统是否选择勾选实时检查。<br>
	 * 3、当前拼写检查器是否支持当前语种的拼写检查。<br>
	 * 4、拼写检查器是否运行错误，或者配置错误。<br>
	 * @return
	 */
	public boolean checkSpellAvailable(String language){
		if (realTimeSpell == null) {
			return false;
		}
		
		if (!realTimeSpell.checkLangAvailable(language)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 获取错误的单词
	 * @param text
	 * @param targetLanguage
	 * @return
	 */
	public List<SingleWord> getErrorWords(String tgtText, String targetLanguage){
		return realTimeSpell.getErrorWords(tgtText, targetLanguage);
	}
	
	
	/**
	 * 加哉实时拼写检查 ;
	 */
	private void runExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CONSTANT_realTimeSpell_extentionId);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof IRealTimeSpellCheck) {
					ISafeRunnable runnable = new ISafeRunnable() {
						public void handleException(Throwable exception) {
							exception.printStackTrace();
						}
						public void run() throws Exception {
							realTimeSpell = (IRealTimeSpellCheck) o;
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
