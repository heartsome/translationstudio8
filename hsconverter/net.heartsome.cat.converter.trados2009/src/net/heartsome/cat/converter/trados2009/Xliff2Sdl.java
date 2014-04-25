package net.heartsome.cat.converter.trados2009;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.trados2009.resource.Messages;
import net.heartsome.cat.converter.util.CalculateProcessedBytes;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Element;
import net.heartsome.xml.vtdimpl.VTDUtils;

/**
 * R8的XLIFF文件转换成trados 2009 的 xliff文件
 * @author robert	2012-06-25
 */
public class Xliff2Sdl implements Converter{
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "sdlxliff";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.SDL");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to SDLXLIFF Conveter";
	
	
	public Map<String, String> convert(Map<String, String> args,
			IProgressMonitor monitor) throws ConverterException {
		Xliff2SdlImpl impl = new Xliff2SdlImpl();
		return impl.run(args, monitor); 
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
	
	class Xliff2SdlImpl{
		/** The segments. */
		private Hashtable<String, Element> segments;

		/** 逆转换的结果文件 */
		private String outputFile;

		/** The encoding. */
		private String encoding;

		/** The catalogue. */
		private Catalogue catalogue;

		/** The detected source lang. */
		private String detectedSourceLang;

		/** The detected target lang. */
		private String detectedTargetLang;

		/** The fr. */
		private InputStream fr = null;

		/** The br. */
		private BufferedReader br = null;

		/** The bos. */
		private BufferedWriter bos = null;

		/** The os. */
		private OutputStream os = null;

		/** The started. */
		private boolean started = false;

		/** The id. */
		private String id = ""; //$NON-NLS-1$

		/** The header. */
		private StringBuffer header;

		/** The end header. */
		private boolean endHeader = false;

		/** The write source. */
		private boolean writeSource = false;

		/** The has tu tag. */
		private boolean hasTuTag = false;

		/** The pre seg id. */
		private String preSegID = ""; //$NON-NLS-1$

		// 计算替换进度的对象
		private CalculateProcessedBytes cpb;

		// 替换过程的进度监视器
		private IProgressMonitor replaceMonitor;
		private boolean isPreviewMode;
		
		/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格，针对匹配 */
		private int workInterval = 1;
		
		/** 骨架文件的解析游标 */
		private VTDNav outputVN;
		/** 骨架文件的修改类实例 */
		private XMLModifier outputXM;
		/** 骨架文件的查询实例 */
		private AutoPilot outputAP;
		/** xliff文件的解析游标 */
		private VTDNav xlfVN;
		/** 全局变量 */
		private List<CommentBean> fileCommentsList = new LinkedList<CommentBean>();
		/** 这是map对象是存储的要添加的批注，key为要添加的批注的ID, */
		private Map<String, List<CommentBean>> commentMap = new HashMap<String, List<CommentBean>>();

		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
			IProgressMonitor subMonitor = null;
			// 把转换过程分为两大部分共 10 个任务，其中加载 xliff 文件占 4，替换过程占 6。
			monitor.beginTask("Converting...", 10);
			Map<String, String> result = new HashMap<String, String>();
			String sklFile = params.get(Converter.ATTR_SKELETON_FILE);
			String xliffFile = params.get(Converter.ATTR_XLIFF_FILE);
			encoding = params.get(Converter.ATTR_SOURCE_ENCODING);
			outputFile = params.get(Converter.ATTR_TARGET_FILE);
			
			try {
				//先将骨架文件的内容拷贝到目标文件，再解析目标文件
				copyFile(sklFile, outputFile);
				parseOutputFile(outputFile, xliffFile);
				parseXlfFile(xliffFile);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
				
				subMonitor = new SubProgressMonitor(monitor, 9);
				ananysisXlfTU(subMonitor);
				subMonitor.done();
				//生成批注节点
				createComments();
				
				outputXM.output(outputFile);
			} catch (Exception e) {
				e.printStackTrace();
				String errorTip = "XLIFF 转换成 trados 2009 双语文件失败！" + "\n" + e.getMessage();
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e);
			}finally{
				if (subMonitor != null) {
					subMonitor.done();
				}
				monitor.done();
			}
			return result;
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
				outputAP.declareXPathNameSpace("sdl", "http://sdl.com/FileTypes/SdlXliff/1.0");
			}else {
				String errorInfo = MessageFormat.format(Messages.getString("sdl.parse.msg2"), 
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
			}else {
				String errorInfo = MessageFormat.format(Messages.getString("sdl.parse.msg1"), 
						new Object[]{new File(xliffFile).getName()});
				throw new Exception(errorInfo);
			}
		}
		
		/**
		 * 分析xliff文件的每一个 trans-unit 节点
		 * @throws Exception
		 */
		private void ananysisXlfTU(IProgressMonitor monitor) throws Exception {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			
			AutoPilot ap = new AutoPilot(xlfVN);
			AutoPilot childAP = new AutoPilot(xlfVN);
			VTDUtils vu = new VTDUtils(xlfVN);
			
			String xpath = "count(/xliff/file/body//trans-unit)";
			ap.selectXPath(xpath);
			int totalTuNum = (int)ap.evalXPathToNumber();
			if (totalTuNum > 500) {
				workInterval = totalTuNum / 500;
			}
			int matchWorkUnit = totalTuNum % workInterval == 0 ? (totalTuNum / workInterval) : (totalTuNum / workInterval) + 1;
			monitor.beginTask("", matchWorkUnit);
			
			
			xpath = "/xliff/file/body//trans-unit";
			String srcXpath = "./source";
			String tgtXpath = "./target";
			ap.selectXPath(xpath);
			int attrIdx = -1;
			//trans-unit的id，对应sdl文件的占位符如%%%1%%% 。
			String segId = "";
			TuMrkBean srcBean = null;
			TuMrkBean tgtBean = null;
			int traversalTuIndex = 0;
			while (ap.evalXPath() != -1) {
				traversalTuIndex ++;
				if ((attrIdx = xlfVN.getAttrVal("id")) == -1) {
					continue;
				}
				srcBean = new TuMrkBean();
				tgtBean = new TuMrkBean();
				segId = xlfVN.toString(attrIdx);
				
				
				//处理source节点
				xlfVN.push();
				childAP.selectXPath(srcXpath);
				if (childAP.evalXPath() != -1) {
					String srcContent = vu.getElementContent();
					srcContent = srcContent == null ? "" : srcContent;
					srcBean.setContent(srcContent);
					srcBean.setSource(true);
				}
				xlfVN.pop();
				
				//处理target节点
				String status = "";	//状态，针对target节点，空字符串为未翻译
				xlfVN.push();
				tgtBean.setSource(false);
				String tgtContent = null;
				childAP.selectXPath(tgtXpath);
				if (childAP.evalXPath() != -1) {
					tgtContent = vu.getElementContent();
					
					if ((attrIdx = xlfVN.getAttrVal("state")) != -1) {
						status = xlfVN.toString(attrIdx);
					}
				}
				tgtContent = tgtContent == null ? "" : tgtContent;
				tgtBean.setContent(tgtContent);
				xlfVN.pop();
				
				//处理批注
				getNotes(xlfVN, tgtBean);
				
				//判断是否处于锁定状态
				if ((attrIdx = xlfVN.getAttrVal("translate")) != -1) {
					if ("no".equalsIgnoreCase(xlfVN.toString(attrIdx))) {
						tgtBean.setLocked(true);
					}
				}
				//判断是否处于批准状态，若是签发，就没有必要判断了，因为签发了的一定就批准了的
				if (!"signed-off".equalsIgnoreCase(status)) {
					if ((attrIdx = xlfVN.getAttrVal("approved")) != -1) {
						if ("yes".equalsIgnoreCase(xlfVN.toString(attrIdx))) {
							status = "approved";	//批准
						}
					}
				}
				tgtBean.setStatus(status);
				replaceSegment(segId, srcBean, tgtBean);
				
				monitorWork(monitor, traversalTuIndex, false);
			}
			monitorWork(monitor, traversalTuIndex, true);
		}
		
		/**
		 * 获取 R8 xliff文件的所有批注信息
		 * @param vn
		 * @param tgtbeBean
		 */
		private void getNotes(VTDNav vn, TuMrkBean tgtbeBean) throws Exception {
			vn.push();
			List<CommentBean> segCommentList = new LinkedList<CommentBean>();
			AutoPilot ap = new AutoPilot(vn);
			String xpath = "./note";
			ap.selectXPath(xpath);
			int atttIdx = -1;
			CommentBean bean;
			while(ap.evalXPath() != -1){
				boolean isCurrent = true;
				if ((atttIdx = vn.getAttrVal("hs:apply-current")) != -1) {
					if ("no".equalsIgnoreCase(vn.toString(atttIdx))) {
						isCurrent = false;
					}
				}
				String user = "";
				String date = "";
				String commentText = "";
				//R8 xliff 文件中没有提示级别一属性，故此处皆为供参考
				String severity = "Low";
				if ((atttIdx = vn.getAttrVal("from")) != -1) {
					user = vn.toString(atttIdx);
				}
				
				if (vn.getText() != -1) {
					String r8NoteText = vn.toString(vn.getText());
					if (r8NoteText.indexOf(":") != -1) {
						date = r8NoteText.substring(0, r8NoteText.indexOf(":"));
						commentText = r8NoteText.substring(r8NoteText.indexOf(":") + 1, r8NoteText.length());
					}else {
						commentText = r8NoteText;
					}
				}
				bean = new CommentBean(user, date, severity, commentText, true);
				
				if (isCurrent) {
					segCommentList.add(new CommentBean(user, date, severity, commentText, true));
				}else {
					if (!fileCommentsList.contains(bean)) {
						fileCommentsList.add(bean);
					}
				}
			}
			tgtbeBean.setCommentList(segCommentList);
			vn.pop();
		}
		
		/**
		 * 替换掉骨架文件中的占位符
		 * @param segId
		 * @param srcBean
		 * @param tgtbeBean
		 */
		private void replaceSegment(String segId, TuMrkBean srcBean, TuMrkBean tgtbeBean) throws Exception {
			String segStr = "%%%" + segId + "%%%";
			String srcXpath = "/xliff/file/body//trans-unit/seg-source//mrk[text()='" + segStr + "']";
			//先处理源文
			outputAP.selectXPath(srcXpath);
			if (outputAP.evalXPath() != -1) {
				int textIdx = outputVN.getText();
				outputXM.updateToken(textIdx, srcBean.getContent().getBytes("utf-8"));
			}
			//处理译文
			String tgtXpath = "/xliff/file/body//trans-unit/target//mrk[text()='" + segStr + "']";
			outputAP.selectXPath(tgtXpath);
			if (outputAP.evalXPath() != -1) {
				String content = tgtbeBean.getContent();
				if (tgtbeBean.getCommentList().size() > 0) {
					String uuId = CommonFunction.createUUID();
					commentMap.put(uuId, tgtbeBean.getCommentList());
					content = "<mrk mtype=\"x-sdl-comment\" sdl:cid=\"" + uuId + "\">" + tgtbeBean.getContent() + "</mrk>";
				}
				int textIdx = outputVN.getText();
				outputXM.updateToken(textIdx, content.getBytes("utf-8"));
				
				//开始处理状态
				int attrIdx = -1;
				if ((attrIdx = outputVN.getAttrVal("mid"))!= -1) {
					boolean needLocked = false;
					String mid = outputVN.toString(attrIdx);
					//下面根据mid找到对应的sdl:seg节点，这个节点里面存放的有每个文本段的状态
					String xpath = "ancestor::trans-unit/sdl:seg-defs/sdl:seg[@id='" + mid + "']";
					outputAP.selectXPath(xpath);
					if (outputAP.evalXPath() != -1) {
						//先判断是否锁定
						if (tgtbeBean.isLocked()) {
							if ((attrIdx = outputVN.getAttrVal("locked")) != -1) {
								if (!"true".equals(outputVN.toString(attrIdx))) {
									outputXM.updateToken(attrIdx, "true");
								}
							}else {
								needLocked = true;
							}
						}else {
							if ((attrIdx = outputVN.getAttrVal("locked")) != -1) {
								if ("true".equals(outputVN.toString(attrIdx))) {
									outputXM.updateToken(attrIdx, "false");
								}
							}
						}
						
						//下面根据R8的状态。修改sdl的状态。
						String conf = "";
						String status = tgtbeBean.getStatus();
						if ("new".equals(status)) {
							conf = "Draft";
						}else if ("translated".equals(status)) {
							conf = "Translated";
						}else if ("approved".equals(status)) {
							conf = "ApprovedTranslation";
						}else if ("signed-off".equals(status)) {
							conf = "ApprovedSignOff";
						}
						
						if ("".equals(conf)) {
							if ((attrIdx = outputVN.getAttrVal("conf")) != -1) {
								outputXM.updateToken(attrIdx, "");
							}
						}else {
							if ((attrIdx = outputVN.getAttrVal("conf")) != -1) {
								if (!conf.equals(outputVN.toString(attrIdx))) {
									outputXM.updateToken(attrIdx, conf);
								}
							}else {
								String attributeStr = "";
								if (needLocked) {
									attributeStr = " locked=\"true\" ";
								}
								attributeStr += " conf=\"" + conf + "\" ";
								outputXM.insertAttribute(attributeStr.getBytes("utf-8"));
								needLocked = false;
							}
						}
						
						if (needLocked) {
							outputXM.insertAttribute(" locked=\"true\" ".getBytes("utf-8"));
						}
					}
					
				}
			}
		}
		
		/**
		 * 创建所有批注节点
		 * @throws Exception
		 */
		private void createComments() throws Exception {
			String xpath = "";
			//先生成全局批注的定义节点
			if (fileCommentsList.size() > 0) {
				String fileCommentId = CommonFunction.createUUID();
				String fileCommentStr = "<sdl:cmt id=\"" + fileCommentId + "\" />";
				xpath = "/xliff/file/header";
				outputAP.selectXPath(xpath);
				while(outputAP.evalXPath() != -1){
					outputXM.insertBeforeTail(fileCommentStr.getBytes("utf-8"));
					commentMap.put(fileCommentId, fileCommentsList);
				}
			}

			//开始生成Comments节点
			if (commentMap.size() == 0) {
				return;
			}
			xpath = "/xliff/doc-info";
			outputAP.selectXPath(xpath);
			if (outputAP.evalXPath() != -1) {
				StringBuffer commentSB = new StringBuffer();
				commentSB.append("<cmt-defs>");
				for (Entry<String, List<CommentBean>> entry : commentMap.entrySet()) {
					String id = entry.getKey();
					commentSB.append("<cmt-def id=\""+ id +"\">");
					commentSB.append("<Comments xmlns=\"\">");
					for(CommentBean bean : entry.getValue()){
						commentSB.append("<Comment severity=\"" + bean.getSeverity() + "\" " +
								"user=\"" + bean.getUser() + "\" date=\"" + bean.getDate() + "\" version=\"1.0\">" +
								bean.getCommentText() + "</Comment>");
					}
					commentSB.append("</Comments>");
					commentSB.append("</cmt-def>");
				}
				commentSB.append("</cmt-defs>");

				outputXM.insertBeforeTail(commentSB.toString().getBytes("utf-8"));
			}
			
		}
		
		
		private boolean monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last){
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
		
		
	}
//----------------------------------------------------Xliff2SdlImpl 结束标志--------------------------------------------//
	
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
	
	public static void main(String[] args) {
		
		
	}
}
