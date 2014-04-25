package net.heartsome.cat.ts.core.qa;

import java.util.StringTokenizer;

/**
 * 字数统计类
 * @author robert	2011-12-10
 */
public class CountWord {
	public CountWord(){
		
		
	}
	
	
	/**
	 * 新的字数统计的方法，旧方法见 {@link #wordCount_old}
	 * 新方法是将几种常见亚洲语系单独陈列，分两种模式进行计算。具体内容及详情见　info/unicodeForWordCountInfo.txt	
	 * 新方法的相关规则，见　info/字数计算-规则.xlsx	
	 * robert 2013-05-31
	 * @param str
	 * @param lang
	 * @return
	 */
	public static int wordCount(String str, String lang){
		int asiaSum = 0;
		StringBuffer europeanSB = new StringBuffer();
		char[] ch = str.toCharArray();
		char curChar;
		
		for (int i = 0; i < ch.length; i++) {
			curChar = ch[i];
			if ((curChar >= 19968 && curChar <= 40869)	// 中文
					|| (curChar >= 12352 && curChar <= 12543) || (curChar >= 12784 && curChar <= 12799) // 日文平假名或片假名或片假名语音扩展
					|| (curChar >= 4352 && curChar <= 4607) || (curChar >= 12592 && curChar <= 12687) || (curChar >= 44032 && curChar <= 55215)	// 朝鲜文
					|| (curChar >= 3584 && curChar <= 3711)	// 泰文
					|| (curChar >= 1424 && curChar <= 1535)	// 希伯来语(犹太语) Hebrew
					|| (curChar >= 8212 && curChar <= 8230) || (curChar >= 12289 && curChar <= 12305) || (curChar >= 65280 && curChar <= 65509)) {	// 部份常用全角标点
				asiaSum ++;
			}else {
				europeanSB.append(curChar);
			}
		}
		
		return asiaSum + europeanCount(europeanSB.toString());
	}

	
	public static void main(String[] args) {
//		int enSum = CountWord.wordCount("3-(3,4-dichlorophenyl)-1", "en-US");
//		int enSum = CountWord.wordCount("—–", "en-US");
//		int zhSum = CountWord.wordCount("—–", "zh-CN");
//		int enSum = CountWord.wordCount("这是一个中文这么多文字我就不信没有不合法的我去", "en-US");
//		int zhSum = CountWord.wordCount("http://www.heartsome.net:8080", "ja");
		int zhSum = CountWord.wordCount("***", "ja");
		
		System.out.println("zhSum = " + zhSum);
		
	}
	
	
	private static int europeanCount(String source) {
		int wordnum = 0;
		StringTokenizer tok = new StringTokenizer(source, " \t\r\n()?\u00A0\u3001\u3002\uff1a\uff01\uff1f\u4ecb"); //$NON-NLS-1$
		// 所有标点都不计算数字
		String exceptCharsRegex = "[-,./;'\\[\\]\\\\=<>?:\"{}|!@#$%^&*()_+]+"; //$NON-NLS-1$
		String regex = "(/(?!/)((?<!/)))|,|<|>";
		while (tok.hasMoreTokens()) {
			String str = tok.nextToken();
			if (str.matches(exceptCharsRegex) ) {
				continue;
			}
			wordnum += str.split(regex).length;
		}
		return wordnum;
	}
	
	
	
	public static int wordCount_old(String str, String lang) {
		if (lang.toLowerCase().startsWith("zh")) { //$NON-NLS-1$
			return chineseCount(str);
		}
		return europeanCount(str);
	}
	
	
	
	private static int chineseCount(String str) {
		// basic idea is that we need to remove unicode that higher than 255
		// and then we count by europeanCount
		// after that remove 0-255 unicode value and just count character
		StringBuffer european = new StringBuffer();
		int chineseCount = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char chr = chars[i];
			if (chr <= 255 || chr == '\u00A0' || chr == '\u3001' || chr == '\u3002' || chr == '\uff1a'
					|| chr == '\uff01' || chr == '\uff1f' || chr == '\u4ecb') {
				
				european.append(chr);
			} else {
				chineseCount++;
			}
		}
		int euroCount = europeanCount_old(european.toString());
		return euroCount + chineseCount;
	}
	
	
	private static int europeanCount_old(String source) {
		int wordnum = 0;
		StringTokenizer tok = new StringTokenizer(source, " \t\r\n()?\u00A0\u3001\u3002\uff1a\uff01\uff1f\u4ecb"); //$NON-NLS-1$
		String charsInNumber = ".,-/<>"; //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String str = tok.nextToken();
			if (charsInNumber.indexOf(str) < 0 && !isFormatNumber(str)) {
				StringTokenizer tok2 = new StringTokenizer(str, charsInNumber);
				while (tok2.hasMoreTokens()) {
					str = tok2.nextToken();
					wordnum++;
				}
			}
		}

		return wordnum;
	}

	
	public static boolean isFormatNumber(String str) {
		char[] chars = str.toCharArray();
		boolean hasDigit = false;
		for (int i = 0; i < chars.length; i++) {
			if (Character.isDigit(chars[i])) {
				hasDigit = true;
			} else if (chars[i] != '/' && chars[i] != '.' && chars[i] != ',' && chars[i] != '-' && chars[i] != '>'
					&& chars[i] != '<') {
				return false;
			}
		}
		return hasDigit;
	}
	
}
