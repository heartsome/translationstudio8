/**
 * Converter.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The Interface Converter. 转换器接口，所有转换器实现都需要实现此接口。 具体的转换器实现，在以 OSGI 服务进行注册的时候，需要指定所支持的 <code>TYPE</code><code
 * ，</code>
 * CONVERT_DIRECTION</code>。否则注册的服务不会被使用。如果不是在 OSGI 环境中使用，则需要实现相应的工厂方法实例化具体的实现类，避免客户端代码直接实例化具体实现类。.
 * @author cheney
 * @version
 * @since JDK1.6
 */
public interface Converter {

	/** The Constant DEBUG_MODE. */
	boolean DEBUG_MODE = true;

	/** The Constant HSSCHEMALOCATION. */
	String HSSCHEMALOCATION = "http://www.heartsome.net.cn/2008/XLFExtension XLFExtension.xsd";

	/** The Constant HSNAMESPACE. */
	String HSNAMESPACE = "http://www.heartsome.net.cn/2008/XLFExtension";

	/** 转换器所支持的转换类型（文件格式）. */
	String ATTR_TYPE = "type";

	/** 所支持的文件类型名称。. */
	String ATTR_TYPE_NAME = "type.name";

	/** 转换器的名字. */
	String ATTR_NAME = "name";

	/** 转换的方向。正向或反向。. */
	String ATTR_DIRECTION = "direction";

	/** 正向转换. */
	String DIRECTION_POSITIVE = "positive";

	/** 反向转换. */
	String DIRECTION_REVERSE = "reverse";

	/** 源文件路径. */
	String ATTR_SOURCE_FILE = "source.file";

	/** XLIFF文件路径. */
	String ATTR_XLIFF_FILE = "xliff.file";

	/** 源文件语言. */
	String ATTR_SOURCE_LANGUAGE = "source.lang";
	
	/** 目标语言. */
	String ATTR_TARGET_LANGUAGE = "target.lang";
	
	/** 源文件编码. */
	String ATTR_SOURCE_ENCODING = "source.encoding";

	/** 骨架文件路径. */
	String ATTR_SKELETON_FILE = "skeleton.file";

	/** 目标文件路径. */
	String ATTR_TARGET_FILE = "target.file";

	/** catalogue 的文件路径. */
	String ATTR_CATALOGUE = "catalogue.file";

	/** 分段规则文件路径. */
	String ATTR_SRX = "srx.file";

	/** 是否以 element 进行分割. */
	String ATTR_SEG_BY_ELEMENT = "seg_by_element";

	/** The Constant TRUE. */
	String TRUE = "true";

	/** The Constant FALSE. */
	String FALSE = "false";

	/** The Constant ATTR_IS_SUITE. */
	String ATTR_IS_SUITE = "isSuite";

	/** The Constant ATTR_QT_TOOLID. */
	String ATTR_QT_TOOLID = "QT_TOOLID";

	/** The Constant QT_TOOLID_DEFAULT_VALUE. */
	String QT_TOOLID_DEFAULT_VALUE = "XLFEditor auto-Quick Translation";

	/** The Constant ATTR_INI_FILE. */
	String ATTR_INI_FILE = "ini.file";

	/** The Constant ATTR_LOCK_XTRANS. */
	String ATTR_LOCK_XTRANS = "lock_xtrans";

	/** The Constant ATTR_LOCK_100. */
	String ATTR_LOCK_100 = "lock_100";

	/** The Constant ATTR_LOCK_101. */
	String ATTR_LOCK_101 = "lock_101";

	/** The Constant ATTR_LOCK_REPEATED. */
	String ATTR_LOCK_REPEATED = "lock_repeated";

	/** The Constant ATTR_IS_INDESIGN. */
	String ATTR_IS_INDESIGN = "isInDesign";

	/** The Constant ATTR_IS_RESX. */
	String ATTR_IS_RESX = "isResx";

	/** The Constant ATTR_IS_GENERIC. */
	String ATTR_IS_GENERIC = "isGeneric";

	/** The Constant ATTR_PROGRAM_FOLDER. 配置文件目录 */
	String ATTR_PROGRAM_FOLDER = "program.folder";

	/** The Constant PROGRAM_FOLDER_DEFAULT_VALUE. 默认配置文件目录 */
	String PROGRAM_FOLDER_DEFAULT_VALUE = "./";

	/** The Constant ATTR_FORMAT. 文件类型 */
	String ATTR_FORMAT = "source.format";

	/** The Constant ATTR_IS_TAGGEDRTF. 是否为 TaggedRTF 格式 */
	String ATTR_IS_TAGGEDRTF = "isTaggedRTF";

	/** 是否按 CR/LF 分段 */
	String ATTR_BREAKONCRLF = "breakOnCRLF";

	/** 将骨架嵌入 xliff 文件 */
	String ATTR_EMBEDSKL = "embedSkl";

	/** ini 目录路径 */
	String ATTR_INIDIR = "iniDir";

	/** 是否为预览翻译模式 */
	String ATTR_IS_PREVIEW_MODE = "isPreviewMode";

	// TODO
	/*
	 * 此接口需要进一步进行完善，传入参数 Map 所需要指定的 key，返回 Map 中所需要指定的 key。
	 */
	/**
	 * Convert.
	 * @param args
	 *            转换文件所需要的特定参数，如源文件路径、目标文件路径等。不能为<code>NULL</code>
	 * @param monitor
	 *            监视转换过程的进度，<code>IProgressMonitor</code>位于 org.eclipse.equinox.common 插件中。此插件中的类不依赖特定 eclispe 平台或 OSGI
	 *            平台的特定类，见<code>ConverterException</code>及相关 API 的说明。允许为 <code>NULL</code>
	 * @return 转换结果。如目标文件路径等
	 * @throws ConverterException
	 *             在转换的过程中发生错误，则抛出<code>ConverterException</code>
	 */
	Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException;

	/**
	 * Gets the name.
	 * @return 返回转换器的名字
	 */
	String getName();

	/**
	 * Gets the type.
	 * @return 返回转换器支持的文件类型
	 */
	String getType();

	/**
	 * Gets the type name.
	 * @return 返回转换器支持的文件类型名称
	 */
	String getTypeName();

}
