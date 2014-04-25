/**
 * SimpleMatcherFactory.java
 *
 * Version information :
 *
 * Date:2012-5-13
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.tm.simpleMatch;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.tm.resource.Messages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class SimpleMatcherFactory {
	public static final Logger logger = LoggerFactory.getLogger(SimpleMatcherFactory.class);

	private final String EXTENSION_ID = "net.heartsome.cat.ts.tm.simplematch.extension";
	private List<ISimpleMatcher> matchers;
	
	private static SimpleMatcherFactory instance;
	
	public static SimpleMatcherFactory getInstance(){
		if(instance == null){
			instance = new SimpleMatcherFactory();
		}
		return instance;
	}
	
	public List<ISimpleMatcher> getCuurentMatcher(){
		return matchers;
	}
	
	private SimpleMatcherFactory() {
		matchers = new ArrayList<ISimpleMatcher>();
		runExtension();
	}

	/**
	 * 加载记忆库匹配实现 ;
	 */
	private void runExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ISimpleMatcher) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("simpleMatch.SimpleMatcherFactory.logger1"), exception);
						}

						public void run() throws Exception {
							ISimpleMatcher simpleMatcher = (ISimpleMatcher) o;							
							matchers.add(simpleMatcher);
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("simpleMatch.SimpleMatcherFactory.logger1"), ex);
		}
	}
}
