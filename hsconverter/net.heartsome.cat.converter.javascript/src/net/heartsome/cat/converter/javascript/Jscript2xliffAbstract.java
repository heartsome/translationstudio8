package net.heartsome.cat.converter.javascript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.javascript.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Jscript 转 xliff 的抽象实现
 * @author cheney
 * @since JDK1.6
 */
public abstract class Jscript2xliffAbstract {

	/**
	 * 输入字符流
	 */
	protected Reader input;

	/**
	 * 输出文件流
	 */
	public OutputStream output; // 设置为 public 是为了方便测试

	/**
	 * 骨架输出文件流
	 */
	public OutputStream skeleton; // 设置为 public 是为了方便测试

	/**
	 * 输出字符流
	 */
	protected BufferedReader buffer;

	/**
	 * 源文件
	 */
	protected String inputFile;

	/**
	 * xliff 文件
	 */
	protected String xliffFile;

	/**
	 * 骨架文件
	 */
	protected String skeletonFile;

	/**
	 * 源语言
	 */
	protected String sourceLanguage;

	protected String targetLanguage;

	/**
	 * segment ID
	 */
	public int segId; // 设置为 public 是为了方便测试

	/**
	 * 源文件的字符编码
	 */
	protected String encoding;

	// 从当前行中截取并进行处理的字符串
	private String processingStr = "";

	// 缓存当前行中未处理的字符串
	private String cacheStr = "";

	// 标识当前行是否包含单行注释
	private boolean isSingleCom = false;

	/**
	 * 当前行单行注释开始的索引
	 */
	int singleComIndex = -1;

	// 标识当前行是包含在多行注释中
	private boolean isMultiRowComStart = false;

	// 当前行多行注释的开始索引
	private int multiRowComStartIndex = -1;

	// 标识当前行是否包含多行注释的结束
	private boolean isMultiRowComEnd = false;

	// 当前行多行注释的结束索引
	private int multiRowComEndIndex = -1;

	// 在处理的过程中缓存的最后一个有效字符，排除空白字符和注释
	private char validCacheChar = '-';

	/** The is suite. */
	protected boolean isSuite;

	/** The qt tool id. */
	protected String qtToolId;

	/**
	 * @param monitor
	 *            进度监视器
	 */
	public Map<String, String> run(IProgressMonitor monitor) throws Exception {
		monitor = Progress.getMonitor(monitor);
		Map<String, String> result = new HashMap<String, String>();
		try {
			// 计算总任务数
			CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(inputFile);
			monitor.beginTask(Messages.getString("javascript.Jscript2xliffAbstract.task1"), cpb.getTotalTask());

			writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
			writeString("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //$NON-NLS-1$
					"xmlns:hs=\"" + Converter.HSNAMESPACE + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
					"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " //$NON-NLS-1$
					+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
			if (!"".equals(targetLanguage)) {
				writeString("<file original=\"" + inputFile //$NON-NLS-1$
						+ "\" source-language=\"" + sourceLanguage //$NON-NLS-1$
						+ "\" target-language=\""+targetLanguage+"\" datatype=\"javascript\">\n"); //$NON-NLS-1$
			} else {
				writeString("<file original=\"" + inputFile //$NON-NLS-1$
						+ "\" source-language=\"" + sourceLanguage //$NON-NLS-1$
						+ "\" datatype=\"javascript\">\n"); //$NON-NLS-1$
			}
			writeString("<header>\n"); //$NON-NLS-1$
			writeString("   <skl>\n"); //$NON-NLS-1$
			String crc = ""; //$NON-NLS-1$
			if (isSuite) {
				crc = "crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("UTF-8")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			writeString("      <external-file href=\"" + TextUtil.cleanString(skeletonFile) + "\" " + crc + "/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writeString("   </skl>\n"); //$NON-NLS-1$
			writeString("   <tool tool-id=\"" + qtToolId + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" + encoding //$NON-NLS-1$
					+ "</hs:prop></hs:prop-group>\n"); //$NON-NLS-1$
			writeString("</header>\n"); //$NON-NLS-1$
			writeString("<body>\n"); //$NON-NLS-1$

			String line = buffer.readLine();

			// 计算转换进度
			cpb.calculateProcessed(monitor, line, encoding);

			while (line != null) {
				// 取消转换操作
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("javascript.cancel"));
				}
				line = line + "\n"; //$NON-NLS-1$
				if (isMultiRowComStart) {
					// 表明当前行在多行注释中或是多行注释的结束行（包含 */)
					line = processMultiRowCom(line);
					if (line == null) {
						// 如果返回的字符串为 NULL，则表明整行都为注释
						line = buffer.readLine();
						// 计算转换进度
						cpb.calculateProcessed(monitor, line, encoding);
						continue;
					} else if (line.equals("\n")) {
						// 只剩下换行符，则直接写骨架
						writeSkeleton(line);
						line = buffer.readLine();
						// 计算转换进度
						cpb.calculateProcessed(monitor, line, encoding);
						continue;
					}
				}
				// 检查当前行是否包含注释
				checkForComment(line);

				// 缓存当前将要处理的字符串
				String cacheTemp = processingStr;

				// 处理 processingStr 字符串
				processingStr = checkForQuote(processingStr);

				// 缓存当前要处理字符串中最后一个有效字符（排除空白字符）
				validCacheChar = cacheLastValidChar(cacheTemp);

				// 如果不存在跨行的情况，则此时 processingStr 应该为空
				if (processingStr.length() > 0) {
					// 检查此行是否以 \ 结尾，需排除所添加的换行符，同时考虑到 processing 是截取的字符串而不是以换行符结束
					char inverseSecondChar = 'a';
					if (processingStr.length() > 1) {
						inverseSecondChar = processingStr.charAt(processingStr.length() - 2);
					}
					if (cacheStr != null || inverseSecondChar != '\\') {
						ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
								Messages.getString("javascript.Jscript2xliffAbstract.msg1"));
					}
					// 存在引号中的字符串跨行的情况
					String nextLine = buffer.readLine();
					if (nextLine == null) {
						ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
								Messages.getString("javascript.Jscript2xliffAbstract.msg2"));
					}
					// 计算转换进度
					cpb.calculateProcessed(monitor, nextLine, encoding);
					line = processingStr + nextLine;
					continue;
				}

				// 处理 cacheStr 字符串
				if (cacheStr != null) {
					// 此行包含注释字符串
					if (isSingleCom) {
						// 把注释字符串写入骨架
						writeSkeleton(cacheStr);
						isSingleCom = false;
						singleComIndex = -1;
					}
					if (isMultiRowComStart) {
						if (isMultiRowComEnd) {
							writeSkeleton(line.substring(multiRowComStartIndex, multiRowComEndIndex + 1));
							// 去除所添加的换行符
							line = line.substring(multiRowComEndIndex + 1, line.length() - 1);
							isMultiRowComStart = false;
							multiRowComStartIndex = -1;
							isMultiRowComEnd = false;
							multiRowComEndIndex = -1;
							continue;
						} else {
							writeSkeleton(cacheStr);
						}
					}
				}
				line = buffer.readLine();
				// 计算转换进度
				cpb.calculateProcessed(monitor, line, encoding);
			}

			// 多行注释没有结束
			if (isMultiRowComStart) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("javascript.Jscript2xliffAbstract.msg2"));
			}

			writeString("</body>\n"); //$NON-NLS-1$
			writeString("</file>\n"); //$NON-NLS-1$
			writeString("</xliff>"); //$NON-NLS-1$
			result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (skeleton != null) {
					skeleton.close();
				}
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (Exception e2) {
				// ignore the exception
				e2.printStackTrace();
			}
			monitor.done();
		}
		return result;
	}

	/**
	 * 缓存当前要处理字符串中最后一个有效字符（排除空白字符和注释）
	 * @param processingStr2
	 * @return
	 */
	private char cacheLastValidChar(String str) {
		char result = '-';
		int length = str.length();
		for (int i = length - 1; i > -1; i--) {
			char temp = str.charAt(i);
			if (Character.isWhitespace(temp)) {
				continue;
			}
			result = temp;
			break;
		}
		return result;
	}

	/**
	 * 检查是否包含可抽取的文本
	 * @param line
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private String checkForQuote(String line) throws IOException {
		Stack<Character> quoteStack = new Stack<Character>();
		Stack<Character> squareBrackets = new Stack<Character>();
		while (line.length() > 0) {
			int quoteStartIndex = -1;
			int quoteEndIndex = -1;
			int length = line.length();
			for (int i = 0; i < length; i++) {
				char temp = line.charAt(i);

				// 在查找正则表达式的结束时，需要忽略正则表达式中括号中出现的正斜杠
				if (temp == '[' || temp == ']') {
					if (!quoteStack.empty() && quoteStack.peek().charValue() == '/') {
						// 排除转义的情况
						boolean isEscape = checkIsEscape(line, i);
						if (isEscape) {
							continue;
						}
						// 在正则表达式开始且在结束之前出现了中括号
						if (squareBrackets.empty()) {
							if (temp == '[') {
								squareBrackets.push(new Character(temp));
							}
						} else {
							if (temp == ']') {
								squareBrackets.pop();
							}
						}
					}
				}

				// 排除正则表达式中括号中的正斜杠
				if (temp == '/' && !squareBrackets.empty()) {
					continue;
				}

				// 需要排除代码中的正则表达式，形如：e.element.text.match(/minorVersion\s*:\s*'(0|X)/)
				if (temp == '"' || temp == '\'' || temp == '/') {
					// 排除转义的情况
					boolean isEscape = checkIsEscape(line, i);
					if (isEscape) {
						continue;
					}
					if (quoteStack.empty()) {
						if (temp == '"' || temp == '\'') {
							quoteStartIndex = i;
							// 把最先找到的匹配字符压入堆栈
							quoteStack.push(new Character(temp));
						} else {
							// 判断找到的正斜杠是正则表达式的开始还是除号
							if (isPerlRegStart(line, i)) {
								quoteStack.push(new Character(temp));
							}
						}
					} else {
						// 检查找到的引号是否与堆栈中的相匹配
						Character character = quoteStack.peek();
						char expectedChar = character.charValue();
						if (expectedChar == temp) {
							if (expectedChar == '"' || expectedChar == '\'') {
								quoteEndIndex = i;
								// 找到匹配引号的结束后跳出当前循环，对可抽取的字符进行处理
								break;
							} else {
								// 找到匹配的正斜杠，把此字符弹出后，继续查找下一个双引号、单引号或正斜杠
								quoteStack.pop();
								// 如果缓存中括号的堆栈不为空，则清空此堆栈
								if (!squareBrackets.empty()) {
									squareBrackets.pop();
								}
							}
						}
					}
				}
			}

			if (!quoteStack.empty() && quoteStartIndex != -1 && quoteEndIndex != -1) {
				// 找到匹配的引号开始和结束
				line = processQuoteStr(line, quoteStack.peek().charValue(), quoteStartIndex, quoteEndIndex);
				// 弹出匹配的引号结束符
				quoteStack.pop();
			} else {
				if (!quoteStack.empty()) {
					// 没有找到匹配的结束符，需要把当前行与下一行连接后进行处理
					break;
				} else {
					// 没有需要处理的可抽取文本，直接写骨架
					writeSkeleton(line);
					line = "";
				}
			}
		}
		return line;
	}

	/**
	 * 判断找到的正斜杠是正则表达式的开始还是除号 正则和除法有哪些区别？从语境上说，原则性的区别就是除法的前面 是被除数，正则的前面不是。而成为被除数的可能无非是以下3种 ： 数字、变量、括号内的运算结果。
	 * @param line
	 *            包含正斜杠的字符串
	 * @param index
	 *            第一个正斜杠的索引
	 * @return 是正则表达式的开始则返回 true，否则返回 false
	 */
	private boolean isPerlRegStart(String line, int index) {
		boolean isRegStart = false;
		boolean isFind = false; // 标识在正斜杠之前找到了能识识此正斜杠类型的字符
		for (int i = index - 1; i > -1; i--) {
			// 忽略空空
			char cc = line.charAt(i);
			if (Character.isWhitespace(cc)) {
				continue;
			}
			isFind = true;
			// 前面一个非空白字符若是字母、数字、下划线或右括号则除号，否则为正则表达式
			isRegStart = (!isLetterOrDigitOrUnderlineOrDollorOrRightParenthese(cc));
			break;
		}
		if (!isFind) {
			isRegStart = (!isLetterOrDigitOrUnderlineOrDollorOrRightParenthese(validCacheChar));
		}
		return isRegStart;
	}

	/**
	 * 判断此字符是否是字母、数字、下划线、美元符、右括号或右中括号（数组）
	 * @param cc
	 * @return 是就返回 true，否则返回 false
	 */
	private boolean isLetterOrDigitOrUnderlineOrDollorOrRightParenthese(char cc) {
		if (Character.isLetterOrDigit(cc) || cc == '_' || cc == '$' || cc == ')' || cc == ']') {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 处理可抽取字符
	 * @param line
	 * @param quote
	 *            引号字符
	 * @param quoteStartIndex
	 *            引号的开始索引
	 * @param quoteEndIndex
	 *            引号的结束索引
	 * @return 引号结束索引后的字符
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private String processQuoteStr(String line, char quote, int quoteStartIndex, int quoteEndIndex) throws IOException {
		String start = line.substring(0, quoteStartIndex + 1);
		String quoteStr = line.substring(quoteStartIndex + 1, quoteEndIndex);
		String remainStr = line.substring(quoteEndIndex + 1);
		writeSkeleton(start);
		writeSegment(quoteStr);
		writeSkeleton("" + quote);
		return remainStr;

	}

	/**
	 * 检查待处理的行是否包含注释，给相应的标识位赋值
	 * @param line
	 */
	private void checkForComment(String line) {

		isSingleCom = false;
		singleComIndex = -1;
		isMultiRowComStart = false;
		multiRowComStartIndex = -1;
		isMultiRowComEnd = false;
		multiRowComEndIndex = -1;

		Stack<Character> quoteStack = new Stack<Character>();
		int length = line.length();
		for (int i = 0; i < length; i++) {
			char temp = line.charAt(i);
			if (temp == '"' || temp == '\'') {
				// 排除转义的情况
				boolean isEscape = checkIsEscape(line, i);
				if (isEscape) {
					continue;
				}
				if (quoteStack.empty()) {
					// 把最先找到的引号压入堆栈
					quoteStack.push(new Character(temp));
				} else {
					// 检查找到的引号是否与堆栈中的引号相匹配，匹配则弹出堆栈中的引号
					Character character = quoteStack.peek();
					if (character.charValue() == temp) {
						quoteStack.pop();
					}
				}
			}
			if (temp == '/' && (i + 1) < length) {
				// 排除转义的情况
				boolean isEscape = checkIsEscape(line, i);
				if (isEscape) {
					continue;
				}
				char nextChar = line.charAt(i + 1);
				if (quoteStack.empty() && (!isSingleCom) && (!isMultiRowComStart)) {
					if (nextChar == '/') {
						// 此行包含单行注释
						processingStr = line.substring(0, i);
						cacheStr = line.substring(i, line.length());
						isSingleCom = true;
						singleComIndex = i;
					} else if (nextChar == '*') {
						// 此行包含多行注释
						processingStr = line.substring(0, i);
						cacheStr = line.substring(i, line.length());
						isMultiRowComStart = true;
						multiRowComStartIndex = i;
					}
				}
			}
			if (isMultiRowComStart) {
				// 进一步判断多行注释开始符后是否存在多行注释的结束符，需要注释如下情况：/*/
				if (temp == '*' && (i + 1) < length && (i != multiRowComStartIndex + 1)) {
					char nextChar = line.charAt(i + 1);
					if (nextChar == '/') {
						isMultiRowComEnd = true;
						multiRowComEndIndex = i + 1;
					}
				}
			}
			if (isSingleCom || (isMultiRowComStart && isMultiRowComEnd)) {
				// 如果查找到单行注释的或多行注释的开始和结束标识，则跳出循环不再查找注释标识
				break;
			}
		}
		if (!(isSingleCom || isMultiRowComStart)) {
			// 如果当前行不包含注释
			processingStr = line;
			cacheStr = null;
		}
	}

	/**
	 * 判断 line 中 index 指引的字符是否为转义字符
	 * @param line
	 * @param index
	 * @return
	 */
	private boolean checkIsEscape(String line, int index) {
		boolean result = false;
		// 排除转义的情况，且需要考虑引号之前的反斜杠也被转义的情况
		int j = index;
		int backslashCount = 0;
		while (j > 0 && j < line.length()) {
			j--;
			if (line.charAt(j) == '\\') {
				backslashCount++;
			} else {
				break;
			}
		}
		// 如果 backslashCount 为奇数，则证明此引号为转义字符
		if (backslashCount % 2 != 0) {
			result = true;
		}
		return result;
	}

	/**
	 * 处理多行注释
	 * @param line
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private String processMultiRowCom(String line) throws IOException {
		String result = null;
		int length = line.length();
		int index = line.indexOf("*/");
		String comment = line; // 需写入骨架的字符串
		if (index > -1) {
			comment = line.substring(0, index + 2);
			isMultiRowComStart = false;
			multiRowComStartIndex = -1;
			isMultiRowComEnd = false;
			multiRowComEndIndex = -1;
			if ((index + 2) <= length) {
				// 多行注释结束符后还存有待处理字符
				result = line.substring(index + 2);
			}
		}
		writeSkeleton(comment);
		return result;
	}

	/**
	 * @param string
	 */
	private void writeSegment(String segment) throws IOException {
		if (segment.equals("")) {
			return;
		} //$NON-NLS-1$
		writeString("   <trans-unit id=\"" + segId //$NON-NLS-1$
				+ "\" xml:space=\"preserve\">\n" + "      <source xml:lang=\"" //$NON-NLS-1$ //$NON-NLS-2$
				+ sourceLanguage + "\">" + TextUtil.cleanString(segment) + "</source>\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "   </trans-unit>\n"); //$NON-NLS-1$
		writeSkeleton("%%%" + segId++ + "%%%"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param string
	 * @throws IOException
	 *             ;
	 */
	private void writeString(String string) throws IOException {
		output.write(string.getBytes("utf-8")); //$NON-NLS-1$
	}

	/**
	 * @param string
	 * @throws IOException
	 *             ;
	 */
	private void writeSkeleton(String string) throws IOException {
		skeleton.write(string.getBytes("utf-8")); //$NON-NLS-1$
	}

}
