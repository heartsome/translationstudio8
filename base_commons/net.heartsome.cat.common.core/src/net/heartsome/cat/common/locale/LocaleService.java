package net.heartsome.cat.common.locale;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import net.heartsome.cat.common.core.CoreActivator;
import net.heartsome.cat.common.file.LanguageConfiger;

/**
 * 对语言、国家/区域、字符集进行操作的方法定义
 * @author cheney
 * @since JDK1.6
 */
public final class LocaleService {

	private static String[] pageCodes;

	private static Map<String, Language> defaultLanguage;
	
	private static LanguageConfiger langConfiger = new LanguageConfiger();
	
	private LocaleService() {
		// 防止此类被实例化
	}

	/**
	 * 获取语言配置对象
	 * @return ;
	 */
	public static LanguageConfiger getLanguageConfiger(){
		if(langConfiger == null){
			langConfiger = new LanguageConfiger();
		}
		return langConfiger;
	}
	
	/**
	 * Java 平台支持的字符集名称
	 * @return Java 平台支持的字符集名称;
	 */
	public static String[] getPageCodes() {
		if (pageCodes == null) {
			TreeMap<String, Charset> charsets = new TreeMap<String, Charset>(Charset.availableCharsets());
			Set<String> keys = charsets.keySet();
			String[] pageCodes = new String[keys.size()];

			Iterator<String> i = keys.iterator();
			int j = 0;
			while (i.hasNext()) {
				Charset cset = charsets.get(i.next());
				pageCodes[j++] = cset.displayName();
			}
			LocaleService.pageCodes = pageCodes;
		}
		return pageCodes;
	}

	/**
	 * 平台（HS 应用）支持的语言集的名称
	 * @return 平台（HS 应用）支持的语言集的代码名称;
	 */
	public static String[] getLanguageCodes() {
		Map<String, Language> languageMap = getDefaultLanguage();
		int size = languageMap.keySet().size();
		String[] result = new String[size];
		return languageMap.keySet().toArray(result);
	}

	/**
	 * 平台（HS 应用）支持的语言集的名称
	 * @return 平台（HS 应用）支持的语言集的代码名称;
	 */
	public static String[] getLanguages() {
		Map<String, Language> languageMap = getDefaultLanguage();
		int size = languageMap.keySet().size();
		String[] result = new String[size];
		TreeMap<String, Language> tree = new TreeMap<String, Language>(languageMap);
		int i = 0;
		for (Language language : tree.values()) {
			result[i++] = getLanguageDisplayString(language);
		}
		return result;
	}

	/**
	 * 得到该语言用于显示的字符串格式。<br/>
	 * 例如：zh-CN Chinese(People's Republic)
	 * @param language
	 * @return ;
	 */
	private static String getLanguageDisplayString(Language language) {
		if (language == null) {
			return "";
		}
		return language.getCode() + " " + language.getName();
	}

	/**
	 * 解析语言代码 xml 文件，如果解析失败，则返回空 map
	 * @param isBidi
	 *            true 即标识 xml 文件中有 bidi 属性，以及返回 code、bidi 对应的 map；否则返回 code、语言名称对应的 map
	 * @return 根据 isBidi 标识，返回相应的 map;
	 */
	private static Map<String, Language> getLanguageConfiguration() {
		if(langConfiger == null){
			langConfiger = new LanguageConfiger();
		}
		return langConfiger.getAllLanguage();
	}

	/**
	 * 把以文件形式存储的语言代码转换成字符串的形式
	 * @return ;
	 */
	public static String getLanguageConfigAsString() {
		StringBuffer result = new StringBuffer();
		InputStream is = CoreActivator.getConfigurationFileInputStream(CoreActivator.LANGUAGE_CODE_PATH);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
			reader.close();
			// 默认的语言代码文件是跟插件打包在一起的，不会出现读取文件失败的问题，所以在此忽略相关异常
		} catch (FileNotFoundException e) {
			// 忽略此异常
			e.printStackTrace();
		} catch (IOException e) {
			// 忽略此异常
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 得到语言代码
	 * @param language
	 *            语言
	 * @return 语言代码，如果没有此代码，则返回空字符串;
	 */
	public static String getLanguageCodeByLanguage(String language) {
		String result = "";
		if (language == null) {
			return result;
		}
		language = language.trim();
		if (language.equals("")) {
			return result;
		}
		int index = language.indexOf(" ");
		if (index > -1) {
			return language.substring(0, index);
		}
		return result;
	}

	/**
	 * 得到语言名称
	 * @param language
	 *            语言
	 * @return 语言名称，如果没有此名称，则返回空字符串;
	 */
	public static String getLanguageNameByLanguage(String language) {
		String result = "";
		if (language == null) {
			return result;
		}
		language = language.trim();
		if (language.equals("")) {
			return result;
		}
		int index = language.indexOf(" ");
		if (index > -1) {
			return language.substring(index + 1);
		}
		return result;
	}

	/**
	 * 根据语言代码得到某种语言
	 * @param LanguageCode
	 *            语言代码
	 * @return 语言，如果没有此语言，则返回空字符串;
	 */
	public static String getLanguage(String LanguageCode) {
		String result = "";
		if (LanguageCode == null) {
			return result;
		}

		Map<String, Language> languageMap = getDefaultLanguage();
		Language language = languageMap.get(LanguageCode);
		return getLanguageDisplayString(language);
	}

	/**
	 * @param languageName
	 *            语言名称
	 * @return 语言名称对应的语言代码，如果没有此名称对应的代码，则返回空字符串;
	 */
	public static String getLanguageCode(String languageName) {
		String result = "";
		if (languageName == null) {
			return result;
		}
		Map<String, Language> languageMap = getDefaultLanguage();
		Set<String> keySet = languageMap.keySet();
		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			Language language = languageMap.get(key);
			if (languageName.equals(language.getName())) {
				result = key;
			}
		}
		return result;
	}

	/**
	 * @param languageCode
	 *            语言代码
	 * @return 语言代码对应的名称，如果没有此代码对应的名称，则返回空字符串;
	 */
	public static String getLanguageName(String languageCode) {
		String result = "";
		if (languageCode == null || languageCode.trim().equals("")) {
			return result;
		}
		Map<String, Language> languageMap = getDefaultLanguage();
		if (languageMap.containsKey(languageCode)) {
			result = languageMap.get(languageCode).getName();
		}
		return result;
	}

	/**
	 * 支持双向的语言集
	 * @return 返回支持双向的语言集;
	 */
	public static String[] getBidirectionalLangs() {
		return new String[0];
	}

	/**
	 * @param country
	 *            国家代码
	 * @return 国家代码对应的国家名称;
	 */
	public static String getCountryName(String country) {
		return "";
	}

	/**
	 * ISO 639 中定义的语言名称
	 * @param languageCode
	 *            语言代码
	 * @return 返回 ISO 639 中定义的语言名称;
	 */
	public static String getISO639(String languageCode) {
		return "";
	}

	/**
	 * 默认或用户更新设置后的语言 Map
	 * @return 返回默认或用户更新设置后的语言 Map;
	 */
	public static Map<String, Language> getDefaultLanguage() {
		if (defaultLanguage == null) {
			defaultLanguage = getLanguageConfiguration();
		}
		return defaultLanguage;
	}

	/**
	 * 验证集合中的字符串是否为语言代码
	 * @param languages
	 * @return ;
	 */
	public static boolean verifyLanguages(Vector<String> languages) {
		Map<String, Language> languageMap = getDefaultLanguage();
		for (String langCode : languages) {
			if (!languageMap.containsKey(langCode)) {
				 return false;
			}
		}
		return true;
	}
}
