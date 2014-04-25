package net.heartsome.cat.database.sqlite;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLite 相关工具类
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class SQLiteUtils {
	private static final Pattern UPPER = Pattern.compile("[\\p{javaUpperCase}&&[^\\p{Upper}]]+");

	/**
	 * select upper(term) from terms where term="Accusé";
	 * SQLite upper() function apparently ignores special characters – it had been uppercasing everything except our é,
	 * causing our dictionary query to fail.
	 * @param str
	 * @param locale
	 * @return ;
	 */
	public static String lowerNonAscii(String str, Locale locale) {
		StringBuilder buffer = new StringBuilder();
		Matcher matcher = UPPER.matcher(str);
		int start = 0;
		while (matcher.find()) {
			String nonMatch = str.substring(start, matcher.start());
			String match = str.substring(matcher.start(), matcher.end()).toLowerCase(locale);
			buffer.append(nonMatch).append(match);
			start = matcher.end();
		}
		String tail = str.substring(start, str.length());
		return buffer.append(tail).toString();
	}

	public static void main(String[] args) {
		String test = "BĒ /BEː/ ";
		System.out.println(test + " > " + lowerNonAscii(test, Locale.ENGLISH));
	}
}
