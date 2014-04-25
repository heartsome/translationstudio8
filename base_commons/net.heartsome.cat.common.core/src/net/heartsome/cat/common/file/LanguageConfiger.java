/**
 * LangugeConfiger.java
 *
 * Version information :
 *
 * Date:Mar 20, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.core.CoreActivator;
import net.heartsome.cat.common.core.resource.Messages;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.FileLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 语言配置，实现从配置文件中存取相应的语言配置，语言code唯一
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class LanguageConfiger {

	private VTDUtils vu;
	private File langConfigFile;
	private AutoPilot ap;
	private static final Logger LOGGER = LoggerFactory.getLogger(LanguageConfiger.class);

	/**
	 * 构造器,如果文件不存在先构建文件,再用vtd解析.
	 */
	public LanguageConfiger() {
		String bundlePath;
		try {
			bundlePath = FileLocator.toFileURL(CoreActivator.getDefault().getBundle().getEntry("")).getPath();
			this.langConfigFile = new File(bundlePath + CoreActivator.LANGUAGE_CODE_PATH);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		parseFile();
	}

	private void parseFile() {
		VTDGen vg = new VTDGen();
		try {
			if (vg.parseFile(langConfigFile.getPath(), true)) {
				vu = new VTDUtils(vg.getNav());
				ap = new AutoPilot(vu.getVTDNav());
			} else {
				throw new ParseException();
			}
		} catch (NavException e) {
			LOGGER.error("", e);
		} catch (ParseException e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 返回所有语言列表，key:code,value:<code>Language</code>
	 */
	public Map<String, Language> getAllLanguage() {
		Map<String, Language> result = new HashMap<String, Language>();
		AutoPilot tempAp = new AutoPilot(vu.getVTDNav());
		String codeAttr = "code";
		String bidiAttr = "bidi";
		String imgAttr = "image";
		String bidiYes = "Yes";
		try {
			tempAp.selectXPath("/languages/lang");
			while (tempAp.evalXPath() != -1) {
				Map<String, String> attrs = vu.getCurrentElementAttributs();
				String code = attrs.get(codeAttr);
				String bidi = attrs.get(bidiAttr);
				String img = attrs.get(imgAttr);
				String langName = vu.getElementPureText();
				boolean isBidi = false;
				if (code != null && langName != null) {
					if (bidi != null && bidi.equals(bidiYes)) {
						isBidi = true;
					}
					result.put(code, new Language(code, langName, img == null ? "" : img, isBidi));
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
		} catch (NavException e) {
			LOGGER.error("", e);
		}
		return result;
	}

	/**
	 * 返回Code对应的Language对象，如果在配置中找到则返回null
	 * @param code
	 * @return ;
	 */
	public Language getLanguageByCode(String code) {
		AutoPilot tempAp = new AutoPilot(vu.getVTDNav());
		String bidiAttr = "bidi";
		String imgAttr = "image";
		String bidiYes = "Yes";
		try {
			tempAp.selectXPath("/languages/lang[@code='" + code + "']");
			if (tempAp.evalXPath() != -1) {
				Map<String, String> attrs = vu.getCurrentElementAttributs();
				String bidi = attrs.get(bidiAttr);
				String img = attrs.get(imgAttr);
				String langName = vu.getElementPureText();
				boolean isBidi = false;
				if (code != null && langName != null) {
					if (bidi != null && bidi.equals(bidiYes)) {
						isBidi = true;
					}
					return new Language(code, langName, img == null ? "" : img, isBidi);
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
		} catch (NavException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	/**
	 * 返回一组语言Code对应的<code>Language</code>对象，如果此Code在语言配置中未找到，此Language对象只保存Code
	 * @param codeList
	 * @return ;
	 */
	public List<Language> getLanuagesByCodes(List<String> codeList) {
		List<Language> result = new ArrayList<Language>();
		for (String code : codeList) {
			Language lang = getLanguageByCode(code);
			if (lang == null) {
				lang = new Language(code, "", "", false);
			}
			result.add(lang);
		}
		return result;
	}

	/**
	 * 通过Code，更新一种语言
	 * @param code
	 *            语言代码
	 * @param newLanguage
	 *            更新后的语言;
	 */
	public void updateLanguageByCode(String code, Language newLanguage) {
		String newValue = generateLangNode(newLanguage);
		XMLModifier xm = vu.update("/languages/lang[@code='" + code + "']", newValue);
		try {
			vu.bind(xm.outputAndReparse());
			saveToFile(xm, langConfigFile);
		} catch (Exception e) {
			LOGGER.error(Messages.getString("file.LanguageConfiger.logger1"), e);
		}
	}

	public void deleteLanguageByCode(String code) {
		if (code != null && !code.equals("")) {
			try {
				XMLModifier xm = vu.delete("/languages/lang[@code='" + code + "']");
				vu.bind(xm.outputAndReparse());
				saveToFile(xm, langConfigFile);
			} catch (Exception e) {
				LOGGER.error(Messages.getString("file.LanguageConfiger.logger2"), e);
			}
		}
	}

	/**
	 * 添加一种语言
	 * @param newLanguage
	 *            ;
	 */
	public void addLanguage(Language newLanguage) {
		String content = generateLangNode(newLanguage);
		if (!content.equals("")) {
			try {
				XMLModifier xm = vu.insert("/languages/text()", content);
				vu.bind(xm.outputAndReparse());
				saveToFile(xm, langConfigFile);
			} catch (Exception e) {
				LOGGER.error(Messages.getString("file.LanguageConfiger.logger3"), e);
			}
			ap.resetXPath();
		}
	}

	private String generateLangNode(Language language) {
		String img = language.getImagePath();
		if (img == null) {
			img = "";
		}
		return "<lang bidi=\"" + language.isBidi() + "\" code=\"" + language.getCode() + "\" image=\"" + img + "\">"
				+ language.getName() + "</lang>";
	}

	/**
	 * 保存文件
	 * @param xm
	 *            XMLModifier对象
	 * @param fileName
	 *            文件名
	 * @return 是否保存成功;
	 */
	private boolean saveToFile(XMLModifier xm, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();
			return true;
		} catch (ModifyException e) {
			LOGGER.error("", e);
		} catch (TranscodeException e) {
			LOGGER.error("", e);
		} catch (IOException e) {
			LOGGER.error("", e);
		}

		return false;
	}
}
