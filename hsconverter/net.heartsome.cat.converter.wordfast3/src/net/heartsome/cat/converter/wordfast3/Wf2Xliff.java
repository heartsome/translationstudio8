package net.heartsome.cat.converter.wordfast3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.wordfast3.resource.Messages;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class Wf2Xliff implements Converter{
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "txml";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.WF");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "TXML to XLIFF Conveter";
	
	public static final Logger LOGGER = LoggerFactory.getLogger(Wf2Xliff.class);
	
	@Override
	public Map<String, String> convert(Map<String, String> args,
			IProgressMonitor monitor) throws ConverterException {
		Wf2XliffImpl converter = new Wf2XliffImpl();
		return converter.run(args, monitor);
	}

	@Override
	public String getName() {
		return NAME_VALUE;
	}

	@Override
	public String getType() {
		return TYPE_VALUE;
	}

	@Override
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}
	
	
	class Wf2XliffImpl{
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
				
				// 下一步是处理译文中的标记与源文中标记 不完全一致(属性的位置不一致，但是内容都是一样) 的情况。
				// 比如：<ph x="1" type="unknown">&amp;lt;p&amp;gt;</ph> 与 <ph type="unknown" x="1" >&amp;lt;p&amp;gt;</ph>
				makeTagTheSame();
				
			}catch (Exception e) {
				e.printStackTrace();
				String errorTip = Messages.getString("wf2Xlf.msg1") + "\n" + e.getMessage();
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e);
				LOGGER.error("", e);
			}finally{
				try {
					output.close();
				} catch (Exception e2) {
					e2.printStackTrace();
					String errorTip = Messages.getString("wf2Xlf.msg1") + "\n" + e2.getMessage();
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
				errorInfo = MessageFormat.format(Messages.getString("wf.parse.msg1"), 
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
			AutoPilot childAP = new AutoPilot(sklVN);
			VTDUtils vu = new VTDUtils(sklVN);
			String xpath = "/txml/translatable/descendant::segment";
			String srxXpath = "./source";
			String tgtXpath = "./target";
			String commentXpath = "./comments/comment";
			
			ap.selectXPath(xpath);
			int attrIdx = -1;
			//xliff 文件的 trans-unit 节点的 id 值
			int segId = 0;
			while (ap.evalXPath() != -1) {
				TuBean tuBean = new TuBean();
				
				sklVN.push();
				childAP.selectXPath(srxXpath);
				while (childAP.evalXPath() != -1) {
					String srcContent = vu.getElementContent();
					if (srcContent != null && !"".equals(srcContent)) {
						tuBean.setSrcContent(analysisTag(srcContent));
						//开始填充占位符
						insertPlaceHolder(vu, segId);
					}
				}
				sklVN.pop();

				sklVN.push();
				childAP.selectXPath(tgtXpath);
				while (childAP.evalXPath() != -1) {
					String tgtContent = vu.getElementContent();
					if (tgtContent != null && !"".equals(tgtContent)) {
						tuBean.setTgtContent(analysisTag(tgtContent));
					}
					attrIdx = sklVN.getAttrVal("score");
					if (attrIdx != -1) {
						String match = sklVN.toRawString(attrIdx);
						tuBean.setMatch(match);
					}
					
					insertPlaceHolder(vu, segId);
				}
				sklVN.pop();

				// 开始处理批注信息
				sklVN.push();
				childAP.selectXPath(commentXpath);
				List<CommentBean> commnentList = new LinkedList<CommentBean>();
				while(childAP.evalXPath() != -1){
					// 获取添加者
					String user = "";
					attrIdx = sklVN.getAttrVal("creationid");
					if (attrIdx != -1) {
						user = sklVN.toRawString(attrIdx);
					}
					
					// 获取添加时间
					String date = "";
					attrIdx = sklVN.getAttrVal("creationdate");
					if (attrIdx != -1) {
						date = sklVN.toRawString(attrIdx);
					}
					
					// 获取批注类型
					String type = "";
					attrIdx = sklVN.getAttrVal("type");
					if (attrIdx != -1) {
						type = sklVN.toRawString(attrIdx);
					}
					
					// 获取批注类容
					String commentText = "";
					attrIdx = sklVN.getText();
					if (attrIdx != -1) {
						commentText = sklVN.toRawString(attrIdx);
					}
					commnentList.add(new CommentBean(user, date, type, commentText, null));
				}
				sklVN.pop();
				
				// 删除所有的批注
				sklVN.push();
				childAP.selectXPath("./comments");
				if (childAP.evalXPath() != -1) {
					sklXM.remove();
				}
				sklVN.pop();
				
				tuBean.setCommentList(commnentList);
				writeSegment(tuBean, segId);
				segId ++;
			}
		}
		
		/**
		 * 给剔去翻译内容后的骨架文件填充占位符
		 * @throws Exception
		 */
		private void insertPlaceHolder(VTDUtils vu, int seg) throws Exception{
			String mrkHeader = vu.getElementHead();
			String nodeName = vu.getCurrentElementName();
			String newMrkStr = mrkHeader + "%%%"+ seg +"%%%" + "</" + nodeName + ">";
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
		private void writeSegment(TuBean bean, int segId) throws Exception{
			String srcContent = bean.getSrcContent();
			String tgtContent = bean.getTgtContent();
			List<CommentBean> commentList = bean.getCommentList();
			srcContent = srcContent == null ? "" : srcContent;
			tgtContent = tgtContent == null ? "" : tgtContent;
			
			StringBuffer tuSB = new StringBuffer();
			
			String tgtStatusStr = "";
			String tuAttrStr = "";
//			boolean isApproved = false;
//			//具体的意思及与R8的转换请查看tgtBean.getStatus()的注释。
//			if ("needs-translation".equals(status) || "".equals(status)) {
//				if (tgtBean.getContent() != null && tgtBean.getContent().length() >= 1) {
//					tgtStatusStr += " state=\"new\"";
//				}
//			}else if ("finish".equals(status)) {
//				isApproved = true;
//				tgtStatusStr += " state=\"translated\"";
//			}else if ("needs-review-translation".equals(status)) {
//				//疑问
//				tuAttrStr += " hs:needs-review=\"yes\"";
//			}
//			tuAttrStr += isApproved ? " approved=\"yes\"" : "";
//			tuAttrStr += tgtBean.isLocked() ? " translate=\"no\"" : "";
//			
//			tuSB.append("	<trans-unit" + tuAttrStr + " id=\"" + segId + "\" xml:space=\"preserve\" >\n");
//			tuSB.append("		<source xml:lang=\"" + userSourceLang + "\">" + srcContent + "</source>\n");
//			if (!tgtBean.isTextNull()) {
//				String tgtContent = tgtBean.getContent();
//				tuSB.append("		<target" + tgtStatusStr + " xml:lang=\"" + 
//						((targetLang == null || "".equals(targetLang)) ? userSourceLang : targetLang) + "\">"
//						+ tgtContent + "</target>\n");
//			}
//			tuSB.append("	</trans-unit>\n");
			
			if (!"".equals(tgtContent)) {
				tgtStatusStr = " state=\"new\"";
			}
			String match = bean.getMatch();
			if (match != null && !"".equals(match)) {
				 //hs:quality="101"
				tgtStatusStr += " hs:quality=\"" + match + "\"";
			}
			
			tuSB.append("	<trans-unit" + tuAttrStr + " id=\"" + segId + "\" xml:space=\"preserve\" >\n");
			tuSB.append("		<source xml:lang=\"" + userSourceLang + "\">" + srcContent + "</source>\n");
			tuSB.append("		<target" + tgtStatusStr + " xml:lang=\"" + 
					((targetLang == null || "".equals(targetLang)) ? userSourceLang : targetLang) + "\">"
					+ tgtContent + "</target>\n");
			for(CommentBean commentBean : commentList){
				String user = commentBean.getUser();
				String date = commentBean.getDate();
				date = getR8dateStrFromUTC(date);
				String commentText = commentBean.getCommentText();
				String hsCommentText = date + ":" + commentText;
				tuSB.append("		<note from='" + user + "'>" + hsCommentText + "</note>");
			}
			tuSB.append("	</trans-unit>\n");
			
			writeString(tuSB.toString());
		}
		
		/**
		 * 将译文与源文的标记整成一样的。
		 * @throws Exception
		 */
		private void makeTagTheSame() throws Exception{
			VTDGen vg = new VTDGen();
			if (!vg.parseFile(xliffFile, true)) {
				String errorInfo = MessageFormat.format(Messages.getString("wf.parse.msg1"), 
						new Object[]{new File(inputFile).getName()});
				throw new Exception(errorInfo);
			}
			VTDNav tagVN = vg.getNav();
			AutoPilot tagAP = new AutoPilot(tagVN);
			AutoPilot childAP = new AutoPilot(tagVN);
			VTDUtils vu = new VTDUtils(tagVN);
			XMLModifier xm = new XMLModifier(tagVN);
			
			String xpath = "/xliff/file/body/descendant::trans-unit";
			String srcPhXpath = "./source/ph";
			String tgtPhXpath = "./target/ph";
			String tgtXpath = "./target";
			
			tagAP.selectXPath(xpath);
			List<TagBean> tagList = new ArrayList<TagBean>();
			tu:while(tagAP.evalXPath() != -1){
				// 如果译文为空，不进行本次操作
				boolean needContinu = false;
				tagVN.push();
				childAP.selectXPath(tgtXpath);
				if (childAP.evalXPath() != -1) {
					// 如果 ph 标记个数为0，不进行本次操作
					if (vu.getChildElementsCount() <= 0) {
						needContinu = true;
					}
				}else {
					needContinu = true;
				}
				tagVN.pop();
				
				if (needContinu) {
					continue;
				}
				
				// 读取 源文，取出其中的标记
				tagVN.push();
				childAP.selectXPath(srcPhXpath);
				while (childAP.evalXPath() != -1) {
					String content = vu.getElementContent();
					String frag = vu.getElementFragment();
					Map<String, String> attributesMap = getElementAttributs(tagVN);
					tagList.add(new TagBean(content, frag, attributesMap));
				}
				tagVN.pop();
				
				// 如果源文中也没有标记，那么退出本次循环
				if (tagList.size() <= 0) {
					continue;
				}
				
				// 循环译文中的每一个标记
				tagVN.push();
				childAP.selectXPath(tgtPhXpath);
				while(childAP.evalXPath() != -1){
					// 如果源文中的标记都没得了。就没求的比头了。
					if (tagList.size() <= 0) {
						tagVN.pop();
						continue tu;
					}
					
					String frag = vu.getElementFragment();
					if (!fragEquls(frag, tagList)) {
						analysisNotEqualsTag(xm, vu, tagVN, tagList);
					}
				}
				tagVN.pop();
			}
			
			xm.output(xliffFile);
		}
		
		/**
		 * 验证是否相等
		 * @param frag
		 * @param tagList
		 * @return
		 */
		private boolean fragEquls(String frag , List<TagBean> tagList){
			for (TagBean bean : tagList) {
				if (frag.equals(bean.getFrag())) {
					tagList.remove(bean);
					return true;
				}
			}
			return false;
		}
		
		/**
		 * 如果译文中的标记 fragment 在源文中找不到。那我们就开始比较是否属性错位了。如果错位，然后就然后。
		 * @param xm
		 * @param tagVN
		 * @param tagList
		 */
		private void analysisNotEqualsTag(XMLModifier xm, VTDUtils vu, VTDNav vn, List<TagBean> tagList) throws Exception{
			vn.push();
			Map<String, String> curAtrriMap = getElementAttributs(vn);
			String curContent = vu.getElementContent();
			boolean attriEquals = true;	// 标记的属性是否相等
			
			tagBeanFor:for (TagBean bean : tagList) {
				Map<String, String> attriMap = bean.getAttributesMap();
				if (curAtrriMap.size() != attriMap.size()) {
					continue;
				}
				
				for (Entry<String, String> entry : attriMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					
					if (!(curAtrriMap.get(key) != null && value.equals(curAtrriMap.get(key)))) {
						attriEquals = false;
						continue tagBeanFor;
					}
				}
				
				// 如果这个标记在源文中也存在（只是属性顺序不同），则开始删除目标中此标记，重新插入
				String content = bean.getContent();
				if (curContent.equals(content) && attriEquals) {
					xm.remove();
					xm.insertAfterElement(bean.getFrag().getBytes("UTF-8"));
					tagList.remove(bean);
					break tagBeanFor;
				}
			}
			vn.pop();
		}
	}
//----------------------------------------------------Wf2XliffImpl 结束标志--------------------------------------------//
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
	
	
	/**
	 * 转换时间
	 * @param strDate
	 * @return
	 */
	private static String getR8dateStrFromUTC(String _UTCDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		ParsePosition pos = new ParsePosition(0);
		Date str2Date = formatter.parse(_UTCDate, pos);
		SimpleDateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd");
		
		return formatter_1.format(str2Date);
	}
	
	/**
	 * 将 wf 文件中的 ut 标记换成 ph 标记
	 * @param text
	 * @return
	 */
	private static String analysisTag(String text){
		//<ut type="content" x="1">&lt;fontformat color="0#0#0"&gt;&lt;b&gt;</ut>
		text = text.replace("<ut ", "<ph ");
		text = text.replace("ut>", "ph>");
		return text;
	}
	
	public static void main(String[] args) {
		String text = "<ut type=\"content\" x=\"1\">&lt;fontformat color=\"0#0#0\"&gt;&lt;b&gt;</ut>";
		System.out.println(analysisTag(text));
		
		LinkedHashMap<String, String> table = new LinkedHashMap<String, String>();
		table.put("1", "11");
		table.put("2", "22");
		table.put("3", "33");
		table.put("4", "4");
		table.put("5", "11");
		table.put("6", "22");
		table.put("7", "33");
		table.put("8", "4");
		
		
		for(Entry<String, String> entry : table.entrySet()){
			System.out.println(entry.getKey());
			
		}
	}
	
	/**
	 * <div style='color:red;'>这个方法是从 VTDUtils 类中考贝的，主要是要有一定的顺序</div>
	 * @param vn
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 */
	private Map<String, String> getElementAttributs(VTDNav vn)
			throws XPathParseException, XPathEvalException, NavException {
		vn.push();
		Map<String, String> attributes = new LinkedHashMap<String, String>();
		AutoPilot apAttributes = new AutoPilot(vn);
		apAttributes.selectXPath("@*");

		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			attributes.put(name, value);
		}
		apAttributes.resetXPath();

		if (attributes.isEmpty()) {
			attributes = null;
		}
		vn.pop();
		return attributes;
	}
	
	

}
