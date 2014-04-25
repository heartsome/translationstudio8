/**
 * Calculator.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.math;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.StringCharacterIterator;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对数学表达式求值的工具类.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class Calculator {
	
	/**
	 * 构造方法.
	 */
	protected Calculator() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		String strInput;
		double fOutput;

		//测试用例:1-3*(4-(2+5*3)+5)-6/(1+2)=23
		//测试用例:11.2+3.1*(423-(2+5.7*3.4)+5.6)-6.4/(15.5+24)=1273.4199746835445
		while (true) {
			System.out.print("输入表达式: ");
			System.out.flush();

			strInput = getString();
			if (strInput.equals("")) {
				break;
			}

			// 以下对输入字符串做规则处理
			strInput = Calculator.checkexpression(strInput);
			if (strInput.equals("")) {
				System.out.println("   表达式出错  ");
			}

			// 以下对输入字符串做表达式转换
			Vector<String> vCompute = Calculator.getexpression(strInput);

			for (int i = 0; i < vCompute.size(); i++) {
				System.out.println("" + vCompute.get(i));
			}

			// 以下进行后缀表达式转换
			Vector<String> vTmpPrefix = Calculator.transformprefix(vCompute);

			for (int i = 0; i < vTmpPrefix.size(); i++) {
				System.out.println(vTmpPrefix.get(i));
			}

			// 以下进行后缀表达式运算
			fOutput = Calculator.evaluateprefix(vTmpPrefix);

			System.out.println("结果 = " + fOutput);

		}
	}

	/**
	 * 静态方法,用来从控制台读入表达式.
	 * @return String
	 * 				表达式字符串
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static String getString() throws IOException {
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		String s = br.readLine();
		return s;
	}

	/**
	 * 输入字符串转换.把从控制台读入的字符串转成表达式存在一个队列中. 例:123+321 存为"123""+""321"
	 * @param str
	 *            表达式字符串
	 * @return Vector&lt;String&gt;
	 * 				表达式集合
	 */
	private static Vector<String> getexpression(String str) {
		Vector<String> vTemp = new Vector<String>();
		char[] temp = new char[str.length()];
		str.getChars(0, str.length(), temp, 0);
		String fi = "";
		int i = 0;
		// 匹配数字和小数点
		String regexFig = "[\\.\\d]"; 
		// 匹配运算符(+,-,*,/)和括号("(",")")
		String regexOperator = "[\\+\\-\\*/\\(\\)]"; 
		Pattern pattertFig = Pattern.compile(regexFig);
		Pattern patternOperator = Pattern.compile(regexOperator);
		Matcher m = null;
		boolean b;
		while (i < str.length()) {
			Character c = new Character(temp[i]);
			String s = c.toString();
			m = patternOperator.matcher(s);
			b = m.matches();

			if (b) {
				vTemp.add(fi);
				fi = "";
				vTemp.add(s);
			}
			m = pattertFig.matcher(s);
			b = m.matches();
			if (b) {
				fi = fi + s;
			}
			i++;
		}
		vTemp.add(fi);

		return vTemp;
	}

	/**
	 * 转换中序表示式为前序表示式.
	 * @param parmVExpression
	 *            表示式的集合
	 * @return Vector&lt;String&gt;
	 * 			  转换后的前序表示式
	 */
	private static Vector<String> transformprefix(Vector<String> parmVExpression) {
		Vector<String> vPrefix = new Vector<String>();
		Stack<String> stackTmp = new Stack<String>();
		// 匹配正浮点数
		String strRegexFloat = "\\d+(\\.\\d+)?"; 
		Pattern patternFloat = Pattern.compile(strRegexFloat);
		Matcher m = null;
		boolean b;
		String strElem = "";

		for (int i = 0; i < parmVExpression.size(); i++) {
			strElem = parmVExpression.get(i).toString();
			m = patternFloat.matcher(strElem);
			b = m.matches();

			if (b) {
				vPrefix.add(strElem);
			}

			if (strElem.equals("+") || strElem.equals("-")) {
				if (stackTmp.isEmpty()) {
					stackTmp.push(strElem);
				} else {
					while (!stackTmp.isEmpty()) {
						String strTmp = stackTmp.peek();

						if (strTmp.equals("(")) {
							break;
						} else {
							vPrefix.add(stackTmp.pop());
						}
					}
					stackTmp.push(strElem);
				}
			}

			if (strElem.equals("*") || strElem.equals("/")) {
				if (stackTmp.isEmpty()) {
					stackTmp.push(strElem);
				} else {
					while (!stackTmp.isEmpty()) {
						String strTmp = stackTmp.peek();

						if (strTmp.equals("(") || strTmp.equals("+") || strTmp.equals("-")) {
							break;
						} else {
							vPrefix.add(stackTmp.pop());
						}
					}
					stackTmp.push(strElem);
				}
			}

			if (strElem.equals("(")) {
				stackTmp.push(strElem);
			}

			if (strElem.equals(")")) {
				while (!stackTmp.isEmpty()) {
					String strTmp = stackTmp.peek();
					if (strTmp.equals("(")) {
						stackTmp.pop();
						break;
					} else {
						vPrefix.add(stackTmp.pop());
					}
				}
			}
		}

		while (!stackTmp.isEmpty()) {
			vPrefix.add(stackTmp.pop());
		}
		return vPrefix;
	}

	/**
	 * 前缀表示式求值.
	 * @param parmVPrefix
	 *            前缀表示式的集合
	 * @return double
	 * 				前缀表示式的值
	 */
	private static strictfp double evaluateprefix(Vector<String> parmVPrefix) {
		String strTmp = "";
		double num1, num2, interans = 0;
		Stack<Double> stackCompute = new Stack<Double>();

		int i = 0;
		while (i < parmVPrefix.size()) {
			strTmp = parmVPrefix.get(i).toString();
			if (!strTmp.equals("+") && !strTmp.equals("-") && !strTmp.equals("*") && !strTmp.equals("/")) {
				interans = stackCompute.push(Double.parseDouble(strTmp));
			} else {
				num2 = (stackCompute.pop());
				num1 = (stackCompute.pop());

				if (strTmp.equals("+")) {
					interans = num1 + num2;
				}
				if (strTmp.equals("-")) {
					interans = num1 - num2;
				}
				if (strTmp.equals("*")) {
					interans = num1 * num2;
				}
				if (strTmp.equals("/")) {
					interans = num1 / num2;
				}
				stackCompute.push(interans);
			}
			i++;
		}
		return interans;
	}

	/**
	 * 表达式求值。
	 * @param parmStrInput
	 *            表达式字符串
	 * @return double
	 * 				表达式的值
	 */
	public static strictfp double evaluateprefix(String parmStrInput) {
		Vector<String> vCompute = Calculator.getexpression(parmStrInput);
		Vector<String> vTmpPrefix = Calculator.transformprefix(vCompute);
		return Calculator.evaluateprefix(vTmpPrefix);
	}

	/**
	 * 括号匹配检测.
	 * @param str
	 *            表达式字符串
	 * @return boolean
	 * 				如果表达式括号匹配，返回true，否则返回 false
	 */
	public static boolean checkbracket(String str) {
		Stack<Character> stackCheck = new Stack<Character>();
		boolean booFlag = true;

		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			switch (ch) {
			case '(':
				stackCheck.push(ch);
				break;
			case ')':
				if (!stackCheck.isEmpty()) {
					char chx = stackCheck.pop();
					if (ch == ')' && chx != '(') {
						booFlag = false;
					}
				} else {
					booFlag = false;
				}
				break;
			default:
				break;
			}
		}
		if (!stackCheck.isEmpty()) {
			booFlag = false;
		}
		return booFlag;
	}

	/**
	 * 表达式正确性规则处理与校验.
	 * @param str
	 *            表达式
	 * @return String
	 * 			  如果表达式非法，返回空串"",否则返回表达式
	 */
	public static String checkexpression(String str) {
		if (str == null || "".equals(str.trim())) {
			return "";
		}
		StringCharacterIterator sci = new StringCharacterIterator(str);
		char lastChar = sci.last();
		switch (lastChar) {
		case '+':
			return "";
		case '*':
			return "";
		case '-':
			return "";
		case '/':
			return "";
		default:
			break;
		}
		if (!Calculator.checkbracket(str)) {
			return "";
		}
		Stack<Character> stackCheck = new Stack<Character>();
		Stack<Character> stackTmp = new Stack<Character>();
		String strResultOne = "";

		// 匹配合法的运算字符"数字,.,+,-,*,/,(,),"
		String strRegex = "^[\\.\\d\\+\\-\\*/\\(\\)]+$"; 
		Pattern patternFiltrate = Pattern.compile(strRegex);
		Matcher m = patternFiltrate.matcher(str);
		boolean booFiltrate = m.matches();
		if (!booFiltrate) {
			strResultOne = "";
			return strResultOne;
		}

		// 匹配非法的浮点数.
		String strErrFloat = ".*(\\.\\d*){2,}.*"; 
		Pattern patternErrFloat = Pattern.compile(strErrFloat);
		Matcher matcherErrFloat = patternErrFloat.matcher(str);
		boolean booErrFloat = matcherErrFloat.matches();
		if (booErrFloat) {
			strResultOne = "";
			return strResultOne;
		}

		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (checkfig(ch)) {
				if (!stackTmp.isEmpty() && stackTmp.peek() == ')') {
					strResultOne = "";
					return strResultOne;
				}
				stackTmp.push(ch);
				strResultOne = strResultOne + ch;
			}

			switch (ch) {
			case '(':
				if (!stackTmp.isEmpty() && stackTmp.peek() == '.') {
					strResultOne = "";
					return strResultOne;
				}
				stackCheck.push(ch);
				if (stackTmp.isEmpty() || (!checkfig(stackTmp.peek()) && stackTmp.peek() != ')')) {
					strResultOne = strResultOne + ch;
				} else {
					strResultOne = strResultOne + "*" + ch;
				}
				stackTmp.push(ch);
				break;
			case ')':
				if (!stackCheck.isEmpty()) {
					char chx = stackCheck.pop();
					if (ch == ')' && chx != '(') {
						strResultOne = "";
						return strResultOne;
					}
				} else {
					strResultOne = "";
					return strResultOne;
				}
				if (stackTmp.peek() == '.' || (!checkfig(stackTmp.peek()) && stackTmp.peek() != ')')) {
					strResultOne = "";
					return strResultOne;
				}
				stackTmp.push(ch);
				strResultOne = strResultOne + ch;
				break;
			case '+':
			case '-':
				if (!stackTmp.isEmpty()
						&& (stackTmp.peek() == '+' || stackTmp.peek() == '-' || stackTmp.peek() == '*' || stackTmp.peek() == '/' || stackTmp
								.peek() == '.')) {
					strResultOne = "";
					return strResultOne;
				}
				if (stackTmp.isEmpty() || stackTmp.peek() == '(') {
					strResultOne = strResultOne + "0" + ch;
				} else {
					strResultOne = strResultOne + ch;
				}
				stackTmp.push(ch);
				break;
			case '*':
			case '/':
				if (stackTmp.isEmpty() || stackTmp.peek() == '.' || (!checkfig(stackTmp.peek()) && stackTmp.peek() != ')')) {
					strResultOne = "";
					return strResultOne;
				}
				stackTmp.push(ch);
				strResultOne = strResultOne + ch;
				break;
			case '.':
				if (stackTmp.isEmpty() || !checkfig(stackTmp.peek())) {
					strResultOne = strResultOne + "0" + ch;
				} else {
					strResultOne = strResultOne + ch;
				}
				stackTmp.push(ch);
				break;

			default:
				break;
			}
		}
		if (!stackCheck.isEmpty()) {
			strResultOne = "";
			return strResultOne;
		}

		return strResultOne;

	}

	/**
	 * 数字匹配检测
	 * @param ch
	 *            要检测的对象
	 * @return boolean
	 * 				如果该对象仅有一个数字组成，返回 true，否则返回 false
	 */
	private static boolean checkfig(Object ch) {
		String s = ch.toString();
		// 匹配数字
		String strRegexfig = "\\d"; 
		Pattern patternFig = Pattern.compile(strRegexfig);
		Matcher matcherFig = patternFig.matcher(s);
		boolean booFig = matcherFig.matches();
		return booFig;
	}

};
