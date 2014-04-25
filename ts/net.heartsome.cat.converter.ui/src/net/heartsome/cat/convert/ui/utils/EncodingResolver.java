/*
 * Created on 24-nov-2004
 *
 */
package net.heartsome.cat.convert.ui.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.util.TextUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编码分析工具
 * @author Gonzalo Pennino Greco Copyright (c) 2004 Heartsome Holdings Pte. Ltd. http://www.heartsome.net
 */

public class EncodingResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(EncodingResolver.class);

	/**
	 * This method returns the file encoding
	 * @param fileName
	 * @param fileType
	 * @return
	 */
	public static String getEncoding(String fileName, String fileType) {
		if (fileType == null || fileName == null) {
			return null;
		} else if (fileType.equals(FileFormatUtils.OO) || fileType.equals(FileFormatUtils.OFF)
				|| fileType.equals(FileFormatUtils.PPTX) || fileType.equals(FileFormatUtils.MSEXCEl2007)
				|| fileType.equals(FileFormatUtils.MSWORD2007)) {
			return "UTF-8";
			// fixed bug 424 by john.
		} else if (fileType.equals(FileFormatUtils.MIF) || fileType.equals(FileFormatUtils.TEXT)
				|| fileType.equals(FileFormatUtils.JS) || fileType.equals(FileFormatUtils.JAVA)
				|| fileType.equals(FileFormatUtils.PO)) {
			return FileEncodingDetector.detectFileEncoding(new File(fileName));
		} else if (fileType.equals(FileFormatUtils.SDL) || fileType.equals(FileFormatUtils.DU)
				|| fileType.equals(FileFormatUtils.MQ) ||fileType.equals(FileFormatUtils.WF)) {
			return "UTF-8";
		}
		if (fileType.equals(FileFormatUtils.RTF) || fileType.equals(FileFormatUtils.TRTF)) {
			return getRTFEncoding(fileName);
		} else if (fileType.equals(FileFormatUtils.XML) || fileType.equals(FileFormatUtils.TTX)
				|| fileType.equals(FileFormatUtils.RESX) || fileType.equals(FileFormatUtils.INX)) {
			return getXMLEncoding(fileName);
		} else if (fileType.equals(FileFormatUtils.RC)) {
			return getRCEncoding(fileName);
		} else if (fileType.equals(FileFormatUtils.IDML)) {
			return getIDMLEncoding(fileName);
		} else if (fileType.equals(FileFormatUtils.HTML)) {
			return getHTMLEncoding(fileName);
		}
		return null;
	}

	private static String getRCEncoding(String fileName) {
		try {
			FileInputStream input = new FileInputStream(fileName);
			// read 4K bytes
			int read = 4096;
			if (input.available() < read) {
				read = input.available();
			}
			byte[] bytes = new byte[read];
			input.read(bytes);
			input.close();
			String content = new String(bytes);

			if (content.indexOf("code_page(") != -1) { //$NON-NLS-1$
				String code = content.substring(content.indexOf("code_page(") + 10); //$NON-NLS-1$
				code = code.substring(0, code.indexOf(")")); //$NON-NLS-1$
				return RTF2JavaEncoding(code);
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	/**
	 * This method returns the most suitable encoding according to the file type
	 * @param fileType
	 * @return
	 */
	public static String getEncoding(String fileType) {
		if (fileType == null) {
			return null;
		} else if (fileType.equals(FileFormatUtils.OO)) {
			return "UTF-8"; //$NON-NLS-1$
		} else if (fileType.equals(FileFormatUtils.MIF)) {
			return "US-ASCII"; //$NON-NLS-1$
		} else if (fileType.equals(FileFormatUtils.JAVA)) {
			return "ISO-8859-1"; //$NON-NLS-1$
		} else if (fileType.equals(FileFormatUtils.TTX)) {
			return "UTF-16LE"; //$NON-NLS-1$
		} else if (fileType.equals(FileFormatUtils.PO) || fileType.equals(FileFormatUtils.XML)
				|| fileType.equals(FileFormatUtils.INX)) {
			return "UTF-8"; //$NON-NLS-1$
		}
		return null;
	}

	public static boolean isFixedEncoding(String fileType) {
		if (fileType.equals(FileFormatUtils.OO) ||
		// fileType.equals(FileFormatUtils.MIF) || //Fixed a bug 1651 by john.
				fileType.equals(FileFormatUtils.RTF) || fileType.equals(FileFormatUtils.TRTF)) {
			return true;
		}
		return false;
	}

	private static String getXMLEncoding(String fileName) {
		// return UTF-8 as default
		String result = "UTF-8"; //$NON-NLS-1$
		try {
			// check if there is a BOM (byte order mark)
			// at the start of the document
			FileInputStream inputStream = new FileInputStream(fileName);
			byte[] array = new byte[2];
			inputStream.read(array);
			inputStream.close();
			byte[] lt = "<".getBytes(); //$NON-NLS-1$
			byte[] feff = { -1, -2 };
			byte[] fffe = { -2, -1 };
			if (array[0] != lt[0]) {
				// there is a BOM, now check the order
				if (array[0] == fffe[0] && array[1] == fffe[1]) {
					return "UTF-16BE"; //$NON-NLS-1$
				}
				if (array[0] == feff[0] && array[1] == feff[1]) {
					return "UTF-16LE"; //$NON-NLS-1$
				}
			}
			// check declared encoding
			FileReader input = new FileReader(fileName);
			BufferedReader buffer = new BufferedReader(input);
			String line = buffer.readLine();
			input.close();
			if (line.startsWith("<?")) { //$NON-NLS-1$
				line = line.substring(2, line.indexOf("?>")); //$NON-NLS-1$
				line = line.replaceAll("\'", "\""); //$NON-NLS-1$ //$NON-NLS-2$
				StringTokenizer tokenizer = new StringTokenizer(line);
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.startsWith("encoding")) { //$NON-NLS-1$
						result = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			if (Constant.RUNNING_MODE == Constant.MODE_DEBUG) {
				e.printStackTrace();
			}

			try {
				File log = File.createTempFile("error", ".log", new File("logs"));
				FileWriter writer = new FileWriter(log);
				PrintWriter print = new PrintWriter(writer);
				e.printStackTrace(print);
				writer.close();
				print.close();
				writer = null;
				print = null;
			} catch (IOException e2) {
				// do nothing
			} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		}
		String[] encodings = TextUtil.getPageCodes();
		for (int i = 0; i < encodings.length; i++) {
			if (encodings[i].equalsIgnoreCase(result)) {
				return encodings[i];
			}
		}
		return result;
	}

	private static String getRTFEncoding(String fileName) {
		try {
			FileInputStream input = new FileInputStream(fileName);
			// read 200 bytes
			int read = 200;
			if (input.available() < read) {
				read = input.available();
			}
			byte[] bytes = new byte[read];
			input.read(bytes);
			input.close();
			String content = new String(bytes);

			StringTokenizer tk = new StringTokenizer(content, "\\", true); //$NON-NLS-1$
			while (tk.hasMoreTokens()) {
				String token = tk.nextToken();
				if (token.equals("\\")) { //$NON-NLS-1$
					token = token + tk.nextToken();
				}
				if (token.startsWith("\\ansicpg")) { //$NON-NLS-1$
					String javaEnc = RTF2JavaEncoding(token.substring(8).trim());
					System.out.println("Encoding: " + javaEnc); //$NON-NLS-1$
					return javaEnc;
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			if (Constant.RUNNING_MODE == Constant.MODE_DEBUG) {
				e.printStackTrace();
			}

			try {
				File log = File.createTempFile("error", ".log", new File("logs"));
				FileWriter writer = new FileWriter(log);
				PrintWriter print = new PrintWriter(writer);
				e.printStackTrace(print);
				writer.close();
				print.close();
				writer = null;
				print = null;
			} catch (IOException e2) {
				// do nothing
			} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			return null;
		}
		// Encoding not declared. Assume OpenOffice and return its XML encoding
		return "UTF-8"; //$NON-NLS-1$
	}

	private static String RTF2JavaEncoding(String encoding) {
		String[] codes = TextUtil.getPageCodes();
		for (int h = 0; h < codes.length; h++) {
			if (codes[h].toLowerCase().indexOf("windows-" + encoding) != -1) { //$NON-NLS-1$
				return codes[h];
			}
		}
		if (encoding.equals("10000")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("macroman") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("10006")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("macgreek") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("10007")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("maccyrillic") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("10029")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("maccentraleurope") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("10079")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("maciceland") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("10081")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("macturkish") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("65000")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("utf-7") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("650001")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("utf-8") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("932")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("shift_jis") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("936")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("gbk") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("949")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("euc-kr") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("950")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("big5") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		if (encoding.equals("1361")) { //$NON-NLS-1$
			for (int h = 0; h < codes.length; h++) {
				if (codes[h].toLowerCase().indexOf("johab") != -1) { //$NON-NLS-1$
					return codes[h];
				}
			}
		}
		return null;
	}

	private static String getIDMLEncoding(String fileName) {
		String encoding = "UTF-8";
		try {
			ZipInputStream in = new ZipInputStream(new FileInputStream(fileName));
			ZipEntry entry = null;
			while ((entry = in.getNextEntry()) != null) {
				if (entry.getName().equals("designmap.xml")) {
					byte[] array = new byte[1024];
					in.read(array);
					String strTmp = new String(array);
					int index = strTmp.indexOf("<?xml");
					if (index != -1) {
						int endIndex = strTmp.indexOf("?>", index);
						strTmp = strTmp.substring(index, endIndex);
						for (String str : strTmp.split(" ")) {
							if (str.startsWith("encoding")) {
								if (str.indexOf("\"") != -1) {
									encoding = str.substring(str.indexOf("\"") + 1, str.lastIndexOf("\""));
								} else if (str.indexOf("'") != -1) {
									encoding = str.substring(str.indexOf("'") + 1, str.lastIndexOf("'"));
								}
								break;
							}
						}
					}
					in.close();
					break;
				}
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return encoding;
	}

	private static String getHTMLEncoding(String fileName) {
		// return UTF-8 as default
		String result = "UTF-8"; //$NON-NLS-1$
		try {
			FileReader input = new FileReader(fileName);
			BufferedReader buffer = new BufferedReader(input);
			String line;
			Pattern pattern = Pattern.compile("[a-zA-Z-_\\d]+");
			while ((line = buffer.readLine()) != null) {
				int index = line.indexOf("charset=");
				if (index != -1) {
					Matcher matcher = pattern.matcher(line.substring(index + "charset=".length()));
					if (matcher.find()) {
						result = matcher.group();
						break;
					}
				}
			}
			input.close();
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		String[] encodings = TextUtil.getPageCodes();
		for (int i = 0; i < encodings.length; i++) {
			if (encodings[i].equalsIgnoreCase(result)) {
				return encodings[i];
			}
		}
		// 如果不是有效的编码就返回 UTF-8
		return "UTF-8";
	}
}
