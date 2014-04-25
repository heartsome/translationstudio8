package net.heartsome.cat.converter.deja_vu_x2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.deja_vu_x2.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

public class Xliff2Du implements Converter{
	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "xlf";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.DU");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to DUXLIFF Conveter";
	
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) 
			throws ConverterException {
		Xliff2DuImpl xliff2DuImpl = new Xliff2DuImpl();
		return xliff2DuImpl.run(args, monitor);
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
	
	class Xliff2DuImpl{
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
				String errorTip = Messages.getString("xlf2du.msg1") + "\n" + e.getMessage();
				ConverterUtils.throwConverterException(Activator.PLUGIN_ID, errorTip, e);
			}finally{
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
				String errorInfo = MessageFormat.format(Messages.getString("du.parse.msg2"), 
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
				String errorInfo = MessageFormat.format(Messages.getString("du.parse.msg1"), 
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
			//trans-unit的id，对应sdl文件的占位符如%%%1%%% 。
			String segId = "";
			TuMrkBean srcBean = null;
			TuMrkBean tgtBean = null;
			while (ap.evalXPath() != -1) {
				if ((attrIdx = hsxlfVN.getAttrVal("id")) == -1) {
					continue;
				}
				srcBean = new TuMrkBean();
				tgtBean = new TuMrkBean();
				segId = hsxlfVN.toString(attrIdx);
				
				//处理source节点
				hsxlfVN.push();
				childAP.selectXPath(srcXpath);
				if (childAP.evalXPath() != -1) {
					String srcContent = vu.getElementContent();
					srcContent = srcContent == null ? "" : srcContent;
					srcBean.setContent(srcContent);
					srcBean.setSource(true);
				}
				hsxlfVN.pop();
				
				//处理target节点
				String status = "";	//状态，针对target节点，空字符串为未翻译
				hsxlfVN.push();
				tgtBean.setSource(false);
				String tgtContent = null;
				childAP.selectXPath(tgtXpath);
				if (childAP.evalXPath() != -1) {
					tgtContent = vu.getElementContent();
					if ((attrIdx = hsxlfVN.getAttrVal("state")) != -1) {
						status = hsxlfVN.toString(attrIdx);
					}
				}
				tgtContent = tgtContent == null ? "" : tgtContent;
				tgtBean.setContent(tgtContent);
				hsxlfVN.pop();	//回到trans-unit节点下
				
				//判断是否处于锁定状态
				if ((attrIdx = hsxlfVN.getAttrVal("translate")) != -1) {
					if ("no".equalsIgnoreCase(hsxlfVN.toString(attrIdx))) {
						tgtBean.setLocked(true);
					}
				}
				//判断是否处于批准状态，若是签发，就没有必要判断了，因为签发了的一定就批准了的
				if (!"signed-off".equalsIgnoreCase(status)) {
					if ((attrIdx = hsxlfVN.getAttrVal("approved")) != -1) {
						if ("yes".equalsIgnoreCase(hsxlfVN.toString(attrIdx))) {
							status = "approved";	//批准
						}
					}
				}
				
				//如果是锁定了的。在deja vu里面的完成翻译状态变成未完成翻译。
				if (tgtBean.isLocked()) {
					status = "needs-translation";
				}else {
					//将状态切换成du xliff的格式
					if ("approved".equals(status) || "signed-off".equals(status)) {
						status = "finish";
					}else {
						status = "needs-translation";
					}
					
					//判断是否有疑问这个属性
					if (!"finish".equals(status)) {
						if ((attrIdx = hsxlfVN.getAttrVal("hs:needs-review")) != -1) {
							if ("yes".equals(hsxlfVN.toString(attrIdx))) {
								status = "needs-review-translation";
							}
						}	
					}
				}
				
				tgtBean.setStatus(status);
				
				//获取批注
				getNotes(hsxlfVN, tgtBean);
				replaceSegment(segId, srcBean, tgtBean);
			}
		}
		
		/**
		 * 替换掉骨架文件中的占位符
		 * @param segId
		 * @param srcBean
		 * @param tgtbeBean
		 */
		private void replaceSegment(String segId, TuMrkBean srcBean, TuMrkBean tgtbeBean) throws Exception {
			int attrIdx = -1;
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
				if (tgtbeBean.getComment().length() > 0) {
					if ((attrIdx = hsxlfVN.getAttrVal("comment")) != -1) {
						outputXM.updateToken(attrIdx, tgtbeBean.getComment().getBytes("utf-8"));
					}else {
						String comment = " comment='" + tgtbeBean.getComment() + "'";
						outputXM.insertAttribute(comment.getBytes("utf-8"));
					}
				}
				int textIdx = outputVN.getText();
				outputXM.updateToken(textIdx, content.getBytes("utf-8"));
				
				//开始处理状态
				if ((attrIdx = outputVN.getAttrVal("mid"))!= -1) {
					//下面进入target父节点，这个节点里面存放的有文本段的状态
					String xpath = "ancestor::target";
					outputAP.selectXPath(xpath);
					if (outputAP.evalXPath() != -1) {
						//下面根据R8的状态。修改sdl的状态。
						String status = tgtbeBean.getStatus();
						String needInsertAttrStr = "";
						if ("".equals(status) || "needs-translation".equals(status)) {
							if ((attrIdx = outputVN.getAttrVal("state")) != -1) {
								if (!"needs-translation".equals(outputVN.toString(attrIdx))) {
									outputXM.updateToken(attrIdx, "needs-translation");
								}
							}else {
								needInsertAttrStr = " state=\"needs-translation\"";
							}
						}else if ("needs-review-translation".equals(status)) {
							if ((attrIdx = outputVN.getAttrVal("state")) != -1) {
								if (!"needs-review-translation".equals(outputVN.toString(attrIdx))) {
									outputXM.updateToken(attrIdx, "needs-review-translation");
								}
							}else {
								needInsertAttrStr = " state=\"needs-review-translation\"";
							}
						}else if ("finish".equals(status)) {
							if ((attrIdx = outputVN.getAttrVal("state")) != -1) {
								if (!"translated".equals(outputVN.toString(attrIdx))) {
									outputXM.updateToken(attrIdx, "translated");
								}
							}else {
								needInsertAttrStr = " state=\"translated\"";
							}
						}
						
						if (needInsertAttrStr.length() > 0) {
							outputXM.insertAttribute(needInsertAttrStr.getBytes("utf-8"));
						}
						
						//如果是完成翻译或需要锁定，那么就要进行trans-unit节点中进行修改。
						if ("finish".equals(status) || tgtbeBean.isLocked()) {
							needInsertAttrStr = "";
							xpath = "ancestor::trans-unit";
							outputAP.selectXPath(xpath);
							if (outputAP.evalXPath() != -1) {
								//先判断是否锁定,这里的判断优先处理锁定
								if (tgtbeBean.isLocked()) {
									if ((attrIdx = outputVN.getAttrVal("translate")) != -1) {
										if (!"no".equals(outputVN.toString(attrIdx))) {
											outputXM.updateToken(attrIdx, "no");
										}
									}else {
										needInsertAttrStr = " translate=\"no\"";
									}
								}else {
									if ((attrIdx = outputVN.getAttrVal("translate")) != -1) {
										if ("no".equals(outputVN.toString(attrIdx))) {
											outputXM.updateToken(attrIdx, "");
										}
									}
								}
								
								//判断是否完成翻译，完成翻译与锁定状态是对立的，两者只能存在一个
								if ("finish".equals(status)) {
									if ((attrIdx = outputVN.getAttrVal("approved")) != -1) {
										if (!"yes".equals(outputVN.toString(attrIdx))) {
											outputXM.updateToken(attrIdx, "yes");
										}
									}else {
										needInsertAttrStr = " approved=\"yes\"";
									}
								}else {
									if ((attrIdx = outputVN.getAttrVal("approved")) != -1) {
										if ("yes".equals(outputVN.toString(attrIdx))) {
											outputXM.updateToken(attrIdx, "");
										}
									}
								}
								if (needInsertAttrStr.length() > 0) {
									outputXM.insertAttribute(needInsertAttrStr.getBytes("utf-8"));
								}
							}
						}
					}
					
				}
			}
		}
		
		
		private void getNotes(VTDNav vn, TuMrkBean tgtBean) throws Exception {
			vn.push();
			AutoPilot ap = new AutoPilot(vn);
			String xpath = "./note";
			ap.selectXPath(xpath);
			String commentText = "";
			int i = 0;
			while(ap.evalXPath() != -1){
				if (vn.getText() != -1) {
					i ++;
					String r8NoteText = vn.toString(vn.getText());
					if (r8NoteText.indexOf(":") != -1) {
						commentText += i + ": " + r8NoteText.substring(r8NoteText.indexOf(":") + 1, r8NoteText.length());
					}else {
						commentText += i + ": " + r8NoteText;
					}
				}
			}
			if (commentText.length() > 0) {
				commentText = commentText.substring(0, commentText.length() - 1);
			}
			tgtBean.setComment(commentText);
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
	}

}
