package net.heartsome.cat.convert.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	/**
	 * 在执行文件转换时，是否打印转换配置信息的控制开关
	 */
	public static final String CONVERSION_DEBUG_ON = "net.heartsome.cat.converter.ui/debug/conversion";

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.ui";

	// The shared instance
	private static Activator plugin;

	// bundle context
	private static BundleContext context;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Activator.context = context;
		plugin = this;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		Activator.context = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * 获得插件的 bundle context
	 * @return 插件的 bundle context;
	 */
	public static BundleContext getContext() {
		return context;
	}
}
