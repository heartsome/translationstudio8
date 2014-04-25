package net.heartsome.cat.ts.core.file;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class SourceTargetComparer {

	/**
	 * 调整空格：<br/>
	 * 比较译文与源文的段首/段末空格，自动调整其段首/段末空格与源文本一致
	 * @param src
	 *            源文本
	 * @param tgt
	 *            目标文本
	 * @return 调整后的目标文本;
	 */
	public static String replaceWhiteSpace(String src, String tgt) {
		if (src == null || tgt == null) {
			return null;
		}
		String[] whiteSpaces = src.split(src.trim());
		String head = "";
		String end = "";
		if (whiteSpaces.length == 1) {
			head = whiteSpaces[0];
		} else if (whiteSpaces.length > 1) {
			head = whiteSpaces[0];
			end = whiteSpaces[1];
		}
		return head + tgt.trim() + end;
	}

	/**
	 * 数字自动替换：<br/>
	 * 比较译文与源文中的数字，当译文中的数字与源文中的数字个数相同但数值不同时，自动使用源文本中的数字替换掉目标文本段中的数字，并根据目标语言，调整其格式与语言代码中设置的数字格式一致。
	 * 两者间数值相同但顺序不同且目标语言为亚洲语系的将不予调整，避免因语序不同导致数字顺序不同时的错误调整。
	 * @param src
	 *            源文本
	 * @param tgt
	 *            目标文本
	 * @return 调整后的目标文本 ;
	 */
	public static String replaceNumber(String src, String tgt) {
		// TODO 数字自动替换，做了简单实现，需改写
		HashMap<Integer, String> srcNumbers = getNumbers(src);
		HashMap<Integer, String> tgtNumbers = getNumbers(tgt);
		if (srcNumbers.size() == tgtNumbers.size()) {
			HashMap<Integer, String> tgtmap = new HashMap<Integer, String>();

			ArrayList<String> list = new ArrayList<String>(srcNumbers.values());
			for (Entry<Integer, String> entry : tgtNumbers.entrySet()) {
				Integer index = entry.getKey();
				String num = entry.getValue();
				if (!list.remove(num)) {
					tgtmap.put(index, num); // 在Target中存在，Source中不存在的数字，即要被替换的数字
				}
			}
			ArrayList<String> srcNums = new ArrayList<String>();
			list = new ArrayList<String>(tgtNumbers.values());
			for (Entry<Integer, String> entry : srcNumbers.entrySet()) {
				String num = entry.getValue();
				if (!list.remove(num)) {
					srcNums.add(num); // 在Source中存在，Target中不存在的数字，即要替换的数字
				}
			}
			int balance = 0;
			int i = 0;
			for (Entry<Integer, String> entry : tgtmap.entrySet()) {
				String srcNum = srcNums.get(i);
				i++;
				String tgtNum = entry.getValue();
				int index = entry.getKey();
				tgt = tgt.substring(0, index + balance) + srcNum
						+ tgt.substring(index + tgtNum.length() + balance, tgt.length());
				balance += tgtNum.length() - srcNum.length();
			}
		}

		return tgt;
	}

	/**
	 * 得到文本中的所有数字字符串
	 * @param text
	 *            指定文本
	 * @return 数字字符及其在文本中的索引。<br/>
	 *         key：索引，value：数字字符串;
	 */
	private static HashMap<Integer, String> getNumbers(String text) {
		HashMap<Integer, String> numbers = new HashMap<Integer, String>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= text.length(); i++) {
			if (i < text.length()) {
				char ch = text.charAt(i);
				if (Character.isDigit(ch)) {
					sb.append(ch);
					continue;
				}
			}
			if (sb.length() > 0) {
				numbers.put(i - sb.length(), sb.toString());
				sb.delete(0, sb.length());
			}
		}
		return numbers;
	}

	/**
	 * 日期时间自动替换
	 * @param src
	 *            源文本
	 * @param tgt
	 *            目标文本
	 * @return 调整后的目标文本 ;
	 */
	public static String replaceDate(String src, String tgt) {
		// TODO 日期时间自动替换，由于判断比较复杂，暂未实现
		return tgt;
	}

	public static void main(String[] args) {
		// String str1 = "11wfw21dwq33dqwaa44";
		// String str2 = "11 bbwd22qwds21gre33";
		// System.out.println(replaceNumber(str1, str2));

		/** Part.1 使用正则表达式 **/
		// String regx = "\\d{4}\\-\\d{1,2}\\-\\d{1,2}\\s\\d{1,2}\\:\\d{1,2}\\:\\d{1,2}";
		// Pattern p = Pattern.compile(regx);
		// Matcher m = p.matcher("你好 - 2008-8-7 12:04:11 - 中国2010-11-11 11:52:30 dwqdqw");
		// while (m.find()) {
		// System.out.println(m.group());
		// }

		/** Part.2 使用 java.text.SimpleDateFormat.parse(String text, ParsePosition pos) 方法 **/
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str = "form:   2008-8-7 12:04:11  to  2010-11-11 11:52:30, over!";

		ParsePosition pos;
		for (int i = 0; i < str.length();) {
			pos = new ParsePosition(i);
			int start = i;
			Date date = sdf.parse(str, pos);
			if (date != null) {
				i = pos.getIndex();
				System.out.println(str.substring(start, i).trim());
			} else {
				i++;
			}
		}

		/** Part.3 使用“()”为正则表达式添加分组 **/
		// Pattern p=Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
		// Matcher m=p.matcher("x20xxx1984-10-20xxx19852x");
		//				     
		// if(m.find()){
		// System.out.println("日期:"+m.group());
		// System.out.println("年:"+m.group(1));
		// System.out.println("月:"+m.group(2));
		// System.out.println("日:"+m.group(3));
		// }
	}
}
