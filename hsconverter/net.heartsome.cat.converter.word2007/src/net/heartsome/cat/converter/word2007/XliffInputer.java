package net.heartsome.cat.converter.word2007;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.heartsome.cat.converter.word2007.common.PathUtil;
import net.heartsome.cat.converter.word2007.common.SectionSegBean;
import net.heartsome.cat.converter.word2007.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * hsxliff 文件的读入器，主要是针对逆转换。根据一个占位符的 id 去获取 hsxliff 对应 id 的译文
 * @author robert	2012-08-20
 */
public class XliffInputer {
	/** hsxliff 读入器要读入的文件路径 */
	private String xliffFile;
	private VTDNav vn;
	private AutoPilot ap;
	private AutoPilot childAP;
	private VTDUtils vu;
	
	
	public XliffInputer(String xliffFile, PathUtil pathUtil) throws Exception{
		this.xliffFile = xliffFile;
		// 首先处理之前正转换时清理的一些标记，这些标记是存放在 /test.docx.skl/interTag.xml 下的。
		String interTagPath = pathUtil.getInterTagPath();
		restoreGTag(interTagPath);
		
		loadXliff();
	}
	
	/**
	 * 解析 hsxliff 文件
	 * @throws Exception
	 */
	private void loadXliff() throws Exception {
		VTDGen vg = new VTDGen();
		if (vg.parseFile(xliffFile, true)) {
			vn = vg.getNav();
			ap = new AutoPilot(vn);
			childAP = new AutoPilot(vn);
			ap.declareXPathNameSpace("hs", "http://www.heartsome.net.cn/2008/XLFExtension");
			childAP.declareXPathNameSpace("hs", "http://www.heartsome.net.cn/2008/XLFExtension");
			vu = new VTDUtils(vn);
		}else {
			throw new Exception(MessageFormat.format(Messages.getString("docxConvert.msg2"), xliffFile));
		}
	}
	
	/**
	 * 通过 trans-unit 的 id 的值去获取当前 trans-unit 节点的译文
	 * @param <div style='color:red'>isText 占位符所代表的字符串是否是 w:t 节点的内容， 如果是，则不需要组建
	 *        w:r 节点，否则要组装 w:r 等一系列节点</div>
	 * @return
	 * @throws Exception
	 */
	public String getTargetStrByTUId(List<String> tuIdList, boolean isText) throws Exception {
		StringBuffer textSB = new StringBuffer();
		for(String id : tuIdList){
//			if ("75".equals(id)) {
//				System.out.println("逆转换调试开始。。。。。。。。");
//			}
			String xpath = "/xliff/file/body/trans-unit[@id='" + id + "']";
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				// 先取 target 节点的内容，如果为空，则取 source 的内容
				boolean targetIsNull = true;
				childAP.selectXPath("./target");
				vn.push();
				if (childAP.evalXPath() != -1) {
					String tgtContent = vu.getElementContent();
					if (tgtContent != null && !"".equals(tgtContent)) {
						anaysisTgtOrSrcNode(textSB, vn, isText);
						targetIsNull = false;
					}
				}
				vn.pop();
				
				// 如果译文为空，则获取源文的信息
				vn.push();
				if (targetIsNull) {
					childAP.selectXPath("./source");
					if (childAP.evalXPath() != -1) {
						String srcContent = vu.getElementContent();
						if (srcContent != null && !"".equals(srcContent)) {
							anaysisTgtOrSrcNode(textSB, vn, isText);
						}
					}
				}
				vn.pop();
			}
		}
		
		return textSB.toString();
	}
	
	/**
	 * 分析 source 或 target 节点，获取其内容
	 * @throws Exception
	 */
	private void anaysisTgtOrSrcNode(StringBuffer textSB, VTDNav vn, boolean isText) throws Exception {
		vn.push();
		AutoPilot otherAP = new AutoPilot(vn);
		String childXpath = "./text()|node()";
		otherAP.selectXPath(childXpath);
		int tokenId = -1;
		int index = -1;
		Map<Integer, SectionSegBean> targetMap = new TreeMap<Integer, SectionSegBean>();
		while (otherAP.evalXPath() != -1) {
			index = vn.getCurrentIndex();
			tokenId = vn.getTokenType(index);
			if (tokenId == 0) {	//节点子节点
				ananysisTag(vn, targetMap);
			}else if (tokenId == 5) { // 文本子节点
//				if ("+1 845-536-1416".equals(vn.toString(index))) {
//					System.out.println("问题开始了。。。。");
//				}
				targetMap.put(index, new SectionSegBean(null, vn.toRawString(index), null, null, null));
			}
		}
		vn.pop();
		
		SectionSegBean bean;
		for (Entry<Integer, SectionSegBean> entry : targetMap.entrySet()) {
			bean = entry.getValue();
			if (isText) {
				if (bean.getText() != null) {
					textSB.append(bean.getText());
				}
			}else {
				// 这个要组装 w:r 等节点
				String ctype = bean.getCtype() == null ? "" : bean.getCtype();
				String style = bean.getStyle() == null ? "" : bean.getStyle();
				String extendNodes = bean.getExtendNodesStr() == null ? "" : bean.getExtendNodesStr();
				if (bean.getPhTagStr() != null) {
					textSB.append(bean.getPhTagStr());
				}else {
					if ("".equals(ctype)) {
						textSB.append("<w:r>" + style + extendNodes);
						textSB.append("<w:t xml:space=\"preserve\">" + bean.getText() + "</w:t></w:r>");
					}else {
						// <w:hyperlink r:id="rId8" w:history="1">
						int endIdx = ctype.indexOf(" ") == -1 ? ctype.indexOf(">") : ctype.indexOf(" ");
						String nodeName = ctype.substring(ctype.indexOf("<") + 1, endIdx);
						textSB.append(ctype);
						textSB.append("<w:r>" + style + extendNodes);
						textSB.append("<w:t xml:space=\"preserve\">" + bean.getText() + "</w:t></w:r>");
						textSB.append("</" + nodeName + ">");
					}
				}
			}
		}
	}
	
	/**
	 * 分析标记
	 */
	private void ananysisTag(VTDNav vn, Map<Integer, SectionSegBean> targetMap) throws Exception {
		vn.push();
		AutoPilot tagAP = new AutoPilot(vn);
		int index = vn.getCurrentIndex();
		String tagName = vn.toString(index);
		if ("g".equals(tagName)) {
			String style = "";
			int attrIdx = -1;
			if ((attrIdx = vn.getAttrVal("rPr")) != -1) {
				style = vn.toString(attrIdx);
			}
			
			String extendNodes = "";
			if ((attrIdx = vn.getAttrVal("extendNodes")) != -1) {
				extendNodes = vn.toString(attrIdx);
			}
			String ctype = "";
			if ((attrIdx = vn.getAttrVal("ctype")) != -1) {
				ctype = vn.toString(attrIdx);
			}
			
			// 首先检查　g 标记下是否有　sub　节点
			int subNodeCount = -1;
			tagAP.selectXPath("count(./descendant::sub)");
			subNodeCount = (int)tagAP.evalXPathToNumber();
			
			tagAP.selectXPath("./node()|text()");
			if (subNodeCount > 0) {
				int curIdx = vn.getCurrentIndex();
				StringBuffer gTextSB = new StringBuffer();
				Map<Integer, String> gTextMap = new TreeMap<Integer, String>();
				while(tagAP.evalXPath() != -1){
					index = vn.getCurrentIndex();
					int tokenType = vn.getTokenType(index);
					if (tokenType == 0) {	//节点子节点
						String nodeName = vn.toString(index);
						if ("ph".equals(nodeName)) {
							gTextMap.put(index, resetCleanStr(vu.getElementContent()));
						}else if ("g".equals(nodeName)) {
							ananysisTag(vn, targetMap);
						}else if ("sub".equals(nodeName)) {
							ananysisSubTag(vn, gTextMap, targetMap);
						}
					}else if (tokenType == 5) {	//文本子节点
						gTextMap.put(index, resetCleanStr(vn.toRawString(index)));
					}
				}
				
				for(Entry<Integer, String> entry : gTextMap.entrySet()){
					gTextSB.append(entry.getValue());
				}
				targetMap.put(curIdx, new SectionSegBean(ctype, gTextSB.toString(), style, extendNodes, null));
			}else {
				while(tagAP.evalXPath() != -1){
					index = vn.getCurrentIndex();
					int tokenType = vn.getTokenType(index);
					if (tokenType == 0) {	//节点子节点
						String nodeName = vn.toString(index);
						if ("ph".equals(nodeName)) {
							targetMap.put(index, new SectionSegBean(null, null, null, null, resetCleanStr(vu.getElementContent())));
						}else if ("g".equals(nodeName)) {
							ananysisTag(vn, targetMap);
						}
					}else if (tokenType == 5) {	//文本子节点
						targetMap.put(index, new SectionSegBean(ctype, vn.toRawString(index), style, extendNodes, null));
					}
				}
			}
		}else if ("ph".equals(tagName)) {
			targetMap.put(index, new SectionSegBean(null, null, null, null, resetCleanStr(vu.getElementContent())));
		}else if ("sub".equals(tagName)) {
			String style = "";
			int attrIdx = -1;
			if ((attrIdx = vn.getAttrVal("rPr")) != -1) {
				style = vn.toString(attrIdx);
			}
			String extendNodes = "";
			if ((attrIdx = vn.getAttrVal("extendNodes")) != -1) {
				extendNodes = vn.toString(attrIdx);
			}
			
			tagAP.selectXPath("./node()|text()");
			while(tagAP.evalXPath() != -1){
				index = vn.getCurrentIndex();
				int tokenType = vn.getTokenType(index);
				if (tokenType == 0) {	//节点子节点
					String nodeName = vn.toString(index);
					if ("ph".equals(nodeName)) {
						targetMap.put(index, new SectionSegBean(null, null, null, null, resetCleanStr(vu.getElementContent())));
					}else if ("g".equals(nodeName)) {
						ananysisTag(vn, targetMap);
					}
				}else if (tokenType == 5) {	//文本子节点
					targetMap.put(index, new SectionSegBean(null, vn.toRawString(index), style, extendNodes, null));
				}
			}
		}else {	//其他节点，一律当做字符串处理
			targetMap.put(index, new SectionSegBean(null, null, null, null, resetCleanStr(vu.getElementFragment())));
		}
		vn.pop();
	}
	
	
	/**
	 * 处理　sub 标记
	 * @param vn
	 * @param textMap
	 */
	private void ananysisSubTag(VTDNav vn, Map<Integer, String> textMap, Map<Integer, SectionSegBean> targetMap) throws Exception{
		vn.push();
		String style = "";
		int attrIdx = -1;
		if ((attrIdx = vn.getAttrVal("rPr")) != -1) {
			style = vn.toString(attrIdx);
		}
		
		String extendNodes = "";
		if ((attrIdx = vn.getAttrVal("extendNodes")) != -1) {
			extendNodes = vn.toString(attrIdx);
		}
		
		AutoPilot curAP = new AutoPilot(vn);
		curAP.selectXPath("./node()|text()");
		while(curAP.evalXPath() != -1){
			int index = vn.getCurrentIndex();
			int tokenType = vn.getTokenType(index);
			if (tokenType == 0) {	//节点子节点
				String nodeName = vn.toString(index);
				if ("ph".equals(nodeName)) {
					textMap.put(index, resetCleanStr(vu.getElementContent()));
				}else if ("g".equals(nodeName)) {
					ananysisTag(vn, targetMap);
				}else if ("sub".equals(nodeName)) {
					ananysisSubTag(vn, textMap, targetMap);
				}
			}else if (tokenType == 5) {	//文本子节点
				StringBuffer textSB = new StringBuffer();
				
				textSB.append("<w:r>" + style + extendNodes);
				textSB.append("<w:t xml:space=\"preserve\">" + resetCleanStr(vn.toRawString(index)) + "</w:t></w:r>");
				
				textMap.put(index, textSB.toString());
			}
		}
		vn.pop();
	}
	
	/**
	 * 转义回去
	 * @param string
	 * @return
	 */
	public String resetCleanStr(String string){
		string = string.replaceAll("&lt;", "<" ); 
		
		string = string.replaceAll("&gt;", ">"); 
		string = string.replaceAll("&quot;", "\""); 
		string = string.replaceAll("&amp;", "&"); 
		return string;
	}

	public static void main(String[] args) {
		try {
			String xlfPath = "/home/robert/Desktop/testXliff.xml";
			XliffInputer inputer = new XliffInputer(xlfPath, null);
			inputer.test_1();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test_1(){
		try {
//			Map<Integer, SectionSegBean> targetMap = new LinkedHashMap<Integer, SectionSegBean>();
//			AutoPilot ap = new AutoPilot(vn);
//			AutoPilot childAP = new AutoPilot(vn);
//			ap.selectXPath("/root/trans-unit/target");
//			while(ap.evalXPath() != -1){
////				vn.push();
////				childAP.selectXPath("./text()|node()");
////				while (childAP.evalXPath() != -1) {
////					System.out.println("vn.//// = " + vn.toString(vn.getCurrentIndex()));
////					int tokenId = vn.getTokenType(vn.getCurrentIndex());
////					if (tokenId == 0) {
////						ananysisTag(vn, targetMap);
////						System.out.println("vn.//// = " + vn.toString(vn.getCurrentIndex()));
////					}
////				}
////				vn.pop();
//			}
			
			
			getTargetStrByTUId(Arrays.asList(new String[]{"75", "5"}), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private void restoreGTag(String interTagPath) throws Exception{
		if (!new File(interTagPath).exists()) {
			return;
		}
		// 先解析 interTag.xml
		VTDGen vg = new VTDGen();
		if (!vg.parseFile(interTagPath, true)) {
			throw new Exception();
		}
		
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		VTDUtils vu = new VTDUtils(vn);
		String xpath = "/docxTags/tag";
		ap.selectXPath(xpath);
		Map<String, String> tagMap = new HashMap<String, String>();
		String tuId = null;
		int index = -1;
		while(ap.evalXPath() != -1){
			tuId = null;
			if ((index = vn.getAttrVal("tuId")) != -1) {
				tuId = vn.toString(index);
			}
			if (tuId == null) {
				continue;
			}
			
			String content = vu.getElementContent().replace("</g>", "");
			if (content.indexOf("<g") != 0) {
				continue;
			}
			tagMap.put(tuId, content);
		}
		
		// 再将结果传至 xliff 文件
		vg = new VTDGen();
		if (!vg.parseFile(xliffFile, true)) {
			throw new Exception();
		}
		vn = vg.getNav();
		vu.bind(vn);
		ap.bind(vn);
		XMLModifier xm = new XMLModifier(vn);
		for(Entry<String, String> entry : tagMap.entrySet()){
			String thisTuId = entry.getKey();
			String tagContent = entry.getValue();
			
			// docx 转换器里面是没有 多个file节点 的情况
			// 先处理源文
			xpath = "/xliff/file/body//trans-unit[@id='" + thisTuId + "']/source";
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				String srcHeader = vu.getElementHead();
				String oldContent = vu.getElementContent();
				xm.remove();
				StringBuffer newFragSB = new StringBuffer();
				newFragSB.append(srcHeader);
				newFragSB.append(tagContent);
				newFragSB.append(oldContent);
				newFragSB.append("</g></source>");
				xm.insertAfterElement(newFragSB.toString());
			}
			// 处理译文
			xpath = "/xliff/file/body//trans-unit[@id='" + thisTuId + "']/target";
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				String srcHeader = vu.getElementHead();
				String oldContent = vu.getElementContent();
				xm.remove();
				StringBuffer newFragSB = new StringBuffer();
				newFragSB.append(srcHeader);
				newFragSB.append(tagContent);
				newFragSB.append(oldContent);
				newFragSB.append("</g></target>");
				xm.insertAfterElement(newFragSB.toString());
			}
		}
		xm.output(xliffFile);
		// 删除 interTag.xml
		File interTagFile = new File(interTagPath);
		interTagFile.delete();
	}
	
	
}
