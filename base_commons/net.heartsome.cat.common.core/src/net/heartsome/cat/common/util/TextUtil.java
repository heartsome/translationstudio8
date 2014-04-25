package net.heartsome.cat.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;

import net.heartsome.cat.common.core.CoreActivator;
import net.heartsome.cat.common.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 处理
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class TextUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(TextUtil.class.getName());

	private static Hashtable<String, String> ISOLang;

	private static Hashtable<String, String> countries;

	private static Hashtable<String, String> descriptions;

	private static Hashtable<String, String> isBidi;

	private static final String _SPACE = " ";

	private TextUtil() {
		// 防止此类被实例化
	}

	public static String cleanString(String input) {
		input = input.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		return validChars(input);
	}

	public static String validChars(String input) {
		// Valid: #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
		// [#x10000-#x10FFFF]
		// Discouraged: [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF]
		//
		StringBuffer buffer = new StringBuffer();
		char c;
		int length = input.length();
		for (int i = 0; i < length; i++) {
			c = input.charAt(i);
			if (c == '\t' || c == '\n' || c == '\r' || c >= '\u0020' && c <= '\uD7DF' || c >= '\uE000' && c <= '\uFFFD') {
				// normal character
				buffer.append(c);
			} else if (c >= '\u007F' && c <= '\u0084' || c >= '\u0086' && c <= '\u009F' || c >= '\uFDD0'
					&& c <= '\uFDDF') {
				// Control character
				buffer.append("&#x" + Integer.toHexString(c) + ";"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (c >= '\uDC00' && c <= '\uDFFF' || c >= '\uD800' && c <= '\uDBFF') {
				// Multiplane character
				buffer.append(input.substring(i, i + 1));
			}
		}
		return buffer.toString();
	}

	/**
	 * @param string
	 * @param trim
	 * @return
	 */
	public static String normalise(String string, boolean trim) {
		boolean repeat = false;
		String rs = ""; //$NON-NLS-1$
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char ch = string.charAt(i);
			if (!Character.isSpaceChar(ch)) {
				if (ch != '\n') {
					rs = rs + ch;
				} else {
					rs = rs + " "; //$NON-NLS-1$
					repeat = true;
				}
			} else {
				rs = rs + " "; //$NON-NLS-1$
				while (i < length - 1 && Character.isSpaceChar(string.charAt(i + 1))) {
					i++;
				}
			}
		}
		if (repeat == true) {
			return normalise(rs, trim);
		}
		if (trim) {
			return rs.trim();
		}
		return rs;
	}

	/**
	 * @param string
	 * @param trim
	 * @return robert添加 2011-11-02
	 */
	public static String normalise(String string) {
		return normalise(string, true);
	}

	/**
	 * 清除特殊字符
	 * @param input
	 * @return
	 */
	public static String cleanSpecialString(String input) {
		input = input.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("\"", "&quot;");
		return input;
	}

	/**
	 * 恢复特殊字符
	 * @param input
	 * @return
	 */
	public static String resetSpecialString(String input) {
		input = input.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("&quot;", "\"");
		input = input.replaceAll("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		return input;

	}

	public static String getISO639(String code, String langFile) {
		if (code.equals("")) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}

		try {
			loadISOLang(langFile);
		} catch (Exception e) {
			e.printStackTrace();
			return ""; //$NON-NLS-1$
		}

		if (ISOLang.containsKey(code.toLowerCase())) {
			return ISOLang.get(code.toLowerCase());
		}

		return ""; //$NON-NLS-1$
	}

	private static void loadISOLang(String strLangFile) {
		if (strLangFile == null) {
			return;
		}
		ISOLang = new Hashtable<String, String>();
		VTDGen vg = new VTDGen();
		// vg.setDoc(strLangFile.getBytes());
		try {
			vg.setDoc(readBytesFromIS(CoreActivator.getConfigurationFileInputStream(strLangFile)));
			vg.parse(true);
			VTDNav vn = vg.getNav();
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/languages/lang");
			int codeIndex;
			String code = null;
			String langName;
			while ((ap.evalXPath()) != -1) {
				codeIndex = vn.getAttrVal("code");
				if (codeIndex != -1) {
					code = vn.toString(codeIndex);
				}
				langName = vu.getElementPureText();
				if (code != null && langName != null) {
					ISOLang.put(code, langName);
				}
			}
			ap.resetXPath();
		} catch (NavException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (XPathParseException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (XPathEvalException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EncodingException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EOFException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EntityException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (ParseException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (IOException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strLangFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} finally {
			vg.clear();
		}
	}

	public static String getCountryName(String country) {
		if (country.equals("")) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		try {
			loadCountries();
		} catch (Exception e) {
			e.printStackTrace();
			return ""; //$NON-NLS-1$
		}

		if (countries.containsKey(country.toUpperCase())) {
			return countries.get(country.toUpperCase());
		}

		return ""; //$NON-NLS-1$
	}

	private static void loadCountries() {
		String strFile = CoreActivator.ISO3166_1_PAHT;
		countries = new Hashtable<String, String>();
		VTDGen vg = new VTDGen();

		try {
			vg.setDoc(readBytesFromIS(CoreActivator.getConfigurationFileInputStream(strFile)));
			vg.parse(true);
			VTDNav vn = vg.getNav();
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/ISO_3166-1_List_en/ISO_3166-1_Entry");
			while ((ap.evalXPath()) != -1) {
				countries.put(vu.getChildContent("ISO_3166-1_Alpha-2_Code_element"),
						vu.getChildContent("ISO_3166-1_Country_name"));
			}
			ap.resetXPath();
		} catch (NavException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (XPathParseException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (XPathEvalException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EncodingException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EOFException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EntityException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (ParseException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (IOException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} finally {
			vg.clear();
		}
	}

	public static byte[] readBytesFromIS(InputStream is) throws IOException {
		int total = is.available();
		byte[] bs = new byte[total];
		is.read(bs);
		return bs;
	}

	public static String getLanguageCode(String language) {

		if (language.equals("")) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}

		try {
			loadLanguages();
		} catch (Exception e) {
			e.printStackTrace();
			return ""; //$NON-NLS-1$
		}
		Enumeration<String> keys = descriptions.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (language.equals(descriptions.get(key) + _SPACE + key)) { //$NON-NLS-1$
				return key;
			}
		}
		return language;
	}

	private static void loadLanguages() {
		descriptions = null;
		isBidi = null;
		descriptions = new Hashtable<String, String>();
		isBidi = new Hashtable<String, String>();
		String strFile = CoreActivator.LANGUAGE_CODE_PATH;
		VTDGen vg = new VTDGen();

		try {
			vg.setDoc(readBytesFromIS(CoreActivator.getConfigurationFileInputStream(strFile)));
			vg.parse(true);
			VTDNav vn = vg.getNav();
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/languages/lang");
			int codeIndex;
			String code = null;
			int bidiIndex;
			String bidi = null;
			String langName;
			while ((ap.evalXPath()) != -1) {
				codeIndex = vn.getAttrVal("code");
				if (codeIndex != -1) {
					code = vn.toString(codeIndex);
				}
				bidiIndex = vn.getAttrVal("bidi");
				if (bidiIndex != -1) {
					bidi = vn.toString(bidiIndex);
				}
				langName = vu.getElementPureText();
				if (code != null && langName != null) {
					descriptions.put(code, langName);
				}
				if (code != null && bidi != null) {
					isBidi.put(code, bidi);
				}
			}
			ap.resetXPath();
		} catch (NavException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (XPathParseException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (XPathEvalException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EncodingException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EOFException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (EntityException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (ParseException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} catch (IOException e) {
			if (LOGGER.isErrorEnabled()) {
				String msg = Messages.getString("util.TextUtil.logger1");
				Object args[] = { strFile };
				LOGGER.error(new MessageFormat(msg).format(args), e);
			}
		} finally {
			vg.clear();
		}
	}

	public static String getLanguageName(String language) {
		if (language.equals("")) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}

		try {
			loadLanguages();
		} catch (Exception e) {
			e.printStackTrace();
			return ""; //$NON-NLS-1$
		}

		Enumeration<String> keys = descriptions.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.toLowerCase().equals(language.toLowerCase())) {
				return descriptions.get(key) + _SPACE + key; //$NON-NLS-1$
			}
		}
		// code not found on the list
		// check if it is possible to build an ISO name
		switch (language.length()) {
		case 2:
			String iso = getISO639(language, CoreActivator.ISO639_1_PAHT); //$NON-NLS-1$
			if (iso.equals("")) { //$NON-NLS-1$
				return language;
			}
			return ISOLang.get(language.toLowerCase()) + _SPACE + language; //$NON-NLS-1$
		case 3:
			iso = getISO639(language, CoreActivator.ISO639_2_PAHT); //$NON-NLS-1$
			if (iso.equals("")) { //$NON-NLS-1$
				return language;
			}
			return ISOLang.get(language.toLowerCase()) + _SPACE + language; //$NON-NLS-1$
		case 5:
			language.replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			if (language.charAt(2) != '-') {
				return language;
			}
			String lang = language.substring(0, 2).toLowerCase();
			if (getISO639(lang, CoreActivator.ISO639_1_PAHT).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				return language;
			}
			String country = language.substring(3).toUpperCase();
			if (getCountryName(country).equals("")) { //$NON-NLS-1$
				return language;
			}
			return ISOLang.get(lang) + " (" + countries.get(country) + ")" + _SPACE + lang + "-" + country; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		case 6:
			language.replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			if (language.charAt(3) != '-') {
				return language;
			}
			lang = language.substring(0, 3).toLowerCase();
			if (getISO639(lang, CoreActivator.ISO639_2_PAHT).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				return language;
			}
			country = language.substring(4).toUpperCase();
			if (getCountryName(country).equals("")) { //$NON-NLS-1$
				return language;
			}
			return ISOLang.get(lang) + " (" + countries.get(country) + ")" + _SPACE + lang + "-" + country; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		default:
			return language;
		}
	}

	/**
	 * 获取字符串 str 在字符串 srcStr 中出现第index次的位置，index范围不符合逻辑返回-1. 如：indexOf("abcabdabe","ab",2)=6
	 * @param srcStr
	 *            任意字符串
	 * @param str
	 *            要搜索的子字符串
	 * @param index
	 *            str 在 srcStr 中出现的次数
	 * @param isCaseSensitive
	 *            是否区分大小写
	 * @return int str 在 srcStr 中出现第index次的位置
	 */
	public static int indexOf(String srcStr, String str, int index, boolean isCaseSensitive) {
		if (index == 0) {
			return -1;
		}
		if (index == 1) {
			if (isCaseSensitive) {
				return srcStr.indexOf(str);
			} else {
				return srcStr.toUpperCase().indexOf(str.toUpperCase());
			}
		}
		if (isCaseSensitive) {
			return srcStr.indexOf(str, indexOf(srcStr, str, index - 1, isCaseSensitive) + str.length());
		} else {
			return srcStr.toUpperCase().indexOf(str.toUpperCase(),
					indexOf(srcStr, str, index - 1, isCaseSensitive) + str.length());
		}
	}

	/**
	 * 当 Sql 语句使用 LIKE 字段时，\ 需要替换成4个 \，％，_,? 需要替换成\\%,\\_,\\?
	 * @param input
	 * @return ;
	 */
	public static String cleanStringByLikeWithMysql(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		input = input.replaceAll("'", "\\\\\\\\'");
		input = input.replaceAll("%", "\\\\\\\\%");
		input = input.replaceAll("_", "\\\\\\\\_");

		return input;
	}

	/**
	 * Oracle 数据库 Sql 中 like 查询条件要替换的特殊字符
	 * @param input
	 * @return ;
	 */
	public static String cleanStringByLikeWithOracle(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\", "\\\\\\\\");
		input = input.replaceAll("'", "''");
		input = input.replaceAll("%", "\\\\\\\\%");
		input = input.replaceAll("_", "\\\\\\\\_");

		return input;
	}

	/**
	 * Sql Server 数据库 Sql 中 like 查询条件要替换的特殊字符
	 * @param input
	 * @return ;
	 */
	public static String cleanStringByLikeWithMsSql(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\", "\\\\\\\\");
		input = input.replaceAll("'", "''");
		// input = input.replaceAll("\"", "\"\"");
		// input = input.replaceAll("[", "[[]");
		input = input.replaceAll("%", "[%]");
		input = input.replaceAll("_", "[_]");
		// input = input.replaceAll("^", "[^]");

		return input;
	}

	/**
	 * Postgre Sql 数据库 Sql 中 like 查询条件要替换的特殊字符
	 * @param input
	 * @return ;
	 */
	public static String cleanStringByLikeWithPostgreSql(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\", "\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
		input = input.replaceAll("'", "''");
		input = input.replaceAll("%", "\\\\\\\\\\\\\\\\%");
		input = input.replaceAll("_", "\\\\\\\\\\\\\\\\_");

		return input;
	}

	/**
	 * Postgre Sql 数据库 Sql 中 like 查询条件要替换的特殊字符
	 * @param input
	 * @return ;
	 */
	public static String cleanStringByLikeWithHSQL(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		input = input.replaceAll("'", "''");
		input = input.replaceAll("%", "\\\\\\\\%");
		input = input.replaceAll("_", "\\\\\\\\_");

		return input;
	}

	public static String replaceRegextSql(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\d", "[0-9]");
		input = input.replaceAll("\\\\D", "[^0-9]");
		input = input.replaceAll("\\\\w", "[a-zA-Z0-9_]");
		input = input.replaceAll("\\\\W", "[^a-zA-Z0-9_]");
		input = input.replaceAll("\\\\\\\\.", ".");
		input = input.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		input = input.replaceAll("'", "''");
		input = input.replaceAll("\\\\\\\\\\\\\\\\s", "[ \\\\f\\\\n\\\\r\\\\t\\\\v]");
		input = input.replaceAll("\\\\\\\\\\\\\\\\S", "[^ \\\\f\\\\n\\\\r\\\\t\\\\v]");
		return input;
	}

	public static String replaceRegextSqlWithMOP(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\d", "[0-9]");
		input = input.replaceAll("\\\\D", "[^0-9]");
		input = input.replaceAll("\\\\w", "[a-zA-Z0-9_]");
		input = input.replaceAll("\\\\W", "[^a-zA-Z0-9_]");
		input = input.replaceAll("\\\\s", "[[:space:]]");
		input = input.replaceAll("\\\\S", "[^[:space:]]");
		input = input.replaceAll("\\\\\\\\.", ".");
		input = input.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
		input = input.replaceAll("'", "''");
		return input;
	}

	/**
	 * HSQL 使用正则表达式时要替换的字符
	 * @param input
	 * @return ;
	 */
	public static String replaceRegextSqlWithHSQL(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("\\\\d", "[0-9]");
		input = input.replaceAll("\\\\D", "[^0-9]");
		input = input.replaceAll("\\\\w", "[a-zA-Z0-9_]");
		input = input.replaceAll("\\\\W", "[^a-zA-Z0-9_]");
		input = input.replaceAll("\\\\\\\\.", ".");
		input = input.replaceAll("'", "''");
		input = input.replaceAll("\\\\", "\\\\\\\\");
		input = input.replaceAll("\\\\\\\\s", "[\\\\f\\\\n\\\\r\\\\t\\\\v]");
		input = input.replaceAll("\\\\\\\\S", "[^ \\\\f\\\\n\\\\r\\\\t\\\\v]");
		return input;
	}

	/**
	 * 将语言进行常态化 robert 2012-02-03
	 * @param language
	 * @return ;
	 */
	public static String normLanguage(String language) {
		if ("".equals(language) || language == null) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		if (language.length() < 2) {
			return language.toLowerCase();
		} else {
			return language.substring(0, 2).toLowerCase() + language.substring(2, language.length());
		}
	}

	/**
	 * 将 xml 中的转义字符还原
	 * @param input
	 * @return ;
	 */
	public static String xmlToString(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("&lt;", "<");
		input = input.replaceAll("&gt;", ">");
		input = input.replaceAll("&apos;", "'");
		input = input.replaceAll("&quot;", "\"");
		input = input.replaceAll("&amp;", "&");
		return input;

	}

	/**
	 * 将 input 中的字符串转义
	 * @param input
	 * @return ;
	 */
	public static String stringToXML(String input) {
		if (input == null) {
			return "";
		}
		input = input.replaceAll("&", "&amp;");
		input = input.replaceAll("<", "&lt;");
		input = input.replaceAll(">", "&gt;");
		input = input.replaceAll("'", "&apos;");
		input = input.replaceAll("\"", "&quot;");
		return input;

	}

	/**
	 * 加载语言，针对插件开发模块 robert 2012-03-15
	 * @throws Exception
	 */
	public static Hashtable<String, String> plugin_loadLanguages() throws Exception {
		// 语言文件位置
		String languageXmlLC_1 = CoreActivator.ISO639_1_PAHT;
		String languageXmlLC_2 = CoreActivator.ISO639_2_PAHT;

		Hashtable<String, String> _languages = new Hashtable<String, String>();

		getLanguages(languageXmlLC_1, _languages);
		getLanguages(languageXmlLC_2, _languages);

		return _languages;
	}

	/**
	 * 加载国家名称，针对插件开发模块 robert 2012-03-15
	 * @param langXMlLC
	 * @param _languages
	 * @throws Exception
	 */
	public static Hashtable<String, String> plugin_loadCoutries() throws Exception {
		// 国家文件位置
		String countryXmlLC = CoreActivator.ISO3166_1_PAHT;
		Hashtable<String, String> _countries = new Hashtable<String, String>();

		VTDGen vg = new VTDGen();
		vg.setDoc(readBytesFromIS(CoreActivator.getConfigurationFileInputStream(countryXmlLC)));
		vg.parse(true);

		VTDNav vn = vg.getNav();

		AutoPilot ap = new AutoPilot(vn);
		AutoPilot childAP = new AutoPilot(vn);
		ap.selectXPath("/ISO_3166-1_List_en/ISO_3166-1_Entry");
		while (ap.evalXPath() != -1) {
			String code = "";
			String name = "";

			int index;
			vn.push();
			childAP.selectXPath("./ISO_3166-1_Alpha-2_Code_element");
			if (childAP.evalXPath() != -1) {
				if ((index = vn.getText()) != -1) {
					code = vn.toString(index).trim().toUpperCase();
				}
			}
			vn.pop();

			vn.push();
			childAP.selectXPath("./ISO_3166-1_Country_name");
			if (childAP.evalXPath() != -1) {
				if ((index = vn.getText()) != -1) {
					name = vn.toString(index).trim();
				}
			}
			vn.pop();

			if (!"".equals(code) && !"".equals(name)) {
				_countries.put(code, name);
			}
		}
		return _countries;
	}

	/**
	 * ，针对插件开发模块 robert 2012-03-15
	 * @param langXMlLC
	 * @param _languages
	 * @throws Exception
	 */
	private static void getLanguages(String langXMlLC, Hashtable<String, String> _languages) throws Exception {
		VTDGen vg = new VTDGen();
		vg.setDoc(readBytesFromIS(CoreActivator.getConfigurationFileInputStream(langXMlLC)));
		vg.parse(true);

		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("/languages/lang");
		while (ap.evalXPath() != -1) {
			if (vn.getAttrVal("code") != -1 && vn.getText() != -1) {
				_languages.put(vn.toString(vn.getAttrVal("code")).toLowerCase(), vn.toString(vn.getText()).trim());
			}
		}
	}

	/**
	 * 半角转全角的函数(SBC case) 任意字符串 全角字符串 全角空格为12288，半角空格为32 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
	 * @param input
	 * @return ;
	 */
	public static String toSBC(String input) {
		// 半角转全角：
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);

			}
		}
		return new String(c);
	}

	/**
	 * 全角转半角的函数(DBC case) 任意字符串 半角字符串 全角空格为12288，半角空格为32 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
	 * @param input
	 * @return ;
	 */
	public static String toDBC(String input) {
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == '\u3000') {
				c[i] = ' ';
			} else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}

	/**
	 * 16进制数字字符集
	 */
	private static String hexString = "0123456789ABCDEF";

	/**
	 * 将字符串编码成16进制数字,适用于所有字符（包括中文）
	 */
	public static String encodeHexString(String str) {
		// 根据默认编码获取字节数组
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// 将字节数组中每个字节拆解成2位16进制整数
		for (int i = 0; i < bytes.length; i++) {
			// int d = (bytes[i] & 0x0f) >> 0;
			// /System.out.println("bytes["+i+"]:"+bytes[i]+"_"+d);//bytes[i]得到的是对应的字符ASCII值
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));// 1-4
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));// 1-2

		}
		return sb.toString();
	}

	/**
	 * 将16进制数字解码成字符串,适用于所有字符（包括中文） decode(String bytes)方法里面的bytes字符串必须大写，即toUpperCase()
	 */
	public static String decodeHexString(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
		// 将每2位16进制整数组装成一个字节
		for (int i = 0; i < bytes.length(); i += 2) {
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
		}
		return new String(baos.toByteArray());
	}

	/**
	 * 不转义实体字符,只转义需要转义的字符
	 * 视图显示没有处理 单引号和双引号字符
	 * add by yule 2013-11-21
	 * @param oldResult
	 * @return ;
	 */
	public static String convertMachineTranslateResult(String oldResult) {
		if (null == oldResult || oldResult.isEmpty()) {
			return "";
		}
		String replaceEntiyChar = oldResult.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
		return replaceEntiyChar.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

}
