package net.heartsome.cat.converter.deja_vu_x2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.deja_vu_x2.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * Deja Vu X2 的双语文件的正向转换器。
 * @author robert	2012-007-09
 */
public class Du2Xliff implements Converter{
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "xlf";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.DU");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "DUXLIFF to XLIFF Conveter";
	
	public Map<String, String> convert(Map<String, String> args,
			IProgressMonitor monitor) throws ConverterException {
		Du2XliffImpl converter = new Du2XliffImpl();
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
	
	class Du2XliffImpl{
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
		/** 转换的编码格式 */
		private String srcEncoding;
		/** 将数据输出到XLIFF文件的输出流 */
		private FileOutputStream output;

		private boolean lockXtrans;
		private boolean lock100;
		
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
			
			lockXtrans = false;
			if (Converter.TRUE.equals(params.get(Converter.ATTR_LOCK_XTRANS))) {
				lockXtrans = true;
			}

			lock100 = false;
			if (Converter.TRUE.equals(params.get(Converter.ATTR_LOCK_100))) {
				lock100 = true;
			}
			
			try {
				output = new FileOutputStream(xliffFile);
				copyFile(inputFile, skeletonFile);
				parseSkeletonFile();
				writeHeader();
				analyzeNodes();
				
				writeString("</body>\n");
				writeString("</file>\n");
				writeString("</xliff>");
				
				sklXM.output(skeletonFile);
				
			}catch (Exception e) {
				e.printStackTrace();
				String errorTip = Messages.getString("du2Xlf.msg1") + "\n" + e.getMessage();
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e);
			}finally{
				try {
					output.close();
				} catch (Exception e2) {
					e2.printStackTrace();
					String errorTip = Messages.getString("du2Xlf.msg1") + "\n" + e2.getMessage();
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e2);
				}
				monitor.done();
			}
			
			return result;
		}
		
		
		/**
		 * 解析骨架文件，此时的骨架文件的内容就是源文件的内容
		 * @throws Exception 
		 */
		private void parseSkeletonFile() throws Exception{
			String errorInfo = "";
			VTDGen vg = new VTDGen();
			if (vg.parseFile(skeletonFile, true)) {
				sklVN = vg.getNav();
				sklXM = new XMLModifier(sklVN);
			}else {
				errorInfo = MessageFormat.format(Messages.getString("du.parse.msg1"), 
						new Object[]{new File(inputFile).getName()});
				throw new Exception(errorInfo);
			}
		}
		
		private void writeString(String string) throws IOException {
			output.write(string.getBytes("utf-8")); //$NON-NLS-1$
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
			writeString("<file original=\"" + TextUtil.cleanString(inputFile) + "\" source-language=\"" + userSourceLang); 
			writeString("\" target-language=\"" + ((targetLang == null || "".equals(targetLang)) ? userSourceLang : targetLang)); 
			writeString("\" datatype=\"" + TYPE_VALUE + "\">\n"); 
			writeString("<header>\n"); 
			writeString("   <skl>\n"); 
			String crc = ""; 
			if (isSuite) {
				crc = "crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("utf-8")) + "\""; 
			}
			writeString("      <external-file href=\"" + TextUtil.cleanString(skeletonFile) + "\" " + crc + "/>\n"); 
			writeString("   </skl>\n"); 
			writeString("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); 
			writeString("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" 
					+ srcEncoding + "</hs:prop></hs:prop-group>\n"); 
			writeString("</header>\n"); 
			writeString("<body>\n"); 
		}
		
		
		/**
		 * 分析每一个节点
		 * @throws Exception
		 */
		private void analyzeNodes() throws Exception{
			AutoPilot ap = new AutoPilot(sklVN);
			AutoPilot mrkAP = new AutoPilot(sklVN);
			VTDUtils vu = new VTDUtils(sklVN);
			String xpath = "/xliff/file/body//trans-unit";
			String srxMrkXpath = "./seg-source//mrk[@mtype=\"seg\"]";
			String tgtMrkXpath = "./target//mrk[@mtype=\"seg\"]";
			//存储源文的集合，key为mrk节点的mid。
			Map<String, TuMrkBean> srcMap = new LinkedHashMap<String, TuMrkBean>();
			//存储译文的集合，key为mrk节点的mid。
			Map<String, TuMrkBean> tgtMap = new LinkedHashMap<String, TuMrkBean>();
			//针对骨架文件的每一个源文节点的mrk保存占位符ID
			Map<String, Integer> segMap = new HashMap<String, Integer>();
			ap.selectXPath(xpath);
			int attrIdx = -1;
			//xliff 文件的 trans-unit 节点的 id 值
			int segId = 0;
			while (ap.evalXPath() != -1) {
				// 清除所有数据
				srcMap.clear();
				tgtMap.clear();
				sklVN.push();
				mrkAP.resetXPath();
				mrkAP.selectXPath(srxMrkXpath);
				while (mrkAP.evalXPath() != -1) {
					// 先获取出节点mrk的属性mid的值
					attrIdx = sklVN.getAttrVal("mid");
					String mid;
					if (attrIdx == -1 || "".equals(mid = sklVN.toString(attrIdx))) {
						continue;
					}
					String srcContent = vu.getElementContent();
					if (srcContent != null && !"".equals(srcContent)) {
						srcMap.put(mid, new TuMrkBean(mid, srcContent, null, true));
						//开始填充占位符
						insertPlaceHolder(vu, segId);
						segMap.put(mid, segId);
						segId ++;
					}
				}
				sklVN.pop();

				// 开始处理骨架文件的译文信息
				//这是判断状态是否为finish
				boolean isApproved = false;
				if ((attrIdx = sklVN.getAttrVal("approved")) != -1) {
					if ("yes".equals(sklVN.toString(attrIdx))) {
						isApproved = true;
					}
				}
				//是否锁定
				boolean isLocked = false;
				if ((attrIdx = sklVN.getAttrVal("translate")) != -1) {
					if ("no".equals(sklVN.toString(attrIdx))) {
						isLocked = true;
					}
				}
				
				sklVN.push();
				mrkAP.resetXPath();
				mrkAP.selectXPath(tgtMrkXpath);
				while (mrkAP.evalXPath() != -1) {
					attrIdx = sklVN.getAttrVal("mid");
					String mid;
					if (attrIdx == -1 || "".equals(mid = sklVN.toString(attrIdx))) {
						continue;
					}
					//注意两个填充占位符方法的位置不同。
					if (segMap.get(mid) != null) {
						insertPlaceHolder(vu, segMap.get(mid));
						TuMrkBean tgtBean = new TuMrkBean();
						tgtBean.setSource(false);
						tgtBean.setLocked(isLocked);
						
						String content = vu.getElementContent();
						if (content != null && !"".equals(content)) {
							tgtBean.setContent(content);
						}

						analysisTuMrkStatus(sklVN, vu, tgtBean, mid, isApproved);
						tgtMap.put(mid, tgtBean);
					}
				}
				
				//开始填充数据到XLIFF文件
				for(Entry<String, TuMrkBean> srcEntry : srcMap.entrySet()){
					String key = srcEntry.getKey();	//这个key是mid
					TuMrkBean srcBean = srcEntry.getValue();
					TuMrkBean tgtBean = tgtMap.get(key) == null ? new TuMrkBean() : tgtMap.get(key);
					int curSegId = segMap.get(key);
					
					writeSegment(srcBean, tgtBean, curSegId);
				}
				sklVN.pop();
			}
		}
		
		/**
		 * 分析每一个文本段的状态
		 * @param vn
		 * @param vu
		 * @param tgtBean
		 * @param mid	target节点下子节点mrk的mid属性，即唯一id值
		 */
		private void analysisTuMrkStatus(VTDNav vn, VTDUtils vu, TuMrkBean tgtBean, String mid, boolean isApproved) throws Exception {
			vn.push();
			String status = "";	//一个空字符串代表未翻译
			if (isApproved) {
				status = "finish";
			}else {
				String xpath = "ancestor::target";
				AutoPilot ap = new AutoPilot(vn);
				ap.selectXPath(xpath);
				if(ap.evalXPath() != -1){
					int attrIdx = -1;
					if ((attrIdx = vn.getAttrVal("state")) != -1) {
						status = vn.toString(attrIdx);
					}
				}
			}
			tgtBean.setStatus(status);
			vn.pop();
		}
		
		/**
		 * 给剔去翻译内容后的骨架文件填充占位符
		 * @throws Exception
		 */
		private void insertPlaceHolder(VTDUtils vu, int seg) throws Exception{
			String mrkHeader = vu.getElementHead();
			String newMrkStr = mrkHeader + "%%%"+ seg +"%%%" + "</mrk>";
			sklXM.remove();
			sklXM.insertAfterElement(newMrkStr.getBytes(srcEncoding));
		}
		
		/**
		 * 向XLIFF文件输入新生成的trans-unit节点
		 * @param srcContent
		 * @param tgtContent
		 * @param segId
		 * @throws Exception
		 */
		private void writeSegment(TuMrkBean srcBean, TuMrkBean tgtBean, int segId) throws Exception{
			String srcContent = srcBean.getContent();
			StringBuffer tuSB = new StringBuffer();
			String status = tgtBean.getStatus() == null ? "" : tgtBean.getStatus();
			
			String tgtStatusStr = "";
			String tuAttrStr = "";
			boolean isApproved = false;
			//具体的意思及与R8的转换请查看tgtBean.getStatus()的注释。
			if ("needs-translation".equals(status) || "".equals(status)) {
				if (tgtBean.getContent() != null && tgtBean.getContent().length() >= 1) {
					tgtStatusStr += " state=\"new\"";
				}
			}else if ("finish".equals(status)) {
				isApproved = true;
				tgtStatusStr += " state=\"translated\"";
			}else if ("needs-review-translation".equals(status)) {
				//疑问
				tuAttrStr += " hs:needs-review=\"yes\"";
			}
			tuAttrStr += isApproved ? " approved=\"yes\"" : "";
			tuAttrStr += tgtBean.isLocked() ? " translate=\"no\"" : "";
			
			tuSB.append("	<trans-unit" + tuAttrStr + " id=\"" + segId + "\" xml:space=\"preserve\" >\n");
			tuSB.append("		<source xml:lang=\"" + userSourceLang + "\">" + srcContent + "</source>\n");
			if (!tgtBean.isTextNull()) {
				String tgtContent = tgtBean.getContent();
				tuSB.append("		<target" + tgtStatusStr + " xml:lang=\"" + 
						((targetLang == null || "".equals(targetLang)) ? userSourceLang : targetLang) + "\">"
						+ tgtContent + "</target>\n");
			}
			tuSB.append("	</trans-unit>\n");
			writeString(tuSB.toString());
		}
	}
//----------------------------------------------------Du2XliffImpl 结束标志--------------------------------------------//
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
