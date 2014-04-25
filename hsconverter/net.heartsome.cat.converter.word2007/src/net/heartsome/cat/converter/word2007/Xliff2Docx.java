/**
 * Xliff2MSOffice.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.word2007;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.cat.converter.word2007.common.PathConstant;
import net.heartsome.cat.converter.word2007.common.PathUtil;
import net.heartsome.cat.converter.word2007.common.ZipUtil;
import net.heartsome.cat.converter.word2007.partOper.DocumentPart;
import net.heartsome.cat.converter.word2007.partOper.DocumentRelation;
import net.heartsome.cat.converter.word2007.resource.Messages;
import net.heartsome.util.CommonFunctions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Xliff2MSOffice.
 * @author robert	2012-08-20
 * @version
 * @since JDK1.6
 */
public class Xliff2Docx implements Converter {

	public static final Logger LOGGER = LoggerFactory.getLogger(Xliff2Docx.class);

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-msofficeWord2007";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.MSWORD2007");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to MS Office Word 2007 Conveter";


	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2DocxImpl converter = new Xliff2DocxImpl();
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
	 * The Class Xliff2MSOfficeImpl.
	 * @author robert	2012-08-20
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2DocxImpl {

		/** The catalogue. */
		private PathUtil pathUtil;

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
		public Map<String, String> run(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();
			Map<String, String> result = new HashMap<String, String>();
			// 备注，这个 xliffFile 文件是原xliff，不做任何修改，要执行修改的是下面的 tempXLiffFile
			String xliffFile = args.get(Converter.ATTR_XLIFF_FILE);
			String outputFile = args.get(Converter.ATTR_TARGET_FILE);
			String skeleton = args.get(Converter.ATTR_SKELETON_FILE);
			
			try {
				File tempXLiffFile = File.createTempFile("tempxliff", "hsxliff");
				tempXLiffFile.deleteOnExit();
				CommonFunctions.copyFile(new File(xliffFile), tempXLiffFile);
				// 把转换过程分为三部分共 20 个任务，解压压缩各1个，页眉，页脚，批注，脚注，尾注各1个，其余 13 个为处理主文档
				monitor.beginTask("", 20);
//				infoLogger.logConversionFileInfo(catalogue, null, xliffFile, null);
//				IProgressMonitor separateMonitor = Progress.getSubMonitor(monitor, 8);
//				long startTime = 0;
				
//				LOGGER.info(Messages.getString("msoffice2007.Xliff2MSOffice.logger1"), startTime);
				
				monitor.setTaskName(Messages.getString("xlf2Docx.task6"));
				String tempFolder = System.getProperty("java.io.tmpdir")
						+ System.getProperty("file.separator") + new File(skeleton).getName();
				String docxFolderPath = ZipUtil.upZipFile(skeleton, tempFolder);
				pathUtil = new PathUtil(docxFolderPath);
				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("docxConvert.task3"));
				}
				
				// 定义一个 hsxliff 的读入器
				XliffInputer xlfInput = new XliffInputer(tempXLiffFile.getAbsolutePath(), pathUtil);
				
				// 正转换是从 主文档入手的，而逆转换则是从 word/_rels/document.xml.rels 入手，先处理掉 页眉，页脚，脚注，批注，尾注
				String docRelsPath = pathUtil.getPackageFilePath(PathConstant.DOCUMENTRELS, false);
				DocumentRelation docRels = new DocumentRelation(docRelsPath, pathUtil);
				docRels.arrangeRelations(xlfInput, monitor);	// 这里用掉 5 个格子
				
				// 再处理主文档
				pathUtil.setSuperRoot();
				String docPath = pathUtil.getPackageFilePath(PathConstant.DOCUMENT, false);
				DocumentPart documentPart = new DocumentPart(docPath, xlfInput, monitor);
				documentPart.reverseConvert();
				
				monitor.setTaskName(Messages.getString("xlf2Docx.task6"));
				ZipUtil.zipFolder(outputFile, pathUtil.getSuperRoot());
				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("docxConvert.task3"));
				}
			} catch (OperationCanceledException e) {
				throw e;
			}catch (Exception e) {
				e.printStackTrace();
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID,
						Messages.getString("xlf2Docx.msg1"), e);
			} finally {
				deleteFileOrFolder(new File(pathUtil.getSuperRoot()));
				monitor.done();
			}
			infoLogger.endConversion();
			result.put(Converter.ATTR_TARGET_FILE, outputFile);
			return result;
		}
	}
	
	public static void deleteFileOrFolder(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFileOrFolder(files[i]);
				}
			}
			file.delete();
		}
	}
}
