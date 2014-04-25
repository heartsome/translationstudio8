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

import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.reader.XliffReader;
import net.heartsome.cat.converter.util.ConverterUtils;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class Xliff2MsExcel implements Converter {
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "Microsoft Office Excel 2007";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = "Microsoft Office Excel 2007";

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to MS Excel 2007 Conveter";

	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2MsExcelImpl impl = new Xliff2MsExcelImpl();
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

	class Xliff2MsExcelImpl {
		// skeleton 文件编码
		// private String encoding;
		// private FileOutputStream output;

		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			Map<String, String> result = new HashMap<String, String>();

			String sklFile = params.get(Converter.ATTR_SKELETON_FILE);
			String xliffFile = params.get(Converter.ATTR_XLIFF_FILE);

			String outputFile = params.get(Converter.ATTR_TARGET_FILE);
			// encoding = params.get(Converter.ATTR_SOURCE_ENCODING);

			XliffReader reader = new XliffReader();
			try {
				reader.read2SpreadsheetDoc(outputFile, xliffFile, sklFile, monitor);
				result.put(Converter.ATTR_TARGET_FILE, outputFile);
			} catch (InternalFileException e) {
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, e.getMessage(), e);
			}

			return result;
		}
	}
}
