package net.heartsome.cat.converter.ttx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.ttx.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.util.ReverseConversionInfoLogRecord;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * trados 2007 的 ttx(tradostag Xliff) 文件逆向转换器。
 * @author robert	2012-07-27
 */
public class Xliff2Ttx implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-ttx";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("ttx.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to TTX Conveter";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 * @param args
	 * @param monitor
	 * @return
	 * @throws ConverterException
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		Xliff2TtxImpl converter = new Xliff2TtxImpl();
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
	 * 逆向转换器实现类
	 * <div style='color:red'>备注：trans-unit的id，对应 ttx 文件的 Tu 节点的id，
	 * 而 xlf 文件的标记 <g id='1'> 对应 ttx 文件的占位符如%%%1%%% 。</div>
	 * @author robert	2012-07-27
	 * @version
	 * @since JDK1.6
	 */
	class Xliff2TtxImpl {
		/** 逆转换p */
		private String outputFile;

		/** The encoding. */
		private String encoding;

		/** ttx 的源语言 */
		private String detectedSourceLang;

		/** ttx 文件的目标语言 */
		private String detectedTargetLang;
		/** 是否是预翻译模式 */
		private boolean isPreviewMode;

		/** 骨架文件的解析游标 */
		private VTDNav outputVN;
		/** 骨架文件的修改类实例 */
		private XMLModifier outputXM;
		/** 骨架文件的查询实例 */
		private AutoPilot outputAP;
		private VTDUtils outputVU;
		/** xliff文件的解析游标 */
		private VTDNav xlfVN;
		/** xliff 文件的查询实例 */
		private AutoPilot xlfAP;
		private static final String nodeXpath = "./node()|text()"; 
		private AutoPilot tagAP;
		private int addedEndTagNum = 0;
		
		/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格 */
		private int workInterval = 1;

		/**
		 * Run.
		 * @param params
		 *            the params
		 * @param monitor
		 *            the monitor
		 * @return the map< string, string>
		 * @throws ConverterException
		 *             the converter exception
		 */
		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			ReverseConversionInfoLogRecord infoLogger = ConverterUtils.getReverseConversionInfoLogRecord();
			infoLogger.startConversion();

			Map<String, String> result = new HashMap<String, String>();
			String sklFile = params.get(Converter.ATTR_SKELETON_FILE);
			String xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			outputFile = params.get(Converter.ATTR_TARGET_FILE);

			String attrIsPreviewMode = params.get(Converter.ATTR_IS_PREVIEW_MODE);
			/* 是否为预览翻译模式 */
			isPreviewMode = attrIsPreviewMode != null && attrIsPreviewMode.equals(Converter.TRUE);

			try {
				infoLogger.logConversionFileInfo(null, null, xliffFile, sklFile);
				// 把转换过程分为两大部分共 10 个任务，其中加载 xliff 文件占 1，替换过程占 9。
				monitor.beginTask(Messages.getString("ttx.Xliff2Ttx.task1"), 10);
				
				//记录加载信息
				infoLogger.startLoadingXliffFile();
				
				String sklTempSkl = "";
				//先将骨架文件的内容拷贝到目标文件，再解析目标文件
				if (encoding.equalsIgnoreCase("utf-8")) {
					copyFile(sklFile, outputFile);
					parseOutputFile(outputFile, xliffFile);
				}else {
					sklTempSkl = File.createTempFile("tempskl", "skl").getAbsolutePath();
					new File(sklTempSkl).deleteOnExit();
					copyFile(sklFile, sklTempSkl);
					parseOutputFile(sklTempSkl, xliffFile);
				}
				parseXlfFile(xliffFile);
				getSrcAndTgtLang();
				infoLogger.endLoadingXliffFile();
				
				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("ttx.cancel"));
				}
				
				ananysisXlfTU(monitor);
				//记录加载完。
				infoLogger.endReplacingSegmentSymbol();
				
				
				// 针对没有目标语言节点为 noLang 的 Tvu，将之 lang 属性变成 ""；
				// 如果 encoding 不是 utf-8，那么将这个临时文件从 utf-8 转换成 encoding 格式
				if (encoding.equalsIgnoreCase("utf-8")) {
					outputXM.output(outputFile);
					parseOutputFile(outputFile, xliffFile);
					deleteTgtLangIfNon();
					outputXM.output(outputFile);
				}else {
					outputXM.output(sklTempSkl);
					parseOutputFile(sklTempSkl, xliffFile);
					deleteTgtLangIfNon();
					outputXM.output(sklTempSkl);
					copyFile(sklTempSkl, outputFile, "UTF-8", encoding);
				}
				//记录完成逆转换
				infoLogger.endConversion();
			} catch (Exception e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("ttx.Xliff2Ttx.msg1"), e);
			} finally {
				monitor.done();
			}
			result.put(Converter.ATTR_TARGET_FILE, outputFile);
			return result;
		}
		
		/**
		 * 进度条前进处理类，若返回false,则标志退出程序的执行
		 * @param monitor
		 * @param traversalTuIndex
		 * @param last
		 * @return ;
		 */
		public boolean monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last){
			if (last) {
				if (traversalTuIndex % workInterval != 0) {
					if (monitor.isCanceled()) {
						return false;
					}
					monitor.worked(1);
				}
			}else {
				if (traversalTuIndex % workInterval == 0) {
					if (monitor.isCanceled()) {
						return false;
					}
					monitor.worked(1);
				}
			}
			return true;
		}

		/**
		 * 解析结果文件（解析时这个结果文件还是一个骨架文件）
		 * @param file
		 * @throws Exception
		 */
		private void parseOutputFile(String file, String xliffFile) throws Exception {
			VTDGen vg = new VTDGen();
			if (vg.parseFile(file, true)) {
				outputVN = vg.getNav();
				outputXM = new XMLModifier(outputVN);
				outputAP = new AutoPilot(outputVN);
				outputVU = new VTDUtils(outputVN);
			}else {
				String errorInfo = MessageFormat.format("文件 {0} 的骨架信息无法解析，逆转换失败！", 
						new Object[]{new File(xliffFile).getName()});
				throw new Exception(errorInfo);
			}
		}

		/**
		 * 解析要被逆转换的xliff文件
		 * @param xliffFile
		 * @throws Exception
		 */
		private void parseXlfFile(String xliffFile) throws Exception {
			VTDGen vg = new VTDGen();
			if (vg.parseFile(xliffFile, true)) {
				xlfVN = vg.getNav();
				xlfAP = new AutoPilot(xlfVN);
			}else {
				String errorInfo = MessageFormat.format("文件 {0} 的骨架信息无法解析，逆转换失败！", 
						new Object[]{new File(xliffFile).getName()});
				throw new Exception(errorInfo);
			}
		}
		
		/**
		 * 分析xliff文件的每一个 trans-unit 节点
		 * <div style='color:red'>备注：trans-unit的id，对应 ttx 文件的 Tu 节点的id，而 xlf 文件的标记 <g id='1'> 对应 ttx 文件的占位符如%%%1%%% 。</div>
		 * @throws Exception
		 */
		private void ananysisXlfTU(IProgressMonitor monitor) throws Exception {
			if (monitor == null) {
				monitor = new NullProgressMonitor(); 
			}
			
			AutoPilot ap = new AutoPilot(xlfVN);
			AutoPilot childAP = new AutoPilot(xlfVN);
			ap.selectXPath("count(/xliff/file/body//trans-unit)");
			int tuTotalSum = (int) ap.evalXPathToNumber();
			if (tuTotalSum > 500) {
				workInterval = tuTotalSum / 500;
			}
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 9, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			subMonitor.beginTask("", tuTotalSum % workInterval == 0 ? (tuTotalSum / workInterval) : (tuTotalSum / workInterval) + 1 );
			
			VTDUtils vu = new VTDUtils(xlfVN);
			String xpath = "/xliff/file/body//trans-unit";
			String srcXpath = "./source";
			String tgtXpath = "./target";
			ap.selectXPath(xpath);
			int attrIdx = -1;
			// trans-unit的id，对应 ttx 文件的 Tu 节点的id
			String segId = "";
			// 添加到 ttx 源文的内容
			String srcText = "";
			// 添加到 ttx 目标的内容
			String tgtText = "";
			List<String> tagBorderList = new LinkedList<String>();
			addedEndTagNum = 0;
			int traversalTuIndex = 0;
			while (ap.evalXPath() != -1) {
				traversalTuIndex ++;
				if ((attrIdx = xlfVN.getAttrVal("id")) == -1) {
					continue;
				}
				
				srcText = "";
				tgtText = "";
				segId = xlfVN.toRawString(attrIdx);
				tagBorderList.clear();
				getStartAndEndTagStr(tagBorderList, segId);
				
				if ((attrIdx = xlfVN.getAttrVal("addedEndTagNum")) != -1) {
					addedEndTagNum = Integer.parseInt(xlfVN.toString(attrIdx));
				}
				
				// 处理source节点
				xlfVN.push();
				childAP.selectXPath(srcXpath);
				if (childAP.evalXPath() != -1) {
					String srcContent = vu.getElementContent();
					srcText = createTTXTuvContent(xlfVN, vu, segId, srcContent, tagBorderList);
					srcText = srcText.replaceAll("(&lt;cf)\\s+(id=')\\d+(')\\s+", "&lt;cf ");
				}
				xlfVN.pop();
				
				// 处理target节点
				xlfVN.push();
				String tgtContent = null;
				childAP.selectXPath(tgtXpath);
				if (childAP.evalXPath() != -1) {
					tgtContent = vu.getElementContent();
					tgtText = createTTXTuvContent(xlfVN, vu, segId, tgtContent, tagBorderList);
					tgtText = tgtText.replaceAll("(&lt;cf)\\s+(id=')\\d+(')\\s+", "&lt;cf ");
				}
				xlfVN.pop();
				
				//判断是否处于锁定状态
				if ((attrIdx = xlfVN.getAttrVal("translate")) != -1) {
					if ("no".equalsIgnoreCase(xlfVN.toRawString(attrIdx))) {
						// DOLATER 这里未处理锁定的相关东西
					}
				}
				
				replaceSegment(segId, srcText, tgtText);
				
				if (!monitorWork(subMonitor, traversalTuIndex, false)) {
					throw new OperationCanceledException(Messages.getString("ttx.cancel"));
				}
			}
			if (!monitorWork(subMonitor, traversalTuIndex, true)) {
				throw new OperationCanceledException(Messages.getString("ttx.cancel"));
			}
			subMonitor.done();
		}

		/**
		 * 生成新的 ttx 文件 tuv 的内容
		 * @param segId		trans-unit 与 Tu 节点的 id
		 * @param content	xlf 节点source 或 target 节点的内容
		 * @param contentSB		新生成的 tuv 节点内容
		 */
		private String createTTXTuvContent(VTDNav vn, VTDUtils vu, String segId,
				String content, List<String> tagBorderList) throws Exception {
			if (content == null || "".equals(content)) {
				return content;
			}
			int index = -1;
			//先处理标记，由R8过来的标记是成对出现的，但是tuv里面的标记很乱，不会成对出现。故找到多余的结束标记进行处理。
//			int tagPairNum = 0;
//			for (int i = 0; i < tagBorderList.size(); i++) {
//				String tagBorder = tagBorderList.get(i);
//				if (i == 0 && "end".equals(tagBorder)) {
//					content = "</g>" + content;
//					while(i + 1 < tagBorderList.size() && "end".equals(tagBorderList.get(i + 1))){
//						content = "</g>" + content;
//						i++;
//					}
//				}else if ("start".equals(tagBorder)) {
//					tagPairNum ++;
//				}else if ("end".equals(tagBorder)) {
//					tagPairNum --;
//				}
//			}
//			if (tagPairNum > 0) {	//结束标记少了，就应该删除多作的<g>
//				while(tagPairNum != 0){
//					if ((index = content.lastIndexOf("</g>")) != -1) {
//						content = content.substring(0, index) + content.substring(index + 4);
//					}
//					tagPairNum--;
//				}
//			}else if (tagPairNum < 0) {	//标记结束点多了<cf>，就应添加<g>，以保持r8与ttx文件标记的对应
//				while(tagPairNum != 0){
//					content = content + "</g>";
//					tagPairNum++;
//				}
//			}
//			
//			
//			//先处理标记，专门处理ttx原文结尾处没有成对的</cf>的情况
//			int start = 0;
//			int end = 0;
//			for (int i = 0; i < tagBorderList.size(); i++) {
//				if ("start".equals(tagBorderList.get(i))) {
//					start ++;
//				}else {
//					if (start > end) {
//						end ++;
//					}
//				}
//			}
//			
			int ends = addedEndTagNum;
			while(ends > 0){
				if ((index = content.lastIndexOf("</g>")) != -1) {
					content = content.substring(0, index) + content.substring(index + 4);
				}
				ends --;
			}
			
			
			//先判断当前节点下有没有子节点，也就是有没有<g>标记，如果没有，直接替换，否则生成新的标记
			if (vu.getChildElementsCount() > 0) {
				index = content.indexOf("<g");
				while(index != -1){
					int idIdx = content.indexOf(">", index);
					String tagStr = content.substring(index, idIdx + 1);
					//处理<g id='null?' />的情况
					if (tagStr.indexOf("/>") != -1) {
						String newTagStr = "<cf" + tagStr.substring(tagStr.indexOf("<g") + 2, tagStr.indexOf("/>")) + ">";
						newTagStr = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">" + TextUtil.cleanSpecialString(newTagStr) + "</ut>" 
							+ "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>";
						content = content.replace(tagStr, newTagStr);
					}else {
						String tagId = getTagId(tagStr.indexOf("/>") == -1 ? (tagStr + "</g>") : tagStr);
//						String tagId = "";
						if ("".equals(tagId)) {
							// 如果找不到相应标记的 id，那么自动将这个标记转换成 ttx 文件的 cf 标记。
							String newTagStr = "<cf" + tagStr.substring(tagStr.indexOf("<g") + 2, tagStr.indexOf(">")) + ">";
							newTagStr = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">" + TextUtil.cleanSpecialString(newTagStr) + "</ut>";
							content = content.replace(tagStr, newTagStr);
						}else {
							String tagParentStr = getTagParent(segId, tagId);
							if ("".equals(tagParentStr)) {
								String newTagStr = "<cf" + tagStr.substring(tagStr.indexOf("<g") + 2, tagStr.indexOf(">")) + ">";
								newTagStr = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">" + TextUtil.cleanSpecialString(newTagStr) + "</ut>";
								content = content.replace(tagStr, newTagStr);
							}else {
								// 如果 cf 标记有父节点（父节点不是tuv），那么直接将占位符替换成这个标记里的内容即可
								int endIdx = content.indexOf("<", idIdx);
								String tagTextStr = content.substring(idIdx + 1, endIdx == -1 ? content.length() : endIdx);
								tagParentStr = tagParentStr.replace("%%%"+tagId+"%%%", tagTextStr);
								content = content.replace(tagStr + tagTextStr, tagParentStr);
							}
						}
					}
					index = content.indexOf("<g");
					
					int aaa = content.indexOf("</g>");
					int b = 0;
					while(aaa != -1){
						b ++;
						aaa = content.indexOf("</g>", aaa + 1);
					}
				}
			}
			
			// 处理ph标记
			index = content.indexOf("<ph");
			while(index != -1){
				String phFrag = content.substring(index, content.indexOf("</ph>", index) + 5);
				content = content.replace(phFrag, replacePH(phFrag));
				index = content.indexOf("<ph", index + 1);
			}
			
			//处理结束标记
			index = content.indexOf("</g>");
			if(index != -1){
				String endTagStr = "<ut Type=\"end\" LeftEdge=\"angle\" DisplayText=\"cf\">&lt;/cf&gt;</ut>";
				content = content.replaceAll("</g>", endTagStr);
			}
			return content;
		}
		
		
		/**
		 * 通过 trans-unit 的 id 及 语言获取 ttx 文件的 tuv 的内容，从而进行组装成新的内容，再将这个新的内容插入到 ttx 所对应的地方
		 */
		private String getTagParent(String segId, String tagId) throws Exception {
			String xpath = "/TRADOStag/Body/Raw//Tu/Tuv[@Lang='" + detectedSourceLang + "']" +
					"//node()[text()='%%%" + tagId + "%%%']";
			String tagParent = "";
			outputAP.selectXPath(xpath);
			if (outputAP.evalXPath() != -1) {
				outputAP.selectXPath("./text()");
				StringBuffer textSB = new StringBuffer();
				while (outputAP.evalXPath() != -1) {
					textSB.append(outputVN.toString(outputVN.getCurrentIndex()));
				}
				// 如果　当前节点里面有多个占位符，那么返回应为空，这是修改　bug Bug #3396 TTX 转换器：无法转换 XLIFF 为目标文件，其中的问题主要是转换回去后，有未替换完的占位符
				int placeHolderNum = 0;
				for(char chr : textSB.toString().toCharArray()){
					if (chr == '%') {
						placeHolderNum ++;
					}
				}
				
				if (placeHolderNum <= 6) {
					tagParent = outputVU.getElementFragment();
				}else {
					tagParent = "";
				}
				
			}
			return tagParent;
		}
		
		/**
		 * 起始标记不会丢失，结束标记会，因此获取出结束标记的数量，
		 * @param tagNumMap
		 */
		private void getStartAndEndTagStr(List<String> tagBorderList, String segId) throws Exception {
			String xpath = "/TRADOStag/Body/Raw//Tu[@id='" + segId + "']" +
					"/Tuv[@Lang='" + detectedSourceLang + "']/descendant::ut[@DisplayText=\"cf\"]";
			outputAP.selectXPath(xpath);
			int index = -1;
			while (outputAP.evalXPath() != -1) {
				if ((index = outputVN.getAttrVal("Type")) != -1) {
					if ("start".equals(outputVN.toRawString(index))) {
						tagBorderList.add("start");
					}else if ("end".equals(outputVN.toRawString(index))) {
						tagBorderList.add("end");
					}
				}
			}
		}
		
		/**
		 * 替换掉骨架文件中的占位符
		 * 
		 * @param segId
		 * @param srcBean
		 * @param tgtbeBean
		 */
		private void replaceSegment(String segId, String srcText, String tgtText) throws Exception {
			String xpath = "/TRADOStag/Body/Raw//Tu[@id='" + segId + "']/Tuv";
			outputAP.selectXPath(xpath);
			int index = -1;
			while (outputAP.evalXPath() != -1) {
				if ((index = outputVN.getAttrVal("Lang")) != -1) {
					String lang = outputVN.toRawString(index);
					if (lang.equals(detectedSourceLang)) {
						String tuvStr = outputVU.getElementHead() + (srcText == null ? "" : srcText) + "</Tuv>";
						outputXM.remove();
						outputXM.insertAfterElement(tuvStr);
					}else if (lang.equals(detectedTargetLang)) {
						if (tgtText != null && !"".equals(tgtText)) {
							String tuvStr = outputVU.getElementHead() + "<df Font=\"Arial Unicode MS\">" + tgtText + "</df>" + "</Tuv>";
							outputXM.remove();
							outputXM.insertAfterElement(tuvStr.getBytes("UTF-8"));
						}
					}
				}
			}
		}
		
		/**
		 * 根据一个<g>标记的头部获取标记的 id 属性值
		 * @param tagStr
		 * @return
		 */
		private String getTagId(String tagStr) throws Exception {
			String tagId = "";
			VTDGen vg = new VTDGen();
			vg.setDoc(tagStr.getBytes());
			vg.parse(false);
			VTDNav vn = vg.getNav();
			tagAP = new AutoPilot(vn);
			tagAP.selectXPath("/g");
			int index = -1;
			if (tagAP.evalXPath() != -1) {
				if ((index = vn.getAttrVal("id")) != -1) {
					tagId = vn.toRawString(index);
				}
			}
			return tagId;
		}
		
		/**
		 * 获取源文件的源语言和目标语言
		 */
		private void getSrcAndTgtLang() throws Exception {
			String srcLang = "";
			String tgtLang = "";
			String xpath = "/TRADOStag/FrontMatter/UserSettings";
			outputAP.selectXPath(xpath);
			int attrIdx = -1;
			if (outputAP.evalXPath() != -1) {
				if ((attrIdx = outputVN.getAttrVal("SourceLanguage")) != -1) {
					srcLang = outputVN.toRawString(attrIdx);
				}
				if ((attrIdx = outputVN.getAttrVal("TargetLanguage")) != -1) {
					tgtLang = outputVN.toRawString(attrIdx);
				}
			}
			detectedSourceLang = srcLang;
			//如果 ttx 的目标文件的目标语言为空，那设成转换的目标语言
			detectedTargetLang = "".equals(tgtLang) ? "noLang" : tgtLang;
		}
		
		/**
		 * 针对没有目标语言节点为 noLang 的 Tvu，将之 lang 属性变成 ""；
		 */
		private void deleteTgtLangIfNon() throws Exception {
			String xpath = "/TRADOStag/Body/Raw//Tu/Tuv[@Lang='noLang']";
			outputAP.selectXPath(xpath);
			while(outputAP.evalXPath() != -1){
				outputXM.updateToken(outputVN.getAttrVal("Lang"), "".getBytes("UTF-8"));
			}
			
			int index = -1;
			xpath = "/TRADOStag/Body/Raw//Tu";
			outputAP.selectXPath(xpath);
			while(outputAP.evalXPath() != -1){
				if ((index = outputVN.getAttrVal("id")) != -1) {
					outputXM.removeAttribute(index - 1);
				}
			}
			
			xpath = "/TRADOStag/Body/Raw//Tu//ut";
			outputAP.selectXPath(xpath);
			while(outputAP.evalXPath() != -1){
				if ((index = outputVN.getAttrVal("id")) != -1) {
					outputXM.removeAttribute(index - 1);
				}
			}
			
		}
		
		/**
		 * 处理 ph 节点
		 * @return
		 * @throws Exception
		 */
		private String replacePH(String phFrag) throws Exception{
			String replaceText = "";
			VTDGen vg = new VTDGen();
			vg.setDoc(phFrag.getBytes());
			vg.parse(true);
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			ap.selectXPath("/ph");
			String phContent = "";
			int attrIdx = -1;
			String utType = "";
			String edgeStr = "";
			if (ap.evalXPath() != -1) {
				phContent = vu.getElementContent();
				// 有type属性的，一般是cf标记
				if ((attrIdx = vn.getAttrVal("type")) != -1) {
					if ("cf".equals(vn.toString(attrIdx))) {
						// 这个cf是开始还是结束&lt;cf size=&quot;11&quot; complexscriptssize=&quot;11&quot;&gt;
						if (phContent.indexOf("&lt;cf") != -1) {
							utType = "start";
							edgeStr = "RightEdge=\"angle\"";
						}else {
							utType = "end";
							edgeStr = "LeftEdge=\"angle\"";
						}
						replaceText += "<ut Type=\"" + utType + "\" "+ edgeStr +" DisplayText=\"cf\">" + phContent + "</ut>";
					}
				}else {
					//没有type的，是其他标记，如&lt;symbol font=&quot;Symbol&quot; character=&quot;F0E2&quot;/&gt
					String tagName = "";
					int startIdx = -1;
					int endIdx = -1;
					// 针对起始标记如&lt;symbol font=&quot;Symbol&quot; character=&quot;F0E2&quot;/&gt
					if ((startIdx = phContent.trim().indexOf("&lt;")) != -1) {
						//针对结束标记如&lt;/null?&gt;
						if ("/".equals(phContent.trim().substring(startIdx + 4, startIdx + 5))) {
							tagName = phContent.trim().substring(startIdx + 5, phContent.trim().indexOf("&gt;"));
							utType = "end";
							edgeStr = " LeftEdge=\"angle\"";
						}else {
							if ((endIdx = phContent.trim().indexOf(" ")) != -1) {
								tagName = phContent.trim().substring(startIdx + 4, endIdx);
								if (phContent.indexOf("/&gt") != -1) {
									utType = "";
								}else {
									utType = "start";
									edgeStr = " RightEdge=\"angle\"";
								}
							}else if((endIdx = phContent.trim().indexOf("/")) != -1) {
								//针对没有空格的如<ph>&lt;field/&gt;</ph>
								tagName = phContent.trim().substring(startIdx + 4, phContent.trim().indexOf("/"));
								utType = "";
							}else{
								//针对 &lt;field&gt;
								tagName = phContent.trim().substring(startIdx + 4, phContent.trim().indexOf("&gt"));
								utType = "start";
								edgeStr = " RightEdge=\"angle\"";
							}	
						}
					}
					String utTypeAttrStr = utType.length() > 0 ? " Type=\""+utType+"\"" : "";
					replaceText = "<ut"+ utTypeAttrStr + edgeStr +" DisplayText=\"" + tagName + "\">" + phContent + "</ut>";
				}
			}
			return replaceText;
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
	
	
	private static void copyFile(String oldFile, String newFilePath,
			String strOldEncoding, String strNewEncoding) throws Exception {
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamRead = null;
		BufferedReader bufferRead = null;

		BufferedWriter newFileBW = null;
		OutputStreamWriter outputStreamWriter = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileInputStream = new FileInputStream(oldFile);
			inputStreamRead = new InputStreamReader(fileInputStream,
					strOldEncoding);
			bufferRead = new BufferedReader(inputStreamRead);

			fileOutputStream = new FileOutputStream(newFilePath, false);
			outputStreamWriter = new OutputStreamWriter(fileOutputStream,
					strNewEncoding);
			newFileBW = new BufferedWriter(outputStreamWriter);

			String strTSVLine = "";
			
			while ((strTSVLine = bufferRead.readLine()) != null) {
				if (strTSVLine.equals("")) {
					continue;
				}
				newFileBW.write(strTSVLine.replaceAll("Shift_JIS", "UTF-8"));
				newFileBW.write("\n");
			}
		} finally {
			if (bufferRead != null)
				bufferRead.close();
			if (newFileBW != null) {
				newFileBW.flush();
				newFileBW.close();
			}
		}
	}
	

	public static void main(String[] args) {
//		String content = "<g id='2'>this is a test</g>asdf as sad ";
//		content = content.substring(0, content.lastIndexOf("</g>")) + content.substring(content.lastIndexOf("</g>") + 4);
//		System.out.println(content);
		
//		String content = "<g id='2'>this is a test</g>asdf as sad</g> ";
//		int index = content.indexOf("</g>");
//		content = "<cf" + content.substring(content.indexOf("<g") + 2, content.indexOf(">")) + ">";
//		System.out.println(content);
		
		int index = -1;
//		String content = "<g id='2' size=\"11\" complexscriptssize=\"11\" complexscriptsbold=\"on\" bold=\"on\" superscript=\"on\"><ph>&lt;symbol font=&quot;Symbol&quot; character=&quot;F0E2&quot;/&gt;</ph></g>";
//		String content = "<g id='2' size=\"11\" complexscriptssize=\"11\" complexscriptsbold=\"on\" bold=\"on\" superscript=\"on\"><ph>&lt;field/&gt;</ph></g>";
//		String content = "<g id='2' size=\"11\" complexscriptssize=\"11\" complexscriptsbold=\"on\" bold=\"on\" superscript=\"on\"><ph>&lt;/cf&gt;</ph></g><ph>&lt;field/&gt;</ph>";
		String content = "<g id='2' size=\"11\" complexscriptssize=\"11\" complexscriptsbold=\"on\" bold=\"on\" superscript=\"on\"><ph>&lt;/cf&gt;</ph></g>这后面是个cf标记了哦。<ph type='cf'>&lt;/cf&gt;</ph>";
		index = content.indexOf("<ph");
		while(index != -1){
			String phFrag = content.substring(index, content.indexOf("</ph>", index) + 5);
			
			System.out.println(phFrag);
			try {
				VTDGen vg = new VTDGen();
				vg.setDoc(phFrag.getBytes());
				vg.parse(true);
				VTDNav vn = vg.getNav();
				AutoPilot ap = new AutoPilot(vn);
				VTDUtils vu = new VTDUtils(vn);
				ap.selectXPath("/ph");
				String replaceText = "";
				String phContent = "";
				int attrIdx = -1;
				if (ap.evalXPath() != -1) {
					phContent = vu.getElementContent();
//					System.out.println("phContent = " + phContent);
					// 有type属性的，一般是cf标记
					if ((attrIdx = vn.getAttrVal("type")) != -1) {
						if ("cf".equals(vn.toString(attrIdx))) {
							String utType = "";
							// 这个cf是开始还是结束&lt;cf size=&quot;11&quot; complexscriptssize=&quot;11&quot;&gt;
							if (phContent.indexOf("&lt;cf") != -1) {
								utType = "start";
							}else {
								utType = "end";
							}
							replaceText = "<ut Type=\"" + utType + "\" RightEdge=\"angle\" DisplayText=\"cf\">" + phContent + "</ut>";
						}
					}else {
						//没有type的，是其他标记，如&lt;symbol font=&quot;Symbol&quot; character=&quot;F0E2&quot;/&gt
						String tagName = "";
						int startIdx = -1;
						int endIdx = -1;
						// 针对起始标记如&lt;symbol font=&quot;Symbol&quot; character=&quot;F0E2&quot;/&gt
						if ((startIdx = phContent.trim().indexOf("&lt;")) != -1) {
							//针对结束标记如&lt;/null?&gt;
							if ("/".equals(phContent.trim().substring(startIdx + 4, startIdx + 5))) {
								tagName = phContent.trim().substring(startIdx + 5, phContent.trim().indexOf("&gt;"));
							}else {
								if ((endIdx = phContent.trim().indexOf(" ")) != -1) {
									tagName = phContent.trim().substring(startIdx + 4, endIdx);
								}else {
									//针对没有空格的如<ph>&lt;field/&gt;</ph>
									tagName = phContent.trim().substring(startIdx + 4, phContent.trim().indexOf("/"));
								}	
							}
//							System.out.println("tagName = '" + tagName + "'");
						}
//						System.out.println( "tagName = " + tagName);
						replaceText = "<ut DisplayText=\"" + tagName + "\">" + phContent + "</ut>";
						
					}
					content = content.replace(phFrag, replaceText);
//					System.out.println(content);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			index = content.indexOf("<ph", index + 1);
		}
//		System.out.println(content);
		
		
		String tagStr = "<g id='1' size='12'/>";
		String newTagStr = "<cf" + tagStr.substring(tagStr.indexOf("<g") + 2, tagStr.indexOf("/>")) + ">";
		newTagStr = "<ut Type=\"start\" RightEdge=\"angle\" DisplayText=\"cf\">" + TextUtil.cleanSpecialString(newTagStr) + "</ut>";
//		System.out.println("newTagStr = " + newTagStr);
		
	}
}
