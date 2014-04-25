/**
 * MsExcel2Xliff.java
 *
 * Version information :
 *
 * Date:2012-8-9
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.preference.Constants;
import net.heartsome.cat.converter.msexcel2007.reader.SpreadsheetReader;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class MsExcel2Xliff implements Converter {
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "Microsoft Office Excel 2007";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("msexcel.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "MS Excel 2007 to XLIFF Conveter";

	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		MsExcel2XliffImpl impl = new MsExcel2XliffImpl();
		return impl.run(args, monitor);
	}

	public String getName() {
		return NAME_VALUE;
	}

	public String getType() {
		return TYPE_VALUE;
	}

	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}

	class MsExcel2XliffImpl {

		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(Messages.getString("msexcel.mse2xliff.task1"), 4);

			Map<String, String> result = new HashMap<String, String>();
			String inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			String xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			String skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			String sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			String encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

			// load segment
			monitor.worked(1);
			String catalogue = params.get(Converter.ATTR_CATALOGUE);
			String initSegmenter = params.get(Converter.ATTR_SRX);
			StringSegmenter segmenter = null;
			try {
				segmenter = new StringSegmenter(initSegmenter, sourceLanguage, catalogue);
			} catch (Exception e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
						Messages.getString("msexcel.mse2xliff.msg1") + e.getMessage(), e);
			}

			BufferedWriter xlfWriter = null;
			SpreadsheetReader reader = new SpreadsheetReader(sourceLanguage, segmenter);
			try {
				xlfWriter = new BufferedWriter(new FileWriter(xliffFile));

				// generation the header of xliff file
				writeXliffHeader(xlfWriter, inputFile, sourceLanguage, skeletonFile, encoding);

				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				reader.read2XliffFile(inputFile, xlfWriter, skeletonFile, store.getBoolean(Constants.EXCEL_FILTER),
						new SubProgressMonitor(monitor, 3));

				// generation the end of xliff file
				writeXliffTail(xlfWriter);
			} catch (IOException e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
						Messages.getString("msexcel.converter.exception.msg2") + e.getMessage(), e);
			} catch (InternalFileException e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, e.getMessage(), e);
			} catch (Exception e) {
				String msg = e.getMessage() == null ? "" : e.getMessage();
				ConverterUtils
						.throwConverterException(
								Activator.PLUGIN_ID,
								MessageFormat.format(Messages.getString("msexcel.converter.exception.msg2"),
										msg), e);
			} finally {
				if (xlfWriter != null) {
					try {
						xlfWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			monitor.done();
			result.put(Converter.ATTR_XLIFF_FILE, xliffFile);
			return result;
		}

		private void writeXliffHeader(BufferedWriter xlfWriter, String inputFile, String srcLang, String sklFile,
				String encoding) throws IOException {
			xlfWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			xlfWriter.write("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" "
					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\""
					+ Converter.HSNAMESPACE + "\" "
					+ "xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd "
					+ Converter.HSSCHEMALOCATION + "\">\n");

			xlfWriter.write("<file original=\"" + TextUtil.cleanString(inputFile) + "\" source-language=\"" + srcLang
					+ "\" datatype=\"" + TYPE_VALUE + "\">\n");

			xlfWriter.write("<header>\n");
			xlfWriter.write("   <skl>\n");
			xlfWriter.write("      <external-file href=\"" + TextUtil.cleanString(sklFile) + "\"/>\n"); //$NON-NLS-2$ //$NON-NLS-3$
			xlfWriter.write("   </skl>\n");
			xlfWriter
					.write("   <tool tool-id=\"" + Converter.QT_TOOLID_DEFAULT_VALUE + "\" tool-name=\"HSStudio\"/>\n"); //$NON-NLS-2$
			xlfWriter.write("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" + encoding
					+ "</hs:prop></hs:prop-group>\n");
			xlfWriter.write("</header>\n");
			xlfWriter.write("<body>\n");
		}

		private void writeXliffTail(BufferedWriter xlfWriter) throws IOException {
			xlfWriter.write("</body>\n");
			xlfWriter.write("</file>\n");
			xlfWriter.write("</xliff>");
		}

	}
}
