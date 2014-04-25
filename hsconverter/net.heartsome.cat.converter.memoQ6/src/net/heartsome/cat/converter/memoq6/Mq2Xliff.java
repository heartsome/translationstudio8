package net.heartsome.cat.converter.memoq6;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.memoq6.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * menoQ 6.0 的转换器
 * @author robert 2012-07-20
 *
 */
public class Mq2Xliff implements Converter{
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "mqxlz";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.MQ");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "MQXLZ to XLIFF Conveter";
	
	public Map<String, String> convert(Map<String, String> args,
			IProgressMonitor monitor) throws ConverterException {
		Mq2XliffImpl converter = new Mq2XliffImpl();
		return converter.run(args, monitor);
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
	
	class Mq2XliffImpl{
		/** 源文件 */
		private String inputFile;
		/** 转换成的XLIFF文件（临时文件） */
		private String xliffFile;
		/** 骨架文件（临时文件） */
		private String skeletonFile;
		/** 源语言 */
		private String userSourceLang;
		/** 目标语言 */
		private String targetLang;
//		private String detectedSourceLang;
//		private String detectedTargetLang;
		/** 转换的编码格式 */
		private String srcEncoding;
//		private Element inputRoot;
//		private Document skeleton;
//		private Element skeletonRoot;
		/** 将数据输出到XLIFF文件的输出流 */
		private FileOutputStream output;
		private boolean isSuite;
		/** 转换工具的ID */
		private String qtToolID;
		/** 解析骨架文件的VTDNav实例 */
		private VTDNav sklVN;
		private XMLModifier sklXM;
		
		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			monitor.beginTask("Converting...", 5);
			Map<String, String> result = new HashMap<String, String>();
			
			inputFile = params.get(Converter.ATTR_SOURCE_FILE);
			xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			skeletonFile = params.get(Converter.ATTR_SKELETON_FILE);
			targetLang = params.get(Converter.ATTR_TARGET_LANGUAGE);
			userSourceLang = params.get(Converter.ATTR_SOURCE_LANGUAGE);
			srcEncoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			
			isSuite = false;
			if (Converter.TRUE.equalsIgnoreCase(params.get(Converter.ATTR_IS_SUITE))) {
				isSuite = true;
			}
			
			qtToolID = params.get(Converter.ATTR_QT_TOOLID) != null ? params.get(Converter.ATTR_QT_TOOLID)
					: Converter.QT_TOOLID_DEFAULT_VALUE;
			
			try {
				output = new FileOutputStream(xliffFile);
				parseMQZip(inputFile, skeletonFile);
//				copyFile(skeletonFile, "C:\\Users\\Administrator\\Desktop\\test.xml");
				//重新解析
				parseHSSkeletonFile();
				//先写下头文件
				writeHeader();
				analyzeNodes();
				
				writeString("</body>\n");
				writeString("</file>\n");
				writeString("</xliff>");

				sklXM.output(skeletonFile);
				
//				String file1 = "/home/robert/Desktop/file1.txt";
//				String file2 = "/home/robert/Desktop/file2.txt";
//				copyFile(skeletonFile, file1);
//				copyFile(xliffFile, file2);
			} catch (Exception e) {
				e.printStackTrace();
				String errorTip = Messages.getString("mq2Xlf.msg1");
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e);
			}finally{
				try {
					output.close();
				} catch (Exception e2) {
					e2.printStackTrace();
					String errorTip = Messages.getString("mq2Xlf.msg1") + "\n" + e2.getMessage();
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e2);
				}
				monitor.done();
			}

			return result;
		}
		
		/**
		 * 解析 memoQ 的源文件，并将内容拷贝至骨架文件中
		 * @param mqZip
		 * @param hsSkeleton	R8 hsxliff的骨架文件
		 * @throws Exception
		 */
		private void parseMQZip(String mqZip, String hsSkeleton) throws Exception{
			ZipFile zipFile = new ZipFile(new File(mqZip), "utf-8");
			Enumeration<?> e = zipFile.getEntries();
			byte ch[] = new byte[1024];
			String outputFile = "";
			File mqSklTempFile = File.createTempFile("tempskl", "skl");
			mqSklTempFile.deleteOnExit();
			while (e.hasMoreElements()) {
				ZipArchiveEntry zipEntry = (ZipArchiveEntry) e.nextElement();
				if ("document.mqxliff".equals(zipEntry.getName())) {
					outputFile = hsSkeleton;
				}else {
					outputFile = mqSklTempFile.getAbsolutePath();
				}
				File zfile = new File(outputFile);
				FileOutputStream fouts = new FileOutputStream(zfile);
				InputStream in = zipFile.getInputStream(zipEntry);
				int i;
				while ((i = in.read(ch)) != -1)
					fouts.write(ch, 0, i);
				fouts.close();
				in.close();
			}
			
			//解析r8骨加文件，并把 mq 的骨架信息添加到 r8 的骨架文件中
			parseHSSkeletonFile();
			copyMqSklToHsSkl(mqSklTempFile);
		}

		/**
		 * 解析R8 的骨架文件
		 * @throws Exception
		 */
		private void parseHSSkeletonFile() throws Exception {
			String errorInfo = "";
			VTDGen vg = new VTDGen();
			if (vg.parseFile(skeletonFile, true)) {
				sklVN = vg.getNav();
				sklXM = new XMLModifier(sklVN);
			}else {
				errorInfo = MessageFormat.format(Messages.getString("mq.parse.msg1"), 
						new Object[]{new File(inputFile).getName()});
				throw new Exception(errorInfo);
			}
		}
		
		/**
		 * 将mq 的骨架文件拷到R8 的骨架文件中
		 * @throws Exception
		 */
		private void copyMqSklToHsSkl(File mqSkeletonFile) throws Exception {
			VTDGen vg = new VTDGen();
			AutoPilot ap = new AutoPilot();
			String mqSklContent = "";
			String xpath = "/mq:externalparts";
			if(vg.parseFile(mqSkeletonFile.getAbsolutePath(), true)){
				VTDNav vn = vg.getNav();
				ap.bind(vn);
				VTDUtils vu = new VTDUtils(vn);
				ap.declareXPathNameSpace("mq", "MemoQ Xliff external parts");
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					mqSklContent = vu.getElementContent();
				}
			}
			
			//下面添加到 r8 的骨架文件中去
			ap.bind(sklVN);
			xpath = "/xliff/file/header/skl";
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				sklXM.insertAfterElement("<sklContent>" + mqSklContent + "</sklContent>");
				sklXM.output(skeletonFile);
			}
		}
		
		/**
		 * 写下XLIFF文件的头节点
		 */
		private void writeHeader() throws IOException {
			writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); 
			writeString("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + 
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " 
					+ "xmlns:hs=\"" 
					+ Converter.HSNAMESPACE + "\" "
					+ "xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd " 
					+ Converter.HSSCHEMALOCATION + "\">\n"); 
			writeString("<file original=\"" + TextUtil.cleanSpecialString(inputFile) + "\" source-language=\"" + userSourceLang); 
			writeString("\" target-language=\"" + ((targetLang == null || "".equals(targetLang)) ? userSourceLang : targetLang)); 
			writeString("\" datatype=\"" + TYPE_VALUE + "\">\n"); 
			writeString("<header>\n"); 
			writeString("   <skl>\n"); 
			String crc = ""; 
			if (isSuite) {
				crc = "crc=\"" + CRC16.crc16(TextUtil.cleanSpecialString(skeletonFile).getBytes("utf-8")) + "\""; 
			}
			writeString("      <external-file href=\"" + TextUtil.cleanSpecialString(skeletonFile) + "\" " + crc + "/>\n"); 
			writeString("   </skl>\n"); 
			writeString("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); 
			writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" 
					+ srcEncoding + "</hs:prop></hs:prop-group>\n"); 
			writeString("</header>\n"); 
			writeString("<body>\n"); 
		}
		
		private void writeString(String string) throws IOException {
			output.write(string.getBytes("utf-8")); //$NON-NLS-1$
		}
		
		/**
		 * 分析每一个节点
		 * @throws Exception
		 */
		private void analyzeNodes() throws Exception{
			AutoPilot ap = new AutoPilot(sklVN);
			AutoPilot childAP = new AutoPilot(sklVN);
			VTDUtils vu = new VTDUtils(sklVN);
			String xpath = "/xliff/file/body//trans-unit[source/text()!='' or source/*]";	///seg-source/mrk[@mtype=\"seg\"]
			String srxXpath = "./source";
			String tgtXpath = "./target";
			//获取每个节点的内容
			TuBean bean = null;
			ap.selectXPath(xpath);
			int index = -1;
			//xliff 文件的 trans-unit 节点的 id 值
			int segId = 0;
			while (ap.evalXPath() != -1) {
				bean = new TuBean();
				sklVN.push();
				childAP.selectXPath(srxXpath);
				while (childAP.evalXPath() != -1) {
					String srcContent = vu.getElementContent();
					if (srcContent != null && !"".equals(srcContent)) {
						bean.setSrcText(TextUtil.cleanSpecialString(srcContent));
						//开始填充占位符
						insertPlaceHolder(vu, "" + segId, "source");
						bean.setSegId("" + segId);
						segId ++;
					}
				}
				sklVN.pop();

				// 开始处理骨架文件的译文信息
				sklVN.push();
				childAP.selectXPath(tgtXpath);
				while (childAP.evalXPath() != -1) {
					if (bean.getSegId() == null || "".equals(bean.getSegId())) {
						continue;
					}
					//注意两个填充占位符方法的位置不同。
					String tgtContent = vu.getElementContent();
					insertPlaceHolder(vu, bean.getSegId(), "target");
					bean.setTgtText(TextUtil.cleanSpecialString(tgtContent));
				}
				
				if ((index = sklVN.getAttrVal("mq:status")) != -1) {
					String status = sklVN.toString(index);
					bean.setStatus(status);
				}
				if ((index = sklVN.getAttrVal("mq:locked")) != -1) {
					bean.setLocked("locked".equals(sklVN.toString(index)));
				}
				
				//开始填充数据到XLIFF文件
				writeSegment(bean);
				sklVN.pop();
			}
		}
		
		
		/**
		 * 给剔去翻译内容后的骨架文件填充占位符
		 * @throws Exception
		 */
		private void insertPlaceHolder(VTDUtils vu, String seg, String nodeName) throws Exception{
			String nodeHeader = vu.getElementHead();
			String newNodeStr = nodeHeader + "%%%" + seg + "%%%" + "</" + nodeName + ">";
			sklXM.remove();
			sklXM.insertAfterElement(newNodeStr);
		}
		
		
		private void writeSegment(TuBean bean) throws Exception {
			String srcContent = bean.getSrcText();
			StringBuffer tuSB = new StringBuffer();
			String status = bean.getStatus();
			
			String tgtStatusStr = "";
			boolean isApproved = false;
			//NotStarted 为未翻译
			//具体的意思及与R8的转换请查看tgtBean.getStatus()的注释。
			if ("PartiallyEdited".equals(status)) {
				tgtStatusStr += " state=\"new\"";
			}else if ("ManuallyConfirmed".equals(status)) {
				isApproved = true;
				tgtStatusStr += " state=\"translated\"";
			}
			String approveStr = isApproved ? " approved=\"yes\"" : "";
			//是否锁定
			String lockStr = bean.isLocked() ? " translate=\"no\"" : "";
			
			tuSB.append("	<trans-unit" + lockStr + approveStr + " id=\"" + bean.getSegId() + "\" xml:space=\"preserve\" >\n");
			tuSB.append("		<source xml:lang=\"" + userSourceLang + "\">" + srcContent + "</source>\n");
			if (!bean.isTgtNull()) {
				String tgtContent = bean.getTgtText();
				tuSB.append("		<target" + tgtStatusStr + " xml:lang=\"" + 
						((targetLang == null || "".equals(targetLang)) ? userSourceLang : targetLang) + "\">"
						+ tgtContent + "</target>\n");
			}
			//添加备注信息
			if (bean.getNote() != null && "".equals(bean.getNote().trim())) {
				//这是R8的标注格式：<note from='robert'>2012-03-06:asdf</note>
				tuSB.append("<note from=''>" + bean.getNote() + "</note>");
			}
			tuSB.append("	</trans-unit>\n");
			writeString(tuSB.toString());
		}
	}
	
	/**
	 * 将一个文件的数据复制到另一个文件
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	private static void copyFile(String in, String out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}
}
