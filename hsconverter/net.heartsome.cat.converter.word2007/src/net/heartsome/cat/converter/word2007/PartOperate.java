package net.heartsome.cat.converter.word2007;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.word2007.common.DocxConverterException;
import net.heartsome.cat.converter.word2007.common.SectionSegBean;
import net.heartsome.cat.converter.word2007.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * 每个要处理的文件(比如 document.xml) 的父类，主要提示 vtd 的处理以及其他相公用方法
 * @author robert	2012-08-08
 */
public abstract class PartOperate {
	/** 文件的绝对路径 */
	protected String partPath;
	protected VTDNav vn;
	protected AutoPilot ap;
	protected XMLModifier xm;
	protected VTDUtils vu;
	/** 其他进行循环用的 autopilot */
	protected AutoPilot otherAP;
	/** 这个 AutoPilot 实例，是在ap 的循环中使用的 */
	protected AutoPilot childAP;
	/** 扩展性 AutoPilot 实例，用于其扩展性情况 */
	protected AutoPilot extendAP;
	/** 可翻译的属性的内容集合 */
	protected Map<String, String> translateAttrMap;
	protected XliffOutputer xlfOutput;
	/** 命名空间 */
	protected Map<String, String> nameSpaceMap = new HashMap<String, String>();
	/** 分段规则 */
	protected StringSegmenter segmenter;
	
	protected XliffInputer xlfInput;
	
	/** 是否是链接 */
	private boolean isLink = false;
	/**　链接文本 */
	private String linkText = null;
	
	/**
	 * 正向转换所用到的构造方法
	 * @param partPath
	 * @param xlfOutput
	 * @param segmenter
	 * @throws Exception
	 */
	public PartOperate(String partPath, XliffOutputer xlfOutput, StringSegmenter segmenter){
		this.partPath = partPath;
		this.xlfOutput = xlfOutput;
		this.segmenter = segmenter;
		translateAttrMap = new HashMap<String, String>();
	}
	
	/**
	 * 逆转换用到的构造函数
	 * @param partPath
	 * @param xlfInput
	 */
	public PartOperate(String partPath, XliffInputer xlfInput){
		this.partPath = partPath;
		this.xlfInput = xlfInput;
	}
	
	/**
	 * 解析文件
	 * @param nameSpaceMap	要申明的命名空间
	 * @throws Exception
	 */
	protected void loadFile(Map<String, String> nameSpaceMap) throws Exception {
		VTDGen vg = new VTDGen();
		if (vg.parseFile(partPath, true)) {
			vn = vg.getNav();
			ap = new AutoPilot(vn);
			otherAP = new AutoPilot(vn);
			childAP = new AutoPilot(vn);
			extendAP = new AutoPilot(vn);
			vu = new VTDUtils(vn);
			xm = new XMLModifier(vn);
			// 给 ap 申明命名空间
			for(Entry<String, String> entry : nameSpaceMap.entrySet()){
				ap.declareXPathNameSpace(entry.getKey(), entry.getValue());
				childAP.declareXPathNameSpace(entry.getKey(), entry.getValue());
				otherAP.declareXPathNameSpace(entry.getKey(), entry.getValue());
				extendAP.declareXPathNameSpace(entry.getKey(), entry.getValue());
			}
		}else {
			throw new DocxConverterException(MessageFormat.format(Messages.getString("docxConvert.msg2"), partPath));
		}
	}
	
	/**
	 * 开始运行正向转换，即 word 2007 转换成 hsxliff
	 */
	protected abstract void converter() throws Exception;
	
	/**
	 * 开始运行逆转换
	 * @throws Exception
	 */
	protected abstract void reverseConvert() throws Exception;
	
	/**
	 * 获取一个 p 节点的样式，限制：当前 vn 所处地点必须为 w:p
	 * @param vn
	 * @return
	 */
	protected String getPStyle() throws Exception {
		String style = null;
		vn.push();
		otherAP.selectXPath("./w:pPr/w:rPr");
		while (otherAP.evalXPath() != -1) {
			style = vu.getElementFragment();
		}
		vn.pop();
		return style;
	}
	
	/**
	 * 获取一个文本段的格式，限制：当前的 vn 所处地点必须为 w:r
	 * @return
	 * @throws Exception
	 */
	protected String getRStyle() throws Exception {
		String style = null;
		vn.push();
		otherAP.selectXPath("./w:rPr");
		while (otherAP.evalXPath() != -1) {
			style = vu.getElementFragment();
		}
		vn.pop();
		if (style != null) {
			// 主要是针对 <w:font /> 与　<w:font>的情况，去除多作的空格
			style = style.replace(" />", "/>").trim();
		}
		return style;
	}
	
	/**
	 * 获取一个 w:r 节点除样式(w:rPr)与文本(w:t)之外的其他结点
	 * @return
	 * @throws Exception
	 */
	protected String getExtendNodes() throws Exception {
		String extendNodesStr = null;
		StringBuffer extendNodesSB = new StringBuffer();
		vn.push();
		otherAP.selectXPath("./node()[name()!='w:rPr' and name()!='w:t']");
		while (otherAP.evalXPath() != -1) {
			extendNodesSB.append(vu.getElementFragment());
		}
		vn.pop();
		extendNodesStr = extendNodesSB.toString();
		return extendNodesStr;
	}
	
	/**
	 * 获取一个 w:r 节点里面的文本值。针对于 w:p/w:r/w:t
	 * @return
	 * @throws Exception
	 */
	protected String getText() throws Exception {
		String nodeName = vn.toRawString(vn.getCurrentIndex());
		if ("w:fldSimple".equals(nodeName)) {
			return null;
		}
		if ("w:hyperlink".equals(nodeName)) {
			if (vn.getAttrVal("w:anchor") != -1) {
				return null;
			}
		}
		
		
		StringBuffer textSB = new StringBuffer();
		vn.push();
		otherAP.selectXPath("./descendant::w:t/text()");
		while (otherAP.evalXPath() != -1) {
			textSB.append(vn.toRawString(vn.getCurrentIndex()));
		}
		vn.pop();
		if (textSB.length() <= 0) {
			return null;
		}

		return textSB.toString();
	}
	
//		<w:hyperlink r:id="rId5" w:history="1">
	//		<w:r w:rsidRPr="007267A9">
	//			<w:rPr>
	//				<w:rStyle w:val="a5" />
	//				<w:noProof />
	//			</w:rPr>
	//			<w:t>www.baidu.</w:t>
	//		</w:r>
	//		<w:r w:rsidRPr="0077163A">
	//			<w:rPr>
	//				<w:rStyle w:val="a5" />
	//				<w:noProof />
	//				<w:color w:val="FF0000" />
	//			</w:rPr>
	//			<w:t>com</w:t>
	//		</w:r>
//	   </w:hyperlink>
	/**
	 * 获取链接的特殊文本<br/>
	 * 备注：上面是一个关于　w:hyperlink 的例子，这时，一个　w:hyperlink　就当作一个　g 标记进行处理。那么，它的特列文本　就是其应提取的所有内容，不单指 "www.baidu.com"，而是　3　个　w:r 节点。<br/>
	 * 若当前节点中只有一个　w:r 节点，那么不予考虑。返回　null,　若有多个　w:r 节点，那将它们拼接成　sub 与 ph 标记组合的文本，当作　特殊文本进行返回。
	 * @return
	 */
	private String getLinkText() throws Exception{
		vn.push();
		String curLinkText = null;
		int rNodeCount = getNodeCount("./w:r");
		if (rNodeCount == 1) {
			return curLinkText;
		}
		
		List<SectionSegBean> segList = new LinkedList<SectionSegBean>();
		extendAP.selectXPath("./node()");
		while(extendAP.evalXPath() != -1){
			String rStyle = getRStyle();
			String text = "";
			String extendNodesStr = getExtendNodes();
			
			int tNodeCount = getNodeCount("./w:t");
			// 有　t 节点的，就一定有数据
			if (tNodeCount == 1) {
				text = getText();
				// extendNodesAttr 这个属性是自定义的，专门针对于除样式与文本之外的其他节点，如 <w:tab/>
				String extendNodesAttr = ("".equals(extendNodesStr) || extendNodesStr == null) ? ""
						: " extendNodes='"+ xlfOutput.cleanString(extendNodesStr) +"'";
				// rprAttr 这个是保存 rpr 属性的。rpr属性是用于保存 w:r 样式。
				String rprAttr = ("".equals(rStyle) || rStyle == null) ? "" 
						: " rPr='" + xlfOutput.cleanString(rStyle) + "'";
				segList.add(new SectionSegBean(null, text, rprAttr, extendNodesAttr, null));
			}else {
				String phTagStr = vu.getElementFragment();
				segList.add(new SectionSegBean(null, null, null, null, xlfOutput.cleanString(phTagStr)));
			}
		}
		vn.pop();
		StringBuffer specialTextSB = new StringBuffer();
		
		
		SectionSegBean bean;
		for (int i = 0; i < segList.size(); i++) {
			bean = segList.get(i);
			String style = bean.getStyle() == null ? "" : bean.getStyle();
			String extendNodes = bean.getExtendNodesStr() == null ? "" : bean.getExtendNodesStr();
			if (("".equals(style)) && ("".equals(extendNodes)) && bean.getPhTagStr() == null) {
				specialTextSB.append(bean.getText());
			}else if (bean.getPhTagStr() == null) {
				specialTextSB.append("<sub id='" + xlfOutput.useTagId() + "'" + style + extendNodes +">");
				specialTextSB.append(bean.getText());
				// 判断下一个是否样式与扩展节点的内容都相同，若相同，就组装成一个g标记
				while(i + 1 < segList.size()){
					bean = segList.get(i +1);
					String curStyle = bean.getStyle() == null ? "" : bean.getStyle();
					String curExtendNodes = bean.getExtendNodesStr() == null ? "" : bean.getExtendNodesStr();
					// 当两个的 ctype 都为空时，才能进行拼接，因为 ctype 多半为 <w:hyperlink .... >
					if (curStyle.equals(style) && curExtendNodes.equals(extendNodes)) {
						specialTextSB.append(bean.getText());
						i++;
					}else {
						break;
					}
				}
				specialTextSB.append("</sub>");
			}else {
				String phTagStr = bean.getPhTagStr();
				if (!"".equals(phTagStr.trim())) {
					specialTextSB.append("<ph id='" + xlfOutput.useTagId() + "'>");
					specialTextSB.append(phTagStr);
					while(i + 1 < segList.size()){
						bean = segList.get(i +1);
						if (bean.getPhTagStr() != null) {
							specialTextSB.append(bean.getPhTagStr());
							i++;
						}else {
							break;
						}
					}
					specialTextSB.append("</ph>");
				}
			}
		}
		curLinkText = specialTextSB.toString();
		if (curLinkText.length() == 0) {
			curLinkText = null;
		}
		return curLinkText;
	}
	
	/**
	 * 获取节点 w:t 的个数，如果个数为1，就不需要粹取标记
	 * @return
	 * @throws Exception
	 */
	protected int getTextCount() throws Exception {
		int textCount = -1;
		otherAP.selectXPath("count(./node()[(name()='w:r' or name()='w:hyperlink') and not(@w:anchor) and not(descendant::node()[name()='w:p'])]//w:t)");
		textCount = (int) otherAP.evalXPathToNumber();
		return textCount;
	}
	
	
	/**
	 * 获取某个节点的数量，
	 * @param xpath	如 ./w:r/w:t
	 * @return
	 * @throws Exception
	 */
	private int getNodeCount(String xpath) throws Exception{
		vn.push();
		int nodeCount = -1;
		otherAP.selectXPath("count(" + xpath + ")");
		nodeCount = (int) otherAP.evalXPathToNumber();
		vn.pop();
		return nodeCount;
	}
	
	/**
	 * 将纯文本段更新成占位符，比如将<w:p><w:r><w:t>文本段</w:t></w:r></w:p> 中的“文本段” 更新成 %%%?%%%
	 */
	protected void updateTextToPlaceHoder(String placeHolderStr) throws Exception {
		// 如果是特殊节点，并且不为空，那么直接删除，并添加进占位符
		if (isLink && linkText != null) {
			xm.remove();
			String nodeName = vn.toString(vn.getCurrentIndex());
			String headStr = vu.getElementHead();
			StringBuffer sb = new StringBuffer();
			sb.append(headStr);
			sb.append(placeHolderStr);
			sb.append("</" + nodeName + ">");
			xm.insertAfterElement(sb.toString());
		}else {
			vn.push();
			otherAP.selectXPath("./w:t/text()");
			while (otherAP.evalXPath() != -1) {
				xm.updateToken(vn.getCurrentIndex(), placeHolderStr);
			}
			vn.pop();	
		}
	}
	
	/**
	 * 根据一个占位符，获取其占位符序列号的值
	 * @param placeHolderStr
	 * @return
	 */
	protected String getSegIdFromPlaceHoderStr(String placeHolderStr) {
		String segIdStr = null;
		int firstPlaceHIdx = placeHolderStr.indexOf("%%%");
		segIdStr = placeHolderStr.substring(firstPlaceHIdx + 3, placeHolderStr.indexOf("%%%", firstPlaceHIdx + 1));
		return segIdStr.trim();
	}
	
	/**
	 * 根据多个占位符，获取其所有的占位符序列号的值，例如 %%%5%%%%%%6%%%%%%7%%%
	 * @param placeHolderStr
	 * @return
	 */
	protected List<String> getAllSegIdsFromPlaceHoderStr(String placeHolderStr) {
		List<String> idList = new LinkedList<String>();
		int firstPlaceHIdx = placeHolderStr.indexOf("%%%");
		while(firstPlaceHIdx != -1){
			int endIdx = placeHolderStr.indexOf("%%%", firstPlaceHIdx + 1);
			String segIdStr = placeHolderStr.substring(firstPlaceHIdx + 3, endIdx);
			idList.add(segIdStr);
			firstPlaceHIdx = placeHolderStr.indexOf("%%%", endIdx + 3);
		}
		return idList;
	}
	
	
	/**
	 * 处理可翻译的属性问题，例如
	 * @throws Exception
	 */
	protected void operateTransAttributes(String attrXpath) throws Exception {
		ap.selectXPath(attrXpath);
		int index = -1;
		while(ap.evalXPath() != -1){
			index = vn.getCurrentIndex() + 1;
			if (!"".equals(vn.toRawString(index))) {
				String placeHoderStr = "%%%" + xlfOutput.useSegId() + "%%%";
				translateAttrMap.put(placeHoderStr, vn.toString(index));
				xm.updateToken(index, placeHoderStr);
			}
		}
		// 如果有修改的属性。那么，将修改的内容保存到文件，再重新解析文件。
		if (translateAttrMap.size() > 0) {
			xm.output(partPath);
			loadFile(nameSpaceMap);
		}
	}
	
	/**
	 * 删除指定字符串的所有空格，因为　trim() 方法无法删除全角字符。
	 * @return
	 */
	public static String deleteBlank(String string){
		return string.replaceAll("[ 　]", "");
	}
	
	
	/**
	 * 分析每个 w:p 节点，将要翻译的东西提取出来，用占位符替代。
	 *  or name()='w:fldSimple'
	 * @throws Exception
	 */
	protected void analysisNodeP() throws Exception {

		// 如果这个节点里面还有 p 节点，那么不进行处理
		int textCount = getTextCount();
		int index = -1;
		Map<Integer, SectionSegBean> sectionSegMap = new TreeMap<Integer, SectionSegBean>();
		StringBuffer placeHolderSB = new StringBuffer();	// 占位符
		// 开始处理每个节点的文本
		if (textCount == 1) {
			// 如果一个节点里面只有一个 w:r//w:t ,那么直接获取出内容，如果这个节点里面还有 p 节点，那么不进行处理
			childAP.selectXPath("./node()[(name()='w:r' or name()='w:hyperlink') and not(@w:anchor) and not(descendant::node()[name()='w:p'])]/descendant::w:t/text()");
			vn.push();
			if(childAP.evalXPath() != -1){
				index = vn.getCurrentIndex();
				String segment = vn.toRawString(index);
				if ("".equals(deleteBlank(segment))) {
					vn.pop();
					return;
				}
				String[] segs = segmenter.segment(segment);
				for(String seg : segs){
					// 生成 trans-unit 节点
					placeHolderSB.append(xlfOutput.addTransUnit(seg));
				}
				xm.updateToken(index, placeHolderSB.toString());
			}
			vn.pop();
		}else if (textCount > 1) {	// 没有 w:/t 节点的段落，不进行处理。如果这个节点里面还有 p 节点，那么不进行处理
//			System.out.println("vn.getCurrentIndex() = " + vn.getCurrentIndex());
//			if (185 == vn.getCurrentIndex()) {
//				System.out.println("调试开始了。。。。");
//			}
			vn.push();
			// 先获取出这个段落里所有要翻译的数据，再分段
			List<StringBuffer> segList = new ArrayList<StringBuffer>();
			StringBuffer segSB = new StringBuffer();
			childAP.selectXPath("./node()[(name()='w:r' or name()='w:hyperlink') and not(@w:anchor) and not(descendant::node()[name()='w:p'])]");
			while(childAP.evalXPath() != -1){
				if (vu.getElementContent().indexOf("<w:br") != -1) {
					segList.add(segSB);
					segSB = new StringBuffer();
				}
				String curText = getText();
				curText = (curText == null ? "" : curText);
				segSB.append(curText);
			}
			segList.add(segSB);
			
			// 如果为空格，直接退出
			StringBuffer checkBlankSB = new StringBuffer();
			for(StringBuffer curSB : segList){
				checkBlankSB.append(curSB);
			}
			if (deleteBlank(checkBlankSB.toString()).length() <= 0) {
				vn.pop();
				return;
			}
			
//			System.out.println("checkBlankSB.toString() =" + checkBlankSB.toString());
//			if (segSB.toString().indexOf("Definition des vorzuhaltenden") != -1) {
//				System.out.println("错误信息开始了。。。。。");
//			}
			vn.pop();
			// 开始分割文本段
			List<String> segArrayList = new ArrayList<String>();
			for(StringBuffer curSB : segList){
				String[] segArray = segmenter.segment(curSB.toString());
				for (int i = 0; i < segArray.length; i++) {
					segArrayList.add(segArray[i]);
				}
			}
			String[] segArray = segArrayList.toArray(new String[]{});
			
			
			// 开始遍历每个节点 ./node()，进行处理
			vn.push();
			childAP.selectXPath("./node()");
			boolean segStart = false;	// 一个文本段的开始
			boolean segOver = false;	// 一个文本段结束的标记
			int segIdx = 0;
			String seg = segArray[segIdx];
			while(childAP.evalXPath() != -1){
				index = vn.getCurrentIndex();
				isLink = false;
				linkText = null;
				
				String nodeName = vu.getCurrentElementName();
				if ("w:r".equals(nodeName) || "w:hyperlink".equals(nodeName) || "w:fldSimple".equals(nodeName)) {
					if ("w:hyperlink".equals(nodeName)) {
						isLink = true;
						linkText = getLinkText();
					}
					
					if ("w:r".equals(nodeName)) {
						// 检查是否有软回车 <w: br/>
						vn.push();
						boolean hasBr = false;
						extendAP.selectXPath("./w:br");
						if (extendAP.evalXPath() != -1) {
							hasBr = true;
						}
						vn.pop();
						if (hasBr && (sectionSegMap.size() > 0)) {
							// 遇到软回车就开始分段
							segOver = true;
							segStart = false;
							String placeHoderStr = xlfOutput.addTransUnit(createSourceStr(sectionSegMap));
							xm.insertBeforeElement(placeHoderStr);
							segArray[segIdx] = seg;
							continue;
						}
					}
					
					String text = getText();
					if (text != null) {
						segStart = true;
						segOver = false;
						String ctypeAttrStr = "";
						String rStyle = null;
						String extendNodesStr = null;
						
						if (isLink) {
							ctypeAttrStr = " ctype='" + xlfOutput.cleanString(vu.getElementHead()) + "'";
							vn.push();
							extendAP.selectXPath("./w:r");
							// 取第一个　w:r 节点的　属性，这种情况针对 特殊　节点中只有一个　w:t 的情况，
							// 如果　linkText 不为空的话，就不用获取　rStyle 与　extendNodesStr 属性了
							if (linkText == null && extendAP.evalXPath() != -1) {
								rStyle = getRStyle();
								extendNodesStr = getExtendNodes();
							}
							vn.pop();
						}else {
							rStyle = getRStyle();
							extendNodesStr = getExtendNodes();
						}

						// extendNodesAttr 这个属性是自定义的，专门针对于除样式与文本之外的其他节点，如 <w:tab/>
						String extendNodesAttr = ("".equals(extendNodesStr) || extendNodesStr == null) ? ""
								: " extendNodes='"+ xlfOutput.cleanString(extendNodesStr) +"'";
						// rprAttr 这个是保存 rpr 属性的。rpr属性是用于保存 w:r 样式。
						String rprAttr = ("".equals(rStyle) || rStyle == null) ? "" 
								: " rPr='" + xlfOutput.cleanString(rStyle) + "'";
						
						// 如果当前文本是一个独立的分段，则将其文本用占位符替换。
						if (text.equals(segArray[segIdx])) {
							String placeHoderStr = "";
							if (isLink && linkText != null) {
								placeHoderStr = xlfOutput.addTransUnit(linkText);
							}else {
								placeHoderStr = xlfOutput.addTransUnit(seg);
							}
							updateTextToPlaceHoder(placeHoderStr);
							if (segIdx + 1 < segArray.length) {
								seg = segArray[++segIdx];
							}
							
							segOver = true;
							segStart = false;
							continue;
						}
						
						// 链接不应支持分段，现开始分析
						if (isLink) {
							List<Object> resultList = modifySeg(segArray, segIdx, text, seg);
							segArray = (String[]) resultList.get(0);
							seg = (String) resultList.get(1);
						}
						
						
						// 分析分割后的文本段
						if (text.equals(seg)) {
							if ("".equals(rprAttr) && "".equals(extendNodesAttr)) {
								// 只添加纯文本
								sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, null, null, null));
							}else {
								sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, rprAttr, extendNodesAttr, null));
							}
							if (segIdx + 1 < segArray.length) {
								seg = segArray[++segIdx];
							}
							segOver = true;
							segStart = false;
							xm.remove();
							String placeHoderStr = xlfOutput.addTransUnit(createSourceStr(sectionSegMap));
							xm.insertAfterElement(placeHoderStr);
						}else if (text.length() < seg.length() && seg.indexOf(text) == 0 ) {	// 如果当前文本长度小于分段长度
							if ("".equals(rprAttr) && "".equals(extendNodesAttr)) {
								sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, null, null, null));
							}else {
								sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, rprAttr, extendNodesAttr, null));
							}
							xm.remove();
							// 在替换有 "(" 的情况。必须加一个 \\( 或者 [(]，否则会报错
							seg = seg.substring(text.length());
						}else if (text.length() > seg.length() && text.indexOf(seg) == 0) {
							if ("".equals(rprAttr) && "".equals(extendNodesAttr)) {
								sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, seg, null, null, null));
							}else {
								sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, seg, rprAttr, extendNodesAttr, null));
							}
							text = text.substring(seg.length());
							// 由于这里有可能要插入多个占位符，所以要把所有的占位符放到一起，一次性存入，因为 xmlModifial 不允许在同一个地方修改多次。
							StringBuffer replaceHolderSB = new StringBuffer();
							replaceHolderSB.append(xlfOutput.addTransUnit(createSourceStr(sectionSegMap)));
							xm.remove();
							if (segIdx + 1 < segArray.length) {
								seg = segArray[++segIdx];
							}
							
							// 开始处理剩下的文本
							while(text.length() != 0){
								if (text.equals(seg)) {
									if ("".equals(rprAttr) && "".equals(extendNodesAttr)) {
										sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, null, null, null));
									}else {
										sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, rprAttr, extendNodesAttr, null));
									}
									text = "";
									segOver = true;
									segStart = false;
									replaceHolderSB.append(xlfOutput.addTransUnit(createSourceStr(sectionSegMap)));
									if (segIdx + 1 < segArray.length) {
										seg = segArray[++segIdx];
									}
								}else if (text.length() < seg.length() && seg.indexOf(text) == 0) {
									if ("".equals(rprAttr) && "".equals(extendNodesAttr)) {
										sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, null, null, null));
									}else {
										sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, text, rprAttr, extendNodesAttr, null));
									}
									seg = seg.substring(text.length());
									text = "";
								}else if (text.length() > seg.length() && text.indexOf(seg) == 0) {
									if ("".equals(rprAttr) && "".equals(extendNodesAttr)) {
										sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, seg, null, null, null));
									}else {
										sectionSegMap.put(index, newSectionSegBean(ctypeAttrStr, seg, rprAttr, extendNodesAttr, null));
									}
									text = text.substring(seg.length());
									replaceHolderSB.append(xlfOutput.addTransUnit(createSourceStr(sectionSegMap)));
									if (segIdx + 1 < segArray.length) {
										seg = segArray[++segIdx];
									}
								}
							}
							xm.insertAfterElement(replaceHolderSB.toString());
						}
					}else if(segStart && !segOver){
						sectionSegMap.put(index, new SectionSegBean(null, null, null, null, 
								xlfOutput.cleanString(vu.getElementFragment())));
						xm.remove();
					}
				}else if(segStart && !segOver){
					sectionSegMap.put(index, new SectionSegBean(null, null, null, null, 
							xlfOutput.cleanString(vu.getElementFragment())));
					xm.remove();
				}
			}
			vn.pop();
		}
	}
	
	/**
	 * 针对特殊情况，w:hyperlink 里面的文本　不应分段，故将不应分段的进行合并
	 * @param segArray
	 * @param segIdx
	 * @param text
	 * @return
	 */
	private List<Object> modifySeg(String[] segArray, int segIdx, String text, String seg){
		List<String> segList = new ArrayList<String>();
		int start = segArray[segIdx].lastIndexOf(seg);
		StringBuffer temSB = new StringBuffer();
		segFor:for(int i = 0; i < segArray.length; i++){
			if (i < segIdx) {
				segList.add(segArray[i]);
			}else if (i == segIdx) {
				if (text.length() != 0) {
					temSB.append(segArray[i]);
					if (text.indexOf(seg) != -1) {
						text = text.substring(seg.length(), text.length());
					}else if(seg.indexOf(text) != -1){
						text = "";
					}
				}else {
					if (temSB.length() != 0) {
						segList.add(temSB.toString());
						temSB = new StringBuffer();
					}
					for(int j = i; j < segArray.length; j ++){
						segList.add(segArray[j]);
					}
					break segFor;
				}
			}else {
				if (text.length() != 0) {
					temSB.append(segArray[i]);
					if (text.indexOf(segArray[i]) != -1) {
						text = text.substring(segArray[i].length(), text.length());
					}else if(segArray[i].indexOf(text) != -1){
						text = "";
					}
				}else {
					if (temSB.length() != 0) {
						segList.add(temSB.toString());
						temSB = new StringBuffer();
					}
					for(int j = i; j < segArray.length; j ++){
						segList.add(segArray[j]);
					}
					break segFor;
				}
			}
			
		}
		if (temSB.length() != 0) {
			segList.add(temSB.toString());
			temSB = new StringBuffer();
		}
		
		segArray = segList.toArray(new String[]{});
		seg = segArray[segIdx].substring(start, segArray[segIdx].length());
		
		List<Object> resultList = new ArrayList<Object>();
		resultList.add(segArray);
		resultList.add(seg);
		
		return resultList;
	}
	
	
	/**
	 *　根据具体情况创建　sectionSegBean 
	 */
	private SectionSegBean newSectionSegBean(String ctype, String text, String style, String extendNodesStr, String phTagStr){
		SectionSegBean bean = null;
		if (isLink) {
			if (linkText == null) {
				bean = new SectionSegBean(ctype, text, style, extendNodesStr, phTagStr);
			}else {
				bean = new SectionSegBean(ctype, linkText, null, null, null);
			}
		}else {
			bean = new SectionSegBean(ctype, text, style, extendNodesStr, phTagStr);
		}
		
		return bean;
	}
	
	/**
	 * 通过sectionSegMap生成要添加到 trans-unit 节点的源文本
	 * @param sectionSegMap
	 * @return
	 */
	private String createSourceStr(Map<Integer, SectionSegBean> sectionSegMap) {
		List<SectionSegBean> segList = new LinkedList<SectionSegBean>();
		for(Entry<Integer, SectionSegBean> entry : sectionSegMap.entrySet()){
			segList.add(entry.getValue());
		}

		StringBuffer srcTextSB = new StringBuffer();
		SectionSegBean bean = null;
		for (int i = 0; i < segList.size(); i++) {
			bean = segList.get(i);
			String ctype = bean.getCtype() == null ? "" : bean.getCtype();
			String style = bean.getStyle() == null ? "" : bean.getStyle();
			String extendNodes = bean.getExtendNodesStr() == null ? "" : bean.getExtendNodesStr();
			if (("".equals(ctype) && "".equals(style)) && ("".equals(extendNodes)) && bean.getPhTagStr() == null) {
				srcTextSB.append(bean.getText());
			}else if (bean.getPhTagStr() == null) {
				srcTextSB.append("<g id='" + xlfOutput.useTagId() + "'" + ctype + style + extendNodes +">");
				srcTextSB.append(bean.getText());
				// 判断下一个是否样式与扩展节点的内容都相同，若相同，就组装成一个g标记
				while(i + 1 < segList.size()){
					bean = segList.get(i +1);
					String curCtype = bean.getCtype() == null ? "" : bean.getCtype();
					String curStyle = bean.getStyle() == null ? "" : bean.getStyle();
					String curExtendNodes = bean.getExtendNodesStr() == null ? "" : bean.getExtendNodesStr();
					// 当两个的 ctype 都为空时，才能进行拼接，因为 ctype 多半为 <w:hyperlink .... >
					if (curStyle.equals(style) && curExtendNodes.equals(extendNodes) && "".equals(ctype) && "".equals(curCtype) ) {
						srcTextSB.append(bean.getText());
						i++;
					}else {
						break;
					}
				}
				srcTextSB.append("</g>");
			}else {
				String phTagStr = bean.getPhTagStr();
				if (!"".equals(phTagStr.trim())) {
					srcTextSB.append("<ph id='" + xlfOutput.useTagId() + "'>");
					srcTextSB.append(phTagStr);
					while(i + 1 < segList.size()){
						bean = segList.get(i +1);
						if (bean.getPhTagStr() != null) {
							srcTextSB.append(bean.getPhTagStr());
							i++;
						}else {
							break;
						}
					}
					srcTextSB.append("</ph>");
				}
			}
		}
		sectionSegMap.clear();
		return srcTextSB.toString();
	}
	
	/**
	 * 逆转换时处理 w:p 节点
	 * @throws Exception
	 */
	protected void analysisReversePnode() throws Exception {
		vn.push();
		childAP.selectXPath("./text()|node()[name()='w:r' or name()='w:hyperlink' or name()='w:fldSimple']");
		int index = -1;
		while(childAP.evalXPath() != -1){
			index = vn.getCurrentIndex();
			int tokenType = vn.getTokenType(index);
			
			if (tokenType == 0) {	// 表示节点子节点
				vn.push();
				
				String nodeName = vn.toString(vn.getCurrentIndex());
				String xpath = "./w:t";
				
				if ("w:hyperlink".equals(nodeName) || "w:fldSimple".equals(nodeName)) {
					//　针对两种情况，一是 <w:hyperlink r:id="rId6" w:history="1">%%%0%%%</w:hyperlink>
					// 二是　<w:hyperlink r:id="rId6" w:history="1"><w:r><w:t>%%%0%%%</w:r></w:t></w:hyperlink>
					xpath = "./w:r/w:t";
					otherAP.selectXPath(xpath);
					index = vn.getText();
					if (index != -1) {
						String text = vn.toRawString(index);
						if (text.indexOf("%%%") != -1) {
							List<String> segIdList = getAllSegIdsFromPlaceHoderStr(text);
							String tgtStr = xlfInput.getTargetStrByTUId(segIdList, false);
							xm.updateToken(index, tgtStr);
						}
					}else {
						if (otherAP.evalXPath() != -1) {
							index = vn.getText();
							if (index != -1) {
								String text = vn.toRawString(index);
								if (text.indexOf("%%%") != -1) {
									List<String> segIdList = getAllSegIdsFromPlaceHoderStr(text);
									String tgtStr = xlfInput.getTargetStrByTUId(segIdList, true);
									xm.remove();
									xm.insertAfterElement("<w:t xml:space=\"preserve\">" + tgtStr + "</w:t>");
								}
							}
						}
					}
				}else {
					otherAP.selectXPath(xpath);
					if (otherAP.evalXPath() != -1) {
						index = vn.getText();
						if (index != -1) {
							String text = vn.toRawString(index);
							if (text.indexOf("%%%") != -1) {
								List<String> segIdList = getAllSegIdsFromPlaceHoderStr(text);
								String tgtStr = xlfInput.getTargetStrByTUId(segIdList, true);
								xm.remove();
								xm.insertAfterElement("<w:t xml:space=\"preserve\">" + tgtStr + "</w:t>");
							}
						}
					}
				}
				
				vn.pop();
			}else if (tokenType == 5) {	// 表示文本子节点
				String placeHolderStr = vn.toRawString(index);
				if (placeHolderStr.indexOf("%%%") != -1) {
					List<String> segIdList = getAllSegIdsFromPlaceHoderStr(placeHolderStr);
					String tgtStr = xlfInput.getTargetStrByTUId(segIdList, false);
					xm.updateToken(index, tgtStr);
				}
			}
		}
		vn.pop();
	}
	
	/**
	 * 逆转换处理可翻译属性
	 * @param xpath
	 * @throws Exception
	 */
	protected void reverseTranslateAttributes(String xpath) throws Exception {
		loadFile(nameSpaceMap);
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			String placeHolderStr = vn.toRawString(vn.getCurrentIndex() + 1);
			List<String> idList = getAllSegIdsFromPlaceHoderStr(placeHolderStr);
			String text = xlfInput.getTargetStrByTUId(idList, true);
			xm.updateToken(vn.getCurrentIndex() + 1, text);
		}
		xm.output(partPath);
	}
	
	public static void main(String[] args) {
//		String placeHolderStr = "asd asd fsad %%%5%%%%%%6%%%%%%7%%%dasdfa sdf asd f%%%7%%%";
//		List<String> idList = new LinkedList<String>();
//		int firstPlaceHIdx = placeHolderStr.indexOf("%%%");
//		while(firstPlaceHIdx != -1){
//			int endIdx = placeHolderStr.indexOf("%%%", firstPlaceHIdx + 1);
//			System.out.println("endIdx =" + endIdx);
//			String segIdStr = placeHolderStr.substring(firstPlaceHIdx + 3, endIdx);
//			idList.add(segIdStr);
//			System.out.println(segIdStr);
//			firstPlaceHIdx = placeHolderStr.indexOf("%%%", endIdx + 3);
//		}
		
		
		
//		<w:p w14:paraId="5D139D30" w14:textId="3565E204" w:rsidR="00814583"
//				w:rsidRDefault="00814583" w:rsidP="00814583">
//				<w:pPr>
//					<w:rPr>
//						<w:noProof />
//					</w:rPr>
//				</w:pPr>
//				<w:r>
//					<w:rPr>
//						<w:noProof />
//					</w:rPr>
//					<w:t xml:space="preserve">go to </w:t>
//				</w:r>
//				<w:hyperlink w:history="1">
//					<w:r w:rsidR="000911EF" w:rsidRPr="00F058AA">
//						<w:rPr>
//							<w:rStyle w:val="a5" />
//							<w:noProof />
//						</w:rPr>
//						<w:t>www.</w:t>
//					</w:r>
//					<w:r w:rsidR="000911EF" w:rsidRPr="00F058AA">
//						<w:rPr>
//							<w:rStyle w:val="a5" />
//							<w:rFonts w:hint="eastAsia" />
//							<w:noProof />
//						</w:rPr>
//						<w:t>this</w:t>
//					</w:r>
//					<w:r w:rsidR="000911EF" w:rsidRPr="00F058AA">
//						<w:rPr>
//							<w:rStyle w:val="a5" />
//							<w:noProof />
//						</w:rPr>
//						<w:t xml:space="preserve"> is a test. google.com</w:t>
//					</w:r>
//				</w:hyperlink>
//			</w:p>
		int segIdx = 0;
		String text = "www.this is a test. google.com";
//		String[] segArray = new String[]{"go to www.this is a test. ", "google.com这后面还有其他东西", "第三块东西"};
		String[] segArray = new String[]{"go to www.this is a test. ", "google.com"};
		String seg = "www.this is a test. ";
		List<String> segList = new ArrayList<String>();

		int start = segArray[segIdx].lastIndexOf(seg);
		StringBuffer temSB = new StringBuffer();
		segFor:for(int i = 0; i < segArray.length; i++){
			if (i < segIdx) {
				segList.add(segArray[i]);
			}else if (i == segIdx) {
				if (text.length() != 0) {
					temSB.append(segArray[i]);
					if (text.indexOf(seg) != -1) {
						text = text.substring(seg.length(), text.length());
					}else if(seg.indexOf(text) != -1){
						text = "";
					}
				}else {
					if (temSB.length() != 0) {
						segList.add(temSB.toString());
						temSB = new StringBuffer();
					}
					for(int j = i; j < segArray.length; j ++){
						segList.add(segArray[j]);
					}
					break segFor;
				}
			}else {
				if (text.length() != 0) {
					temSB.append(segArray[i]);
					if (text.indexOf(segArray[i]) != -1) {
						text = text.substring(segArray[i].length(), text.length());
					}else if(segArray[i].indexOf(text) != -1){
						text = "";
					}
				}else {
					if (temSB.length() != 0) {
						segList.add(temSB.toString());
						temSB = new StringBuffer();
					}
					for(int j = i; j < segArray.length; j ++){
						segList.add(segArray[j]);
					}
					break segFor;
				}
			}
			
		}
		if (temSB.length() != 0) {
			segList.add(temSB.toString());
			temSB = new StringBuffer();
		}
		
		segArray = segList.toArray(new String[]{});
		seg = segArray[segIdx].substring(start, segArray[segIdx].length());
		
		for (String str : segArray) {
			System.out.println(str);
		}
		
		System.out.println("-----------------");
		System.out.println("seg =" + seg);
	}
	
}
