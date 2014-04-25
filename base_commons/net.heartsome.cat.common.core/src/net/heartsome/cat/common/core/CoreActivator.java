package net.heartsome.cat.common.core;

import java.io.InputStream;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreActivator extends AbstractUIPlugin {

	/** 插件ID。 */
	public static final String PLUGIN_ID = "net.heartsome.cat.common.core";

	/** 共享的插件实例。 */
	private static CoreActivator plugin;

	// 配置文件存放的路径前缀
	private static final String PATH_PREF = "/configuration";

	// 默认的语言代码文件存放的路径
	public static final String LANGUAGE_CODE_PATH = PATH_PREF + "/langCodes.xml";

	// 国家代码文件存放的路径
	public static final String ISO3166_1_PAHT = PATH_PREF + "/ISO3166-1.xml";

	// 语言代码 ISO689-1 文件存放的路径
	public static final String ISO639_1_PAHT = PATH_PREF + "/ISO639-1.xml";

	// 语言代码 ISO689-2 文件存放的路径
	public static final String ISO639_2_PAHT = PATH_PREF + "/ISO639-2.xml";

	public final static Logger logger = LoggerFactory.getLogger(CoreActivator.class.getName());

	public CoreActivator() {

	}

	/**
	 * 启动插件应用，创建共享的插件实例。
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/**
	 * 停止插件应用，并销毁共享的插件实例。
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * 获得默认的共享的插件实例。
	 * @return the shared instance
	 */
	public static CoreActivator getDefault() {
		return plugin;
	}

	/**
	 * @param path
	 *            相对插件根目录的文件路径，如/configuration/langCodes.xml
	 * @return 返回插件内部配置文件输入流，如果找不到路径对应的文件，则返回 NULL;
	 */
	public static InputStream getConfigurationFileInputStream(String path) {
		return CoreActivator.class.getResourceAsStream(path);
	}
}
