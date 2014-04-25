/**
 * StringUtilsBasic.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.string;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.org.tools.utils.regexp.Regexp;

/**
 * 所有关于字符串的基本操作.
 * @author  simon
 * @version 
 * @since   JDK1.6
 */
public class StringUtilsBasic {

	/**
	 * 构造方法.
	 */
	protected StringUtilsBasic() {
		throw new UnsupportedOperationException(); // prevents calls from subclass
	}

	/**
	 * 用新的字符串替换旧字符串中需要替换的字符（这个方法的特点是忽略了用作正则的特殊字符）.
	 * @param line
	 *            需要做替换处理的字符串，如果为NULL，返回NULL
	 * @param oldString
	 *            替换的字符串，如果为NULL，返回 line
	 * @param newString
	 *            新的字符串，如果为NULL，返回空字符串
	 * @return String
	 * 			  经过处理后的字符串
	 */
	public static String replaceString(String line, String oldString, String newString) {
		return replaceString(line, oldString, newString, false);
	}

	/**
	 * 用新的字符串替换旧字符串中需要替换的字符（这个方法的特点是忽略了用作正则的特殊字符）.
	 * @param line
	 *            需要做替换处理的字符串，如果为 null，返回 null
	 * @param oldString
	 *            替换的字符串，如果为 null，返回 line
	 * @param newString
	 *            新的字符串，如果为 null，返回空字符串
	 * @param trimSingleQuotes
	 * 			  是否去除除首尾之外的单引号
	 * @return String
	 * 			  经过处理后的字符串
	 */
	public static String replaceString(String line, String oldString, String newString, boolean trimSingleQuotes) {
		if (line == null) {
			return null;
		}
		if (oldString == null) {
			return line;
		}
		if (newString == null) {
			newString = "";
		}

		if (trimSingleQuotes) {
			newString = newString.replaceAll("\\\\", "\\\\\\\\");
			newString = newString.replaceAll("'", "\\\\'");
		}

		int i = 0;
		i = line.indexOf(oldString, i);
		if (i >= 0) {
			char[] line2 = line.toCharArray();
			char[] newString2 = newString.toCharArray();
			int oLength = oldString.length();
			StringBuffer buf = new StringBuffer(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;
			int j = i;
			while ((i = line.indexOf(oldString, i)) > 0) {
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
				j = i;
			}
			buf.append(line2, j, line2.length - j);
			return buf.toString();
		}
		return line;
	}

	/**
	 * 空值检测.
	 * @param data
	 *            要检查的字符串
	 * @return String
	 * 			  如果传入的字符串为 null，返回空串“”，否则返回 data
	 */
	public static String checkNullStr(String data) {
		if (data == null) {
			return "";
		}
		return data;
	}

	/**
	 * 如果传入的字符串为 null 或空字符串 则返回 returnValue 空值检测.
	 * @param data
	 *            要检查的字符串
	 * @param returnValue
	 *            data 为空或者 data 中无实际数据时返回的值
	 * @return String
	 * 			  data 为空或者 data 中无实际数据时，返回returnValue；否则返回 data
	 */
	public static String checkNullStr(String data, String returnValue) {
		if (data == null || "".equals(data.trim())) {
			return returnValue;
		}
		return data;
	}

	/**
	 * 去除字符串 data 首尾的逗号.
	 * @param data
	 *            要处理的字符串
	 * @return String
	 * 			  如果 data 为空或者 data 中无实际数据，返回空串"";如果 data 以逗号开始或者结束，
	 * 			  则将 data 首尾的逗号删除后返回；否则返回 data
	 */
	public static String checkStrStartOrEndWithComma(String data) {
		if (data == null || "".equals(data.trim())) {
			return "";
		}
		data = data.trim();
		if (data.startsWith(",")) {
			data = data.substring(data.indexOf(",") + 1, data.length());
		}
		if (data.endsWith(",")) {
			data = data.substring(0, data.lastIndexOf(","));
		}
		return data;
	}

	/**
	 * 去除字符串 data 首尾的 regex 字符串
	 * @param data
	 *            要处理的字符串
	 * @param regex
	 *            data 首尾要删除的字符串
	 * @return String
	 * 			  如果 data 为空或者 data 中无实际数据，返回空串"";如果 data 以 regex 开始或者结束，
	 * 			  则将 data 首尾的 regex 删除后返回；否则返回 data
	 */
	public static String checkStrStartOrEndWithComma(String data, String regex) {
		if (data == null || "".equals(data.trim())) {
			return "";
		}
		data = data.trim();
		if (data.startsWith(regex)) {
			data = data.substring(data.indexOf(regex) + 1, data.length());
		}
		if (data.endsWith(regex)) {
			data = data.substring(0, data.lastIndexOf(regex));
		}
		return data;
	}

	/**
	 * 空值检测.
	 * @param data
	 *            要检查的字符串
	 * @return boolean 
	 * 			  false：空值或者空字符串；true：非空
	 */
	public static boolean checkNull(String data) {
		if (data == null || "".equals(data.trim())) {
			return false;
		}
		data = data.replaceAll("　", "");
		if ("".equals(data.trim())) {
			return false;
		}
		return true;
	}

	/**
	 * 空值检测.
	 * @param data
	 *            要检查的字符串
	 * @return boolean 
	 * 			  false：空值或者空字符串；true：非空
	 */
	public static boolean checkNull(StringBuffer data) {
		if (data == null || "".equals(data.toString().trim())) {
			return false;
		}
		String dataStr = data.toString();
		dataStr = dataStr.replaceAll(" ", "");
		if ("".equals(dataStr.trim())) {
			return false;
		}
		return true;
	}

	/**
	 * 清空StringBuffer中的值.
	 * @param strbf
	 *            需要被清空的 StringBuffer
	 * @return StringBuffer
	 * 			  清空后的 StringBuffer
	 */
	public static StringBuffer clearStringBuffer(StringBuffer strbf) {
		return strbf.delete(0, strbf.length());
	}

	/**
	 * 判断参数 str 是否存在于参数 src 中(即 str 是否是 src 的一个子串).
	 * @param src
	 *            用逗号隔开的字符串，例如 simon,terry,steven
	 * @param str
	 *            用来判断是否存在的参数，例如 simon 或者 arlene
	 * @return boolean
	 * 			  true 表示存在，false 表示不存在；src 或者 str 为空时，返回 false
	 */
	public static boolean checkExistStr(String src, String str) {
		if (!checkNull(src)) {
			return false;
		}
		if (!checkNull(str)) {
			return false;
		}
		return (("," + src + ",").indexOf("," + str + ",") != -1);
	}

	/**
	 * 在字符串 src 中去除字符串 str,并保持 str 原有的逗号隔开的规则.
	 * @param src
	 *            用逗号隔开的字符串，例如 simon,terry,steven
	 * @param str
	 *            去除的参数，例如 simon 或者 arlene
	 * @return String
	 * 				用逗号隔开的字符串。src 或者 str 为空时，返回 stc
	 */
	public static String removeExistStr(String src, String str) {
		if (!checkNull(src) || !checkNull(str)) {
			return src;
		}
		String result = replaceString("," + src + ",", "," + str, "");
		if (result.length() != 1) {
			return result.substring(1, result.length() - 1);
		} else {
			return "";
		}
	}

	/**
	 * 判断两个字符串数组中有没有重复项.
	 * @param str1
	 *            字符串数组1
	 * @param str2
	 *            字符串数组2
	 * @return boolean
	 * 				true: 有重复项；false：没有重复项。str1 或者 str2 为空时，返回 false
	 */
	public static boolean checkExist(String[] str1, String[] str2) {
		if (str1 == null || str2 == null) {
			return false;
		}
		List<String> list = Arrays.asList(str2);
		for (String str : str1) {
			if (list.contains(str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 用字符串数组 replace 中的字符串去替换 source 中的相应字符串.
	 * 如 source="The disk \"{1}\" contains {0} file(s)."
	 * 	replace=new String[]{"1","MyDisk"}，则经过该方法处理后的字符串为
	 * The disk "MyDisk" contains 1 file(s).如果 replace 的长度大于source中的空余位置的个数(即source中{0},{1}...的个数)，
	 * 则超过的部分舍弃，如果 source 中无空余位置，则该方法返回 source
	 * @param source
	 *            要替换的字符串
	 * @param replace
	 *            替换的字符串数组
	 * @return String
	 * 				经过替换后的字符串。如果 source 为 null，返回 null;如果 replace 为 null，返回source
	 */
	public static String replaceString(String source, String[] replace) {
		if (source == null) {
			return null;
		}
		MessageFormat message = new MessageFormat(source);
		return message.format(replace);
	}

	/**
	 * 检查 str 的长度.
	 * @param str
	 *            要检查的字符串
	 * @return boolean
	 * 				如果 str 中有实际数据(即 str 不是只有空格或者换行符组成的字符串)，返回 true，否则返回 false
	 */
	public static boolean hasLength(String str) {
		return str != null && str.trim().length() > 0;
	}

	/**
	 * 将 value 中 按 separator 分隔的字符串数组替换为 map 中的 value 值(map 中的 key 为要替换的字符串).
	 * 如：value="abc-def-ghi",separator="-",map中有一个元素，key 为 abc, value 为 123,则经过该方法处理后
	 * 返回的字符串为 "123-def-ghi"
	 * @param value
	 *            要处理的字符串
	 * @param separator
	 *            分隔符
	 * @param map
	 *            key 为 要替换的字符串(也是 value 中按 separator 拆分的字符串数组中的某一项)，value 为 新的字符串
	 * @return String
	 * 				经过处理后的字符串，如果 map 为空，返回 value; 如果 map 中无 value 中按 separator 拆分的字符串数组中的
	 * 				任意一项(即没有可替换的字符串)，则返回 value
	 */
	public static String replaceSeparator(String value, String separator, Map<String, String> map) {
		if (map == null || map.size() == 0) {
			return value;
		}
		StringBuilder sb = new StringBuilder();
		String[] valueSplit = value.split(separator);
		for (int i = 0; i < valueSplit.length; i++) {
			String replace = map.get(valueSplit[i]);
			if (null != replace && !"".equals(replace)) {
				sb.append(replace);
			} else {
				sb.append(valueSplit[i]);
			}
			if (i < valueSplit.length - 1) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	/**
	 * 获取字符串 str 在字符串 srcStr 中出现第index次的位置，index范围不符合逻辑返回-1.
	 * 如：indexOf("abcabdabe","ab",2)=6
	 * @param srcStr
	 *            任意字符串
	 * @param str
	 *            要搜索的子字符串
	 * @param index
	 *            str 在 srcStr 中出现的次数
	 * @return int
	 * 				str 在 srcStr 中出现第index次的位置	
	 */
	public static int indexOf(String srcStr, String str, int index) {
		if (index == 0) {
			return -1;
		}
		if (index == 1) {
			return srcStr.indexOf(str);
		}
		return srcStr.indexOf(str, indexOf(srcStr, str, index - 1) + str.length());
	}

	/**
	 * 比较 string 的字符长度与 maxLength 的关系，一个汉字代表两个字符.
	 * @param string
	 *            任意字符串
	 * @param maxLength
	 *            规定的最大长度
	 * @return boolean
	 * 				如果 string 的字符长度不大于 maxLength, 返回 true;如果 string 为空，返回 true；否则返回 false
	 */
	public static boolean checkMaxLength(String string, int maxLength) {
		if (string == null) {
			return true;
		}
		return string.replaceAll("[^\\x00-\\xff]", "**").length() <= maxLength;
	}

	/**
	 * 按规定的长度拆分字符串.
	 * @param string
	 *            任意字符串
	 * @param length
	 *            指定的长度
	 * @return String[]
	 * 				按指定长度拆分后的字符串数组，如果 string 为 null，返回 null
	 */
	public static String[] splitString(String string, int length) {
		if (string == null) {
			return null;
		}
		char[] bytes = string.toCharArray();
		int resultLength = bytes.length / length + 1;
		String[] result = new String[resultLength];

		for (int i = 0; i < resultLength; i++) {
			if (i == resultLength - 1) {
				result[i] = new String(bytes, i * length, bytes.length - i * length);
			} else {
				result[i] = new String(bytes, i * length, length);
			}
		}
		return result;
	}

	/**
	 * 获取随机数字符串.
	 * @return String
	 * 				随机数字符串.
	 */
	public static String getRandomStr() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis());
		sb.append(new Random().nextInt(10000));
		return sb.toString();
	}
	
	/**
	 * 将 String 列表转化成字符串，格式是：1,2,3,4,...
	 * @param list
	 * 			要处理的字符串集合
	 * @return String
	 * 				list 转化后的字符串，如果 list 为 null 或者 list 中无数据，返回空串""
	 */
	public static String listToString(List<String> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		String temp = list.toString();
		return temp.substring(1, temp.length() - 1);
	}
	/**
	 * 将 String 列表转化成字符串，格式是：'1','2','3','4',...
	 * @param list
	 * 			要处理的字符串集合
	 * @return String
	 * 				list 转化后的字符串，如果 list 为 null 或者 list 中无数据，返回空串""
	 */
	public static String listToStringWithSingleQuote(List<String> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		List<String> newList = new ArrayList<String>();
		for (String temp : list) {
			newList.add("'" + temp + "'");
		}
		return listToString(newList);
	}
	
	/**
	 * 将字符串的两端都加上单引号('), 如：singleQuote("a")="'a'"
	 * @param source
	 * 			任意字符串
	 * @return String
	 * 				source 两端加上单引号的字符串
	 */
	public static String singleQuote(String source) {
		return "'" + source + "'";
	}
	
	/**
	 * 将字符串str按中英文分组，如:str="123测试test",则返回值为[1123, 0测试, 1test]，
	 * 其中List中的每个字符串的首字符为标志位，1代表英文，0代表中文;如果str为空字符串，则返回null
	 * @param str
	 * 			任意字符串
	 * @return List&lt;String&gt;
	 * 				str 按中英文分组后的字符串集合
	 */
	public static List<String> getChineseAndEnglish(String str) {
		List<String> strList = new ArrayList<String>();
		if (str == null || str.trim().equals("")) {
			return null;
		}
		char[] ch = str.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			if (i == 0) {
				if (isChinese(ch[i])) {
					strList.add("0" + ch[i]);
				} else {
					strList.add("1" + ch[i]);
				}
			} else {
				if (isChinese(ch[i]) && isChinese(ch[i - 1])) {
					String s = strList.get(strList.size() - 1);
					s += ch[i];
					strList.remove(strList.size() - 1);
					strList.add(s);
				} else if (!isChinese(ch[i]) && !isChinese(ch[i - 1])) {
					String s = strList.get(strList.size() - 1);
					s += ch[i];
					strList.remove(strList.size() - 1);
					strList.add(s);
				} else {
					if (isChinese(ch[i])) {
						strList.add("0" + ch[i]);
					} else {
						strList.add("1" + ch[i]);
					}
				}
			}
		}
		return strList;
	}
	
	/**
	 * 判断字符c是否为中文
	 * @param c
	 * 			要判断的字符c
	 * @return boolean
	 * 			true：中文，false：英文
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c); 
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION	//GENERAL_PUNCTUATION 判断中文的“号
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION	//CJK_SYMBOLS_AND_PUNCTUATION 判断中文的。号
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS)	//HALFWIDTH_AND_FULLWIDTH_FORMS 判断中文的，号
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 判断字符串是否是由小数组成
	 * @param str
	 * 			要检查的字符串
	 * @return boolean
	 * 				true: str 由小数组成
	 */
	public static boolean isFloat(String str) {
		Pattern patternFig = Pattern.compile(Regexp.RATIONAL_NUMBERS_REGEXP);
		Matcher matcherFig = patternFig.matcher(str);
		return matcherFig.matches();
	}
	
	/**
	 * 判断字符串是否为日期
	 * @param str
	 * 			要判断的字符串
	 * @param format
	 * 			日期格式，按该格式检查字符串
	 * @return boolean
	 * 			符合为true,不符合为false
	 */
	public static boolean isDate(String str, String format) {
		if (hasLength(str)) {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			formatter.setLenient(false);
			try {
				formatter.format(formatter.parse(str));
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		return false;
	}
	
}