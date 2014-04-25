/**
 * Mif2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.mif;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.mif.common.MifParseException;
import net.heartsome.cat.converter.mif.common.UnSuportedFileExcetption;
import net.heartsome.cat.converter.mif.parser.XliffReader;
import net.heartsome.cat.converter.mif.preference.Constants;
import net.heartsome.cat.converter.mif.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * The Class Mif2Xliff.
 * @author John Zhu
 */
public class Mif2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "mif";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("mif.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "MIF to XLIFF Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Mif2XliffImpl converter = new Mif2XliffImpl();
//		String iniDir = args.get(Converter.ATTR_INIDIR);
//		String iniFile = iniDir + System.getProperty("file.separator") + "init_mif.xml";
//		args.put(Converter.ATTR_INI_FILE, iniFile);
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
	 * The Class Mif2XliffImpl.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	class Mif2XliffImpl {

		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			Map<String, String> result = new HashMap<String, String>();
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}

			String inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			String xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			String skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			String sourceLanguage = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			String encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

//			String iniFile = params.get(Converter.ATTR_INI_FILE);
//			if (iniFile == null || "".equals(iniFile)) {
//				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("mif.Mif2Xliff.msg1"));
//			}

			String catalogue = params.get(Converter.ATTR_CATALOGUE);
			String initSegmenter = params.get(Converter.ATTR_SRX);

			StringSegmenter segmenter = null;
			try {
				segmenter = new StringSegmenter(initSegmenter, sourceLanguage, catalogue);
			} catch (Exception e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("mif.mif2xliff.msg3") + e.getMessage(), e);
			}

			BufferedWriter xlfWriter = null;
			BufferedWriter sklOs = null;
			try {
				monitor.beginTask(Messages.getString("mif.Mif2Xliff.task1"), 4);

				xlfWriter = new BufferedWriter(new FileWriter(xliffFile));

				// generation the header of xliff file
				writeXliffHeader(xlfWriter, inputFile, sourceLanguage, skeletonFile, encoding);

				monitor.worked(1);
				monitor.setTaskName(Messages.getString("mif.Mif2Xliff.task2"));
				XliffReader r = new XliffReader(inputFile, encoding, sourceLanguage, segmenter);
				monitor.worked(1);

				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				r.readXliffFile(xlfWriter, /*iniFile*/null, !store.getBoolean(Constants.FRAMEMAKER_FILTER), new SubProgressMonitor(monitor, 1));

				// generation the end of xliff file
				writeXliffTail(xlfWriter);

				sklOs = new BufferedWriter(new FileWriter(skeletonFile));
				r.readSkeletonFile(sklOs, new SubProgressMonitor(monitor, 1));

			} catch (IOException e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("mif.Mif2Xliff.error1") + e.getMessage(), e);
			} catch (MifParseException e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("mif.Mif2Xliff.error2") + e.getMessage(), e);
			} catch (UnSuportedFileExcetption e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("mif.Mif2Xliff.error3") + e.getMessage(), e);
			} finally {
				
				try {
					if (xlfWriter != null) {
						xlfWriter.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (sklOs != null) {
						sklOs.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				monitor.done();
			}

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

			xlfWriter.write("<file original=\"" + TextUtil.cleanString(inputFile) + "\" source-language=\"" + srcLang + "\" datatype=\""
					+ TYPE_VALUE + "\">\n");

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