/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.ts.ui;

import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * . Activator类控制插件生命周期。
 * @author stone
 * @version
 * @since JDK1.6
 */
public class Activator extends AbstractUIPlugin {

	/** 插件ID。 */
	public static final String PLUGIN_ID = "net.heartsome.cat.ts.ui";

	/** 共享的插件实例。 */
	private static Activator plugin;

	public static BundleContext context;
	/**
	 * 构造器。
	 */
	public Activator() {
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
		Activator.context = context;
		// Load the font for preference store
		String fontName = getPreferenceStore().getString(IPreferenceConstants.XLIFF_EDITOR_FONT_NAME);
		int size = getPreferenceStore().getInt(IPreferenceConstants.XLIFF_EDITOR_FONT_SIZE);
		FontData fontData = new FontData();
		fontData.setHeight(size);
		fontData.setName(fontName);
		JFaceResources.getFontRegistry().put(Constants.XLIFF_EDITOR_TEXT_FONT, new FontData[]{fontData});
		
		fontName = getPreferenceStore().getString(IPreferenceConstants.MATCH_VIEW_FONT_NAME);
		size = getPreferenceStore().getInt(IPreferenceConstants.MATCH_VIEW_FONT_SIZE);
		fontData = new FontData();
		fontData.setHeight(size);
		fontData.setName(fontName);
		JFaceResources.getFontRegistry().put(Constants.MATCH_VIEWER_TEXT_FONT, new FontData[]{fontData});
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
