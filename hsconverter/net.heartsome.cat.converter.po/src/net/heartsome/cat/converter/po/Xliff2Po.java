/**
 * Xliff2Po.java
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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.po.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.xml.sax.SAXException;

/**
 * The Class Xliff2Po.
 * @author John Zhu
 */
public class Xliff2Po implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "po";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("po.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to PO Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2PoImpl converter = new Xliff2PoImpl();
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
	 * The Class Xliff2PoImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2PoImpl {

		private static final String UTF_8 = "UTF-8";

		/** The input. */
		private InputStreamReader input;

		/** The buffer. */
		private BufferedReader buffer;

		/** The skl file. */
		private String sklFile;

		/** The xliff file. */
		private String xliffFile;

		/** The line. */
		private String line;

		/** The segments. */
		private Hashtable<String, Element> segments;

		/** The output. */
		private FileOutputStream output;

		/** The encoding. */
		private String encoding;

		private boolean isPreviewMode;

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
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();
			Map<String, String> result = new HashMap<String, String>();

			sklFile = params.get(Converter.ATTR_SKELETON_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			String outputFile = params.get(Converter.ATTR_TARGET_FILE);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				// 把转换过程分为两部分共 10 个任务，其中加载 xliff 占 5，替换过程占 5。
				monitor.beginTask("", 10);
				infoLogger.logConversionFileInfo(null, null, xliffFile, sklFile);
				monitor.subTask(Messages.getString("po.Xliff2Po.task2"));
				infoLogger.startLoadingXliffFile();
				output = new FileOutputStream(outputFile);
				loadSegments();
				infoLogger.endLoadingXliffFile();
				monitor.worked(5);

				IProgressMonitor replaceMonitor = Progress.getSubMonitor(monitor, 5);
				try {
					// 是否取消操作
					if (monitor.isCanceled()) {
						throw new OperationCanceledException(Messages.getString("po.cancel"));
					}
					infoLogger.startReplacingSegmentSymbol();
					CalculateProcessedBytes cpb = ConverterUtils.getCalculateProcessedBytes(sklFile);
					replaceMonitor.beginTask(Messages.getString("po.Xliff2Po.task3"), cpb.getTotalTask());
					replaceMonitor.subTask("");
					input = new InputStreamReader(new FileInputStream(sklFile), UTF_8); //$NON-NLS-1$
					buffer = new BufferedReader(input);
					line = buffer.readLine();
					while (line != null) {
						// 是否取消
						if (replaceMonitor.isCanceled()) {
							throw new OperationCanceledException(Messages.getString("po.cancel"));
						}
						cpb.calculateProcessed(replaceMonitor, line, UTF_8);

						line = line + "\n"; //$NON-NLS-1$

						if (line.indexOf("%%%") != -1) { //$NON-NLS-1$
							//
							// contains translatable text
							//
							int index = line.indexOf("%%%"); //$NON-NLS-1$
							while (index != -1) {
								String start = line.substring(0, index);
								writeString(start);
								line = line.substring(index + 3);
								String code = line.substring(0, line.indexOf("%%%")); //$NON-NLS-1$
								line = line.substring(line.indexOf("%%%") + 3); //$NON-NLS-1$
								Element segment = segments.get(code);
								if (segment != null) {
									writeSegment(segment);
								} else {
									ConverterUtils.throwConverterException(Activator.PLUGIN_ID, MessageFormat.format(
											Messages.getString("po.Xliff2Po.msg1"), code));
								}

								index = line.indexOf("%%%"); //$NON-NLS-1$
								if (index == -1) {
									writeString(line);
								}
							} // end while
						} else {
							//
							// non translatable portion
							//
							writeString(line);
						}

						line = buffer.readLine();
					}
				} finally {
					replaceMonitor.done();
				}
				infoLogger.endReplacingSegmentSymbol();
				output.close();
				result.put(Converter.ATTR_TARGET_FILE, outputFile);
				infoLogger.endConversion();
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("po.Xliff2Po.msg2"), e);
			} finally {
				monitor.done();
			}
			return result;
		}

		/**
		 * Write segment.
		 * @param segment
		 *            the segment
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeSegment(Element segment) throws IOException {
			Element target = segment.getChild("target"); //$NON-NLS-1$
			Element source = segment.getChild("source"); //$NON-NLS-1$
			boolean newLine = false;
			boolean fuzzy = false;
			if (isPreviewMode
					|| !segment.getAttributeValue("approved", "no").equalsIgnoreCase("Yes") && target != null && !target.getText().trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				fuzzy = true;
			}
			writeComments(segment);
			writeContext(segment);
			writeReferences(segment);
			writeFlags(segment, fuzzy);

			if (source.getText().endsWith("\n")) { //$NON-NLS-1$
				newLine = true;
			} else {
				newLine = false;
			}
			if (!segment.getAttributeValue("restype", "").equals("x-gettext-domain-header")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				writeString("msgid \"" + addQuotes(source.getText()) + "\"\n"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				writeString("msgid \"\"\n"); //$NON-NLS-1$
			}
			if (target != null) {
				String text = target.getText();
				if (newLine && !text.endsWith("\n")) { //$NON-NLS-1$
					text = text + "\"\"\n"; //$NON-NLS-1$
				}
				writeString("msgstr \"" + addQuotes(text) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				writeString("msgstr \"\""); //$NON-NLS-1$
			}
		}

		/**
		 * Write flags.
		 * @param segment
		 *            the segment
		 * @param fuzzy
		 *            the fuzzy
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeFlags(Element segment, boolean fuzzy) throws IOException {
			List<Element> groups = segment.getChildren("hs:prop-group"); //$NON-NLS-1$
			Iterator<Element> i = groups.iterator();
			String flags = ""; //$NON-NLS-1$
			while (i.hasNext()) {
				Element group = i.next();
				List<Element> contexts = group.getChildren();
				Iterator<Element> h = contexts.iterator();
				while (h.hasNext()) {
					Element prop = h.next();
					if (prop.getAttributeValue("ctype", "").equals("x-po-flags")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						flags = prop.getText();
					}
				}
			}
			if (fuzzy) {
				if (flags.indexOf("fuzzy") == -1) { //$NON-NLS-1$
					writeString("#, fuzzy " + flags + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					writeString("#, " + flags + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				if (flags.indexOf("fuzzy") == -1) { //$NON-NLS-1$
					if (!flags.equals("")) { //$NON-NLS-1$
						writeString("#, " + flags + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					flags = flags.substring(0, flags.indexOf("fuzzy")) + flags.substring(flags.indexOf("fuzzy") + 5); //$NON-NLS-1$ //$NON-NLS-2$
					if (!flags.equals("")) { //$NON-NLS-1$
						writeString("#, " + flags + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}

		/**
		 * Write references.
		 * @param segment
		 *            the segment
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeReferences(Element segment) throws IOException {
			String reference = "#:"; //$NON-NLS-1$
			List<Element> groups = segment.getChildren("context-group"); //$NON-NLS-1$
			Iterator<Element> i = groups.iterator();
			while (i.hasNext()) {
				Element group = i.next();
				if (group.getAttributeValue("name", "").startsWith("x-po-reference") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						&& group.getAttributeValue("purpose").equals("location")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					String file = ""; //$NON-NLS-1$
					String linenumber = ""; //$NON-NLS-1$
					List<Element> contexts = group.getChildren();
					Iterator<Element> h = contexts.iterator();
					while (h.hasNext()) {
						Element context = h.next();
						if (context.getAttributeValue("context-type", "").equals("sourcefile")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							file = context.getText();
						}
						if (context.getAttributeValue("context-type", "").equals("linenumber")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							linenumber = context.getText();
						}
					}
					String test = reference + " " + file + ":" + linenumber; //$NON-NLS-1$ //$NON-NLS-2$
					if (test.substring(test.lastIndexOf("#:")).length() > 80) { //$NON-NLS-1$
						reference = reference + "\n#:"; //$NON-NLS-1$
					}
					reference = reference + " " + file + ":" + linenumber; //$NON-NLS-1$ //$NON-NLS-2$

					// fixed bug 425 by john.
					if (reference.endsWith(":")) { //$NON-NLS-1$
						reference = reference.substring(0, reference.length() - 1);
					}
				}
			}
			if (!reference.equals("#:")) { //$NON-NLS-1$
				writeString(reference + "\n"); //$NON-NLS-1$
			}
		}

		/**
		 * Write context.
		 * @param segment
		 *            the segment
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeContext(Element segment) throws IOException {
			List<Element> groups = segment.getChildren("context-group"); //$NON-NLS-1$
			Iterator<Element> i = groups.iterator();
			while (i.hasNext()) {
				Element group = i.next();
				if (group.getAttributeValue("name", "").startsWith("x-po-entry-header") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						&& group.getAttributeValue("purpose").equals("information")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					List<Element> contexts = group.getChildren();
					Iterator<Element> h = contexts.iterator();
					while (h.hasNext()) {
						Element context = h.next();
						if (context.getAttributeValue("context-type", "").equals("x-po-autocomment")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							Vector<String> comments = splitLines(context.getText());
							for (int j = 0; j < comments.size(); j++) {
								String comment = comments.get(j);
								if (!comment.trim().equals("")) { //$NON-NLS-1$
									writeString("#. " + comment.trim() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
								} else {
									writeString("#.\n"); //$NON-NLS-1$
								}
							}
						}
					}
				}
			}
		}

		/**
		 * Write comments.
		 * @param segment
		 *            the segment
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeComments(Element segment) throws IOException {
			List<Element> notes = segment.getChildren("note"); //$NON-NLS-1$
			Iterator<Element> i = notes.iterator();
			while (i.hasNext()) {
				Element note = i.next();
				Vector<String> lines = splitLines(note.getText());
				Iterator<String> h = lines.iterator();
				while (h.hasNext()) {
					String comment = h.next();
					writeString("# " + comment.trim() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		/**
		 * Split lines.
		 * @param text
		 *            the text
		 * @return the vector< string>
		 */
		private Vector<String> splitLines(String text) {
			Vector<String> result = new Vector<String>();
			StringTokenizer tokenizer = new StringTokenizer(text, "\n"); //$NON-NLS-1$
			if (text.startsWith("\n\n")) { //$NON-NLS-1$
				result.add(""); //$NON-NLS-1$
			}
			while (tokenizer.hasMoreTokens()) {
				result.add(tokenizer.nextToken());
			}
			if (text.endsWith("\n\n")) { //$NON-NLS-1$
				result.add(""); //$NON-NLS-1$
			}
			tokenizer = null;
			return result;
		}

		/**
		 * Load segments.
		 * @throws SAXException
		 *             the SAX exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void loadSegments() throws SAXException, IOException {

			SAXBuilder builder = new SAXBuilder();

			Document doc = builder.build(xliffFile);
			Element root = doc.getRootElement();
			segments = new Hashtable<String, Element>();

			recurse(root);

		}

		/**
		 * Recurse.
		 * @param e
		 *            the e
		 */
		private void recurse(Element e) {
			List<Element> list = e.getChildren();
			Iterator<Element> i = list.iterator();
			while (i.hasNext()) {
				Element u = i.next();
				if (u.getName().equals("trans-unit")) { //$NON-NLS-1$
					segments.put(u.getAttributeValue("id"), u); //$NON-NLS-1$
				} else {
					recurse(u);
				}
			}
		}

		/**
		 * Write string.
		 * @param string
		 *            the string
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		private void writeString(String string) throws IOException {
			output.write(string.getBytes(encoding));
		}
	}

	/**
	 * Adds the quotes.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private static String addQuotes(String string) {
		return string.replaceAll("\n", "\"\n\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
