/**
 * Properties2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.javaproperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.javaproperties.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * The Class Properties2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Properties2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "javalistresourcebundle";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("javaproperties.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "Java Properties to XLIFF Conveter";

	/**
	 * (non-Javadoc).
	 * @param args
	 *            the args
	 * @param monitor
	 *            the monitor
	 * @return the map< string, string>
	 * @throws ConverterException
	 *             the converter exception
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Properties2XliffImpl converter = new Properties2XliffImpl();
		return converter.run(args, monitor);
	}

	/**
	 * (non-Javadoc).
	 * @return the name
	 * @see net.heartsome.cat.converter.Converter#getName()
	 */
	public String getName() {
		return NAME_VALUE;
	}

	/**
	 * (non-Javadoc).
	 * @return the type
	 * @see net.heartsome.cat.converter.Converter#getType()
	 */
	public String getType() {
		return TYPE_VALUE;
	}

	/**
	 * (non-Javadoc).
	 * @return the type name
	 * @see net.heartsome.cat.converter.Converter#getTypeName()
	 */
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	/**
	 * The Class Properties2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Properties2XliffImpl {

		/** The input. */
		private InputStreamReader input;

		/** The output. */
		private FileOutputStream output;

		/** The skeleton. */
		private FileOutputStream skeleton;

		/** The buffer. */
		private BufferedReader buffer;

		/** The input file. */
		private String inputFile;

		/** The xliff file. */
		private String xliffFile;

		/** The skeleton file. */
		private String skeletonFile;

		/** The source. */
		private String source;

		/** The source language. */
		private String sourceLanguage;

		private String targetLanguage;

		/** The seg id. */
		private int segId;

		/** The segmenter. */
		private StringSegmenter segmenter;

		/** The seg by element. */
		private boolean segByElement;

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
			monitor = Progress.getMonitor(monitor);
			Map<String, String> result = new HashMap<String, String>();
			segId = 0;

			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			targetLanguage = params.get(Converter.ATTR_TARGET_LANGUAGE);
			sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			String srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			String catalogue = params.get(Converter.ATTR_CATALOGUE);
			String elementSegmentation = params.get(Converter.ATTR_SEG_BY_ELEMENT);
			boolean isSuite = false;
			if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}

			String qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			if (elementSegmentation == null) {
				segByElement = false;
			} else {
				if (elementSegmentation.equals(Converter.TRUE)) {
					segByElement = true;
				} else {
					segByElement = false;
				}
			}

			source = "";
			try {
				// 计算总任务数
				File temp = new File(inputFile);
				long totalSize = 0;
				if (temp.exists()) {
					totalSize = temp.length();
				}
				CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(totalSize);
				monitor.beginTask(Messages.getString("javaproperties.Properties2Xliff.task1"), cpb.getTotalTask());
				monitor.subTask("");
				// if (segByElement == false) {
				if (!segByElement) {
					String initSegmenter = params.get(Converter.ATTR_SRX);
					segmenter = new StringSegmenter(initSegmenter, sourceLanguage, catalogue);
				}

				FileInputStream stream = new FileInputStream(inputFile);
				input = new InputStreamReader(stream, srcEncoding);
				buffer = new BufferedReader(input);

				output = new FileOutputStream(xliffFile);

				writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
				writeString("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //$NON-NLS-1$
						"xmlns:hs=\"" + Converter.HSNAMESPACE + "\" " + //$NON-NLS-1$ //$NON-NLS-2$
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
				
				// 写一个正则表达式，专门去除前面的空格 robert	2012-11-13
				String regex = "=\\s*";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = null;
				
				String line;
				while ((line = buffer.readLine()) != null) {
					// 检查是否已取消转换操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("javaproperties.cancel"));
					}
					// 计算转换进度
					int size = line.getBytes(srcEncoding).length;
					if (size > 0) {
						int workedTask = cpb.calculateProcessed(size + 1);
						if (workedTask > 0) {
							monitor.worked(workedTask);
						}
					}

					if (line.trim().length() == 0) {
						// no text in this line
						// segment separator
						writeSkeleton(line + "\n"); //$NON-NLS-1$
					} else if (line.trim().startsWith("#")) { //$NON-NLS-1$
						// this line is a comment
						// send to skeleton
						writeSkeleton(line + "\n"); //$NON-NLS-1$
					} else {
						String tmp = line;
						if (line.endsWith("\\")) { //$NON-NLS-1$
							do {
								line = buffer.readLine();
								// 计算转换进度
								int tempSize = line.getBytes(srcEncoding).length;
								if (tempSize > 0) {
									int tempWorkedTask = cpb.calculateProcessed(tempSize);
									if (tempWorkedTask > 0) {
										monitor.worked(tempWorkedTask);
									}
								}

								tmp += "\n" + line; //$NON-NLS-1$
							} while (line != null && line.endsWith("\\")); //$NON-NLS-1$
						}
						matcher = pattern.matcher(tmp);
						String matcherResult = "=";
						if (matcher.find()) {
							matcherResult = matcher.group();
						}
						
						int index = tmp.indexOf(matcherResult); //$NON-NLS-1$
						if (index != -1) {
							String key = tmp.substring(0, index + matcherResult.length());
							writeSkeleton(key);
							source = tmp.substring(index + matcherResult.length());
							writeSegment();
							writeSkeleton("\n"); //$NON-NLS-1$
						} else {
							// this line may be wrong, send to skeleton
							// and continue
							writeSkeleton(tmp);
						}
					}
				}

				skeleton.close();

				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</file>\n"); //$NON-NLS-1$
				writeString("</xliff>"); //$NON-NLS-1$
				input.close();
				output.close();

				result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
						Messages.getString("javaproperties.Properties2Xliff.msg1"), e);
			} finally {
				monitor.done();
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
		}

		/**
		 * Write segment.
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSegment() throws IOException {
			String[] segments;
			// if (segByElement == false) {
			if (!segByElement) {
				segments = segmenter.segment(fixChars(source));
			} else {
				segments = new String[1];
				segments[0] = fixChars(source);
			}
			for (int i = 0; i < segments.length; i++) {
				if (!segments[i].trim().equals("")) { //$NON-NLS-1$
					writeString("   <trans-unit id=\"" + segId //$NON-NLS-1$
							+ "\" xml:space=\"preserve\" approved=\"no\">\n" //$NON-NLS-1$
							+ "      <source xml:lang=\"" + sourceLanguage + "\">" //$NON-NLS-1$ //$NON-NLS-2$
							+ TextUtil.cleanString(segments[i]) + "</source>\n"); //$NON-NLS-1$
					writeString("   </trans-unit>\n"); //$NON-NLS-1$
					writeSkeleton("%%%" + segId++ + "%%%"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					writeSkeleton(segments[i]);
				}
			}
			source = ""; //$NON-NLS-1$
		}
	}

	/**
	 * Fix chars.
	 * @param line
	 *            the line
	 * @return the string
	 */
	private static String fixChars(String line) {
		int start = line.indexOf("\\u"); //$NON-NLS-1$
		while (start != -1) {
			if (line.substring(start + 2, start + 6).toLowerCase()
					.matches("[\\dabcdef][\\dabcdef][\\dabcdef][\\dabcdef]")) { //$NON-NLS-1$
				line = line.substring(0, start) + toChar(line.substring(start + 2, start + 6))
						+ line.substring(start + 6);
			}
			start = line.indexOf("\\u", start + 1); //$NON-NLS-1$
		}
		return line;
	}

	/**
	 * To char.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private static String toChar(String string) {
		int hex = Integer.parseInt(string, 16);
		char result = (char) hex;
		return "" + result; //$NON-NLS-1$
	}
}
