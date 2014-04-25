package net.heartsome.cat.converter.memoq6;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.memoq6.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * memoQ 6.0 逆向转换器
 * @author robert	2012-07-20
 *
 */
public class Xliff2Mq implements Converter{

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "mqxlz";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("utils.FileFormatUtils.MQ");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "XLIFF to MQXLZ Conveter";
	
	public Map<String, String> convert(Map<String, String> args,
			IProgressMonitor monitor) throws ConverterException {
		Xliff2MqImpl converter = new Xliff2MqImpl();
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
	
	class Xliff2MqImpl{
		/** The encoding. */
		private String encoding;
		
		/** 逆转换的结果文件 */
		private String outputFile;
		/** 骨架文件的解析游标 */
		private VTDNav sklVN;
		/** 骨架文件的修改类实例 */
		private XMLModifier sklXM;
		/** 骨架文件的查询实例 */
		private AutoPilot sklAP;
		/** xliff文件的解析游标 */
		private VTDNav xlfVN;
		
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
				parseOutputFile(sklFile, xliffFile);
				parseXlfFile(xliffFile);
				ananysisXlfTU();
				
				//下面这是 memoQ 的骨架文件。而sklFile为R8的骨架文件。
				File mqTempSkl = File.createTempFile("mqSkeleton", ".mqskl");
				mqTempSkl.deleteOnExit();
				createMqSkeleton(mqTempSkl);
				
				sklXM.output(sklFile);
				createMQZipFile(sklFile, mqTempSkl);
				
			} catch (Exception e) {
				e.printStackTrace();
				String errorTip = Messages.getString("xlf2mq.msg1") + "\n" + e.getMessage();
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
//			copyFile(file, "C:\\Users\\Administrator\\Desktop\\test.xml");
			VTDGen vg = new VTDGen();
			if (vg.parseFile(file, true)) {
				sklVN = vg.getNav();
				sklXM = new XMLModifier(sklVN);
				sklAP = new AutoPilot(sklVN);
				sklAP.declareXPathNameSpace("mq", "MQXliff");
			}else {
				String errorInfo = MessageFormat.format(Messages.getString("mq.parse.msg2"), 
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
				String errorInfo = MessageFormat.format(Messages.getString("mq.parse.msg1"), 
						new Object[]{new File(xliffFile).getName()});
				throw new Exception(errorInfo);
			}
		}
		
		/**
		 * 分析xliff文件的每一个 trans-unit 节点
		 * @throws Exception
		 */
		private void ananysisXlfTU() throws Exception {
			AutoPilot ap = new AutoPilot(xlfVN);
			AutoPilot childAP = new AutoPilot(xlfVN);
			VTDUtils vu = new VTDUtils(xlfVN);
			String xpath = "/xliff/file/body//trans-unit";
			String srcXpath = "./source";
			String tgtXpath = "./target";
			ap.selectXPath(xpath);
			int attrIdx = -1;
			//trans-unit的id，对应sdl文件的占位符如%%%1%%% 。
			String segId = "";
			TuBean bean = null;
			while (ap.evalXPath() != -1) {
				if ((attrIdx = xlfVN.getAttrVal("id")) == -1) {
					continue;
				}
				bean = new TuBean();
				segId = xlfVN.toString(attrIdx);
				
				//处理source节点
				xlfVN.push();
				childAP.selectXPath(srcXpath);
				if (childAP.evalXPath() != -1) {
					String srcContent = vu.getElementContent();
					srcContent = srcContent == null ? "" : srcContent;
					bean.setSrcText(TextUtil.resetSpecialString(srcContent));
				}
				xlfVN.pop();
				
				//处理target节点
				String status = "";	//状态，针对target节点，空字符串为未翻译
				xlfVN.push();
				String tgtContent = null;
				childAP.selectXPath(tgtXpath);
				if (childAP.evalXPath() != -1) {
					tgtContent = vu.getElementContent();
					if ((attrIdx = xlfVN.getAttrVal("state")) != -1) {
						status = xlfVN.toString(attrIdx);
					}
				}
				tgtContent = tgtContent == null ? "" : tgtContent;
				bean.setTgtText(TextUtil.resetSpecialString(tgtContent));
				xlfVN.pop();
				
				//处理批注
				getNotes(xlfVN, bean);
				
				//判断是否处于锁定状态
				if ((attrIdx = xlfVN.getAttrVal("translate")) != -1) {
					if ("no".equalsIgnoreCase(xlfVN.toString(attrIdx))) {
						bean.setLocked(true);
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
				bean.setStatus(status);
				replaceSegment(segId, bean);
			}
		}
		
		
		/**
		 * 获取 R8 xliff文件的所有批注信息
		 * @param vn
		 * @param tgtbeBean
		 */
		private void getNotes(VTDNav vn, TuBean bean) throws Exception {
			vn.push();
			AutoPilot ap = new AutoPilot(vn);
			String xpath = "./note";
			ap.selectXPath(xpath);
			StringBuffer noteSB = new StringBuffer();
			while(ap.evalXPath() != -1){
				String commentText = "";
				if (vn.getText() != -1) {
					String r8NoteText = vn.toString(vn.getText());
					if (r8NoteText.indexOf(":") != -1) {
						commentText = r8NoteText.substring(r8NoteText.indexOf(":") + 1, r8NoteText.length());
					}else {
						commentText = r8NoteText;
					}
				}
				noteSB.append(commentText + ";\n");
			}
			bean.setNote(noteSB.toString());
			vn.pop();
		}
		
		
		/**
		 * 替换掉骨架文件中的占位符
		 * @param segId
		 * @param srcBean
		 * @param tgtbeBean
		 */
		private void replaceSegment(String segId, TuBean bean) throws Exception {
			String segStr = "%%%" + segId + "%%%";
			String srcXpath = "/xliff/file/body//trans-unit/source[text()='" + segStr + "']";
			//先处理源文
			sklAP.selectXPath(srcXpath);
			if (sklAP.evalXPath() != -1) {
				int textIdx = sklVN.getText();
				sklXM.updateToken(textIdx, bean.getSrcText().getBytes("utf-8"));
			}
			//处理译文
			String tgtXpath = "/xliff/file/body//trans-unit/target[text()='" + segStr + "']";
			sklAP.selectXPath(tgtXpath);
			if (sklAP.evalXPath() != -1) {
				String content = bean.getTgtText();
				int textIdx = sklVN.getText();
				sklXM.updateToken(textIdx, content.getBytes("utf-8"));
			}
			
			//开始处理状态等其他东西
			tgtXpath = "/xliff/file/body//trans-unit[target/text()='" + segStr + "']";
			sklAP.selectXPath(tgtXpath);
			int index = -1;
			boolean needLocked = false;
			if (sklAP.evalXPath() != -1) {
				//先判断是否锁定
				if (bean.isLocked()) {
					if ((index = sklVN.getAttrVal("mq:locked")) != -1) {
						if (!"locked".equals(sklVN.toString(index))) {
							sklXM.updateToken(index, "locked");
						}
					}else {
						needLocked = true;
					}
				}else {
					if ((index = sklVN.getAttrVal("mq:locked")) != -1) {
						if ("locked".equals(sklVN.toString(index))) {
							sklXM.updateToken(index, "false");
						}
					}
				}
				
				//下面根据R8的状态。修改sdl的状态。
				String mqStatus = "";
				String status = bean.getStatus();
				if ("".equals(status)) {
					mqStatus = "NotStarted";
				}else if ("new".equals(status)) {
					mqStatus = "PartiallyEdited";
				}else if ("approved".equals(status)) {
					mqStatus = "ManuallyConfirmed";
				}else if ("signed-off".equals(status)) {
					mqStatus = "ManuallyConfirmed";
				}
				
				if ("".equals(mqStatus)) {
					if ((index = sklVN.getAttrVal("mq:status")) != -1) {
						sklXM.updateToken(index, "");
					}
				}else {
					if ((index = sklVN.getAttrVal("mq:status")) != -1) {
						if (!mqStatus.equals(sklVN.toString(index))) {
							sklXM.updateToken(index, mqStatus);
						}
					}else {
						String attributeStr = "";
						if (needLocked) {
							attributeStr = " mq:locked=\"locked\" ";
						}
						attributeStr += " mq:status=\"" + mqStatus + "\" ";
						sklXM.insertAttribute(attributeStr.getBytes("utf-8"));
						needLocked = false;
					}
				}
				if (needLocked) {
					sklXM.insertAttribute(" mq:locked=\"locked\" ".getBytes("utf-8"));
				}
			
			}
		}
		
		private void createMqSkeleton(File mqTempSkl) throws Exception{
			String xpath = "/xliff/file/header/sklContent";
			sklAP.selectXPath(xpath);
			String mqSklContent = "";
			if(sklAP.evalXPath() != -1){
				VTDUtils vu = new VTDUtils(sklVN);
				mqSklContent = vu.getElementContent();
				sklXM.remove();
			}
			
			FileOutputStream output = new FileOutputStream(mqTempSkl);
			output.write("﻿<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes("UTF-8"));
			output.write("<mq:externalparts xmlns:mq=\"MemoQ Xliff external parts\">".getBytes("UTF-8"));
			output.write(mqSklContent.getBytes("UTF-8"));
			output.write("</mq:externalparts>".getBytes("UTF-8"));
			output.close();
		}
		
		
		
		private void createMQZipFile(String sklFile, File mqTempSkl){
			File files[] = new File[]{new File(sklFile), mqTempSkl};
			ZipArchiveOutputStream zaos = null;
			
			try {
				File zipFile = new File(outputFile);
				zaos = new ZipArchiveOutputStream(zipFile);
				// Use Zip64 extensions for all entries where they are
				// required
//				zaos.setUseZip64(Zip64Mode.AsNeeded);

				// 将每个文件用ZipArchiveEntry封装
				// 再用ZipArchiveOutputStream写到压缩文件中
				for (File file : files) {
					if (file != null) {
						String fileName = file.getName();
						if (fileName.endsWith(".skl")) {
							fileName = "document.mqxliff";
						}else if (fileName.endsWith(".mqskl")) {
							fileName = "skeleton.xml";
						}
						ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry( file, fileName);
						zaos.putArchiveEntry(zipArchiveEntry);
						InputStream is = null;
						try {
							is = new BufferedInputStream(
									new FileInputStream(file));
							byte[] buffer = new byte[1024 * 5];
							int len = -1;
							while ((len = is.read(buffer)) != -1) {
								// 把缓冲区的字节写入到ZipArchiveEntry
								zaos.write(buffer, 0, len);
							}
							// Writes all necessary data for this entry.
							zaos.closeArchiveEntry();
						} catch (Exception e) {
							throw new RuntimeException(e);
						} finally {
							if (is != null)
								is.close();
						}
					}
				}
				zaos.finish();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				try {
					if (zaos != null) {
						zaos.close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
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

}
