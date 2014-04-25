/**
 * Text2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.text.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * The Class Text2Xliff.
 * @author John Zhu
 */
public class Text2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "plaintext";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("text.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "Text to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Text2XliffImpl converter = new Text2XliffImpl();
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
	 * The Class Text2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Text2XliffImpl {

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
			if (!segByElement) {
				segments = segmenter.segment(source);
			} else {
				segments = new String[1];
				segments[0] = source;
			}
			for (int i = 0; i < segments.length; i++) {
				if (TextUtil.cleanString(segments[i]).trim().equals("")) { //$NON-NLS-1$
					writeSkeleton(segments[i]);
				} else {
					writeString("   <trans-unit id=\"" + segId //$NON-NLS-1$
							+ "\" xml:space=\"preserve\" approved=\"no\">\n" //$NON-NLS-1$
							+ "      <source xml:lang=\"" + sourceLanguage + "\">" //$NON-NLS-1$ //$NON-NLS-2$
							+ TextUtil.cleanString(segments[i]) + "</source>\n"); //$NON-NLS-1$
					writeString("   </trans-unit>\n"); //$NON-NLS-1$
					writeSkeleton("%%%" + segId++ + "%%%"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (segments.length > 0) {
				writeSkeleton("\n");
			}
			source = ""; //$NON-NLS-1$
		}

		/**
		 * Run.
		 * @param args
		 *            the args
		 * @param monitor
		 *            the monitor
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			inputFile = args.get(Converter.ATTR_SOURCE_FILE);
			File temp = new File(inputFile);
			long totalSize = 0;
			if (temp.exists()) {
				totalSize = temp.length();
			}
			
			// BUG 2761	robert	2012-10-31
			if (totalSize <= 0 ) {
				String errorTip = MessageFormat.format(Messages.getString("text.Text2Xliff.addTip1"), temp.getName());
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, null);
			}
			
			CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(totalSize);

			monitor.beginTask(Messages.getString("text.Text2Xliff.task1"), cpb.getTotalTask());
			monitor.subTask("");
			Map<String, String> result = new HashMap<String, String>();
			try {
				segId = 0;

				xliffFile = args.get(Converter.ATTR_XLIFF_FILE);
				skeletonFile = args.get(Converter.ATTR_SKELETON_FILE);
				sourceLanguage = args.get(Converter.ATTR_SOURCE_LANGUAGE);
				targetLanguage = args.get(Converter.ATTR_TARGET_LANGUAGE);
				String srcEncoding = args.get(Converter.ATTR_SOURCE_ENCODING);
				String catalogue = args.get(Converter.ATTR_CATALOGUE);
				String elementSegmentation = args.get(Converter.ATTR_SEG_BY_ELEMENT);
				boolean isSuite = false;
				if (Converter.TRUE.equalsIgnoreCase(args.get(Converter.ATTR_IS_SUITE))) {
					isSuite = true;
				}

				String qtToolID = args.get(Converter.ATTR_QT_TOOLID) != null ? args.get(Converter.ATTR_QT_TOOLID)
						: Converter.QT_TOOLID_DEFAULT_VALUE;

				// fixed bug 479 by john.
				//		boolean breakOnCRLF = "yes".equals(params.get("breakOnCRLF")); //$NON-NLS-1$ //$NON-NLS-2$

				if (elementSegmentation == null) {
					segByElement = false;
				} else {
					if (elementSegmentation.equals(Converter.TRUE)) {
						segByElement = true;
					} else {
						segByElement = false;
					}
				}

				source = ""; //$NON-NLS-1$
				try {
					if (!segByElement) {
						String initSegmenter = args.get(Converter.ATTR_SRX);
						segmenter = new StringSegmenter(initSegmenter, sourceLanguage, catalogue);
					}
					FileInputStream stream = new FileInputStream(inputFile);
					input = new InputStreamReader(stream, srcEncoding);
					buffer = new BufferedReader(input);

					output = new FileOutputStream(xliffFile);

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
					writeString("      <external-file href=\"" + TextUtil.cleanString(skeletonFile) + "\" " + crc + "/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					writeString("   </skl>\n"); //$NON-NLS-1$
					writeString("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
					writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" + srcEncoding //$NON-NLS-1$
							+ "</hs:prop></hs:prop-group>\n"); //$NON-NLS-1$
					writeString("</header>\n"); //$NON-NLS-1$
					writeString("<body>\n"); //$NON-NLS-1$

					skeleton = new FileOutputStream(skeletonFile);

					// fixed bug 479 by john.
					// remarked breakOnCRLF validation. used the same implements
					// with
					// breakOnCRLF is true.
					// if (breakOnCRLF) {

					// Fixed a bug 1609 by John.
					while ((source = buffer.readLine()) != null) {
						int size = source.getBytes(srcEncoding).length;
						if (source.trim().length() == 0) {
							writeSkeleton(source + "\n"); //$NON-NLS-1$
						} else {
							writeSegment();
						}
						if (monitor.isCanceled()) {
							throw new OperationCanceledException(Messages.getString("text.Text2Xliff.msg1"));
						}
						if (size > 0) {
							// 考虑加上换行符的一个字节数
							int workedTask = cpb.calculateProcessed(size + 1);
							if (workedTask > 0) {
								monitor.worked(workedTask);
							}
						}
						// source = buffer.readLine();
					}
					// } else {
					// String line = changeEncoding(buffer.readLine(), srcEncoding);
					// while (line != null) {
					//					line = line + "\n"; //$NON-NLS-1$
					//
					// if (line.trim().length() == 0) {
					// // no text in this line
					// // segment separator
					// writeSkeleton(line);
					// } else {
					// while (line != null && line.trim().length() != 0) {
					// source = source + line;
					// line = changeEncoding(buffer.readLine(), srcEncoding);
					// if (line != null) {
					//								line = line + "\n"; //$NON-NLS-1$
					// }
					// }
					// writeSegment();
					// }
					// line = changeEncoding(buffer.readLine(), srcEncoding);
					// }
					// }
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

					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("text.Text2Xliff.msg2"),
							e);
				}
			} finally {
				monitor.done();
			}
			return result;
		}
	}
}