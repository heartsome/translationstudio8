/**
 * Po2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.po;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.po.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * The Class Po2Xliff.
 * @author John Zhu
 */
public class Po2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "po";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("po.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "PO to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Po2XliffImpl converter = new Po2XliffImpl();
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
	 * The Class Po2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Po2XliffImpl {

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

		/** The target. */
		private String target;

		/** The comment. */
		private String comment;

		/** The context. */
		private String context;

		/** The reference. */
		private String reference;

		/** The flags. */
		private String flags;

		/** The fuzzy. */
		private boolean fuzzy;

		/** The cformat. */
		private boolean cformat;

		/** The in domain. */
		private boolean inDomain;

		/** The source language. */
		private String sourceLanguage;

		private String targetLanguage;

		/** The seg id. */
		private int segId;

		/** The domain id. */
		private int domainId;

		/** The context id. */
		private int contextId = 1;

		/** The ref id. */
		private int refId = 1;

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

			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			targetLanguage = params.get(Converter.ATTR_TARGET_LANGUAGE);
			String srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			boolean isSuite = false;
			if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}

			String qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			source = ""; //$NON-NLS-1$
			target = ""; //$NON-NLS-1$
			comment = ""; //$NON-NLS-1$
			context = ""; //$NON-NLS-1$
			reference = ""; //$NON-NLS-1$
			flags = ""; //$NON-NLS-1$
			fuzzy = false;
			inDomain = false;
			cformat = false;

			try {
				// 计算总任务数
				CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(inputFile);
				monitor.beginTask(Messages.getString("po.Po2Xliff.task1"), cpb.getTotalTask());
				monitor.subTask("");

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
							+ "\" target-language=\""+targetLanguage+"\" datatype=\"" + TYPE_VALUE + "\">\n"); //$NON-NLS-1$
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
				writeString("   <hs:prop-group name=\"encoding\">\n" + //$NON-NLS-1$
						"      <hs:prop prop-type=\"encoding\">" //$NON-NLS-1$
						+ srcEncoding + "</hs:prop>\n" //$NON-NLS-1$
						+ "   </hs:prop-group>\n"); //$NON-NLS-1$
				writeString("</header>\n"); //$NON-NLS-1$
				writeString("<body>\n"); //$NON-NLS-1$

				skeleton = new FileOutputStream(skeletonFile);

				String line = buffer.readLine();
				cpb.calculateProcessed(monitor, line, srcEncoding);
				while (line != null) {
					line = line + "\n"; //$NON-NLS-1$

					if (line.trim().length() == 0) {
						// no text in this line
						// segment separator
						writeSkeleton(line);
					} else {
						if (line.startsWith("#:")) { //$NON-NLS-1$
							// it is a reference
							if (reference.equals("")) { //$NON-NLS-1$
								reference = line.substring(2);
							} else {
								reference = reference + " " + line.substring(2); //$NON-NLS-1$
							}
						}
						if (line.startsWith("# ")) { //$NON-NLS-1$
							// translator comment
							comment = comment + line.substring(2);
						}
						if (line.trim().equals("#")) { //$NON-NLS-1$
							comment = comment + "\n"; //$NON-NLS-1$
						}
						if (line.startsWith("#.")) { //$NON-NLS-1$
							// automatic comment
							context = context + line.substring(2);
						}
						if (line.startsWith("#,")) { //$NON-NLS-1$
							flags = line.substring(2);
							// check for fuzzy
							if (flags.indexOf("fuzzy") != -1) { //$NON-NLS-1$
								fuzzy = true;
							}
							// Only c-format is parsed. Tags from other
							// formats, like php-format or python-format,
							// are left as part of the text
							if (flags.indexOf("c-format") != -1 //$NON-NLS-1$
									&& flags.indexOf("no-c-format") == -1) { //$NON-NLS-1$
								cformat = true;
							}
						}
						if (line.startsWith("#~")) { //$NON-NLS-1$
							// commented entry
							writeSkeleton(line);
						}
						if (line.startsWith("msgid")) { //$NON-NLS-1$
							// get source text
							line = line.substring(5);
							source = line.substring(line.indexOf("\"") + 1, //$NON-NLS-1$
									line.lastIndexOf("\"")); //$NON-NLS-1$
							line = buffer.readLine();
							cpb.calculateProcessed(monitor, line, srcEncoding);
							while (line.startsWith("\"")) { //$NON-NLS-1$
								source = source + "\n" //$NON-NLS-1$
										+ line.substring(line.indexOf("\"") + 1, //$NON-NLS-1$
												line.lastIndexOf("\"")); //$NON-NLS-1$
								line = buffer.readLine();
								cpb.calculateProcessed(monitor, line, srcEncoding);
							}
							continue;
						}
						if (line.startsWith("msgstr")) { //$NON-NLS-1$
							// get the target
							line = line.substring(6);
							target = line.substring(line.indexOf("\"") + 1, //$NON-NLS-1$
									line.lastIndexOf("\"")); //$NON-NLS-1$
							line = buffer.readLine();
							cpb.calculateProcessed(monitor, line, srcEncoding);
							while (line != null && line.startsWith("\"")) { //$NON-NLS-1$
								target = target + "\n" //$NON-NLS-1$
										+ line.substring(line.indexOf("\"") + 1, //$NON-NLS-1$
												line.lastIndexOf("\"")); //$NON-NLS-1$
								line = buffer.readLine();
								cpb.calculateProcessed(monitor, line, srcEncoding);
								if (line == null) {
									line = ""; //$NON-NLS-1$
								}
							}
							writeSegment();
							continue;
						}
						if (line.startsWith("domain")) { //$NON-NLS-1$
							if (inDomain) {
								writeString("   </group>\n"); //$NON-NLS-1$
							}
							inDomain = true;
							writeString("   <group id=\"##" //$NON-NLS-1$
									+ domainId++ + "\" restype=\"x-gettext-domain\" resname=\"" //$NON-NLS-1$
									+ line.substring(6).trim() + "\">\n"); //$NON-NLS-1$
							writeSkeleton(line);
						}
					}

					line = buffer.readLine();
					cpb.calculateProcessed(monitor, line, srcEncoding);
				}

				skeleton.close();

				if (inDomain) {
					writeString("   </group>\n"); //$NON-NLS-1$
				}
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

				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("po.Po2Xliff.msg1"), e);
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
			String approved = "no"; //$NON-NLS-1$
			if (!fuzzy && target.trim().length() > 0) {
				approved = "yes"; //$NON-NLS-1$
			}
			String restype = ""; //$NON-NLS-1$
			if (source.trim().equals("")) { //$NON-NLS-1$
				restype = " restype=\"x-gettext-domain-header\" "; //$NON-NLS-1$
			}
			writeString("   <trans-unit id=\"" //$NON-NLS-1$
					+ segId + "\" xml:space=\"preserve\" approved=\"" //$NON-NLS-1$
					+ approved + "\"" //$NON-NLS-1$
					+ restype + ">\n"); //$NON-NLS-1$
			if (cformat) {
				writeString("      <source xml:lang=\"" //$NON-NLS-1$
						+ sourceLanguage + "\">" //$NON-NLS-1$
						+ parseString(TextUtil.cleanString(source)) + "</source>\n"); //$NON-NLS-1$
				if (target.length() > 0 || approved.equals("yes")) { //$NON-NLS-1$
					writeString("      <target>" //$NON-NLS-1$
							+ parseString(TextUtil.cleanString(target)) + "</target>\n"); //$NON-NLS-1$
				}
			} else {
				if (source.trim().equals("")) { //$NON-NLS-1$
					source = target;
				}
				writeString("      <source xml:lang=\"" //$NON-NLS-1$
						+ sourceLanguage + "\">" //$NON-NLS-1$
						+ TextUtil.cleanString(source) + "</source>\n"); //$NON-NLS-1$
				if (target.length() > 0 || approved.equals("yes")) { //$NON-NLS-1$
					writeString("      <target>" //$NON-NLS-1$
							+ TextUtil.cleanString(target) + "</target>\n"); //$NON-NLS-1$
				}
			}
			if (!comment.equals("")) { //$NON-NLS-1$
				writeString("      <note from=\"po-file\">" //$NON-NLS-1$
						+ TextUtil.cleanString(comment) + "</note>\n"); //$NON-NLS-1$
			}
			if (!context.equals("")) { //$NON-NLS-1$
				writeString("      <context-group name=\"x-po-entry-header#" //$NON-NLS-1$
						+ contextId++ + "\" purpose=\"information\">\n" //$NON-NLS-1$
						+ "         <context context-type=\"x-po-autocomment\">" //$NON-NLS-1$
						+ TextUtil.cleanString(context) + "</context>\n" //$NON-NLS-1$
						+ "      </context-group>\n"); //$NON-NLS-1$
			}
			if (!reference.equals("")) { //$NON-NLS-1$
				parseReference(TextUtil.cleanString(reference));
			}
			if (!flags.equals("")) { //$NON-NLS-1$
				writeString("      <hs:prop-group>\n" //$NON-NLS-1$
						+ "         <hs:prop prop-type=\"x-po-flags\">" //$NON-NLS-1$
						+ TextUtil.cleanString(flags).trim() + "</hs:prop>\n" //$NON-NLS-1$
						+ "      </hs:prop-group>\n"); //$NON-NLS-1$
			}
			writeString("   </trans-unit>\n"); //$NON-NLS-1$
			writeSkeleton("%%%" + segId++ + "%%%\n"); //$NON-NLS-1$ //$NON-NLS-2$

			source = ""; //$NON-NLS-1$
			target = ""; //$NON-NLS-1$
			comment = ""; //$NON-NLS-1$
			context = ""; //$NON-NLS-1$
			reference = ""; //$NON-NLS-1$
			flags = ""; //$NON-NLS-1$
			fuzzy = false;
			cformat = false;
		}

		/**
		 * Parses the reference.
		 * @param ref
		 *            the ref
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void parseReference(String ref) throws IOException {
			if (ref.trim().equals("")) { //$NON-NLS-1$
				return;
			}

			// fixed bug 425 by john. added context element to context-group
			// element.
			String[] refs = ref.trim().split("#:"); //$NON-NLS-1$
			for (int i = 0, size = refs.length; i < size; i++) {
				writeString("      <context-group name=\"x-po-reference#" //$NON-NLS-1$
						+ refId++ + "\" purpose=\"location\">\n"); //$NON-NLS-1$
				String token = refs[i];
				if (token.indexOf(":") != -1) { //$NON-NLS-1$
					writeString("         <context context-type=\"sourcefile\">" //$NON-NLS-1$
							+ token.substring(0, token.indexOf(":")) //$NON-NLS-1$
							+ "</context>\n"); //$NON-NLS-1$
					writeString("         <context context-type=\"linenumber\">" //$NON-NLS-1$
							+ token.substring(token.indexOf(":") + 1) //$NON-NLS-1$
							+ "</context>\n"); //$NON-NLS-1$
				} else {
					writeString("         <context context-type=\"sourcefile\">" + token //$NON-NLS-1$
							+ "</context>\n"); //$NON-NLS-1$
					writeString("         <context context-type=\"linenumber\" />\n"); //$NON-NLS-1$
				}
				writeString("      </context-group>\n"); //$NON-NLS-1$
			}
			refs = null;
		}
	}

	/**
	 * Parses the string.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private static String parseString(String string) {
		// Valid c format especifications must end
		// with one of diouxXfeEgGcs

		int id = 1;
		int index = string.indexOf("%"); //$NON-NLS-1$
		if (index == -1) {
			return string;
		}
		if (string.charAt(index + 1) == '%') {
			index = string.indexOf("%", index + 2); //$NON-NLS-1$
		}
		String result = ""; //$NON-NLS-1$
		while (index != -1) {
			result = result + string.substring(0, index) + "<ph ctype=\"x-c-param\" id=\"" + id++ + "\">"; //$NON-NLS-1$ //$NON-NLS-2$
			int i = index;
			char c = string.charAt(i++);
			while (i < string.length() && "diouxXfeEgGcs".indexOf(c) == -1) { //$NON-NLS-1$
				result = result + c;
				c = string.charAt(i++);
			}
			result = result + c + "</ph>"; //$NON-NLS-1$
			string = string.substring(i);
			index = string.indexOf("%"); //$NON-NLS-1$
			if (index != -1 && index < string.length() && string.charAt(index + 1) == '%') {
				index = string.indexOf("%", index + 2); //$NON-NLS-1$
			}
		}
		result = result + string;
		return result;
	}
}
