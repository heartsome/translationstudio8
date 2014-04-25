/**
 * MSOffice2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.word2007;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.word2007.common.DocxConverterException;
import net.heartsome.cat.converter.word2007.common.PathConstant;
import net.heartsome.cat.converter.word2007.common.PathUtil;
import net.heartsome.cat.converter.word2007.common.ZipUtil;
import net.heartsome.cat.converter.word2007.partOper.DocumentPart;
import net.heartsome.cat.converter.word2007.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * The Class MSOffice2Xliff.
 * @author robert	2012-08-21
 * @version
 * @since JDK1.6
 */
public class Docx2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-msofficeWord2007";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.MSWORD2007");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "MS Office Word 2007 to XLIFF Conveter";

	private static final Logger LOGGER = LoggerFactory.getLogger(Docx2Xliff.class);

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		MSOffice2XliffImpl converter = new MSOffice2XliffImpl();
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
	 * The Class MSOffice2XliffImpl.
	 * @author robert
	 * @version
	 * @since JDK1.6
	 */
	class MSOffice2XliffImpl {

		/** The qt tool id. */
		private String qtToolID = null;

		/** The input file. */
		private String inputFile;

		/** The xliff file. */
		private String xliffFile;

		/** The skeleton file. */
		private String skeletonFile;

		/** The source language. */
		private String sourceLanguage;

		/** The XLIFF file target language **/
		private String targetLanguage;

		/** The catalogue. */
		private String catalogue;

		/** The src encoding. */
		private String srcEncoding;

		/** The is suite. */
		private boolean isSuite;

		/** The srx. */
		private String srx;
		private XliffOutputer xlfOutput;

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
			// 给 20 个进度格，解压加压各 1 个。处理页眉页脚共 1 个。其他 17 个处理主文档
			monitor.beginTask("", 20);
			
			Map<String, String> result = new HashMap<String, String>();

			inputFile = args.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = args.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = args.get(Converter.ATTR_SKELETON_FILE);
			isSuite = false;
			if (Converter.TRUE.equals(args.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}
			qtToolID = args.get(Converter.ATTR_QT_TOOLID) != null ? args.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;

			sourceLanguage = args.get(Converter.ATTR_SOURCE_LANGUAGE);
			targetLanguage = args.get(Converter.ATTR_TARGET_LANGUAGE);
			catalogue = args.get(Converter.ATTR_CATALOGUE);
			srx = args.get(Converter.ATTR_SRX);
			srcEncoding = args.get(Converter.ATTR_SOURCE_ENCODING);
			PathUtil pathUtil = null;
			String docxFolderPath = "";	// docx 文件解压后的存放的临时目录
			try {

				StringSegmenter segmenter = new StringSegmenter(srx, sourceLanguage, catalogue);
				
				// 先解压 docx 文件
				monitor.setTaskName(Messages.getString("docxConvert.task1"));
				String tempFolder = System.getProperty("java.io.tmpdir")
						+ System.getProperty("file.separator") + new File(inputFile).getName();
				docxFolderPath = ZipUtil.upZipFile(inputFile, tempFolder);
				pathUtil = new PathUtil(docxFolderPath);
				monitor.worked(1);
				
				// 定义一个 hsxliff 文件的写入方法
				xlfOutput = new XliffOutputer(xliffFile, sourceLanguage, targetLanguage);
				xlfOutput.writeHeader(inputFile, skeletonFile, isSuite, "UTF-8", qtToolID);
//				System.out.println(skeletonFile);
//				System.out.println(tempFolder);
				
				// 先从主文档 document 文件入手
				String docPath = pathUtil.getPackageFilePath(PathConstant.DOCUMENT, false);
				DocumentPart docPart = new DocumentPart(docPath, pathUtil, xlfOutput, segmenter, inputFile, monitor);
				docPart.clearNoUseNodeAndDealBR();
				docPart.converter();
				
				xlfOutput.outputEndTag();
				idealizeGTag(xliffFile, pathUtil.getInterTagPath());
				
				
				// 开始将文件进行压缩
				monitor.setTaskName(Messages.getString("docx2Xlf.task4"));
				ZipUtil.zipFolder(skeletonFile, pathUtil.getSuperRoot());
				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("docxConvert.task3"));
				}
			} catch (DocxConverterException e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, e.getMessage(), e);
			} catch (OperationCanceledException e) {
				throw e;
			}catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				LOGGER.error(Messages.getString("docx2Xlf.msg1"), e);
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, 
						Messages.getString("docx2Xlf.msg1") + "\n" + e.getMessage(), e);
				
			} finally {
				deleteFileOrFolder(new File(docxFolderPath));
				monitor.done();
				try {
					if (xlfOutput != null) {
						xlfOutput.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
	
	/**
	 * 简化 xliff 文件的标记，主要功能是将一个源文中的 g 标记进行抽取到骨架的操作。针对一个源文中只有一个 g 标记，并且该 g 标记包褒全文本段
	 * 生成一个 名为 interTag.xml 的文件，存放于骨架文件的第一级子目录，与 word 文件夹同目录
	 * 其结构大致为<br>
	 * &lt;docxTags&gt;<br>
	 * 		&lt;tag tuId="0" &gt;this is a tag&lt;/tag&gt;<br>
	 * &lt;/docxTags&gt;<br>
	 * <div style="color:red">备注：interTag.xml 介绍： 此文件并非 docx 的内部文件，而是保存转换 docx 文件时的部份 g标记(源文中只有一对 g 标记，并且是它包褒一整个文本段)</div>
	 */
	private static void idealizeGTag(String xliffPath, String interTagPath) throws Exception{
		final String constantGHeader = "<g";
		final String constantGEnd = "</g>";
		
		VTDGen vg = new VTDGen();
		if (!vg.parseFile(xliffPath, true)) {
			throw new Exception();
		}
		
		VTDNav vn = vg.getNav();
		String xpath = "/xliff/file/body/descendant::trans-unit[source/text()!=''  or source/*]";
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot childAP = new AutoPilot(vn);
		VTDUtils vu = new VTDUtils(vn);
		XMLModifier xm = new XMLModifier(vn);
		ap.selectXPath(xpath);
		int index = -1;
		String id = null;
		
		StringBuffer tagContentSB = new StringBuffer();
		while(ap.evalXPath() != -1){
			id = null;
			index = vn.getAttrVal("id");
			if (index != -1) {
				id = vn.toString(index);
			}
			if (id == null) {
				vn.pop();
				continue;
			}
			
			vn.push();
			childAP.selectXPath("./source");
			if (childAP.evalXPath() == -1) {
				vn.pop();
				continue;
			}
			String srcText = vu.getElementContent();
			childAP.selectXPath("count(./g)");
			// 如果 g 标签个数为 1 ,并且包褒整个文本段，那么便可进行清理
			if (childAP.evalXPathToNumber() == 1) {
				if (srcText.indexOf(constantGHeader) == 0 && srcText.indexOf(constantGEnd) == (srcText.length() - 4)) {
					childAP.selectXPath("./g");
					if (childAP.evalXPath() != -1) {
						String header = vu.getElementHead();
						String content = vu.getElementContent();
						// 删除 g 标记
						xm.remove();
						xm.insertAfterElement(content);
						// 将删除的 g 标记保存至 interTag.xml 文件中
						tagContentSB.append("\t<tag tuId=\"" + id + "\">" + header + "</g>" + "</tag>\n");
					}
				}
			}
			vn.pop();
		}
		
		xm.output(xliffPath);
		
		if (tagContentSB.length() > 0) {
			// 开始创建 interTag.xml 文件
			File file = new File(interTagPath);
			if (!file.exists()) {
				FileOutputStream output;
				output = new FileOutputStream(interTagPath);
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
				output.write("<docxTags>\n".getBytes("UTF-8"));
				output.write(tagContentSB.toString().getBytes("UTF-8"));
				output.write("</docxTags>".getBytes("UTF-8"));
				output.close();
			}
		}
	}
	
	public static void main(String[] args) {
		Docx2Xliff docx2Xliff = new Docx2Xliff();
		String xliffPath = "/home/robert/Desktop/Heartsome Support Revised_转换后少一个字.docx.hsxliff";
		try {
			docx2Xliff.idealizeGTag(xliffPath, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		final String constantGHeader = "<g";
//		final String constantGEnd = "</g>";
//		String srcText = "<g  rPr='&lt;w:rPr&gt;&lt;w:rFonts w:hint=&quot;eastAsia&quot;/&gt;&lt;w:kern w:val=&quot;0&quot;/&gt;&lt;/w:rPr&gt;'>Heartsome 技术支持服务</g>";
//		
//		if (srcText.indexOf(constantGHeader) == 0 && srcText.indexOf(constantGEnd) == (srcText.length() - 4)) {
//			System.out.println("这是正确的。");
//		}
	}

		
}
