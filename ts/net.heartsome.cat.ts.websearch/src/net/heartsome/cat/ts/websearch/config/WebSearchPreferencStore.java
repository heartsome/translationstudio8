/**
 * WebSearchPreferencStore.java
 *
 * Version information :
 *
 * Date:2013-9-22
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.websearch.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.websearch.Activator;
import net.heartsome.cat.ts.websearch.bean.SearchEntry;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 配置信息的保存类
 * @author yule
 * @version
 * @since JDK1.6
 */
public class WebSearchPreferencStore {

	public final static Logger logger = LoggerFactory.getLogger(WebSearchPreferencStore.class);

	public static String preFix_path = Activator.getPath();

//	private String customConfigPath = preFix_path + "configure/WebSearchConfig.xml";
	private String customConfigPath = null;

//	private String defaultConfigPath = preFix_path + "configure/WebSearchDefaultConfig.xml";
	private String defaultConfigPath = null;

	private VTDUtils customVu;

	private VTDUtils defaultVu;

	private static WebSearchPreferencStore ins = null;

	private PropertyChangeSupport support = new PropertyChangeSupport(this);

	private WebSearchPreferencStore() {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		try {
			customConfigPath = FileLocator.toFileURL(bundle.getEntry("configure/WebSearchConfig.xml")).getPath();
			defaultConfigPath = FileLocator.toFileURL(bundle.getEntry("configure/WebSearchDefaultConfig.xml")).getPath();
		} catch (Exception e) {
			logger.error("", e);
		}
		
		customVu = new VTDUtils();
		defaultVu = new VTDUtils();
		try {
			customVu.parseFile(customConfigPath, false);
			defaultVu.parseFile(defaultConfigPath, false);
		} catch (ParseException e) {
			logger.error("path:" + customVu + "|" + defaultVu, e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("path:" + customVu + "|" + defaultVu, e);
			e.printStackTrace();
		}

	}

	public static WebSearchPreferencStore getIns() {
		if (null == ins) {
			ins = new WebSearchPreferencStore();
		}
		return ins;

	}

	/**
	 * @param vu
	 * @param isDefault
	 *            getCustomContent===false:返回所有内容 getCustomContent===true ：返回非默认的内容
	 * @return ;
	 */
	private List<SearchEntry> getConfig(VTDUtils vu, boolean getCustomContent) {
		VTDNav vn = vu.getVTDNav();
		AutoPilot mainAp = new AutoPilot(vn);
		AutoPilot uesedAp = new AutoPilot(vn);
		AutoPilot nameAp = new AutoPilot(vn);
		AutoPilot urlAp = new AutoPilot(vn);
		List<SearchEntry> cache = new ArrayList<SearchEntry>();
		try {
			mainAp.selectXPath("/WebSearchInfo/Providers/ProviderInfo");
			uesedAp.selectXPath("./Used");
			nameAp.selectXPath("./Name");
			urlAp.selectXPath("./Url");
			SearchEntry temp = null;
			while (mainAp.evalXPath() != -1) {
				int attrVal = vn.getAttrVal("isDefault");
				if (getCustomContent) {
					if (-1 != attrVal) {
						continue;
					}
				}

				temp = new SearchEntry();
				if (-1 != attrVal) {
					temp.setDefault(true);
				}

				int idIndex = vn.getAttrVal("id");
				if (-1 != idIndex) {
					temp.setId(vn.toString(idIndex));
				}

				vn.push();
				uesedAp.resetXPath();
				if (uesedAp.evalXPath() != -1) {
					int textIndex = vn.getText();
					if (-1 != textIndex && null != temp) {
						temp.setChecked("true".equalsIgnoreCase(vn.toString(textIndex)) ? true : false);
					}
				} else {
					temp = null;
				}
				vn.pop();

				vn.push();
				nameAp.resetXPath();
				if (nameAp.evalXPath() != -1) {
					int textIndex = vn.getText();
					if (-1 != textIndex && null != temp) {
						temp.setSearchName(TextUtil.resetSpecialString(vn.toString(textIndex)));
					}
				} else {
					temp = null;
				}

				vn.pop();

				vn.push();
				urlAp.resetXPath();
				if (urlAp.evalXPath() != -1) {
					int textIndex = vn.getText();
					if (-1 != textIndex && null != temp) {
						temp.setSearchUrl((TextUtil.resetSpecialString(vn.toString(textIndex))));
					}
				} else {
					temp = null;
				}

				vn.pop();
				if (null != temp) {
					cache.add(temp);
				}
			}
		} catch (XPathEvalException e) {
			e.printStackTrace();
			logger.error("", e);
		} catch (NavException e) {
			e.printStackTrace();
			logger.error("", e);
		} catch (XPathParseException e) {
			e.printStackTrace();
			logger.error("", e);
		}

		return cache;
	}

	/**
	 * 获取所有自定义的配置信息
	 * @return ;
	 */
	public List<SearchEntry> getSearchConfig() {
		List<SearchEntry> config = getConfig(customVu, false);
		return config;
	}

	/**
	 * 获取自定义的配置信息中,已经勾选显示在视图页面中的选项
	 * @return ;
	 */
	public List<SearchEntry> getUseredConfig() {
		List<SearchEntry> config = getConfig(customVu, false);
		return getUseredConfig(config);
	}

	private List<SearchEntry> getUseredConfig(List<SearchEntry> searchEntrys) {
		List<SearchEntry> list = new ArrayList<SearchEntry>();
		if (null == searchEntrys) {
			return list;
		}
		for (SearchEntry s : searchEntrys) {
			if (s.isChecked()) {
				list.add(s);
			}
		}
		return list;

	}

	/**
	 * 获取默认的配置信息,包括自定义配置文件和默认配置文件两个文件中的信息
	 * @return ;
	 */
	public List<SearchEntry> getDefaluSearchConfig() {
		List<SearchEntry> config = getConfig(customVu, true);
		for (SearchEntry temp : config) {
			temp.setChecked(false);
		}
		List<SearchEntry> defaultconfig = getConfig(defaultVu, false);
		defaultconfig.addAll(config);
		return defaultconfig;
	}

	/**
	 * 保存设置值
	 * @param searchEntry
	 *            ;
	 */
	public void storeConfig(List<SearchEntry> searchEntry) {
		FileOutputStream out = null;
		boolean hasError = false;
		try {
			out = new FileOutputStream(customConfigPath);
			writeContent(out, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			writeContent(out,
					"<WebSearchInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instancexmlns:xsd=http://www.w3.org/2001/XMLSchema\">\r\n");
			writeContent(out, "	<Providers>\r\n");
			if (null != searchEntry) {
				for (SearchEntry s : searchEntry) {
					writeContent(out, convert2Xml(s));
				}
			}
			writeContent(out, "	</Providers>\r\n </WebSearchInfo>");
			out.flush();
			support.firePropertyChange("URL", null, getUseredConfig(searchEntry));
		} catch (FileNotFoundException e) {
			hasError = true;
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			hasError = true;
			e.printStackTrace();
		} catch (IOException e) {
			hasError = true;
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (hasError) {
				XMLModifier modifier;
				try {
					modifier = new XMLModifier(customVu.getVTDNav());
					modifier.output(customConfigPath);
				} catch (ModifyException e) {
					e.printStackTrace();
					logger.error("", e);
				} catch (TranscodeException e) {
					e.printStackTrace();
					logger.error("", e);
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("", e);
				}
			} else {
				try {
					customVu.parseFile(customConfigPath, false);
				} catch (ParseException e) {
					e.printStackTrace();
					logger.error("", e);
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("", e);
				}
			}

		}
	}

	private void writeContent(OutputStream out, String str) throws UnsupportedEncodingException, IOException {
		out.write(str.getBytes("utf-8"));
	}

	private String convert2Xml(SearchEntry searchEntry) {
		StringBuilder sb = new StringBuilder();
		String id = searchEntry.getId();
		id = " id = \"" + id + "\" ";
		boolean isDefault = searchEntry.isDefault();
		String defaultAttr = "";
		if (isDefault) {
			defaultAttr = " isDefault=" + "\"" + true + "\" ";
		}
		sb.append("		<ProviderInfo " + id + defaultAttr + ">\r\n");
		sb.append("				<Used >" + searchEntry.isChecked() + "</Used>\r\n");
		sb.append("				<Name>" + TextUtil.cleanSpecialString(searchEntry.getSearchName()) + "</Name>\r\n");
		sb.append("				<Url>" + TextUtil.cleanSpecialString(searchEntry.getSearchUrl()) + "</Url>\r\n");
		sb.append("		</ProviderInfo>\r\n");
		return sb.toString();
	}

	public void addProperChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void removeProperChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * 导入
	 * @param filePath
	 * @return ;
	 */
	public List<SearchEntry> importSearchConfig(String filePath) {
		VTDUtils vuTemp = new VTDUtils();
		try {
			vuTemp.parseFile(filePath, false);
			return getConfig(vuTemp, false);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error("", e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("", e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return null;
	}

	/**
	 * 导出
	 * @param targetFilePath
	 * @return ;
	 */
	public boolean exportSearchConfig(String targetFilePath) {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(customConfigPath);
			out = new FileOutputStream(targetFilePath);
			byte[] b = new byte[1024];
			int count = -1;
			while ((count = in.read(b)) != -1) {
				out.write(b, 0, count);
				out.flush();
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("", e);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("", e);
			return false;
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("", e);
				}
			}
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("", e);
				}
			}
		}
	}

}
