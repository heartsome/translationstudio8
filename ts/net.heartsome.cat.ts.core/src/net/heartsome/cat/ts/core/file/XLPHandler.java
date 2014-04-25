package net.heartsome.cat.ts.core.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.IByteBuffer;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 项目配置文件（XLP 文件）的处理类
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class XLPHandler {

	// 日志管理器
	private static final Logger LOGGER = LoggerFactory.getLogger(XLPHandler.class);

	/** 文件分割符 */
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/** 换行符 */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** 默认格式 */
	public static final String ATTR_DEFAULT_FORMAT = "defaultFormat";

	/** 格式 */
	public static final String ATTR_FORMAT = "format";

	/** 源语言 */
	public static final String ATTR_SRC_LANG = "srcLang";

	/** 源编码 */
	public static final String ATTR_SRC_ENC = "srcEnc";

	/** 目标编码 */
	public static final String ATTR_TGT_ENC = "tgtEnc";

	/** 源文件 */
	public static final String ATTR_SOURCE = "source";

	/** XLIFF 文件 */
	public static final String ATTR_XLIFF = "xliff";

	/** 目标文件 */
	public static final String ATTR_TARGET = "target";

	/** XLP 文件名 */
	public static final String FILENAME = ".xlp";

	/** VTDNav 对象 */
	private VTDNav vn;

	/** 项目配置文件路径 */
	private final String xlpPath;

	public XLPHandler(String projectPath) throws Exception {
		if (projectPath == null) {
			throw new Exception(Messages.getString("file.XLPHandler.msg1"));
		}
		xlpPath = projectPath + FILE_SEPARATOR + FILENAME;
		File file = new File(xlpPath);
		if (!file.exists()) {
			throw new Exception(MessageFormat.format(Messages.getString("file.XLPHandler.msg2"), xlpPath));
		}
		vn = openFile(xlpPath);
	}

	/**
	 * 打开 XLP 文件
	 * @param xlpPath
	 *            XLP 文件路径
	 * @return ;
	 */
	private VTDNav openFile(String xlpPath) {
		VTDGen vg = new VTDGen();
		if (vg.parseFile(xlpPath, true)) {
			return vg.getNav();
		} else {
			String errorMsg = MessageFormat.format(Messages.getString("file.XLPHandler.logger1"), xlpPath);
			LOGGER.error(errorMsg);
			return null;
		}
	}

	/**
	 * 是否是 XLP 文件
	 * @return ;
	 */
	public boolean IsXLPFile() {
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/xlfedit-project");
			if (ap.evalXPath() != -1) {
				return true;
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 得到配置文件中记录的默认值项
	 * @return ;
	 */
	public Map<String, String> getDefaultItem() {
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/xlfedit-project");
			if (ap.evalXPath() != -1) {
				Map<String, String> defaultItem = new VTDUtils(vn).getCurrentElementAttributs();
				if (defaultItem == null) {
					defaultItem = new Hashtable<String, String>();
				}
				return defaultItem;
				// xlfedit-project 节点的所有属性
				// String[] attrs = { ATTR_DEFAULT_FORMAT, ATTR_SRC_LANG, ATTR_SRC_ENC, ATTR_TGT_LANG, ATTR_TGT_ENC,
				// ATTR_XLIFF, ATTR_TARGET, ATTR_PREFIX, ATTR_SUFFIX };
				// for (String attr : attrs) {
				// int index = vn.getAttrVal(attr);
				// String value = index == -1 ? "" : vn.toNormalizedString(index);
				// defaultItem.put(attr, value);
				// }
			}
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 得到配置文件中记录的所有项
	 * @param attrName
	 *            属性名
	 * @param values
	 *            属性值
	 * @return ;
	 */
	public List<Map<String, String>> getItems(String attrName, List<String> values) {
		HashMap<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		AutoPilot ap = new AutoPilot(vn);
		try {
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath("/xlfedit-project/item[@" + attrName + "]");
			XMLModifier xm = new XMLModifier(vn);
			boolean isModified = false;
			while (ap.evalXPath() != -1) {
				int index = vn.getAttrVal(attrName);
				String value = index != -1 ? vn.toString(index) : "";
				if (values.contains(value) && !map.containsKey(value)) {
					Map<String, String> item = vu.getCurrentElementAttributs();
					if (item == null) {
						item = new Hashtable<String, String>();
					}
					map.put(value, item);
				} else {
					xm.remove(); // 去除无用的记录
					isModified = true;
				}
			}
			if (isModified) {
				save(xm);
			}
			for (String value : values) {
				if (!map.containsKey(value)) {
					HashMap<String, String> item = new HashMap<String, String>();
					item.put(attrName, value);
					map.put(value, item);
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (TranscodeException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		// 按顺序添加
		ArrayList<Map<String, String>> items = new ArrayList<Map<String, String>>();
		for (String value : values) {
			items.add(map.get(value));
		}
		return items;
	}

	/**
	 * 保存 XLIFF 信息
	 * @param sourceInfo
	 *            源文件信息 ;
	 */
	public void saveSourceInfo(List<? extends Map<String, String>> sourceInfo) {
		try {
			XMLModifier xm = new XMLModifier(vn);
			AutoPilot ap = new AutoPilot(vn);
			StringBuffer items = new StringBuffer();
			String pattern = "<item {0}=\"{1}\" {2}=\"{3}\" {4}=\"{5}\" {6}=\"{7}\" />";
			ap.selectXPath("/xlfedit-project");
			if (ap.evalXPath() != -1) {
				for (Map<String, String> map : sourceInfo) {
					vn.push();

					String source = map.get(ATTR_SOURCE);
					String format = map.get(ATTR_FORMAT);
					String srcLang = map.get(ATTR_SRC_LANG);
					String srcEnc = map.get(ATTR_SRC_ENC);

					String xpath = "./item[@" + ATTR_SOURCE + "=\"" + source + "\"]";
					ap.selectXPath(xpath);
					if (ap.evalXPath() != -1) {
						updateAttribute(xm, ATTR_FORMAT, format);
						updateAttribute(xm, ATTR_SRC_LANG, srcLang);
						updateAttribute(xm, ATTR_SRC_ENC, srcEnc);
					} else {
						String item = MessageFormat.format(pattern, ATTR_SOURCE, source, ATTR_FORMAT, format,
								ATTR_SRC_LANG, srcLang, ATTR_SRC_ENC, srcEnc);
						ap.selectXPath(xpath);
						items.append(item).append(LINE_SEPARATOR);
					}

					vn.pop();
				}
				if (items.length() > 1) {
					xm.insertBeforeTail(items.toString());
				}
				save(xm);
			}
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (TranscodeException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 保存 XLIFF 信息
	 * @param xliffInfo
	 *            XLIFF 信息 ;
	 */
	public void saveXliffInfo(List<? extends Map<String, String>> xliffInfo) {
		try {
			XMLModifier xm = new XMLModifier(vn);
			AutoPilot ap = new AutoPilot(vn);
			StringBuffer items = new StringBuffer();
			String pattern = "<item {0}=\"{1}\" {2}=\"{3}\" />";
			ap.selectXPath("/xlfedit-project");
			if (ap.evalXPath() != -1) {
				for (Map<String, String> map : xliffInfo) {
					vn.push();

					String xliff = map.get(ATTR_XLIFF);
					String tgtEnc = map.get(ATTR_TGT_ENC);

					String xpath = "./item[@" + ATTR_XLIFF + "='" + xliff + "']";
					ap.selectXPath(xpath);
					if (ap.evalXPath() != -1) {
						updateAttribute(xm, ATTR_TGT_ENC, tgtEnc);
					} else {
						String item = MessageFormat.format(pattern, ATTR_XLIFF, xliff, ATTR_TGT_ENC, tgtEnc);
						items.append(item).append(LINE_SEPARATOR);
					}

					vn.pop();
				}
				if (items.length() > 1) {
					xm.insertBeforeTail(items.toString());
				}
				save(xm);
			}
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (TranscodeException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (ModifyException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathParseException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 修改属性
	 * @param xm
	 *            XMLModifier 对象
	 * @param attrName
	 *            属性名
	 * @param attrValue
	 *            属性值
	 * @throws ModifyException
	 * @throws UnsupportedEncodingException
	 * @throws NavException
	 *             ;
	 */
	private void updateAttribute(XMLModifier xm, String attrName, String attrValue) throws ModifyException,
			UnsupportedEncodingException, NavException {
		int index = vn.getAttrVal(attrName);
		if (index != -1) {
			xm.updateToken(index, attrValue);
		} else {
			xm.insertAttribute(" " + attrName + "=\"" + attrValue + "\"");
		}
	}

	/**
	 * 保存文件
	 * @param xm
	 * @throws IOException
	 * @throws ParseException
	 * @throws TranscodeException
	 * @throws ModifyException
	 *             ;
	 */
	private void save(XMLModifier xm) throws IOException, ParseException, TranscodeException, ModifyException {
		vn = xm.outputAndReparse();
		IByteBuffer buffer = vn.getXML();

		// 写隐藏文件，在Windows 平台下不能使用 FileOutputStream。会抛出“拒绝访问”的异常。因此采用 RandomAccessFile
		RandomAccessFile raf = new RandomAccessFile(xlpPath, "rw");
		raf.setLength(0); // 清除文件内容
		raf.seek(0); // 设置文件指针到文件起始位置
		raf.write(buffer.getBytes());
		raf.close();
	}

}
