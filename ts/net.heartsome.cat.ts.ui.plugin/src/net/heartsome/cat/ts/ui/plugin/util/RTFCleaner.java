package net.heartsome.cat.ts.ui.plugin.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 清理 RTF 文件（与 R7 中 net.heartsome.plugin.rtfcleaner.RTFCleaner 
 * 类代码一样）
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class RTFCleaner {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);

	private static FileInputStream input;
	private static FileOutputStream output;
	private static String content;
	private static Hashtable<String, String> styles;
	private static Hashtable<String, String> chars;
	private static int EOFStyles;

	/**
	 * 私有构造方法
	 */
	private RTFCleaner() {
		// do not instantiate this class
	}

	public static Vector<String> run(Hashtable<String, String> table) {

		Vector<String> result = new Vector<String>();
		try {
			String inputFile = table.get("source"); //$NON-NLS-1$
			String outputFile = table.get("output"); //$NON-NLS-1$

			// read the file into a String

			input = new FileInputStream(inputFile);
			int size = input.available();
			byte[] array = new byte[size];
			input.read(array);
			content = new String(array, "US-ASCII"); //$NON-NLS-1$
			array = null;
			input.close();

			content = content.replaceAll("\\\\\\{", "\uE001"); //$NON-NLS-1$ //$NON-NLS-2$
			content = content.replaceAll("\\\\\\}", "\uE002"); //$NON-NLS-1$ //$NON-NLS-2$
			content = content.replaceAll("\\\\\\\\", "\uE003"); //$NON-NLS-1$ //$NON-NLS-2$

			buildStyleSheet();
			fillChars();

			String leftPart = content.substring(0, EOFStyles);
			content = content.substring(EOFStyles);

			removeHidden();

			output = new FileOutputStream(outputFile);

			leftPart = leftPart.replaceAll("\uE001", "\\\\{"); //$NON-NLS-1$ //$NON-NLS-2$
			leftPart = leftPart.replaceAll("\uE002", "\\\\}"); //$NON-NLS-1$ //$NON-NLS-2$
			leftPart = leftPart.replaceAll("\uE003", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			output.write(leftPart.getBytes("US-ASCII")); //$NON-NLS-1$

			content = content.replaceAll("\uE001", "\\\\{"); //$NON-NLS-1$ //$NON-NLS-2$
			content = content.replaceAll("\uE002", "\\\\}"); //$NON-NLS-1$ //$NON-NLS-2$
			content = content.replaceAll("\uE003", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			output.write(content.getBytes("US-ASCII")); //$NON-NLS-1$
			output.close();

			result.add("0"); //$NON-NLS-1$
		} catch (Exception e) {
			LOGGER.error(Messages.getString("util.RTFCleaner.logger1"), e);
			result.add("1"); //$NON-NLS-1$
			result.add(e.getMessage());
		}
		return result;
	}

	/**
	 * 初始化 chars 变量
	 *  ;
	 */
	private static void fillChars() {
		chars = new Hashtable<String, String>();
		chars.put("\\emdash", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\endash", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\lquote", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\rquote", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\ldblquote", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\rdblquote", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\tab", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\enspace", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\emspace", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\qmspace", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\~", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\_", ""); //$NON-NLS-1$ //$NON-NLS-2$
		chars.put("\\-", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void removeHidden() throws Exception {
		Stack<Boolean> stack = new Stack<Boolean>();
		stack.push(new Boolean(false));
		boolean inHidden = false;
		StringTokenizer tk = new StringTokenizer(content, "{}\\", true); //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer();
		while (tk.hasMoreTokens()) {
			String token = tk.nextToken();
			if (token.equals("{")) { //$NON-NLS-1$
				stack.push(new Boolean(inHidden));
				buffer.append(token);
				continue;
			}
			if (token.equals("}")) { //$NON-NLS-1$
				if (!stack.isEmpty()) {
					inHidden = stack.pop().booleanValue();
				} else {
					throw new Exception(Messages.getString("util.RTFCleaner.msg1")); //$NON-NLS-1$
				}
				buffer.append(token);
				continue;
			}
			if (token.equals("\\")) { //$NON-NLS-1$
				token = token + tk.nextToken();
			}
			if (token.equals("\\*")) { //$NON-NLS-1$
				token = token + tk.nextToken() + tk.nextToken();
			}
			if (token.matches("\\\\pard.*")) { //$NON-NLS-1$
				inHidden = false;
				stack.pop();
				stack.push(new Boolean(false));
			}
			String control = getControl(token);
			if (control.equals("\\v0")) { //$NON-NLS-1$
				inHidden = false;
			}
			if (control.equals("\\v")) { //$NON-NLS-1$
				control = ""; //$NON-NLS-1$
				inHidden = true;
			}
			if (styles.containsKey(control)) {
				inHidden = true;
			}
			if (inHidden) {
				if (control.matches("\\\\uc.*")) { //$NON-NLS-1$
					control = ""; //$NON-NLS-1$
				} else if (control.matches("\\\\u[0-9]+.*")) { //$NON-NLS-1$
					control = ""; //$NON-NLS-1$
				} else if (control.matches("\\\\\'.*")) { //$NON-NLS-1$
					control = ""; //$NON-NLS-1$
				} else if (chars.containsKey(control)) {
					control = ""; //$NON-NLS-1$
				}
				buffer.append(control);
			} else {
				buffer.append(token);
			}
		}
		content = buffer.toString();
	}

	private static String getControl(String token) {
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (; i < token.length(); i++) {
			char c = token.charAt(i);
			if (c == '\\' || c == '*') {
				buffer.append(c);
			} else {
				break;
			}
		}
		for (; i < token.length(); i++) {
			char c = token.charAt(i);
			if (c == '\'') {
				buffer.append(c);
				buffer.append(token.charAt(i + 1));
				buffer.append(token.charAt(i + 2));
				return buffer.toString();
			}
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
				break;
			}
			buffer.append(c);
		}
		for (; i < token.length(); i++) {
			char c = token.charAt(i);
			if ((c >= '0' && c <= '9') || c == '-') {
				buffer.append(c);
			} else {
				break;
			}
		}
		return buffer.toString();
	}

	private static void buildStyleSheet() {
		int i = content.indexOf('{', content.indexOf("\\stylesheet") + 11); //$NON-NLS-1$
		int level = 0;
		StringBuffer buffer = new StringBuffer();
		styles = new Hashtable<String, String>();
		while (level >= 0) {
			char c = content.charAt(i++);
			buffer.append(c);
			if (c == '{') {
				level++;
			}
			if (c == '}') {
				level--;
			}
			if (level == 0) {
				String style = buffer.toString().trim();
				if (style.matches(".*\\\\v[^0].*")) { //$NON-NLS-1$
					StringTokenizer tk = new StringTokenizer(style, "\\"); //$NON-NLS-1$
					while (tk.hasMoreElements()) {
						String token = tk.nextToken();
						if (token.matches(".*cs[0-9]+.*")) { //$NON-NLS-1$
							styles.put("\\" + token.trim(), ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				buffer = null;
				buffer = new StringBuffer();
			}
		}
		EOFStyles = i;
	}
}
