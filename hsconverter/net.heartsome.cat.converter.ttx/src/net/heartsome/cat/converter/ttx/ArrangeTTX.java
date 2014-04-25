package net.heartsome.cat.converter.ttx;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * 整体ttx文件，主要内容就是将一个未翻译的ttx文件进行相关的处理，分段，添加tu节点，这样方便进行转换。
 * @author robert	2012-07-16
 */
public class ArrangeTTX {
	private StringSegmenter segmenter;
	private VTDNav sklVN;
	private XMLModifier sklXM;
	/** 当前要处理的 ttx 文件自带的源语言 */
	private String detectedSourceLang;
	/** 当前要处理的 ttx 文件自带的目标语言 */
	private String detectedTargetLang;
	private VTDUtils vu;
	/** 对 Raw 节点下的文本节点单独取出后存放, key值为token */
	private List<Integer> rawTextTokenList;
	/** 单元的起始符，单元为 ut 节点 DisplayText="paragraph" 或者 DisplayText="^" 的区间，单元的结束标志为 <ut Type="end" ... > 或遇到 <Tu> 节点 */
	private boolean start = false;
	private boolean end = false;
	/** 针对每个文本段，是否需要加入segBeanMap中 */
	private boolean needAdd = false;
	
	public ArrangeTTX() { }
	
	// UNDO 这里处理的 paragraph 还有问题，而且问题还插严重。
	public ArrangeTTX(VTDNav sklVN, XMLModifier sklXM, String elementSegmentation, String initSegmenter, 
			String userSourceLang, String catalogue, String detectedSourceLang, String detectedTargetLang) throws Exception {
		
		boolean segByElement = false;
		if (Converter.TRUE.equals(elementSegmentation)) {
			segByElement = true;
		} else {
			segByElement = false;
		}

		if (!segByElement) {
			segmenter = new StringSegmenter(initSegmenter, userSourceLang, catalogue);
		}
		
		this.sklVN = sklVN;
		this.sklXM = sklXM;
		this.detectedSourceLang = detectedSourceLang;
		this.detectedTargetLang = detectedTargetLang;
		System.out.println("detectedTargetLang = " + detectedTargetLang);
		vu = new VTDUtils(sklVN);
		rawTextTokenList = new LinkedList<Integer>();
	}
	
	/**
	 * 生成tu节点
	 */
	public void createTuNode(String skeletonFile) throws Exception{
		//循环每一个文本子节点进行，先对它进行分段，再对分段后的进行包裹 Tu 节点
		// 首先分段，读取每一个单元，在这个单元内进行分段操作。这个单元就是 ut 节点 DisplayText="paragraph" 或者 DisplayText="^" 的区间
		getRawTextToken();
		String xpath = "/TRADOStag/Body/Raw/node()";
		AutoPilot ap = new AutoPilot(sklVN);
		ap.selectXPath(xpath);
		TreeMap<Integer, SegmentBean> segBeanMap = new TreeMap<Integer, SegmentBean>();
		SegmentBean segBean;
		int index = -1;
		while(ap.evalXPath() != -1){
			String nodeName = vu.getCurrentElementName();
			segBean = new SegmentBean();
			
			// 首先处理raw节点的直接子节点
			index = sklVN.getCurrentIndex() - 1;
			if (rawTextTokenList.contains(index)) {
				if (start && !end) {	//处在开始标签与结束标签之前的节点才进行处理
					segBean.setHasTag(false);
					segBean.setParentNodeFrag(sklVN.toRawString(index));
					segBean.setSegment(sklVN.toRawString(index));
					segBeanMap.put(index, segBean);
					//删除这个 raw 节点的文本子节点
					sklXM.updateToken(index, "");
					segBean = new SegmentBean();
				}
			}
			
			// DOLATER 这个地方还没有验证一个文件中间地方出现一个 Tu 节点的情况。
			//处理子节点
			if ("df".equals(nodeName)) {
				analysisDF(sklVN, segBean);
				if (!end && needAdd) {	//如果未结束，并且可以添加进segBeanMap，就添加
					segBean.setParentNodeFrag(vu.getElementFragment());
					segBeanMap.put(sklVN.getCurrentIndex(), segBean);
					//删除这个节点
					sklXM.remove();
				}
				
			}else if ("ut".equals(nodeName)) {
				analysisUT(sklVN, segBean);
				if (!end && needAdd) {	//如果未结束，并且可以添加进segBeanMap，就添加
					segBean.setParentNodeFrag(vu.getElementFragment());
					segBeanMap.put(sklVN.getCurrentIndex(), segBean);
					//删除这个节点
					sklXM.remove();
				}
			}else if ("Tu".equals(nodeName)) {
				end = true;
			}
			
			// 处于单元内部，进行相关处理
			if (end) {
				splitSegment(sklVN, segBeanMap);
			}
		}
		sklXM.output(skeletonFile);
	}
	
	/**
	 * 处理一个单元格区间的每一个节点，
	 * @return true:调用此方法的程序继续执行， false:调用此方法的程序终止执行或跳出当前循环
	 */
	private void analysisDF(VTDNav vn, SegmentBean segBean) throws Exception{
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		String xpath = "./node()|text()";
		ap.selectXPath(xpath);
		int index = -1;
		while(ap.evalXPath() != -1){
			int tokenType = vn.getTokenType(vn.getCurrentIndex());
			if (tokenType == 0) {	//等于0表示为节点
				String nodeName = vu.getCurrentElementName();
				if ("ut".equals(nodeName)) {
					String typeAtt = "";
					if ((index = vn.getAttrVal("Type")) != -1) {
						typeAtt = vn.toRawString(index);
					}
					String displayTextAtt = "";
					if ((index = vn.getAttrVal("DisplayText")) != -1) {
						displayTextAtt = vn.toRawString(index);
					}
					//判断开始与结束点
					if ("start".equals(typeAtt) && !"cf".equals(displayTextAtt)) {
						start = true;	//一个单元的开始
						end = false;
					}else if ("end".equals(typeAtt) && !"cf".equals(displayTextAtt)) {
						start = false;
						end = true;	// 一个单元的结束
					}
					segBean.setTagStr(vu.getElementFragment());
					needAdd = true;
				}else if ("Tu".equals(nodeName)) {	//遇到 Tu 节点，单元结束，end = true，但是 start仍保持之前状态
					needAdd = false;
					end = true;
				}
				segBean.setHasTag(true);	//有标记存在
			}else if (tokenType == 5) {	//等于5表示为文本子节点
				segBean.setSegment(vn.toRawString(vn.getCurrentIndex()));
				System.out.println(vn.toRawString(vn.getCurrentIndex()));
				needAdd = true;
			}
		}
		vn.pop();
	}
	
	/**
	 * 分析 ut 节点，这个节点里面保存的一般都是 cf 标记或者单元内容
	 * @throws Exception
	 */
	private void analysisUT(VTDNav vn, SegmentBean segBean) throws Exception{
		vn.push();
		int index = -1;
		String typeAtt = "";
		if ((index = vn.getAttrVal("Type")) != -1) {
			typeAtt = vn.toRawString(index);
		}
		String displayTextAtt = "";
		if ((index = vn.getAttrVal("DisplayText")) != -1) {
			displayTextAtt = vn.toRawString(index);
		}
		//判断开始与结束点
		if ("start".equals(typeAtt) && !"cf".equals(displayTextAtt)) {
			start = true;	//一个单元的开始
			end = false;
		}else if ("end".equals(typeAtt) && !"cf".equals(displayTextAtt)) {
			start = false;
			end = true;	// 一个单元的结束
		}
		segBean.setTagStr(vu.getElementFragment());
		segBean.setHasTag(true);
		needAdd = true;
		vn.pop();
	}
	
	/**
	 * 开始拆分文本段，从而进行组装
	 * @param vn
	 * @param segBeanMap
	 */
	private void splitSegment(VTDNav vn, TreeMap<Integer, SegmentBean> segBeanMap) throws Exception{
		StringBuffer segSB = new StringBuffer();
		for(Entry<Integer, SegmentBean> entry : segBeanMap.entrySet()){
			if (entry.getValue().getSegment() != null) {
				segSB.append(entry.getValue().getSegment());
			}
		}
		System.out.println("未拆分前的名子=" + segSB.toString());
		// 开始进行拆分
		String[] segArray = segmenter.segment(segSB.toString());
		System.out.println("拆分后的句子=");
		for(String str : segArray){
			System.out.println(str);
		}
		
		List<String> segList = new LinkedList<String>();
		for (String str : segArray) {
			if (str.trim().length() > 0) {
				segList.add(str);
			}
		}

		//拆分之后，就查看拆分后的每个小文本段是否在一个 df 节点或是一个单独的 raw 文本子节点，
		//如果是，直接在里面进行生成tu，如果不是，直接生成一个tu。把这几个小文本段的父节点一下包括进来。
		StringBuffer addSB = new StringBuffer();
		StringBuffer tuSB;
		for (Iterator<Entry<Integer, SegmentBean>> it = segBeanMap.entrySet().iterator(); it.hasNext();) {
			SegmentBean segBean = it.next().getValue();
			tuSB = new StringBuffer();
			if (segBean.getSegment() != null) {
				if (segList.size() > 0) {
					if (segBean.getSegment().trim().equals(segList.get(0).trim())) {
						tuSB.append("<Tu Origin=\"manual\"><Tuv Lang=\"" + detectedSourceLang + "\">");
						tuSB.append(segBean.getSegment() + "</Tuv>");
						tuSB.append("<Tuv Lang=\"" + detectedTargetLang + "\"></Tuv></Tu>");
						addSB.append(segBean.getParentNodeFrag().replace(segBean.getSegment(), tuSB.toString()));
						segList.remove(0);
					} else if (segBean.getSegment().length() > segList.get(0).length()) {
						//这是一个未分割的文本段分割后的段数大于1的情况。
						while(segList.size() > 0 && segBean.getSegment().indexOf(segList.get(0)) != -1){
							tuSB.append("<Tu Origin=\"manual\"><Tuv Lang=\"" + detectedSourceLang + "\">");
							tuSB.append(segList.get(0) + "</Tuv>");
							tuSB.append("<Tuv Lang=\"" + detectedTargetLang + "\"></Tuv></Tu>");
							segList.remove(0);
						}
						addSB.append(segBean.getParentNodeFrag().replace(segBean.getSegment(), tuSB.toString()));
					}else if (segBean.getSegment().length() < segList.get(0).length()) {
						//这种情况是未分割的文本段分割后不足一个文本段的情况
						tuSB.append("<Tu Origin=\"manual\"><Tuv Lang=\"" + detectedSourceLang + "\">");
						tuSB.append(segBean.getParentNodeFrag());
						while(it.hasNext()){
							//segBean.getSegment() == null || (segList.get(0).indexOf(segBean.getSegment()) != -1 &&
							segBean = it.next().getValue();
							if (segBean.getSegment() != null && segList.get(0).indexOf(segBean.getSegment()) == -1) {
								break;
							}
							tuSB.append(segBean.getParentNodeFrag());
						}
						tuSB.append("</Tuv><Tuv Lang=\"" + detectedTargetLang + "\"></Tuv></Tu>");
						addSB.append(tuSB.toString());
						segList.remove(0);
					}
				}
			} else {
				addSB.append(segBean.getParentNodeFrag());
			}
		}
		sklXM.insertBeforeElement(addSB.toString());
		
		System.out.println("-----------------------");
		segBeanMap.clear();
		end = false;	// 处理完后，单元格标识为未结束状态
	}
	
	
	
	/**
	 * 取出 Raw 节点下的文本子节点的 token 值进行存放，单独进行处理
	 */
	private void getRawTextToken() throws Exception{
		AutoPilot ap = new AutoPilot(sklVN);
		String xpath = "/TRADOStag/Body/Raw/text()";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			rawTextTokenList.add(sklVN.getCurrentIndex());
		}
	}
	
}
