package net.heartsome.cat.ts.ui.qa.fileAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.qa.FAModel;
import net.heartsome.cat.ts.core.qa.FileAnalysis;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.EditProgressFAResult;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编辑进度分析
 * @author robert 2011-12-14
 */
public class EditProgressFA extends FileAnalysis {
	private FAModel model;
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格 */
	private static int workInterval = 4;
	/** 翻译进度分析结果，key为文件的绝对路径，值为已翻译文本段等值的pojo类 */
	private Map<String, EditProgressFAResult> editProgFAResultMap;
	/** 所有包括分析文件的容器 */
	private List<IContainer> allFolderList;
	public final static Logger logger = LoggerFactory.getLogger(EditProgressFA.class.getName());

	@Override
	public int beginAnalysis(FAModel model, IProgressMonitor monitor, QAXmlHandler handler) {
		this.model = model;
		super.setModel(model);

		editProgFAResultMap = new HashMap<String, EditProgressFAResult>();

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		// 要分析的文件的所有
		int allTUSize = model.getAllTuSize();

		int traversalTuIndex = 0;

		int totalWork = allTUSize % workInterval == 0 ? (allTUSize / workInterval) : (allTUSize / workInterval) + 1;
		monitor.beginTask("", totalWork);

		for (int index = 0; index < model.getAnalysisIFileList().size(); index++) {
			IFile iFile = model.getAnalysisIFileList().get(index);
			monitor.setTaskName(MessageFormat.format(Messages.getString("qa.fileAnalysis.EditProgressFA.tip1"), iFile
					.getFullPath().toOSString()));
			String iFilePath = iFile.getLocation().toOSString();

			// 获取单个文件的翻译进度的数据
			Map<String, Integer> editProgMap = handler.getEditProgressData(iFilePath, monitor, workInterval,
					traversalTuIndex);
			// 如果获取的结果为null,则表示用户退出程序
			if (editProgMap == null) {
				return QAConstant.QA_ZERO;
			}

			traversalTuIndex += handler.getTuSizeMap().get(iFilePath);

			int notApprovedParas = editProgMap.get("notApprovedParas");
			int approvedParas = editProgMap.get("approvedParas");
			int lockedParas = editProgMap.get("lockedParas");
			int notApprovedWords = editProgMap.get("notApprovedWords");
			int approvedWords = editProgMap.get("approvedWords");
			int lockedWords = editProgMap.get("lockedWords");
			

			EditProgressFAResult editResult = new EditProgressFAResult(
					notApprovedParas, approvedParas, lockedParas,
					notApprovedWords, approvedWords, lockedWords);
			editProgFAResultMap.put(iFilePath, editResult);
		}

		if (!handler.monitorWork(monitor, traversalTuIndex, workInterval, true)) {
			return QAConstant.QA_ZERO;
		}

		// 开始填充数据
		printTransProgFAReslut();

		return QAConstant.QA_ZERO;
	}

	public void printTransProgFAReslut() {
		String htmlPath = createFAResultHtml();
		try {
			model.getAnalysisIFileList().get(0).getProject().getFolder("Intermediate").getFolder("Report").refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.EditProgressFA.log1"), e1);
		}
		
		final FileEditorInput input = new FileEditorInput(ResourceUtils.fileToIFile(htmlPath));
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, QAConstant.FA_HtmlBrowserEditor, true);
				} catch (PartInitException e) {
					e.printStackTrace();
					logger.error(Messages.getString("qa.fileAnalysis.EditProgressFA.log2"), e);
				}
			}
		});
	}

	public String createFAResultHtml() {
		allFolderList = new LinkedList<IContainer>();
		
		Date createDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String createTime = formatter.format(createDate);
		formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String htmlNameTime = formatter.format(createDate);
		
		IProject curProject = model.getAnalysisIFileList().get(0).getProject();
		String htmlName = "EDLog" + htmlNameTime + ".html";
		String htmlPath = curProject.getLocation().append("Intermediate").append("Report").append(htmlName).toOSString();
		
		File htmlFile = new File(htmlPath);
		if (!htmlFile.getParentFile().exists()) {
			htmlFile.getParentFile().mkdirs();
		}

		FileOutputStream output;
		try {
			output = new FileOutputStream(htmlPath);
			output.write(QAConstant.FA_HtmlDoctype.getBytes("UTF-8"));
			output.write("<html>\n".getBytes("UTF-8"));

			String headerNode = QAConstant.FA_htmlHeader;
			headerNode = headerNode.replace("###Title###", Messages.getString("qa.fileAnalysis.EditProgressFA.tip2"));

			output.write(headerNode.getBytes("UTF-8"));
			output.write("\t<body>\n".getBytes("UTF-8"));
			output.write(("<p class=\"title\">" + Messages.getString("qa.fileAnalysis.EditProgressFA.name1") + "</p>")
					.getBytes("UTF-8"));
			
			// ----------------<<<<<<start-- 下面是相关信息提示部份---------------------
			output.write("\t<div>\n".getBytes("UTF-8"));
			output.write("\t\t<table class=\"infoTableStyle\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n".getBytes("UTF-8"));
			// 分析文件总数
			String title = Messages.getString("qa.fa.info.fileSum");
			String content = "" + model.getSubFileNum();
			String infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", content);
			output.write(infoStr.getBytes("UTF-8"));
			
			// 分析失败文件
			title = Messages.getString("qa.fa.info.errorFiles");
			StringBuffer errorFileSB = new StringBuffer();
			errorFileSB.append("\t\t\t<div style=\"margin-bottom: 2px;\">");
			errorFileSB.append(model.getErrorIFileList().size());
			errorFileSB.append("</div>");
			for(IFile iFile : model.getErrorIFileList()){
				errorFileSB.append("\t\t\t<div style=\"margin-bottom: 2px;\">");
				errorFileSB.append(iFile.getFullPath().toOSString());
				errorFileSB.append("</div>");
			}
			infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", errorFileSB.toString());
			output.write(infoStr.getBytes("UTF-8"));
			
			// 报告生成时间
			title = Messages.getString("qa.fa.info.createTime");
			infoStr = QAConstant.FA_Report_Info.replace("###Title###", title).replace("###Content###", createTime);
			output.write(infoStr.getBytes("UTF-8"));
			output.write("</table></div><br>\n".getBytes("UTF-8"));
			// ---------------->>>>>>end-- 报表信息提示部分结束---------------------
			

			// －－－－－－－－－－－－－－字数---------------------
			output.write(("\t\t<div class=\"legendStyle\"><b>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.words") + "</b></div>").getBytes("UTF-8"));

			StringBuffer wordsFAdata = new StringBuffer();

			getAllFolder(curProject, allFolderList);
			setDataToFolder(curProject);

			// 创建一个表
			wordsFAdata.append("\t<table class='tableStyle' cellpadding='0' cellspacing='1'> \n");
			// 表头
			wordsFAdata.append("\t\t<tr>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='22%'>"
					+ Messages.getString("qa.all.fa.fileName") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' colSpan='2' width='26%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.nonApprove") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' colSpan='2' width='26%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approved") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.lockWordsNum") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.wordsSum") + "</td>\n");
			wordsFAdata.append("\t\t</tr>\n");
			
			wordsFAdata.append("\t\t<tr>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveWords") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveProp") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveWords") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveProp") + "</td>\n");
			wordsFAdata.append("\t\t</tr>\n");

			// 首先写下项目
			EditProgressFAResult proFaResult = editProgFAResultMap.get(curProject.getLocation().toOSString());
			int paddLeft = 6;
			String folderId = curProject.getFullPath().toOSString();
			wordsFAdata.append("<tr onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n" +
					"<td class='folderTd' style='padding-left: 6'>\n"
					+ "<a href='javascript:void(0)' id='" + folderId + "' name='words' class='link'  "
					+ "title='"+Messages.getString("qa.all.fa.clickToShrink")+"' onclick='clickFolder(id, name)' >" +
							"<span id='" + folderId + "_span'>-</span> " + curProject.getName() + "</a></td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotApprovedWords() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotApprovedWordsRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getApprovedWords() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getApprovedWordsRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getLockedWords() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTotalWords() + "</td>\n" 
					+ "</tr>\n");
			wordsSetInputData(curProject, wordsFAdata, paddLeft);

			wordsFAdata.append("\t</table>\n");
			output.write(wordsFAdata.toString().getBytes("UTF-8"));
			output.write("<br/>\n".getBytes("UTF-8"));

			// －－－－－－－－－－－－－－文本段---------------------

			output.write(("\t\t<div class=\"legendStyle\"><b>" + 
					Messages.getString("qa.fileAnalysis.EditProgressFA.paragraph") +"</b></div>").getBytes("UTF-8"));
			StringBuffer paraFAdata = new StringBuffer();

			// 创建一个表
			paraFAdata.append("\t<table class='tableStyle' cellpadding='0' cellspacing='1'> \n");
			// 表头
			paraFAdata.append("\t\t<tr>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='22%'>"
					+ Messages.getString("qa.all.fa.fileName") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' colSpan='2' width='26%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.nonApprove") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' colSpan='2' width='26%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approved") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.lockParasNum") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.paraSum") + "</td>\n");
			paraFAdata.append("\t\t</tr>\n");
			
			paraFAdata.append("\t\t<tr>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveParaNum") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveProp") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveParaNum") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.EditProgressFA.approveProp") + "</td>\n");
			paraFAdata.append("\t\t</tr>\n");

			// 首先写下项目
			paddLeft = 6;
			paraFAdata.append("<tr onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n" +
					"<td class='folderTd' style='padding-left: 6'>\n"
					+ "<a href='javascript:void(0)' id='"
					+ folderId
					+ "' name='paras' class='link' title='"+Messages.getString("qa.all.fa.clickToShrink")+"' "
					+ " onclick='clickFolder(id, name)'><span id='" + folderId + "_span'>-</span> " + curProject.getName() + "</a></td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotApprovedParas() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotApprovedParasRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getApprovedParas() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getApprovedParasRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getLockedParas() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTotalParas() + "</td>\n" 
					+ "</tr>");
			paraSetInputData(curProject, paraFAdata, paddLeft);

			paraFAdata.append("\t</table>\n");
			output.write(paraFAdata.toString().getBytes("UTF-8"));
			
			/*String htmlPathDiv = "<div style='width:100%;font-size:14;color:blue;'>"+Messages.getString("qa.all.fa.fileLocation")
				+ curProject.getFullPath().append("Report").append(htmlName).toOSString() + "</div>";
			output.write(htmlPathDiv.getBytes("UTF-8"));*/

			// -----------文本段结束--------------
			
			output.write("\t</body>\n".getBytes("UTF-8"));
			output.write("</html>".getBytes("UTF-8"));
			output.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.EditProgressFA.log3"), e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.EditProgressFA.log4"), e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.EditProgressFA.log5"), e);
		}

		return htmlPath;
	}

	/**
	 * 将文件下所有的子文件（直接或间接子文件）的值加到文件夹中
	 */
	public void setDataToFolder(IProject curProject) {
		// 先存放项目相关的信息，所有的文件都存放在该项目中的，因此直接遍历所有的文件
		EditProgressFAResult proFaResult = new EditProgressFAResult();
		for (int i = 0; i < model.getAnalysisIFileList().size(); i++) {
			IFile ifile = model.getAnalysisIFileList().get(i);
			EditProgressFAResult faResult = editProgFAResultMap.get(ifile.getLocation().toOSString());

			proFaResult.setNotApprovedParas(faResult.getNotApprovedParas());
			proFaResult.setApprovedParas(faResult.getApprovedParas());
			proFaResult.setLockedParas(faResult.getLockedParas());
			proFaResult.setNotApprovedWords(faResult.getNotApprovedWords());
			proFaResult.setApprovedWords(faResult.getApprovedWords());
			proFaResult.setLockedWords(faResult.getLockedWords());

		}
		editProgFAResultMap.put(curProject.getLocation().toOSString(), proFaResult);

		// 先遍历所有的文件夹
		for (int index = 0; index < allFolderList.size(); index++) {
			IContainer container = allFolderList.get(index);
			EditProgressFAResult folderFaResult = new EditProgressFAResult();
			// 循环所有的已经分析完的文件
			for (int i = 0; i < model.getAnalysisIFileList().size(); i++) {
				IFile ifile = model.getAnalysisIFileList().get(i);
				IContainer iFileParent = ifile.getParent();
				while (iFileParent != null) {
					if (iFileParent.equals(container)) {
						EditProgressFAResult faResult = editProgFAResultMap.get(ifile.getLocation().toOSString());

						folderFaResult.setNotApprovedParas(faResult.getNotApprovedParas());
						folderFaResult.setApprovedParas(faResult.getApprovedParas());
						folderFaResult.setLockedParas(faResult.getLockedParas());
						folderFaResult.setNotApprovedWords(faResult.getNotApprovedWords());
						folderFaResult.setApprovedWords(faResult.getApprovedWords());
						folderFaResult.setLockedWords(faResult.getLockedWords());
						break;
					} else {
						iFileParent = iFileParent.getParent();
					}
				}
			}
			editProgFAResultMap.put(container.getLocation().toOSString(), folderFaResult);
		}
	}

	/**
	 * 向字数分析的字数展示模块填充数据
	 * @param curContainer
	 * @param wordsFAdata
	 */
	public void wordsSetInputData(IContainer curContainer, StringBuffer wordsFAdata, int paddLeft) {
		paddLeft += 10;
		// 先判断该容器中是否有直接子文件为本次分析文件
		if (hasFAIFiles(curContainer)) {
			// 每个文件的具体数据
			for (int fIndex = 0; fIndex < model.getAnalysisIFileList().size(); fIndex++) {
				IFile curIFile = model.getAnalysisIFileList().get(fIndex);
				if (curIFile.getParent().equals(curContainer)) {
					EditProgressFAResult faResult = editProgFAResultMap.get(curIFile.getLocation().toOSString());

					wordsFAdata.append("\t\t<tr id='" + curContainer.getFullPath().toOSString() + "' name='words' " +
							"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n");
					wordsFAdata.append("\t\t\t<td class='fileTd' style='padding-left: " + paddLeft + "'>"
							+ curIFile.getName() + "</td>\n");// 文件名
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getNotApprovedWords()
							+ "</td>\n"); // 未批准字数
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>"
							+ faResult.getNotApprovedWordsRatio() + "</td>\n"); // 未批准字数比例
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getApprovedWords()
							+ "</td>\n"); // 已批准字数
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>"
							+ faResult.getApprovedWordsRatio() + "</td>\n"); // 已批准字数比例
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>"
							+ faResult.getLockedWords() + "</td>\n"); // 锁定字数
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getTotalWords()
							+ "</td>\n"); // 总字数
					wordsFAdata.append("\t\t</tr>\n");
				}
			}
		}
		// 遍历所有含有分析文件的容器，找出当前容器的子容器，
		for (int index = 0; index < allFolderList.size(); index++) {
			IContainer childContainer = allFolderList.get(index);
			if (childContainer.getParent().equals(curContainer)) {
				EditProgressFAResult faResult = editProgFAResultMap.get(childContainer.getLocation().toOSString());
				String folderId = childContainer.getFullPath().toOSString();
				wordsFAdata.append("<tr id='" + curContainer.getFullPath().toOSString() + "' name='words' " +
						"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n"
						+ "<td class='folderTd' ><a href='javascript:void(0)' id='" + folderId
						+ "' name='words' class='link' style='padding-left: " + paddLeft + "'"
						+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)'>" +
								"<span id='" + folderId + "_span'>-</span> " + childContainer.getName() + "</a></td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotApprovedWords() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotApprovedWordsRatio() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getApprovedWords() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getApprovedWordsRatio() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getLockedWords() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getTotalWords() + "</td>\n" 
						+ "</tr>\n");
				wordsSetInputData(childContainer, wordsFAdata, paddLeft);
			}
		}
	}

	public void paraSetInputData(IContainer curContainer, StringBuffer paraFAdata, int paddLeft) {
		paddLeft += 10;
		// 先判断该容器中是否有直接子文件为本次分析文件
		if (hasFAIFiles(curContainer)) {
			// 每个文件的具体数据
			for (int fIndex = 0; fIndex < model.getAnalysisIFileList().size(); fIndex++) {
				IFile curIFile = model.getAnalysisIFileList().get(fIndex);
				if (curIFile.getParent().equals(curContainer)) {
					EditProgressFAResult faResult = editProgFAResultMap.get(curIFile.getLocation().toOSString());

					paraFAdata.append("\t\t<tr id='" + curContainer.getFullPath().toOSString() + "' name='paras' " +
							"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n");
					paraFAdata.append("\t\t\t<td class='fileTd' style='padding-left: " + paddLeft + "'>"
							+ curIFile.getName() + "</td>\n");// 文件名
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getNotApprovedParas()
							+ "</td>\n"); // 未批准文本段个数
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>"
							+ faResult.getNotApprovedParasRatio() + "</td>\n"); // 未批准文本段比例
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getApprovedParas()
							+ "</td>\n"); // 已批准文本段个数
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getApprovedParasRatio()
							+ "</td>\n"); // 已批准文本段比例
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getLockedParas()
							+ "</td>\n"); // 锁定文本段
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getTotalParas()
							+ "</td>\n"); // 总段数
					paraFAdata.append("\t\t</tr>\n");
				}
			}
		}
		// 遍历所有含有分析文件的容器，找出当前容器的子容器，
		for (int index = 0; index < allFolderList.size(); index++) {
			IContainer childContainer = allFolderList.get(index);
			if (childContainer.getParent().equals(curContainer)) {
				EditProgressFAResult faResult = editProgFAResultMap.get(childContainer.getLocation().toOSString());
				String folderId = childContainer.getFullPath().toOSString();
				paraFAdata.append("<tr id='" + curContainer.getFullPath().toOSString() + "' name='paras' " +
						"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n"
						+ "<td class='folderTd' ><a href='javascript:void(0)' id='" + folderId
						+ "' name='paras' class='link' style='padding-left: " + paddLeft + "'"
						+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)'>" +
								"<span id='" + folderId + "_span'>-</span> " + childContainer.getName() + "</a></td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotApprovedParas() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotApprovedParasRatio() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getApprovedParas() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getApprovedParasRatio() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getLockedParas() + "</td>\n" 
						+ "<td class='folderTd' align='right'>"+ faResult.getTotalParas() + "</td>\n" 
						+ "</tr>\n");
				paraSetInputData(childContainer, paraFAdata, paddLeft);
			}
		}
	}
}
