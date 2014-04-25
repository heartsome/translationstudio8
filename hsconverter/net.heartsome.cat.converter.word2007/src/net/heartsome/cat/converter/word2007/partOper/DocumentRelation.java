package net.heartsome.cat.converter.word2007.partOper;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import net.heartsome.cat.converter.word2007.XliffInputer;
import net.heartsome.cat.converter.word2007.common.PathUtil;
import net.heartsome.cat.converter.word2007.resource.Messages;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * 管理 document.xml 与其他文件之间的关联的类
 * @author robert	2012-08-08
 */
public class DocumentRelation {
	private VTDNav vn;
	private AutoPilot ap;
	/** document.xml 关系文件 document.xml.rels 的绝对路径 */
	private String xmlPath;
	private PathUtil pathUtil;
	
	public DocumentRelation(String xmlPath, PathUtil pathUtil) throws Exception {
		this.xmlPath = xmlPath;
		this.pathUtil = pathUtil;
		loadFile();
	}
	
	private void loadFile() throws Exception {
		VTDGen vg = new VTDGen();
		if (vg.parseFile(xmlPath, true)) {
			vn = vg.getNav();
			ap = new AutoPilot(vn);
		}else {
			throw new Exception(MessageFormat.format(Messages.getString("docxConvert.msg2"), xmlPath));
		}
	}
	
	/**
	 * 通过传入的 id 获取出与 document.xml 文件相关的其他文件
	 * @param Id
	 * @return
	 */
	public String getPartPathById(String Id) throws Exception {
		String partPath = "";
		String xpath = "/Relationships/Relationship[@Id='" + Id + "']";
		ap.selectXPath(xpath);
		if (ap.evalXPath() != -1) {
			int index = -1;
			if ((index = vn.getAttrVal("Target")) != -1) {
				partPath = vn.toString(index);
			}
		}
		
		pathUtil.setWordRoot();	// 回到 superRoot/word 目录
		partPath = pathUtil.getPackageFilePath(partPath, false);
		pathUtil.setSuperRoot();	// 回到根目录
		return partPath;
	}
	
	/**
	 * 处理主文档的关系
	 * @throws Exception
	 */
	public void arrangeRelations (XliffInputer xlfInput, IProgressMonitor monitor) throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		String xpath = "";
		
		// 先获取页眉的文件路径
		monitor.setTaskName(Messages.getString("xlf2Docx.task1"));
		String headerType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/header";
		xpath = "/Relationships/Relationship[@Type='" + headerType + "']/@Target";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			String target = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(target)) {
				pathUtil.setWordRoot();
				String partPath = pathUtil.getPackageFilePath(target, false);
				HeaderPart part = new HeaderPart(partPath, xlfInput);
				part.reverseConvert();
			}
		}
		monitorWork(monitor);
		
		// 再处理页脚
		monitor.setTaskName(Messages.getString("xlf2Docx.task2"));
		String footerType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer";
		xpath = "/Relationships/Relationship[@Type='" + footerType + "']/@Target";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			String target = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(target)) {
				pathUtil.setWordRoot();
				String partPath = pathUtil.getPackageFilePath(target, false);
				FooterPart part = new FooterPart(partPath, xlfInput);
				part.reverseConvert();
			}
		}
		monitorWork(monitor);
		
		// 再处理批注
		monitor.setTaskName(Messages.getString("xlf2Docx.task3"));
		String CommentsType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments";
		xpath = "/Relationships/Relationship[@Type='" + CommentsType + "']/@Target";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			String target = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(target)) {
				pathUtil.setWordRoot();
				String partPath = pathUtil.getPackageFilePath(target, false);
				CommentsPart part = new CommentsPart(partPath, xlfInput);
				part.reverseConvert();
			}
		}
		monitorWork(monitor);
		
		// 再处理脚注
		monitor.setTaskName(Messages.getString("xlf2Docx.task4"));
		String footNotesType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes";
		xpath = "/Relationships/Relationship[@Type='" + footNotesType + "']/@Target";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			String target = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(target)) {
				pathUtil.setWordRoot();
				String partPath = pathUtil.getPackageFilePath(target, false);
				FooterNodesPart part = new FooterNodesPart(partPath, xlfInput);
				part.reverseConvert();
			}
		}
		monitorWork(monitor);
		
		// 最后处理尾注
		monitor.setTaskName(Messages.getString("xlf2Docx.task5"));
		String endNotesType = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/endnotes";
		xpath = "/Relationships/Relationship[@Type='" + endNotesType + "']/@Target";
		ap.selectXPath(xpath);
		while(ap.evalXPath() != -1){
			String target = vn.toRawString(vn.getCurrentIndex() + 1);
			if (!"".equals(target)) {
				pathUtil.setWordRoot();
				String partPath = pathUtil.getPackageFilePath(target, false);
				EndNotesPart part = new EndNotesPart(partPath, xlfInput);
				part.reverseConvert();
			}
		}
		monitorWork(monitor);
		
	}
	
	public void monitorWork(IProgressMonitor monitor){
		monitor.worked(1);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException(Messages.getString("docxConvert.task3"));
		}
	}
	

}
