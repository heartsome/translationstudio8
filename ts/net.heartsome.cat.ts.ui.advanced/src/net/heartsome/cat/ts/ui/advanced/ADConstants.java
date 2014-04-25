package net.heartsome.cat.ts.ui.advanced;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * 高级插件所用到的常量
 * @author robert 2012-02-17
 * @version
 * @since JDK1.6
 */
public class ADConstants {
	private static String sep = File.separator;
	public static String configLocation = Platform.getConfigurationLocation().getURL().getPath();
	
	/** catalogue.xml的文件路径 */
	public static final String catalogueXmlPath = ".metadata/net.heartsome.cat.converter/catalogue/catalogue.xml";
	public static final String cataloguePath = ".metadata/net.heartsome.cat.converter/catalogue";
	/** 选择目录后保存所选文件的文件夹路径 */
	public static final String AD_cataBrowseFileMemory = "ts.ad.cataBrowseFileMemory";
	/** xml转换器配置中，配置文件在产品所存放的位置 */
	public static final String AD_xmlConverterConfigFolder = ".metadata/net.heartsome.cat.converter/ini";
	public static final String AD_SRXConfigFolder = "net.heartsome.cat.converter" + sep + "srx";

}
