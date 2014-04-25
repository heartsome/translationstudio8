package net.heartsome.cat.ts.pretranslation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.heartsome.cat.ts.pretranslation"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
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
	 * 提供一个图片文件对插件的相对路径，返回该图片的描述信息。
	 * @param path
	 *            图片资源对插件的相对路径。
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * 提供一个图片文件对插件的相对路径，返回该图片被伸缩变换为16*16像素的描述信息。
	 * @param path
	 *            the path
	 * @return the icon descriptor
	 */
	public static ImageDescriptor getIconDescriptor(String path) {
		ImageDescriptor image = getImageDescriptor(path);
		ImageData data = image.getImageData();
		data = data.scaledTo(16, 16);
		image = ImageDescriptor.createFromImageData(data);
		return image;
	}
}
