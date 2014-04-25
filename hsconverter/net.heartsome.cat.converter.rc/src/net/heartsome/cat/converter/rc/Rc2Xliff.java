/**
 * Rc2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.rc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.rc.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * The Class Rc2Xliff.
 * @author John Zhu
 */

public class Rc2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "winres";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("rc.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "RC to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Rc2XliffImpl converter = new Rc2XliffImpl();
		return converter.run(args, monitor);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getName()
	 * @return
	 */
	public String getName() {
		return NAME_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getType()
	 * @return
	 */
	public String getType() {
		return TYPE_VALUE;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#getTypeName()
	 * @return
	 */
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * The Class Rc2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Rc2XliffImpl {

		/** The input. */
		private FileInputStream input;

		/** The output. */
		private FileOutputStream output;

		/** The skeleton. */
		private FileOutputStream skeleton;

		/** The buffer. */
		private InputStreamReader buffer;

		/** The input file. */
		private String inputFile;

		/** The xliff file. */
		private String xliffFile;

		/** The skeleton file. */
		private String skeletonFile;

		/** The last word. */
		private String lastWord = ""; //$NON-NLS-1$

		/** The source language. */
		private String sourceLanguage;

		private String targetLanguage;

		/** The seg id. */
		private int segId;

		/** The stack. */
		private String stack;

		/** The block stack. */
		private int blockStack;

		/**
		 * 源文件编码
		 */
		private String srcEncoding;

		/**
		 * 计算当前转换的进度
		 */
		private CalculateProcessedBytes cpb;

		/**
		 * 转换进度监视器
		 */
		private IProgressMonitor monitor;

		/**
		 * Run.
		 * @param params
		 *            the params
		 * @param monitor
		 *            the monitor
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			this.monitor = Progress.getMonitor(monitor);
			Map<String, String> result = new HashMap<String, String>();
			segId = 0;
			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			targetLanguage = params.get(Converter.ATTR_TARGET_LANGUAGE);
			srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			boolean isSuite = false;
			if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}

			String qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;
			try {
				/*
				 * 此转换器的实现逻辑比较复杂，且存在各方法相互调用的情况，故在逻辑中添加进度计算的代码比较困难。考虑到转换过程中，要么把源文件的可翻译单元写入到 xliff 文件中，要么把源文件中的不可翻译单元写入到
				 * skeleton 文件中，所以可以根据源文件的大小，以及当前已经写 xliff 和 skeleton 文件的大小，算出当前的转换进度。
				 */
				cpb = ConverterUtils.getCalculateProcessedBytes(inputFile);
				this.monitor.beginTask(Messages.getString("rc.Rc2Xliff.task1"), cpb.getTotalTask());
				this.monitor.subTask("");
				input = new FileInputStream(inputFile);
				buffer = new InputStreamReader(input, srcEncoding);
				output = new FileOutputStream(xliffFile);
				stack = ""; //$NON-NLS-1$
				writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
				writeString("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\""
						+ Converter.HSNAMESPACE + "\" " + //$NON-NLS-1$ 
						"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " //$NON-NLS-1$
						+ Converter.HSSCHEMALOCATION + "\">\n"); //$NON-NLS-1$
				if (!"".equals(targetLanguage)) {
					writeString("<file original=\"" + inputFile //$NON-NLS-1$
							+ "\" source-language=\"" + sourceLanguage //$NON-NLS-1$
							+ "\" target-language=\"" + targetLanguage + "\" datatype=\"" + TYPE_VALUE + "\">\n"); //$NON-NLS-1$
				} else {
					writeString("<file original=\"" + inputFile //$NON-NLS-1$
							+ "\" source-language=\"" + sourceLanguage //$NON-NLS-1$
							+ "\" datatype=\"" + TYPE_VALUE + "\">\n"); //$NON-NLS-1$
				}
				writeString("<header>\n"); //$NON-NLS-1$
				writeString("   <skl>\n"); //$NON-NLS-1$
				String crc = ""; //$NON-NLS-1$
				if (isSuite) {
					crc = "crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("UTF-8")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				writeString("      <external-file href=\"" + skeletonFile + "\" " + crc + "/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				writeString("   </skl>\n"); //$NON-NLS-1$
				writeString("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
				writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\" >" //$NON-NLS-1$
						+ srcEncoding + "</hs:prop></hs:prop-group>\n"); //$NON-NLS-1$
				writeString("</header>\n"); //$NON-NLS-1$
				writeString("<body>\n"); //$NON-NLS-1$

				skeleton = new FileOutputStream(skeletonFile);

				parseRC();

				skeleton.close();

				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</file>\n"); //$NON-NLS-1$
				writeString("</xliff>"); //$NON-NLS-1$
				buffer.close();
				input.close();
				output.close();

				result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("rc.Rc2Xliff.msg1"), e);
			} finally {
				this.monitor.done();
			}

			return result;
		}

		/**
		 * Write string.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeString(String string) throws IOException {
			output.write(string.getBytes("UTF-8")); //$NON-NLS-1$
		}

		/**
		 * Write skeleton.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSkeleton(String string) throws IOException {
			skeleton.write(string.getBytes("UTF-8")); //$NON-NLS-1$
			// 计算转换进度
			caculateProcessed(string);
		}

		/**
		 * Write skeleton.
		 * @param character
		 *            the character
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSkeleton(char character) throws IOException {
			writeSkeleton(String.valueOf(character));
		}

		/**
		 * Write segment.
		 * @param segment
		 *            the segment
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSegment(String segment) throws IOException {
			// add sentence segmentation
			if (segment.equals("")) {
				return;
			} //$NON-NLS-1$
			writeString("   <trans-unit id=\"" + segId //$NON-NLS-1$
					+ "\" xml:space=\"preserve\">\n" + "      <source xml:lang=\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ sourceLanguage + "\">" + TextUtil.cleanString(segment) + "</source>\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ "   </trans-unit>\n"); //$NON-NLS-1$
			writeSkeleton("%%%" + segId++ + "%%%"); //$NON-NLS-1$ //$NON-NLS-2$
			// 计算转换进度
			caculateProcessed(segment);
		}

		/**
		 * 计算转换进度
		 * @param str
		 *            要写入 xliff 或 skeleton 的源文件中的字符串 ;
		 */
		private void caculateProcessed(String str) {
			// 是否取消操作
			if (this.monitor.isCanceled()) {
				throw new OperationCanceledException(Messages.getString("rc.cancel"));
			}
			int workedTask = 0;
			try {
				workedTask = cpb.calculateProcessed(str.getBytes(srcEncoding).length);
			} catch (UnsupportedEncodingException e) {
				// ignore the exception
				e.printStackTrace();
			}
			if (workedTask > 0) {
				this.monitor.worked(workedTask);
			}
		}

		/**
		 * Parses the rc.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseRC() throws IOException {
			char character;
			while (buffer.ready()) {
				character = (char) buffer.read();
				if (character == '#') { // directives
					parseDirective();
				} else if (character == '/') { // comments
					// comment /* or //
					parseComment(); // Keep the state
				} else if (!blankChar(character)) {
					parseStatement(character);
				} else {
					writeSkeleton(character);
				}
			}
		}

		/**
		 * Parses the comment.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseComment() throws IOException {
			writeSkeleton("/"); //Last character read //$NON-NLS-1$

			boolean bLargeComment;
			char prevChar = ' ';
			char character;

			if (buffer.ready()) {
				character = (char) buffer.read();
				writeSkeleton(character);
				bLargeComment = character == '*'; // Large comment /* */

				// Add the comment to the skeleton
				while (buffer.ready()) {
					character = (char) buffer.read();
					writeSkeleton(character);
					if (bLargeComment && prevChar == '*' && character == '/') {
						break;
					} else if (!bLargeComment && (character == '\n' || character == '\r')) {
						break;
					}
					prevChar = character;
				}
			}
		}

		/**
		 * Parses the statement.
		 * @param initial
		 *            the initial
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseStatement(char initial) throws IOException {
			String statement = String.valueOf(initial);
			writeSkeleton(initial);
			statement = statement.concat(parseWords(" ,\n\r\t", true, false)); //$NON-NLS-1$
			if (statement.trim().equals("STRINGTABLE")) { //$NON-NLS-1$
				parseStringTable();
			} else if (statement.trim().equals("DIALOG") || statement.trim().equals("DIALOGEX")) { //$NON-NLS-1$ //$NON-NLS-2$
				parseDialog();
			} else if (statement.trim().equals("MENU") || statement.trim().equals("MENUEX")) { //$NON-NLS-1$ //$NON-NLS-2$
				parseMenu();
			} else if (statement.trim().equals("POPUP")) { //$NON-NLS-1$
				parsePopup();
			} else if (statement.trim().equals("DLGINIT")) { //$NON-NLS-1$
				parseDlgInit();
			} else if (beginBlock(statement.trim())) { // BEGIN
				blockStack = 0;
				parseBlock();
			}
		}

		/**
		 * Parses the directive.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseDirective() throws IOException {
			char character = ' ';
			writeSkeleton('#');
			String statement = parseWords(" \t", true, false); //$NON-NLS-1$
			if (statement.trim().equals("define")) { //$NON-NLS-1$
				parseDefine();
			} else {
				while (buffer.ready() && character != '\r' && character != '\n') {
					character = (char) buffer.read();
					writeSkeleton(character);
				}
			}
		}

		/**
		 * Parses the define.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseDefine() throws IOException {
			String word = ""; //$NON-NLS-1$
			while (buffer.ready()) {
				stack = ""; //$NON-NLS-1$
				word = parseWords(" \n\t\r,\"L", false, true); //$NON-NLS-1$
				if (word.trim().equals("\"")) { //$NON-NLS-1$
					captureString(true);
				} else { // is END or ID
					writeSkeleton(stack);
					if (word.equals("\r") || word.equals("\n")) { //$NON-NLS-1$ //$NON-NLS-2$
						break;
					}
				}
			}
		}

		/**
		 * Parses the block.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseBlock() throws IOException {
			blockStack++;
			String statement = ""; //$NON-NLS-1$
			while (blockStack != 0 && buffer.ready()) {
				statement = parseWords(" \n\t\r", true, false); //$NON-NLS-1$
				if (beginBlock(statement.trim())) {
					blockStack++;
				} else if (endBlock(statement.trim())) {
					blockStack--;
				}
			}
		}

		/**
		 * Parses the dialog.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseDialog() throws IOException {
			parseDialogContent();
			parseControlBlock();
		}

		/**
		 * Parses the dialog content.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseDialogContent() throws IOException {
			String word = " "; //$NON-NLS-1$
			while (!beginBlock(word)) {
				word = parseWords(" \n\t\r(),", true, false); //$NON-NLS-1$
				if (word.trim().equals("CAPTION")) { //$NON-NLS-1$
					captureString(false);
				}
			}
		}

		/**
		 * Parses the control block.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseControlBlock() throws IOException {
			boolean isEnd = false;
			parseWords(" (),\r\n\t", true, false); //$NON-NLS-1$
			do {
				lastWord = lastWord.trim();
				if (lastWord.equals("CONTROL") || lastWord.equals("LTEXT") || lastWord.equals("CTEXT") || lastWord.equals("RTEXT") || lastWord.equals("AUTO3STATE") || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						lastWord.equals("AUTOCHECKBOX") || lastWord.equals("AUTORADIOBUTTON") || lastWord.equals("CHECKBOX") || lastWord.equals("PUSHBOX") || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						lastWord.equals("PUSHBUTTON") || lastWord.equals("DEFPUSHBUTTON") || lastWord.equals("RADIOBUTTON") || lastWord.equals("STATE3") || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						lastWord.equals("USERBUTTON") || lastWord.equals("GROUPBOX")) { //$NON-NLS-1$ //$NON-NLS-2$
					isEnd = parseControlTypeI();
				} else if (lastWord.equals("EDITTEXT") || lastWord.equals("BEDIT") || lastWord.equals("IEDIT") || lastWord.equals("HEDIT") || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						lastWord.equals("COMBOBOX") || lastWord.equals("LISTBOX") || lastWord.equals("SCROLLBAR") || lastWord.equals("ICON")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					isEnd = parseControlTypeI();
				} else {
					parseWords(" (),\r\t\n", true, false); //$NON-NLS-1$
				}
				if (isEnd) { // End of block in last control
					break;
				}
			} while (true);
		}

		// return true if it has a block in the control
		/**
		 * Parses the control type i.
		 * @return true, if successful
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private boolean parseControlTypeI() throws IOException {
			char cIni = ' ';
			while (blankChar(cIni) && buffer.ready()) {
				cIni = (char) buffer.read();
				if (cIni != '"' && cIni != 'L') {
					writeSkeleton(cIni);
				}
			}

			// first parameter optional string
			if (cIni == '"') {
				captureString(true);
			} else {
				if (cIni == 'L') { // String type L"String"
					writeSkeleton(cIni);
					cIni = (char) buffer.read();
					if (cIni == '"') {
						captureString(true);
					} else { // don't have string
						writeSkeleton(cIni);
					}
				}
			}

			String word = " "; //$NON-NLS-1$
			boolean hasBlock = false;
			while (!isEndControlStatement(word)) {
				word = parseWords(" \t(),\r\n", true, false).trim(); //$NON-NLS-1$
				if (beginBlock(word)) { // begin an optional data block in the
					// control
					hasBlock = true;
				}
			}
			return !hasBlock && endBlock(word); // end of control block?
		}

		/**
		 * Write conditional.
		 * @param character
		 *            the character
		 * @param write
		 *            the write
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeConditional(char character, boolean write) throws IOException {
			if (write) {
				writeSkeleton(character);
			} else {
				stack += String.valueOf(character);
			}

		}

		/**
		 * Parses the comment.
		 * @param write
		 *            the write
		 * @param large
		 *            the large
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseComment(boolean write, boolean large) throws IOException {
			writeConditional('/', write); // Last character read
			if (large) {
				writeConditional('*', write);
			} else {
				writeConditional('/', write);
			}

			char prevChar = ' ';
			char character;

			// Add the comment to the skeleton
			while (buffer.ready()) {
				character = (char) buffer.read();
				writeConditional(character, write);
				if (large && prevChar == '*' && character == '/') {
					break;
				} else if (!large && (character == '\n' || character == '\r')) {
					break;
				}
				prevChar = character;
			}

		}

		/**
		 * Parses the words.
		 * @param separators
		 *            the separators
		 * @param write
		 *            the write
		 * @param withSeparator
		 *            the with separator
		 * @return the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private String parseWords(String separators, boolean write, boolean withSeparator) throws IOException {
			String word = ""; //$NON-NLS-1$
			char lastChar;
			char character = 'a'; // initial value any character not in
			// separators
			while (buffer.ready() && separators.indexOf(character) == -1) {
				lastChar = character;
				character = (char) buffer.read();
				if (character == '/') {
					lastChar = character;
					character = (char) buffer.read();
					if (character == '/') {
						parseComment(write, false);
						break;
					} else if (character == '*') { // skip comments
						parseComment(write, true);
						break;
					} else { // write the last two characters
						if (write) {
							writeSkeleton(lastChar);
							writeSkeleton(character);
						} else {
							stack += String.valueOf(lastChar);
							stack += String.valueOf(character);
						}
						word = word.concat(String.valueOf(lastChar));
						if (separators.indexOf(character) == -1 || withSeparator) { // return with the separator
							// or not
							word = word.concat(String.valueOf(character));
						}
					}
				} else if (character == '\\') {
					lastChar = character;
					character = (char) buffer.read();
					if (character != '\r' || character != '\n') {
						if (write) { // must write and \character
							writeSkeleton(lastChar);
							writeSkeleton(character);
							while (blankChar(character) && character != ' ') {
								lastChar = character;
								character = (char) buffer.read();
								writeSkeleton(character);
							}
							word = word.concat(String.valueOf(character));
						} else { // not write and \ character
							stack += String.valueOf(lastChar);
							stack += String.valueOf(character);
							while (blankChar(character)) {
								lastChar = character;
								character = (char) buffer.read();
								stack += String.valueOf(character);
							}
							word = word.concat(String.valueOf(character));
						}
					} else {
						if (write) {
							writeSkeleton(lastChar);
							writeSkeleton(character);
						} else {
							stack += String.valueOf(lastChar);
							stack += String.valueOf(character);
						}
						word = word.concat(String.valueOf(lastChar));
						if (separators.indexOf(character) == -1 || withSeparator) { // return with the separator
							// or not
							word = word.concat(String.valueOf(character));
						}
					}
				} else { // not is a special character (comment or \)

					if (write) {
						writeSkeleton(character);
					} else {

						stack += String.valueOf(character);
					}
					if (separators.indexOf(character) == -1 || withSeparator) { // return
						// with
						// the
						// separator
						// or
						// not
						word = word.concat(String.valueOf(character));
					}
				}
			}
			lastWord = word;
			return word;
		}

		/**
		 * Capture string.
		 * @param startNow
		 *            the start now
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void captureString(boolean startNow) throws IOException {
			int quotes = 0;
			if (startNow) {
				quotes = 1; // now in the string
			}
			char character = ' ';
			char lastChar;
			String word = ""; //$NON-NLS-1$
			while (buffer.ready() && quotes < 2) {
				lastChar = character;
				character = (char) buffer.read();
				if (quotes == 0) { // not in the string yet
					if (character == '"') { // begining of string
						quotes++;
					} else { // not in the string yet
						writeSkeleton(character);
					}
				} else { // is in the string
					if (character == '"') { // end of string Careful can be the
						// \" escape character
						if (word.equals("")) { //$NON-NLS-1$
							writeSkeleton('"');
							writeSkeleton('"');
						} else { // string is empty string
							writeSegment(word);
						}
						quotes++;
					} else { // midle of string
						if (character == '\\') { // if is escape character
							lastChar = character;
							character = (char) buffer.read();
							if (character == '\n' || character == '\r') {
								while (blankChar(character) && character != ' ') {
									lastChar = character;
									character = (char) buffer.read();
								}
								if (character == '"') { // if end of string in
									// first character of
									// the next line
									if (word.equals("")) { //$NON-NLS-1$
										writeSkeleton('"');
										writeSkeleton('"');
									} else { // string is empty string
										writeSegment(word);
									}
								} else { // normal character in the line below \
									word = word.concat(String.valueOf(character));
								}
							} else { // Normal escape character
								word = word.concat(String.valueOf(lastChar));
								word = word.concat(String.valueOf(character));
							}
						} else { // Normal character
							word = word.concat(String.valueOf(character));
						}
					}
				}
			}
		}

		/**
		 * Parses the string table.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseStringTable() throws IOException {
			String word = " "; //$NON-NLS-1$
			while (!beginBlock(word)) {
				word = parseWords(" \n\t\r,", true, false); //$NON-NLS-1$
			}

			while (!endBlock(word)) {
				stack = ""; //$NON-NLS-1$
				word = parseWords(" \n\t\r,\"L", false, true); //$NON-NLS-1$
				if (word.trim().equals("\"")) { //$NON-NLS-1$
					captureString(true);
				} else { // is END or ID
					writeSkeleton(stack);
				}
			}
		}

		/**
		 * Parses the menu.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseMenu() throws IOException {
			String word = " "; //$NON-NLS-1$
			while (!beginBlock(word)) {
				word = parseWords(" \n\t\r,", true, false); //$NON-NLS-1$
			}
			parseMenuBlock();
		}

		/**
		 * Parses the menu block.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseMenuBlock() throws IOException {
			String word = " "; //$NON-NLS-1$
			while (!endBlock(word)) {
				word = parseWords(" ,\n\t\r\"", false, true); //$NON-NLS-1$
				if (word.trim().equals("MENUITEM")) { //$NON-NLS-1$
					writeSkeleton(stack);
					stack = ""; //$NON-NLS-1$
					word = parseWords(" ,\n\t\r\"L", false, true); //$NON-NLS-1$
					if (word.trim().equals("\"")) { //$NON-NLS-1$
						stack = ""; //$NON-NLS-1$
						captureString(true);
					} else { // SEPARATOR
						writeSkeleton(stack);
						stack = ""; //$NON-NLS-1$
					}
				} else if (word.trim().equals("POPUP")) { //$NON-NLS-1$
					writeSkeleton(stack);
					stack = ""; //$NON-NLS-1$
					parsePopup();
				} else if (word.trim().equals("\"")) { //$NON-NLS-1$
					stack = ""; //$NON-NLS-1$
					captureString(true);
				} else {
					writeSkeleton(stack);
					stack = ""; //$NON-NLS-1$
				}
			}
		}

		/**
		 * Parses the popup.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parsePopup() throws IOException {
			String word = " "; //$NON-NLS-1$

			while (!beginBlock(word)) {
				stack = ""; //$NON-NLS-1$
				word = parseWords(" \n\t\r,\"L", false, true); //$NON-NLS-1$
				if (word.trim().equals("\"")) { //$NON-NLS-1$
					captureString(true);
				} else { // is END or ID
					writeSkeleton(stack);
				}
			}

			stack = ""; //$NON-NLS-1$
			parseMenuBlock();
		}

		/**
		 * Parses the dlg init.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseDlgInit() throws IOException {
			String word = ""; //$NON-NLS-1$
			while (buffer.ready() && !beginBlock(word)) {
				word = parseWords(" \n\t\r,", true, false); //$NON-NLS-1$
			}

			parseDlgInitBlock();
		}

		/**
		 * Parses the dlg init block.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseDlgInitBlock() throws IOException {
			String word = ""; //$NON-NLS-1$
			int position = 0; // parse position in the dlginitblock
			int dataLength = 0;
			stack = ""; //$NON-NLS-1$
			while (buffer.ready() && !endBlock(word)) {
				word = parseWords(" \n\t\r,", false, false).trim(); //$NON-NLS-1$
				if (!word.equals("")) { //$NON-NLS-1$
					if (word.equals("0") && position == 0) { //$NON-NLS-1$
						break;
					}
					switch (position) {
					case 0: // id
						dataLength = 0;
						position++;
						break;
					case 1: // type
						if (word.equals("0x403") || word.equals("0x1234")) { //$NON-NLS-1$ //$NON-NLS-2$
							position++;
						} else {
							position = 6;
						}
						writeSkeleton(stack);
						stack = ""; //$NON-NLS-1$
						break;

					case 2: // length
						if (validateNumber(word)) {
							dataLength = Integer.parseInt(word); // *****Controlar
							// si no es
							// un n�mero
							position++;
						} else {
							position = 6;
						}
						break;
					case 3: // end of align 0
						position++;
						break;
					case 4: // first time data
						if (word.charAt(0) == '\"') { // case "/000"
							position = 0;
							writeSkeleton(stack);
							stack = ""; //$NON-NLS-1$
							break;
						}
						stack = ""; //$NON-NLS-1$
						writeSkeleton("###" + segId + "###,0 \r\n"); //$NON-NLS-1$ //$NON-NLS-2$
						position++; // go to the next case now without break
					case 5: // midle of data
						extractString(word, dataLength);
						position = 0;
						stack = ""; //$NON-NLS-1$
						break;
					case 6: // not string
						writeSkeleton(stack);
						stack = ""; //$NON-NLS-1$
						break;
					default:
						break;
					}
				}
			}
			writeSkeleton(stack);
			stack = ""; //$NON-NLS-1$
		}

		/**
		 * Extract string.
		 * @param ini
		 *            the ini
		 * @param dataLength
		 *            the data length
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void extractString(String ini, int dataLength) throws IOException {
			byte[] array = new byte[dataLength];
			String word = ""; //$NON-NLS-1$
			int i = 1;
			int length = 0;
			Integer tmpNum = Integer.decode(ini);
			array[length++] = (byte) tmpNum.intValue();
			array[length++] = (byte) (tmpNum.intValue() >> 8);
			tmpNum = null;
			while (i < dataLength / 2 && buffer.ready()) {
				word = parseWords(",\"", false, false).trim(); //$NON-NLS-1$
				if (word.charAt(1) == 'x' && word.length() > 3) {
					tmpNum = Integer.decode(word);
					array[length++] = (byte) tmpNum.intValue();
					array[length++] = (byte) (tmpNum.intValue() >> 8);
					tmpNum = null;
				}
				i++;
			}
			if (dataLength % 2 > 0) {
				while (!word.equals("\"000\"")) { //$NON-NLS-1$
					word = parseWords(",\n\t\r ", false, false); //$NON-NLS-1$
				}
			}
			writeSegment(new String(array, "UTF-8")); //$NON-NLS-1$
		}
	}

	/**
	 * Validate number.
	 * @param num
	 *            the num
	 * @return true, if successful
	 */
	private static boolean validateNumber(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks if is end control statement.
	 * @param word
	 *            the word
	 * @return true, if is end control statement
	 */
	private static boolean isEndControlStatement(String word) {
		// list of all posible controls and END keyword
		String[] controls = new String[] { "END", "CONTROL", "LTEXT", "CTEXT", "RTEXT", "AUTO3STATE", "AUTOCHECKBOX", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"AUTORADIOBUTTON", "CHECKBOX", "PUSHBOX", "PUSHBUTTON", "DEFPUSHBUTTON", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"RADIOBUTTON", "STATE3", "USERBUTTON", "GROUPBOX", "EDITTEXT", "BEDIT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"IEDIT", "HEDIT", "COMBOBOX", "LISTBOX", "SCROLLBAR", "ICON" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		for (int i = 0; i < controls.length; i++) {
			if (controls[i].equals(word)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Blank char.
	 * @param c
	 *            the c
	 * @return true, if successful
	 */
	private static boolean blankChar(char c) {
		return c == ' ' || c == '\n' || c == '\t' || c == '\r';
	}

	/**
	 * Begin block.
	 * @param word
	 *            the word
	 * @return true, if successful
	 */
	private static boolean beginBlock(String word) {
		return word.trim().equals("BEGIN") || word.trim().equals("{"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * End block.
	 * @param word
	 *            the word
	 * @return true, if successful
	 */
	private static boolean endBlock(String word) {
		return word.trim().equals("END") || word.trim().equals("}"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
