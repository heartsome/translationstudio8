package net.heartsome.cat.convert.ui.model;

import static net.heartsome.cat.converter.Converter.FALSE;
import static net.heartsome.cat.converter.Converter.TRUE;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.heartsome.cat.converter.Converter;

import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在转换文件的过程，用于构建除用户(UI)设置的其他所需配置信息
 * @author cheney
 * @since JDK1.6
 */
public class ConverterContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterContext.class);

	// 转换器相关配置文件顶层目录的名称
	private static final String CONVERTER_ROOT_CONFIG_FOLDER_NAME = "net.heartsome.cat.converter";

	/**
	 * srx 目录
	 */
	public static String srxFolder;

	// 文件路径分割符
	private static String fileSeparator = System.getProperty("file.separator");

	// 系统的配置文件夹路径
	private URL configurationLocation;

	// 转换器相关配置文件所在的顶层目录
	private String configurationFolderForConverter;

	// 转换 xliff 的配置 bean
	private ConversionConfigBean configBean;

	/**
	 * catalogue 文件路径
	 */
	public static String catalogue;

	// default srx 文件路径
	public static String defaultSrx;

	// ini 目录路径
	private String iniDir;

	static {
		URL temp = Platform.getConfigurationLocation().getURL();
		if (temp != null) {
			String configurationPath = new File(temp.getPath()).getAbsolutePath();
			String converterConfigurationPath = configurationPath + fileSeparator
					+ CONVERTER_ROOT_CONFIG_FOLDER_NAME + fileSeparator;
			srxFolder = converterConfigurationPath + "srx" + fileSeparator;
			defaultSrx = srxFolder + "default_rules.srx";
			catalogue = converterConfigurationPath + "catalogue" + fileSeparator + "catalogue.xml";
		}
	}

	/**
	 * 构建函数
	 * @param configBean
	 *            用户在 UI 所设置的配置信息
	 */
	public ConverterContext(ConversionConfigBean configBean) {
		this.configBean = configBean;
		configurationLocation = Platform.getConfigurationLocation().getURL();
		if (configurationLocation != null) {
			try {
				String configurationPath = new File(configurationLocation.toURI()).getAbsolutePath();
				configurationFolderForConverter = configurationPath + fileSeparator + CONVERTER_ROOT_CONFIG_FOLDER_NAME
						+ fileSeparator;
				iniDir = configurationFolderForConverter + "ini";
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 转换文件所需要的配置信息
	 * @return 返回转换文件所需要的配置信息;
	 */
	public Map<String, String> getConvertConfiguration() {
		Map<String, String> configuration = new HashMap<String, String>();
		configuration.put(Converter.ATTR_SOURCE_FILE, ConverterUtil.toLocalPath(configBean.getSource()));
		configuration.put(Converter.ATTR_XLIFF_FILE, ConverterUtil.toLocalPath(configBean.getTarget()));
		configuration.put(Converter.ATTR_SKELETON_FILE, ConverterUtil.toLocalPath(configBean.getSkeleton()));
		configuration.put(Converter.ATTR_SOURCE_LANGUAGE, configBean.getSrcLang());
		configuration.put(Converter.ATTR_TARGET_LANGUAGE, configBean.getTgtLang());
		configuration.put(Converter.ATTR_SOURCE_ENCODING, configBean.getSrcEncoding());
		boolean segByElement = configBean.isSegByElement();
		configuration.put(Converter.ATTR_SEG_BY_ELEMENT, segByElement ? TRUE : FALSE);
		configuration.put(Converter.ATTR_INI_FILE, ConverterUtil.toLocalPath(configBean.getInitSegmenter()));
		String srx = configBean.getInitSegmenter();
		if (srx == null || srx.trim().equals("")) {
			srx = defaultSrx;
		}
		configuration.put(Converter.ATTR_SRX, srx);
		configuration.put(Converter.ATTR_LOCK_XTRANS, configBean.isLockXtrans() ? TRUE : FALSE);
		configuration.put(Converter.ATTR_LOCK_100, configBean.isLock100() ? TRUE : FALSE);
		configuration.put(Converter.ATTR_LOCK_101, configBean.isLock101() ? TRUE : FALSE);
		configuration.put(Converter.ATTR_LOCK_REPEATED, configBean.isLockRepeated() ? TRUE : FALSE);
		configuration.put(Converter.ATTR_BREAKONCRLF, configBean.isBreakOnCRLF() ? TRUE : FALSE);
		configuration.put(Converter.ATTR_EMBEDSKL, configBean.isEmbedSkl() ? TRUE : FALSE);
		configuration = getCommonConvertConfiguration(configuration);
		if (LOGGER.isInfoEnabled()) {
			printConfigurationInfo(configuration);
		}
		return configuration;
	}

	/**
	 * 逆转换文件所需要的配置信息
	 * @return 返回逆转换文件所需要的配置信息;
	 */
	public Map<String, String> getReverseConvertConfiguraion() {
		Map<String, String> configuration = new HashMap<String, String>();
		configuration.put(Converter.ATTR_XLIFF_FILE, ConverterUtil.toLocalPath(configBean.getSource()));
		configuration.put(Converter.ATTR_TARGET_FILE, ConverterUtil.toLocalPath(configBean.getTarget()));
		configuration.put(Converter.ATTR_SKELETON_FILE, configBean.getSkeleton());
		configuration.put(Converter.ATTR_SOURCE_ENCODING, configBean.getTargetEncoding());
		configuration.put(Converter.ATTR_IS_PREVIEW_MODE, configBean.isPreviewMode() ? TRUE : FALSE);
		configuration = getCommonConvertConfiguration(configuration);
		if (LOGGER.isInfoEnabled()) {
			printConfigurationInfo(configuration);
		}
		return configuration;
	}

	/**
	 * 设置通用的配置信息
	 * @param configuration
	 * @return ;
	 */
	private Map<String, String> getCommonConvertConfiguration(Map<String, String> configuration) {
		configuration.put(Converter.ATTR_CATALOGUE, catalogue);
		configuration.put(Converter.ATTR_INIDIR, iniDir);

		// 设置 xml 转换器中需要用到的 program folder，指向程序的配置目录
		configuration.put(Converter.ATTR_PROGRAM_FOLDER, configurationFolderForConverter);
		return configuration;
	}

	/**
	 * 打印配置信息
	 * @param configuration
	 */
	private void printConfigurationInfo(Map<String, String> configuration) {
		Iterator<String> iterator = configuration.keySet().iterator();
		StringBuffer buffer = new StringBuffer();
		buffer.append("configuration:--------------------\n");
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = configuration.get(key);
			buffer.append("key:" + key + "--value:" + value + "\n");
		}
		buffer.append("---------------------------------");
		System.out.println(buffer.toString());
	}

}
