package net.heartsome.cat.tmx.converter;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public final class LanguageUtils {
	
	/**
	 * 转换语言代码，转换成平台标准的语言代码，如en-US,zh
	 * @param lang
	 * @return ;
	 */
	public static String convertLangCode(String lang) {
		if (lang == null || lang.equals("")) {
			return lang;
		}
		if (lang.length() == 5) {
			String[] code = lang.split("-");
			if (code.length == 2) {
				return code[0].toLowerCase() + "-" + code[1].toUpperCase();
			} else {
				return lang;
			}
		} else if (lang.length() == 2) {
			return lang.toLowerCase();
		} else {
			return lang;
		}
	}
}