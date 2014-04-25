package net.heartsome.cat.converter.wordfast3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.converter.wordfast3.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

public class Xliff2Wf implements Converter {
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "txml";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.WF");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to TXML Conveter";
	public static final Logger LOGGER = LoggerFactory.getLogger(Xliff2Wf.class);
	
	@Override
	public Map<String, String> convert(Map<String, String> args,
			IProgressMonitor monitor) throws ConverterException {
		Xliff2WfImpl xliff2DuImpl = new Xliff2WfImpl();
		return xliff2DuImpl.run(args, monitor);
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
	
	class Xliff2WfImpl{
		/** 逆转换的结果文件 */
		private String outputFile;
		/** 骨架文件的解析游标 */
		private VTDNav outputVN;
		/** 骨架文件的修改类实例 */
		private XMLModifier outputXM;
		/** 骨架文件的查询实例 */
		private AutoPilot outputAP;
		/** hsxliff文件的解析游标 */
		private VTDNav hsxlfVN;
		/** The encoding. */
		private String encoding;
		
		public Map<String, String> run(Map<String, String> params, IProgressMonitor monitor) throws ConverterException {
			monitor = Progress.getMonitor(monitor);
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
				ananysisXlfTU();

				outputXM.output(outputFile);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("", e);
				String errorTip = Messages.getString("xlf2wf.msg1") + "\n" + e.getMessage();
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e);
			}finally{
				monitor.done();
			}
			result.put(Converter.ATTR_TARGET_FILE, outputFile);
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
			}else {
				String errorInfo = MessageFormat.format(Messages.getString("wf.parse.msg2"), 
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
				hsxlfVN = vg.getNav();
			}else {
				String errorInfo = MessageFormat.format(Messages.getString("wf.parse.msg1"), 
						new Object[]{new File(xliffFile).getName()});
				throw new Exception(errorInfo);
			}
		}
		
		/**
		 * 分析xliff文件的每一个 trans-unit 节点
		 * @throws Exception
		 */
		private void ananysisXlfTU() throws Exception {
			AutoPilot ap = new AutoPilot(hsxlfVN);
			AutoPilot childAP = new AutoPilot(hsxlfVN);
			VTDUtils vu = new VTDUtils(hsxlfVN);
			String xpath = "/xliff/file/body//trans-unit";
			String srcXpath = "./source";
			String tgtXpath = "./target";
			
			ap.selectXPath(xpath);
			int attrIdx = -1;
			//trans-unit的id，对应 wf 双语文件的占位符如%%%1%%% 。
			String segId = "";
			TuBean tuBean = null;
			while (ap.evalXPath() != -1) {
				if ((attrIdx = hsxlfVN.getAttrVal("id")) == -1) {
					continue;
				}
				tuBean = new TuBean();
				segId = hsxlfVN.toString(attrIdx);
				
				//处理source节点
				hsxlfVN.push();
				childAP.selectXPath(srcXpath);
				if (childAP.evalXPath() != -1) {
					String srcContent = vu.getElementContent();
					srcContent = srcContent == null ? "" : srcContent;
					tuBean.setSrcContent(analysisTag(srcContent));
				}
				hsxlfVN.pop();
				
				//处理target节点
				String status = "";	//状态，针对target节点，空字符串为未翻译
				hsxlfVN.push();
				String tgtContent = null;
				childAP.selectXPath(tgtXpath);
				if (childAP.evalXPath() != -1) {
					tgtContent = vu.getElementContent();
					if ((attrIdx = hsxlfVN.getAttrVal("state")) != -1) {
						status = hsxlfVN.toString(attrIdx);
					}
				}
				tgtContent = tgtContent == null ? "" : tgtContent;
				tuBean.setTgtContent(analysisTag(tgtContent));
				hsxlfVN.pop();	//回到trans-unit节点下
				
//				//判断是否处于锁定状态
//				if ((attrIdx = hsxlfVN.getAttrVal("translate")) != -1) {
//					if ("no".equalsIgnoreCase(hsxlfVN.toString(attrIdx))) {
//						tgtBean.setLocked(true);
//					}
//				}
//				//判断是否处于批准状态，若是签发，就没有必要判断了，因为签发了的一定就批准了的
//				if (!"signed-off".equalsIgnoreCase(status)) {
//					if ((attrIdx = hsxlfVN.getAttrVal("approved")) != -1) {
//						if ("yes".equalsIgnoreCase(hsxlfVN.toString(attrIdx))) {
//							status = "approved";	//批准
//						}
//					}
//				}
//				
//				//如果是锁定了的。在deja vu里面的完成翻译状态变成未完成翻译。
//				if (tgtBean.isLocked()) {
//					status = "needs-translation";
//				}else {
//					//将状态切换成du xliff的格式
//					if ("approved".equals(status) || "signed-off".equals(status)) {
//						status = "finish";
//					}else {
//						status = "needs-translation";
//					}
//					
//					//判断是否有疑问这个属性
//					if (!"finish".equals(status)) {
//						if ((attrIdx = hsxlfVN.getAttrVal("hs:needs-review")) != -1) {
//							if ("yes".equals(hsxlfVN.toString(attrIdx))) {
//								status = "needs-review-translation";
//							}
//						}	
//					}
//				}
//				
//				tgtBean.setStatus(status);
				
				//获取批注
				getNotes(hsxlfVN, tuBean);
				replaceSegment(segId, tuBean);
			}
		}
		
		/**
		 * 替换掉骨架文件中的占位符
		 * @param segId
		 * @param srcBean
		 * @param tgtbeBean
		 */
		private void replaceSegment(String segId, TuBean tuBean) throws Exception {
			String segStr = "%%%" + segId + "%%%";
			String segXpath = "/txml/translatable/descendant::segment[source/text()='" + segStr + "' or target/text()='" + segStr + "']";   
			String srcXpath = "./source";
			String tgtXapth = "./target";
			
			AutoPilot childAp = new AutoPilot(outputVN);
			
			outputAP.selectXPath(segXpath);
			if (outputAP.evalXPath() != -1) {
				StringBuffer addedSb = new StringBuffer();
				// 开始处理源文
				outputVN.push();
				childAp.selectXPath(srcXpath);
				if (childAp.evalXPath() != -1) {
					int textIdx = outputVN.getText();
					outputXM.updateToken(textIdx, tuBean.getSrcContent().getBytes("utf-8"));
				}
				outputVN.pop();
				
				// 开始处理译文
				String tgtContent = tuBean.getTgtContent();
				if (tgtContent != null && !"".equals(tgtContent)) {
					boolean tgtExsit = false;
					outputVN.push();
					childAp.selectXPath(tgtXapth);
					if (childAp.evalXPath() != -1) {
						int textIdx = outputVN.getText();
						outputXM.updateToken(textIdx, tgtContent.getBytes("utf-8"));
						tgtExsit = true;
					}
					outputVN.pop();
					
					if (!tgtExsit) {
						String tgtFrag = "<target>" + tgtContent + "</target>";
						addedSb.append(tgtFrag);
					}
				}
				
				// 开始处理批注信息
				List<CommentBean> commentList = tuBean.getCommentList();
				if (commentList != null && commentList.size() > 0) {
					StringBuffer commentSB = new StringBuffer();
					commentSB.append("<comments>");
					for(CommentBean bean : commentList){
						String user = bean.getUser();
						String date = bean.getDate();
						String type = bean.getType();
						String commentText = bean.getCommentText();
						
						if (date == null || "".equals(date)) {
							date = "000-00-00";
						}
						date = getUTCDateStr(date);
						
						commentSB.append("<comment creationid=\"" + user + "\" creationdate=\"" + date + "\" type=\"" + type + "\">");
						commentSB.append(commentText);
						commentSB.append("</comment>");
					}
					commentSB.append("</comments>");
					addedSb.append(commentSB);
				}
				
				outputXM.insertBeforeTail(addedSb.toString().getBytes("utf-8"));
			}
			
		}
		
		
		private void getNotes(VTDNav vn, TuBean tuBean) throws Exception {
			List<CommentBean> commentList = new LinkedList<CommentBean>();
			vn.push();
			AutoPilot ap = new AutoPilot(vn);
			String xpath = "./note";
			ap.selectXPath(xpath);
			String commentText = "";
			int attrIdx = -1;
			while(ap.evalXPath() != -1){
				attrIdx = vn.getAttrVal("from");
				String user = "";
				String date = "";
				String type = "text";
				
				if (attrIdx != -1) {
					user = vn.toRawString(attrIdx);
				}
				if (vn.getText() != -1) {
					String r8NoteText = vn.toString(vn.getText());
					if (r8NoteText.indexOf(":") != -1) {
						commentText += r8NoteText.substring(r8NoteText.indexOf(":") + 1, r8NoteText.length());
						date = r8NoteText.substring(0, r8NoteText.indexOf(":"));
					}else {
						commentText += r8NoteText;
					}
				}
				
				commentList.add(new CommentBean(user, date, type, commentText, null));
			}
			if (commentText.length() > 0) {
				commentText = commentText.substring(0, commentText.length() - 1);
			}
			tuBean.setCommentList(commentList);
			vn.pop();
		}
	}
//----------------------------------------------------Xliff2DuImpl 结束标志--------------------------------------------//
	
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
		String comment = "this is a comment;\n";
		System.out.println(comment.substring(0, comment.length() - 1));
		
		System.out.println(getUTCDateStr("0000-00-00"));
	}
	
	/**
	 * 转换时间
	 * @param strDate
	 * @return
	 */
	public static String getUTCDateStr(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date str2Date = formatter.parse(strDate, pos);
		SimpleDateFormat formatter_1 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		
		return formatter_1.format(str2Date);
	}
	
	/**
	 * 将 wf 文件中的 ut 标记换成 ph 标记
	 * @param text
	 * @return
	 */
	private static String analysisTag(String text){
		//<ut type="content" x="1">&lt;fontformat color="0#0#0"&gt;&lt;b&gt;</ut>
		text = text.replace("<ph ", "<ut ");
		text = text.replace("ph>", "ut>");
		return text;
	}

}
