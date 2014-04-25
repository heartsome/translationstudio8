package net.heartsome.cat.ts.ui.plugin.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.heartsome.cat.ts.ui.plugin.Activator;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工具类
 * @author peason
 * @version
 * @since JDK1.6
 */
public class PluginUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);

	/**
	 * 根据相对路径获取绝对路径
	 * @param relativePath
	 * @return ;
	 */
	public static String getAbsolutePath(String relativePath) {
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL defaultUrl = buddle.getEntry(relativePath);
		String imagePath = relativePath;
		try {
			imagePath = new File(FileLocator.toFileURL(defaultUrl).getPath()).getAbsolutePath();
		} catch (IOException e) {
			LOGGER.error(Messages.getString("util.PluginUtil.logger1"), e);
			e.printStackTrace();
		}
		return imagePath;
	}

	/**
	 * 获取 catalogue.xml 所在路径
	 * @return ;
	 */
	public static String getCataloguePath() {
		String path = Platform.getConfigurationLocation().getURL().getPath();
		String catalogPath = path + "net.heartsome.cat.converter" + System.getProperty("file.separator") + "catalogue"
				+ System.getProperty("file.separator") + "catalogue.xml";
		return catalogPath;
	}

	/**
	 * 获取 XCS 模板所在路径
	 * @return ;
	 */
	public static String getTemplatePath() {
		String path = Platform.getConfigurationLocation().getURL().getPath();
		String templatePath = path + "net.heartsome.cat.converter" + System.getProperty("file.separator") + "templates";
		return templatePath;
	}

	public static String getConfigurationFilePath(String filePath) {
		StringBuffer configPath = new StringBuffer(
				new File(Platform.getConfigurationLocation().getURL().getPath()).getAbsolutePath());
		return configPath.append(File.separator).append("net.heartsome.cat.ts.ui.plugin").append(File.separator).append(filePath).toString();
	}

	/**
	 * 创建 Label，文本右对齐
	 * @param parent
	 *            父控件
	 * @param text
	 *            Label 上显示的文本
	 */
	public static void createLabel(Composite parent, String text) {
		Label lbl = new Label(parent, SWT.None);
		lbl.setText(text);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(lbl);
	}
}
