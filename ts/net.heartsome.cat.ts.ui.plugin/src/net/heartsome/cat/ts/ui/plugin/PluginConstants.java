package net.heartsome.cat.ts.ui.plugin;

import net.heartsome.cat.ts.ui.plugin.resource.Messages;

/**
 * 常量类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public interface PluginConstants {
	
	/** 版权信息 */
	String PLUGIN_COPY_RIGHT = Messages.getString("plugin.PluginConstants.right");
	
	/** 网址信息 */
	String PLUGIN_WEB_SITE = "http://www.heartsome.net";
	
	/** XSL Transformation 的 Logo 路径 */
	String LOGO_XSL_PATH = "icons/XSLConverter.png";
	
	/** XSL Transformation 菜单项显示的图片路径 */
	String LOGO_XSL_MENU_PATH = "icons/XSLMenu.png";
	
	/** TBXMaker 的 Logo 路径 */
	String LOGO_TBXMAKER_PATH = "icons/CSV2TBXConverter.png";
	
	/** TBXMaker 菜单项显示的图片路径 */
	String LOGO_TBXMAKER_MENU_PATH = "icons/CSV2TBXMenu.png";
	
	/** TBXMaker 对话框中打开 CSV 文件的工具栏图片路径 */
	String PIC_OPEN_CSV_PATH = "icons/open.png";
	
	/** TBXMaker 对话框中保存为 TBX 文件的工具栏图片路径 */
	String PIC_EXPORT_TBX_PATH = "icons/save.png";
	
	/** TBXMaker 对话框中删除列的工具栏图片路径 */
	String PIC_DELETE_COLUMN_PATH = "icons/remove.png";
	
	/** TBXMaker 对话框中选择列属性的工具栏图片路径 */
	String PIC_SET_COLUMN_PATH = "icons/sellang.png";
	
	/** TBXMaker 对话框中帮助内容的工具栏图片路径 */
	String PIC_HELP_PATH = "icons/help.png";
	
	/** 插件配置相关信息所存放的文件相对工作空间的路径--robert */
	String PC_pluginConfigLocation = ".metadata/.preference/pluginConfig.xml"; 
	
	/** CSV 2 TMX Converter 的 Logo 路径 */
	String LOGO_CSV2TMX_PATH = "icons/CSV2TMXConverter.png";
	
	/** CSV 2 TMX Converter 菜单项的 Logo 路径 */
	String LOGO_CSV2TMX_MENU_PATH = "icons/CSV2TMXMenu.png";
	
	/** CSV 2 TMX Converter 的版本 */
	public static final String CSV2TMX_VERSION = "1.1.0";
	
	/** 插件配置中，输入或输出的值(对应返回与进程):当前文档 --robert */
    String DOCUMENT = "document"; 
    /** 插件配置中，输入或输出的值(对应返回与进程):当前文本 --robert */
    String SEGMENT = "seg";     
    /** 插件配置中，输入或输出的值(对应返回与进程):已经转换文件 --robert */
    String EXCHANGEFILE = "file";
    /** 插件配置中，输入或输出的值(对应返回与进程):空 --robert */
    String NONE = "none"; 
    
	/** MARTIF 2 TBX Converter 的 Logo 路径 */
	String LOGO_MARTIF2TBX_PATH = "icons/MARTIF2TBXConverter.png";
	
	/** MARTIF 2 TBX Converter 菜单项显示的图片路径 */
	String LOGO_MARTIF2TBX_MENU_PATH = "icons/MARTIF2TBXMenu.png";
	
	/** java properties viewer 的 Logo 路径 */
	String LOGO_PROERTIESVIEWER_PATH = "icons/JPropViewer.png";
	
	/** java properties viewer 菜单项显示的图片路径 */
	String LOGO_PROERTIESVIEWER_MENU_PATH = "icons/JPropViewerMenu.png";
	
	/** RTFCleaner 的 Logo 路径 */
	String LOGO_RTFCLEANER_PATH = "icons/RTFCleaner.png";
	
	/** RTFCleaner 菜单项显示的图片路径 */
	String LOGO_RTFCLEANER_MENU_PATH = "icons/RTFCleanerMenu.png";
	
	/** tmx to txt converter 的 Logo 路径 */
	String LOGO_TMX2TXTCONVERTER_PATH = "icons/TMX2TXTConverter.png";
	
	/** tmx to txt converter 菜单项显示的图片路径 */
	String LOGO_TMX2TXTCONVERTER_MENU_PATH = "icons/TMX2TXTConverterMenu.png";
	
	/** TMX Validator 的 Logo 路径 */
	String LOGO_TMXVALIDATOR_PATH = "icons/TMXValidator.png";
	
	/** TMX Validator 菜单项显示的图片路径 */
	String LOGO_TMXVALIDATOR_MENU_PATH = "icons/TMXValidatorMenu.png";
	
	/** TBXMaker 对话框中清理无效字符 工具栏图片路径 */
	String PIC_clearChar_PATH = "icons/chars.png";
	
	/** 以下为帮助中的图片路径 */
	String HELP_TOC_CLOSED = "icons/help/Normal/tocclosed.png";
	String HELP_TOC_OPEN = "icons/help/Normal/tocopen.png";
	String HELP_BOOK_CLOSED = "icons/help/Normal/bookclosed.png";
	String HELP_BOOK_OPEN = "icons/help/Normal/bookopen.png";
	String HELP_OPEN_FILE = "icons/help/Normal/open.png";
	String HELP_BACK = "icons/help/Normal/back.png";
	String HELP_FORWARD = "icons/help/Normal/forward.png";
	String HELP_FIND = "icons/help/Normal/find.png";
	String HELP_TOPIC = "icons/help/Normal/topic.png";
	String HELP_SPLASH = "icons/help/Splash/about.png";
}
