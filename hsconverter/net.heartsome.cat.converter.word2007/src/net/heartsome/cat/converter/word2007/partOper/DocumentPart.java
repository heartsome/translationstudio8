package net.heartsome.cat.converter.word2007.partOper;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.word2007.PartOperate;
import net.heartsome.cat.converter.word2007.XliffInputer;
import net.heartsome.cat.converter.word2007.XliffOutputer;
import net.heartsome.cat.converter.word2007.common.DocxConverterException;
import net.heartsome.cat.converter.word2007.common.PathConstant;
import net.heartsome.cat.converter.word2007.common.PathUtil;
import net.heartsome.cat.converter.word2007.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * 文件 *.zip/word/document.xml 的处理类，主要是获取内容，添加占位符<br/>也是处理所有文件的入口类。
 * @author robert	2012-08-08
 */
public class DocumentPart extends PartOperate {
	private PathUtil pathUtil;
	/** 主文档与其他文件的关联类实例 */
	private DocumentRelation docRel;
	private String inputFile;

	private FooterNodesPart footerNodesPart;
	private CommentsPart commentsPart;
	private EndNotesPart endNotesPart;
	private IProgressMonitor monitor;
	
	/** 这是进度条的前进间隔，也就是当循环多少个 w:p 节点后前进一格 */
	private static int workInterval = 1;
	
	/**
	 * 正转换的构造函数
	 * @param partPath
	 * @param pathUtil
	 * @param xlfOutput
	 * @param segmenter
	 * @throws Exception
	 */
	public DocumentPart(String partPath, PathUtil pathUtil, XliffOutputer xlfOutput, 
			StringSegmenter segmenter, String inputFile, IProgressMonitor monitor) throws Exception {
		super(partPath, xlfOutput, segmenter);
		this.pathUtil = pathUtil;
		this.inputFile = inputFile;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		this.monitor = monitor;
		
		init();
	}
	
	/**
	 * 逆转换用到的构造函数
	 * @param partPath
	 * @param xlfInput
	 */
	public DocumentPart(String partPath, XliffInputer xlfInput, IProgressMonitor monitor) throws Exception {
		super(partPath, xlfInput);
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		this.monitor = monitor;
		
		init();
	}

	/**
	 * 初始化，解析文件
	 */
	private void init() throws Exception {
		nameSpaceMap = new HashMap<String, String>();
		nameSpaceMap.put("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		nameSpaceMap.put("v", "urn:schemas-microsoft-com:vml");
		loadFile(nameSpaceMap);
	}
	
	
	/**
	 * 清除一些无用的节点或属性，比如　<w:rFonts w:hint="eastAsia" /> 中的　w:hint、拼写检查
	 * <w:proofErr w:type="spellStart" />	<w:proofErr w:type="spellEnd" />
	 * 其他的可以进行补充
	 * 
	 * 以及，处理软回车的情况。将软回车的标识符单独成　w:r。
	 */
	public void clearNoUseNodeAndDealBR() throws Exception{
		ap.selectXPath("/w:document/w:body/descendant::w:p//w:proofErr[@w:type='spellStart' or @w:type='spellEnd']");
		while(ap.evalXPath() != -1){
			xm.remove();
		}
		
		ap.selectXPath("/w:document/w:body/descendant::w:p//w:rFonts/@w:hint");
		while(ap.evalXPath() != -1){
			xm.remove();
		}
		
//		// 处理软回车
//		ap.selectXPath("/w:document/w:body/descendant::w:p//w:r[w:br]");
//		while (ap.evalXPath() != -1) {
//			String fragment = vu.getElementFragment();
//			String brFrag = null;
//			String textFrag = null;
//			vn.push();
//			childAP.selectXPath("./w:br");
//			if (childAP.evalXPath() != -1) {
//				brFrag = vu.getElementFragment();
//			}
//			vn.pop();
//			vn.push();
//			childAP.selectXPath("./w:t");
//			if (childAP.evalXPath() != -1) {
//				textFrag = vu.getElementFragment();
//			}
//			vn.pop();
//			
//			if (!(textFrag == null || textFrag.trim().isEmpty())) {
//				StringBuffer sb = new StringBuffer();
//				sb.append(fragment.replace(textFrag, "<w:t></w:t>"));
//				sb.append(fragment.replace(brFrag, ""));
//				xm.remove();
//				xm.insertAfterElement(sb.toString());
//			}
//		}
		
		xm.output(partPath);
		super.loadFile(super.nameSpaceMap);
	}
	
	
	@Override
	public void converter() throws Exception {
		// 第一步是判断当前文档是否有未接受修订的文本，如果有，提示
		if (validIsRevision()) {
			String messages = MessageFormat.format(Messages.getString("docx2Xlf.msg2"),
					new File(inputFile).getName());
			throw new DocxConverterException(messages);
		}
		
		// 第一步先解析节点 w:sectPr 获取页眉与页脚的信息
		monitor.setTaskName(Messages.getString("docx2Xlf.task1"));
		operateHeaderAndFooter();
		monitor.worked(1);
		// 获取主文档的内容
		getDocumentContent();

	}
	
	/**
	 * 判断是否接修个修订
	 * @return
	 */
	private boolean validIsRevision() throws Exception {
		String xpath = "/w:document/w:body/descendant::w:ins/w:r/w:t[text()!='']";
		ap.selectXPath(xpath);
		if (ap.evalXPath() != -1) {
			return true;
		}
		return false;
	}
	
	public void testSegFile() throws Exception {
		// 处理的单元为 w:p
		String xpath = "/w:document/w:body/descendant::w:p";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			// 开始分析所循环的每一个 w:p 节点
			analysisNodeP();
		}
		xm.output(partPath);
	}
	
	/**
	 * 获取页眉与页脚的信息，依据主文档的 W:sectPr 节点里面页眉与页脚的顺序。
	 * @throws Exception
	 */
	private void operateHeaderAndFooter() throws Exception {
		String xpath = "/w:document/w:body/descendant::w:sectPr/descendant::node()[name()='w:headerReference' or name()='w:footerReference']";
		ap.selectXPath(xpath);
		Map<String, String> idMap = new LinkedHashMap<String, String>();
		int index = -1;
		while (ap.evalXPath() != -1) {
			String nodeName = vn.toString(vn.getCurrentIndex());
			if((index = vn.getAttrVal("r:id")) != -1){
				idMap.put(vn.toString(index), nodeName);
			}
		}
		
		// 根据获取出的 id 去document.xml.rels 文件获取对应的页眉与页脚的文件路径
		pathUtil.setSuperRoot();
		String docRelPath = pathUtil.getPackageFilePath(PathConstant.DOCUMENTRELS, false);
		docRel = new DocumentRelation(docRelPath, pathUtil);
		
		// 测试单个文件。 header1.xml
//		HeaderPart headerPart = new HeaderPart("/home/robert/Desktop/header1.xml", xlfOutput, segmenter);
//		headerPart.run();
		
		// 新建一个 footerList ，主要是为了先解析页眉，后解析页脚。
		List<String> footerList = new LinkedList<String>();
		for (Entry<String, String> entry : idMap.entrySet()) {
			String hfPartPath = docRel.getPartPathById(entry.getKey());
			if ("w:headerReference".equals(entry.getValue())) {
				HeaderPart headerPart = new HeaderPart(hfPartPath, xlfOutput, segmenter);
				headerPart.converter();
			}else if ("w:footerReference".equals(entry.getValue())) {
				footerList.add(hfPartPath);
			}
		}
		for (String footerPartPath : footerList) {
			FooterPart footerPart = new FooterPart(footerPartPath, xlfOutput, segmenter);
			footerPart.converter();
		}
	}
	
	
	/**
	 * 获取文本档 document.xml 的内容。
	 */
	private void getDocumentContent() throws Exception {
		// 先处理可翻译属性
		operateTransAttributes("/w:document/w:body/descendant::w:p/w:r/w:pict/v:shape/@alt");
		//String xpath="/root/descendant::a";
		String xpath = "/w:document/w:body/descendant::w:p";
		int allPSum = getNodeCount(xpath);
		initWorkInterval(allPSum);
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 17, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		subMonitor.beginTask("", 
				allPSum % workInterval == 0 ? (allPSum / workInterval) : (allPSum / workInterval) + 1);
		subMonitor.setTaskName(Messages.getString("docx2Xlf.task2"));
		
		int traveledPIdx = 0;
		// 处理的单元为 w:p
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
//			String pid = vn.toString(vn.getAttrVal("w:rsidRDefault"));
//			if ("00320541".equals(pid)) {
//				System.out.println("开始处理了");
//			}
//			System.out.println("pId =" + pid);
			
			traveledPIdx ++;
			
			List<String> transAttrList = new LinkedList<String>();
			
			vn.push();
			// 寻找可翻译的属性，并添加到 transAttrList 中，这时的属性值已经为占位符了。
			childAP.selectXPath("./w:r/w:pict/v:shape/@alt");
			while(childAP.evalXPath() != -1){
				String altText = vn.toRawString(vn.getCurrentIndex() + 1);
				transAttrList.add(altText);
			}
			vn.pop();
			
			// 寻找当前 w:p 节点是否有脚注，如果有，则最后时添加
			List<String> footerNodesIdList = new LinkedList<String>();
			getFooterNodesId(footerNodesIdList);
			
			// 录找当前 w:p 节点是否有批注，如果有，则最后时添加
			List<String> commentsIdList = new LinkedList<String>();
			getCommentsId(commentsIdList);

			// 录找当前 w:p 节点是否有尾注，如果有，则最后时添加
			List<String> endNotesIdList = new LinkedList<String>();
			getEndNotesId(endNotesIdList);
			
			// 开始分析所循环的每一个 w:p 节点
			analysisNodeP();
			
			// transAttrPlaceHolderStr 为可翻译属性的占位符，通过占位符获取其代替的值，再将值进行分割后写入 trans-unit 中。
			if (transAttrList.size() > 0) {
				for (String transAttrPlaceHolderStr : transAttrList) {
					String transAttrStr = translateAttrMap.get(transAttrPlaceHolderStr);
					if (transAttrStr != null) {
						String segIdStr = getSegIdFromPlaceHoderStr(transAttrPlaceHolderStr);
						String[] segs = segmenter.segment(transAttrStr);
						for(String seg : segs){
							// 生成 trans-unit 节点
							xlfOutput.addTransUnit(seg, segIdStr);
						}
					}
				}
			}
			
			// 处理脚注
			if (footerNodesIdList.size() > 0) {
				operateFooterNodes(footerNodesIdList);
			}
			// 处理批注
			if (commentsIdList.size() > 0) {
				operateComments(commentsIdList);
			}
			// 处理尾注
			if (endNotesIdList.size() > 0) {
				operateEndNotes(endNotesIdList);
			}
			
			monitorWork(subMonitor, traveledPIdx, false);
				
		}
		
		monitorWork(subMonitor, traveledPIdx, true);
		subMonitor.done();
		
		
		//结束时，先保存修改 footerNodes.xml 文件
		if (footerNodesPart != null) {
			footerNodesPart.outputPart();
		}
		if (commentsPart != null) {
			commentsPart.outputPart();
		}
		if (endNotesPart != null) {
			endNotesPart.outputPart();
		}
		
		xm.output(partPath);
	}
	
	/**
	 * 获取脚注的ID
	 * @throws Exception
	 */
	private void getFooterNodesId(List<String> footerNodesIdList) throws Exception {
		otherAP.selectXPath("./w:r/w:footnoteReference/@w:id");
		while(otherAP.evalXPath() != -1) {
			String id = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(id) && id != null) {
				footerNodesIdList.add(id);
			}
		}
	}
	
	/**
	 * 获取批注的id
	 * @param commentsIdList
	 * @throws Exception
	 */
	private void getCommentsId(List<String> commentsIdList) throws Exception {
		otherAP.selectXPath("./w:r/w:commentReference/@w:id");
		while(otherAP.evalXPath() != -1) {
			String id = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(id) && id != null) {
				commentsIdList.add(id);
			}
		}
	}
	
	/**
	 * 获取尾注的 id
	 */
	private void getEndNotesId(List<String> endNotesIdList) throws Exception {
		otherAP.selectXPath("./w:r/w:endnoteReference/@w:id");
		while(otherAP.evalXPath() != -1) {
			String id = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(id) && id != null) {
				endNotesIdList.add(id);
			}
		}
	}
	
	/**
	 * 根据id去处理脚注
	 * @param footerNodesIdList
	 * @throws Exception
	 */
	private void operateFooterNodes(List<String> footerNodesIdList) throws Exception {
		if (footerNodesPart == null) {
			pathUtil.setSuperRoot();
			String footerNodesPartPath = pathUtil.getPackageFilePath(PathConstant.FOOTNOTES, false);
			footerNodesPart = new FooterNodesPart(footerNodesPartPath, xlfOutput, segmenter);
			footerNodesPart.converter();
		}
		for (String footerNodesId : footerNodesIdList) {
			footerNodesPart.operateFooterNodesContent(footerNodesId);
		}
	}
	
	/**
	 * 根据id去处理批注
	 * @param commentsIdList
	 * @throws Exception
	 */
	private void operateComments(List<String> commentsIdList) throws Exception {
		if (commentsPart == null) {
			pathUtil.setSuperRoot();
			String commentsPartPath = pathUtil.getPackageFilePath(PathConstant.COMMENTS, false);
			commentsPart = new CommentsPart(commentsPartPath, xlfOutput, segmenter);
			commentsPart.converter();
		}
		for (String footerNodesId : commentsIdList) {
			commentsPart.operateCommentsContent(footerNodesId);
		}
	}
	
	/**
	 * 根据id去处理尾注
	 * @param commentsIdList
	 * @throws Exception
	 */
	private void operateEndNotes(List<String> endNotesIdList) throws Exception {
		if (endNotesPart == null) {
			pathUtil.setSuperRoot();
			String commentsPartPath = pathUtil.getPackageFilePath(PathConstant.ENDNOTES, false);
			endNotesPart = new EndNotesPart(commentsPartPath, xlfOutput, segmenter);
			endNotesPart.converter();
		}
		for (String endNotesId : endNotesIdList) {
			endNotesPart.operateEndNotesContent(endNotesId);
		}
	}

	/**
	 * 初始化进度条前进前隔值，使之总值不大于五百。
	 */
	private void initWorkInterval(int allPSum){
		if (allPSum > 500) {
			workInterval = allPSum / 500;
		}
	}
	
	/**
	 * 获取某节点的总数
	 * @return
	 */
	public int getNodeCount(String xpath){
		int nodeNum = 0;
		try {
			ap.selectXPath("count(" + xpath + ")");
			nodeNum = (int) ap.evalXPathToNumber();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return nodeNum;
	}
	
	/**
	 * 进度条前进处理类，若返回false,则标志退出程序的执行
	 * @param monitor
	 * @param traverPIdx
	 * @param last
	 * @return ;
	 */
	public boolean monitorWork(IProgressMonitor monitor, int traverPIdx, boolean last){
		if (last) {
			if (traverPIdx % workInterval != 0) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("docxConvert.task3"));
				}
				monitor.worked(1);
			}
		}else {
			if (traverPIdx % workInterval == 0) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException(Messages.getString("docxConvert.task3"));
				}
				monitor.worked(1);
			}
		}
		return true;
		
	}
	
// ------------------------------------------  下面是逆转换的代码 ------------------------------------------------
	@Override
	public void reverseConvert() throws Exception {
		// 处理的单元为 w:p
		String xpath = "/w:document/w:body/descendant::w:p";
		
		int allPSum = getNodeCount(xpath);
		initWorkInterval(allPSum);
		
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 12, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		subMonitor.beginTask("", 
				allPSum % workInterval == 0 ? (allPSum / workInterval) : (allPSum / workInterval) + 1);
		subMonitor.setTaskName(Messages.getString("xlf2Docx.task5"));
		
		ap.selectXPath(xpath);
		int traverPIdx = 0;
		while(ap.evalXPath() != -1){
			traverPIdx ++;
			analysisReversePnode();
			
			monitorWork(subMonitor, traverPIdx, false);
		}
		xm.output(partPath);
		monitorWork(subMonitor, traverPIdx, true);
		subMonitor.done();
		
		
		//再处理可翻译属性
		xpath = "/w:document/w:body/descendant::w:p/w:r/w:pict/v:shape/@alt";
		reverseTranslateAttributes(xpath);
		monitor.worked(1);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException(Messages.getString("docxConvert.task3"));
		}
	}
	
	

}
