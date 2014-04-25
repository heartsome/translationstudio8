package net.heartsome.cat.converter.trados2009;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.trados2009.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.vtdimpl.VTDLoader;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * trados 2009 的sdlXliff文件转换成R8的xliff文件
 * @author robert	2012-06-27
 */
public class Sdl2Xliff implements Converter{
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "sdlxliff";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.SDL");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "SDLXLIFF to XLIFF Conveter";

	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Sdl2XliffImpl converter = new Sdl2XliffImpl();
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
	
	
	
	class Sdl2XliffImpl{
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
//		private StringSegmenter segmenter;
//		/** xliff 文件的 trans-unit 节点的 id 值 */
//		private int segId;
		/** sdl文件的全局批注 */
		private List<CommentBean> fileCommentsList = new LinkedList<CommentBean>();

		private int tagId;

		private String catalogue;

		private boolean ignoreTags;

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
			catalogue = params.get(Converter.ATTR_CATALOGUE);
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
				getFileComments();
				//先写下头文件
				writeHeader();
				analyzeNodes();
				
				writeString("</body>\n");
				writeString("</file>\n");
				writeString("</xliff>");

				//为了逆转换时的方便，此时删除所有的批注
				deleteSklComments();
				sklXM.output(skeletonFile);
				
//				String file1 = "/home/robert/Desktop/file1.txt";
//				String file2 = "/home/robert/Desktop/file2.txt";
//				copyFile(skeletonFile, file1);
//				copyFile(xliffFile, file2);
			} catch (Exception e) {
				e.printStackTrace();
				String errorTip = Messages.getString("sdl2Xlf.msg1") + "\n" + e.getMessage();
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e);
			}finally{
				try {
					if (output != null) {
						output.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
					String errorTip = Messages.getString("sdl2Xlf.msg1") + "\n" + e2.getMessage();
					ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e2);
				}
				monitor.done();
			}
			return result;
		}
		
		/**
		 * 写下XLIFF文件的头节点
		 */
		private void writeHeader() throws IOException {
			writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); 
			writeString("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + 
					"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " 
					+ "xmlns:sdl=\"http://sdl.com/FileTypes/SdlXliff/1.0\" " 
					+ "xmlns:hs=\"" + Converter.HSNAMESPACE + "\" "
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
		
		private void writeString(String string) throws IOException {
			output.write(string.getBytes("utf-8")); //$NON-NLS-1$
		}
		
		/**
		 * 解析骨架文件，此时的骨架文件的内容就是源文件的内容
		 * @throws Exception 
		 */
		private void parseSkeletonFile() throws Exception{
//			String errorInfo = "";
			VTDGen vg = VTDLoader.loadVTDGen(new File(skeletonFile), "UTF-8");
//			if (vg.parseFile(skeletonFile, true)) {
				sklVN = vg.getNav();
				sklXM = new XMLModifier(sklVN);
//			}else {
//				errorInfo = MessageFormat.format(Messages.getString("sdl.parse.msg1"), 
//						new Object[]{new File(inputFile).getName()});
//				throw new Exception(errorInfo);
//			}
		}
		
		
		/**
		 * 分析每一个节点
		 * @throws Exception
		 */
		private void analyzeNodes() throws Exception{
			AutoPilot ap = new AutoPilot(sklVN);
			AutoPilot mrkAP = new AutoPilot(sklVN);
			VTDUtils vu = new VTDUtils(sklVN);
			String xpath = "/xliff/file/body//trans-unit[not(@translate='no')]";	///seg-source/mrk[@mtype=\"seg\"]
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
						srcMap.put(mid, new TuMrkBean(mid, srcContent, null, null, true));
						//开始填充占位符
						insertPlaceHolder(vu, segId);
						segMap.put(mid, segId);
						segId ++;
					}
				}
				sklVN.pop();

				// 开始处理骨架文件的译文信息
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
						anysisTuMrkNode(sklVN, vu, tgtBean);
						anysisTuMrkStatus(sklVN, vu, tgtBean, mid);
						tgtBean.addFileComments(fileCommentsList);
						tgtMap.put(mid, tgtBean);
					}
				}
				
				//UNDO 这里还没有处理目标节点mrk的mid值与源文相比遗失的情况	2012-06-29
				
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
			String status = tgtBean.getStatus();
			
			String tgtStatusStr = "";
			boolean isApproved = false;
//			具体的意思及与R8的转换请查看tgtBean.getStatus()的注释。
			if ("Draft".equals(status)) {
				tgtStatusStr += " state=\"new\"";
			}else if ("Translated".equals(status)) {
				tgtStatusStr += " state=\"translated\"";
			}else if ("RejectedTranslation".equals(status)) {
				tgtStatusStr += " state=\"new\"";
			}else if ("ApprovedTranslation".equals(status)) {
				isApproved = true;
				tgtStatusStr += " state=\"translated\"";
			}else if ("RejectedSignOff".equals(status)) {
				isApproved = true;
				tgtStatusStr += " state=\"translated\"";
			}else if ("ApprovedSignOff".equals(status)) {
				isApproved = true;
				tgtStatusStr += " state=\"signed-off\"";
			}
			String strMatchType = null;
			String strQuality = null;
			strMatchType = tgtBean.getMatchType() == null ? "" : " hs:matchType=\"".concat(tgtBean.getMatchType()).concat("\"");
			strQuality = tgtBean.getQuality() == null ? "" : " hs:quality=\"".concat(tgtBean.getQuality()).concat("\"");
			String approveStr = isApproved ? " approved=\"yes\"" : "";
			//是否锁定
			String lockStr = tgtBean.isLocked() ? " translate=\"no\"" : "";
			
			
			tuSB.append("	<trans-unit" + lockStr + approveStr + " id=\"" + segId + "\" xml:space=\"preserve\" >\n");
			tuSB.append("		<source xml:lang=\"" + userSourceLang + "\">" + srcContent + "</source>\n");
			if (!tgtBean.isTextNull()) {
				String tgtContent = tgtBean.getContent();
				tuSB.append("<target").append(tgtStatusStr).append(" xml:lang=\"").append((targetLang == null || "".equals(targetLang)) ? userSourceLang : targetLang).append("\"")
					.append(strMatchType).append(strQuality).append(">").append(tgtContent).append("</target>\n");
			}
			//添加备注信息
			if (tgtBean.getCommentList() != null && tgtBean.getCommentList().size() > 0) {
				//这是R8的标注格式：<note from='robert'>2012-03-06:asdf</note>
				for(CommentBean cBean : tgtBean.getCommentList()){
					tuSB.append("<note from='" + cBean.getUser() + "'"+ (cBean.isCurrent() ? "" : " hs:apply-current='No'") 
							+ ">" + cBean.getR8NoteText() + "</note>");
				}
			}
			tuSB.append("	</trans-unit>\n");
			writeString(tuSB.toString());
		}
		
		/**
		 * 获取sdlXliff文件的tu节点下的源文或译文，主要是分析mrk节点下的内容
		 * @param vu
		 * @return
		 */
		private void anysisTuMrkNode(VTDNav vn, VTDUtils vu, TuMrkBean bean) throws Exception{
			vn.push();
			AutoPilot ap = new AutoPilot(vn);
			if (vu.getChildElementsCount() > 0) {
				ap.selectXPath("./mrk[@mtype='x-sdl-comment']");
				int attrIdx = -1;
				if(ap.evalXPath() != -1){
					//这是有标注的情况，那么添加标注,备注信息只需添加到目标文本段中
					if (!bean.isSource()) {
						String commandId = "";
						if ((attrIdx = vn.getAttrVal("sdl:cid")) != -1) {
							commandId = vn.toString(attrIdx);
						}
						getSdlComment(vn, vu, commandId, bean);
					}
				}
			}
			String content = vu.getElementContent();
			if (content != null && !"".equals(content)) {
				bean.setContent(content);
			}
			
			vn.pop();
		}
		
		/**
		 * 分析每一个文本段的状态
		 * @param vn
		 * @param vu
		 * @param tgtBean
		 * @param mid	target节点下子节点mrk的mid属性，即唯一id值
		 */
		private void anysisTuMrkStatus(VTDNav vn, VTDUtils vu, TuMrkBean tgtBean, String mid) throws Exception {
			vn.push();
			String status = "";	//一个空字符串代表未翻译
			String xpath = "ancestor::trans-unit/sdl:seg-defs/sdl:seg[@id='" + mid + "']";
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace("sdl", "http://sdl.com/FileTypes/SdlXliff/1.0");
			ap.selectXPath(xpath);
			if(ap.evalXPath() != -1){
				int attrIdx = -1;
				if ((attrIdx = vn.getAttrVal("conf")) != -1) {
					status = vn.toString(attrIdx);
					tgtBean.setStatus(status);
				}
				//判断是否是锁定
				if ((attrIdx = vn.getAttrVal("locked")) != -1) {
					if ("true".equals(vn.toString(attrIdx))) {
						tgtBean.setLocked(true);
					}
				}
				
				if ((attrIdx = vn.getAttrVal("origin")) != -1) {
					//TODO R8中占时没有与 SDL 相对应的匹配类型，这里以后处理
					tgtBean.setMatchType("TM");
				}
				
				if ((attrIdx = vn.getAttrVal("percent")) != -1) {
					tgtBean.setQuality(vn.toString(attrIdx));
				}
			}
			vn.pop();
		}
		
		/**
		 * 获取sdl文件的全局批注。
		 */
		private void getFileComments() throws Exception{
			AutoPilot ap = new AutoPilot(sklVN);
			//无论有几个file节点，都获取所有的备注信息
			String xpath = "/xliff/file/header/sdl:cmt";
			ap.declareXPathNameSpace("sdl", "http://sdl.com/FileTypes/SdlXliff/1.0");
			ap.selectXPath(xpath);
			//sdl="http://sdl.com/FileTypes/SdlXliff/1.0"
			VTDUtils vu = new VTDUtils(sklVN);
			while(ap.evalXPath() != -1){
				if (sklVN.getAttrVal("id") != -1) {
					String commentId = sklVN.toString(sklVN.getAttrVal("id"));
					getSdlComment(sklVN, vu, commentId, null);
				}
			}
		}
		
		/**
		 * 获取trados 2009的备注信息
		 */
		private void getSdlComment(VTDNav vn, VTDUtils vu, String commentId, TuMrkBean bean) throws Exception{
			vn.push();
			List<CommentBean> commentList = new LinkedList<CommentBean>();
			boolean isCurrentSeg = true;	//当前文本段的批注
			if (bean == null) {
				isCurrentSeg = false;
			}
			int attrIdx = -1;
			if (commentId != null && !"".equals(commentId)) {
				String commandXpath = "/xliff/doc-info/cmt-defs/cmt-def[@id='" + commentId + "']/Comments/Comment";	
				AutoPilot ap = new AutoPilot(vn);
				ap.selectXPath(commandXpath);
				while(ap.evalXPath() != -1){
					String severity = null;
					String user = null;
					String date = null;
					String commentText = null;
					if ((attrIdx = vn.getAttrVal("severity")) != -1) {
						severity = vn.toString(attrIdx);
					}
					if ((attrIdx = vn.getAttrVal("user")) != -1) {
						user = vn.toString(attrIdx);
					}
					if ((attrIdx = vn.getAttrVal("date")) != -1) {
						date = vn.toString(attrIdx);
					}
					commentText = vu.getElementContent();
					commentList.add(new CommentBean(user, date, severity, commentText, isCurrentSeg));
				}
			}
			//针对单一文本段的批注
			if (isCurrentSeg) {
				bean.setCommentList(commentList);
			}else {
				//这时是全局批注
				fileCommentsList.addAll(commentList);
			}
			vn.pop();
		}
		
		/**
		 * 删除骨架文件中的所有批注
		 * @throws Exception
		 */
		private void deleteSklComments() throws Exception{
			AutoPilot ap = new AutoPilot(sklVN);
			String xpath;
			//寻找全局批注的定义处，并删除
			xpath = "/xliff/file/header/sdl:cmt";
			ap.declareXPathNameSpace("sdl", "http://sdl.com/FileTypes/SdlXliff/1.0");
			ap.selectXPath(xpath);
			while(ap.evalXPath() != -1){
				sklXM.remove();
			}
			
			//先删除每个文本段的批注
			xpath = "/xliff/doc-info/cmt-defs";
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				sklXM.remove();
			}
		}
	}

//----------------------------------------------------Sdl2XliffImpl 结束标志--------------------------------------------//
	
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
