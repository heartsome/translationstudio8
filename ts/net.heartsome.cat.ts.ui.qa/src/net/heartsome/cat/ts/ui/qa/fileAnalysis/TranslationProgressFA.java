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
import net.heartsome.cat.ts.ui.qa.model.TransProgressFAResult;
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
 * 翻译进度分析 monitor进度提示语句： 翻译进度分析: 文件{0}翻译进度分析 ... ...
 * @author robert 2011-12-13
 */
public class TranslationProgressFA extends FileAnalysis {
	private FAModel model;
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格 */
	private static int workInterval = 4;
	/** 翻译进度分析结果，key为文件的绝对路径，值为已翻译文本段等值的pojo类 */
	private Map<String, TransProgressFAResult> transProgFAResultMap;
	/** 所有包括分析文件的容器 */
	private List<IContainer> allFolderList;
	public final static Logger logger = LoggerFactory.getLogger(TranslationProgressFA.class.getName());
	
	@Override
	public int beginAnalysis(FAModel model, IProgressMonitor monitor, QAXmlHandler handler) {
		this.model = model;
		super.setModel(model);

		transProgFAResultMap = new HashMap<String, TransProgressFAResult>();

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
			monitor.setTaskName(MessageFormat.format(Messages.getString("qa.fileAnalysis.TranslationProgressFA.tip1"),
					iFile.getFullPath().toOSString()));
			String iFilePath = iFile.getLocation().toOSString();

			// 获取单个文件的翻译进度的数据
			Map<String, Integer> transProgMap = handler.getTransProgressData(iFilePath, monitor, workInterval, traversalTuIndex);
			// 如果获取的结果为null,则表示用户退出程序
			if (transProgMap == null) {
				return QAConstant.QA_ZERO;
			}

			traversalTuIndex += handler.getTuSizeMap().get(iFilePath);

			int notTransPara = transProgMap.get("notTransPara");
			int translatedPara = transProgMap.get("translatedPara");
			int notTransWords = transProgMap.get("notTransWords");
			int translatedWords = transProgMap.get("translatedWords");
			int lockedWords = transProgMap.get("lockedWords");
			int lockedPara = transProgMap.get("lockedPara");

			TransProgressFAResult transResult = new TransProgressFAResult(
					notTransPara, translatedPara, lockedPara,
					notTransWords, translatedWords, lockedWords );
			transProgFAResultMap.put(iFilePath, transResult);
		}

		if (!handler.monitorWork(monitor, traversalTuIndex, workInterval, true)) {
			return QAConstant.QA_ZERO;
		}

		// 开始填充数据
		printTransProgFAReslut();

		return QAConstant.QA_FIRST;
	}

	public void printTransProgFAReslut() {
		String htmlPath = createFAResultHtml();
		try {
			model.getAnalysisIFileList().get(0).getProject().getFolder("Intermediate").getFolder("Report").refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.TranslationProgressFA.log1"), e1);
		}
		
		final FileEditorInput input = new FileEditorInput(ResourceUtils.fileToIFile(htmlPath));
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, QAConstant.FA_HtmlBrowserEditor, true);
				} catch (PartInitException e) {
					e.printStackTrace();
					logger.error(Messages.getString("qa.fileAnalysis.TranslationProgressFA.log2"), e);
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
		String htmlName = "TRLog" + htmlNameTime + ".html";
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
			headerNode = headerNode.replace("###Title###",
					Messages.getString("qa.fileAnalysis.TranslationProgressFA.name1"));

			output.write(headerNode.getBytes("UTF-8"));
			output.write("\t<body>\n".getBytes("UTF-8"));
			output.write(("<p class=\"title\">"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.TransProgresFA") + "</p>")
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
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.words") + "</b></div>")
					.getBytes("UTF-8"));

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
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.nonTrans") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' colSpan='2' width='26%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.translated") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.lockedWords") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.wordsSum") + "</td>\n");
			wordsFAdata.append("\t\t</tr>\n");
			
			wordsFAdata.append("\t\t<tr>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transWords") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transWordsProp") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transWords") + "</td>\n");
			wordsFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transWordsProp") + "</td>\n");
			wordsFAdata.append("\t\t</tr>\n");

			// 首先写下项目
			TransProgressFAResult proFaResult = transProgFAResultMap.get(curProject.getLocation().toOSString());
			int paddLeft = 6;
			String folderId = curProject.getFullPath().toOSString();
			wordsFAdata.append("<tr onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n" +
					"<td class='folderTd' style='padding-left: 6'>\n"
					+ "<a href='javascript:void(0)' id='" + folderId
					+ "' name='words' class='link' title='" + Messages.getString("qa.all.fa.clickToShrink") + "' "
					+ " onclick='clickFolder(id, name)' ><span id='" + folderId + "_span'>-</span> "
					+ curProject.getName() + "</a></td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotTransWords() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotTransWordsRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTranslatedWords() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTransWordsRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getLockedWords() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTotalWords() + "</td>\n" + "</tr>");
			wordsSetInputData(curProject, wordsFAdata, paddLeft);

			wordsFAdata.append("\t</table>\n");
			output.write(wordsFAdata.toString().getBytes("UTF-8"));
			output.write("<br/>\n".getBytes("UTF-8"));

			// －－－－－－－－－－－－－－文本段---------------------

			output.write(("\t\t<div class=\"legendStyle\"><b>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.paragraph") + "</b></div>")
					.getBytes("UTF-8"));
			StringBuffer paraFAdata = new StringBuffer();

			// 创建一个表
			paraFAdata.append("\t<table class='tableStyle' cellpadding='0' cellspacing='1'> \n");
			// 表头
			paraFAdata.append("\t\t<tr>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='22%'>"
					+ Messages.getString("qa.all.fa.fileName") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' colSpan='2' width='26%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.nonTrans") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' colSpan='2' width='26%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.translated") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.lockedParasNum") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' rowSpan='2' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.parasSum") + "</td>\n");
			paraFAdata.append("\t\t</tr>\n");
			
			paraFAdata.append("\t\t<tr>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transParasNum") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transWordsProp") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transParasNum") + "</td>\n");
			paraFAdata.append("\t\t\t<td class='headerTd' width='13%'>"
					+ Messages.getString("qa.fileAnalysis.TranslationProgressFA.transWordsProp") + "</td>\n");
			paraFAdata.append("\t\t</tr>\n");

			// 首先写下项目
			paddLeft = 6;
			paraFAdata.append("<tr onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n" +
					"<td class='folderTd' style='padding-left: 6'>\n"
					+ "<a href='javascript:void(0)' id='" + folderId
					+ "' name='paras' class='link' title='" + Messages.getString("qa.all.fa.clickToShrink") + "' "
					+ " onclick='clickFolder(id, name)' ><span id='" + folderId + "_span'>-</span> "
					+ curProject.getName() + "</a></td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotTransPara() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getNotTransParasRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTranslatedPara() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTransParasRatio() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getLockedPara() + "</td>\n"
					+ "<td class='folderTd' align='right'>" + proFaResult.getTotalParas() + "</td>\n" 
					+ "</tr>");
			paraSetInputData(curProject, paraFAdata, paddLeft);

			paraFAdata.append("\t</table>\n");
			output.write(paraFAdata.toString().getBytes("UTF-8"));
//			output.write("\t\t</fieldset><br/>\n".getBytes("UTF-8"));

			// -----------文本段结束--------------
/*			String htmlPathDiv = "<div style='width:100%;font-size:14;color:blue;'>" + Messages.getString("qa.all.fa.fileLocation")
					+ curProject.getFullPath().append("Report").append(htmlName).toOSString() + "</div>";
			output.write(htmlPathDiv.getBytes("UTF-8"));*/
			
			output.write("\t</body>\n".getBytes("UTF-8"));
			output.write("</html>".getBytes("UTF-8"));
			output.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.TranslationProgressFA.log3"), e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.TranslationProgressFA.log4"), e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.fileAnalysis.TranslationProgressFA.log5"), e);
		}

		return htmlPath;
	}

	/**
	 * 将文件下所有的子文件（直接或间接子文件）的值加到文件夹中
	 */
	public void setDataToFolder(IProject curProject) {
		// 先存放项目相关的信息，所有的文件都存放在该项目中的，因此直接遍历所有的文件
		TransProgressFAResult proFaResult = new TransProgressFAResult();
		for (int i = 0; i < model.getAnalysisIFileList().size(); i++) {
			IFile ifile = model.getAnalysisIFileList().get(i);
			TransProgressFAResult faResult = transProgFAResultMap.get(ifile.getLocation().toOSString());

			proFaResult.setNotTransPara(faResult.getNotTransPara());
			proFaResult.setTranslatedPara(faResult.getTranslatedPara());
			proFaResult.setLockedPara(faResult.getLockedPara());
			proFaResult.setNotTransWords(faResult.getNotTransWords());
			proFaResult.setTranslatedWords(faResult.getTranslatedWords());
			proFaResult.setLockedWords(faResult.getLockedWords());
		}
		transProgFAResultMap.put(curProject.getLocation().toOSString(), proFaResult);

		// 先遍历所有的文件夹
		for (int index = 0; index < allFolderList.size(); index++) {
			IContainer container = allFolderList.get(index);
			TransProgressFAResult folderFaResult = new TransProgressFAResult();
			// 循环所有的已经分析完的文件
			for (int i = 0; i < model.getAnalysisIFileList().size(); i++) {
				IFile ifile = model.getAnalysisIFileList().get(i);
				IContainer iFileParent = ifile.getParent();
				while (iFileParent != null) {
					if (iFileParent.equals(container)) {
						TransProgressFAResult faResult = transProgFAResultMap.get(ifile.getLocation().toOSString());

						folderFaResult.setNotTransPara(faResult.getNotTransPara());
						folderFaResult.setTranslatedPara(faResult.getTranslatedPara());
						folderFaResult.setLockedPara(faResult.getLockedPara());
						folderFaResult.setNotTransWords(faResult.getNotTransWords());
						folderFaResult.setTranslatedWords(faResult.getTranslatedWords());
						folderFaResult.setLockedWords(faResult.getLockedWords());
						break;
					} else {
						iFileParent = iFileParent.getParent();
					}
				}
			}
			transProgFAResultMap.put(container.getLocation().toOSString(), folderFaResult);
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
					TransProgressFAResult faResult = transProgFAResultMap.get(curIFile.getLocation().toOSString());

					wordsFAdata.append("\t\t<tr id='" + curContainer.getFullPath().toOSString() + "' name='words' " +
							"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n");
					wordsFAdata.append("\t\t\t<td class='fileTd' style='padding-left: " + paddLeft + "'>"
							+ curIFile.getName() + "</td>\n");// 文件名
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getNotTransWords()
							+ "</td>\n"); // 未翻译字数
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>"
							+ faResult.getNotTransWordsRatio() + "</td>\n"); // 未翻译字数比例
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getTranslatedWords()
							+ "</td>\n"); // 已翻译字数
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getTransWordsRatio()
							+ "</td>\n"); // 已翻译字数比例
					wordsFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getLockedWords()
							+ "</td>\n"); // 锁定字数
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
				TransProgressFAResult faResult = transProgFAResultMap.get(childContainer.getLocation().toOSString());
				String folderId = childContainer.getFullPath().toOSString();
				wordsFAdata.append("<tr id='" + curContainer.getFullPath().toOSString() + "' name='words' " +
						"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n"
						+ "<td class='folderTd' ><a href='javascript:void(0)' id='" + folderId
						+ "' name='words' class='link' style='padding-left: " + paddLeft + "'"
						+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)'>" +
								"<span id='" + folderId + "_span'>-</span> " + childContainer.getName() + "</a></td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotTransWords() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotTransWordsRatio() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getTranslatedWords() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getTransWordsRatio() + "</td>\n" 
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
					TransProgressFAResult faResult = transProgFAResultMap.get(curIFile.getLocation().toOSString());

					paraFAdata.append("\t\t<tr id='" + curContainer.getFullPath().toOSString() + "' name='paras' " +
							"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n");
					paraFAdata.append("\t\t\t<td class='fileTd' style='padding-left: " + paddLeft + "'>"
							+ curIFile.getName() + "</td>\n");// 文件名
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getNotTransPara()
							+ "</td>\n"); // 未翻译文本段数
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getNotTransParasRatio()
							+ "</td>\n"); // 未翻译文本段比例
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getTranslatedPara()
							+ "</td>\n"); // 已翻译文本段数
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getTransParasRatio()
							+ "</td>\n"); // 已翻译文本段比例
					paraFAdata.append("\t\t\t<td class='fileTd' align='right'>" + faResult.getLockedPara()
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
				TransProgressFAResult faResult = transProgFAResultMap.get(childContainer.getLocation().toOSString());
				String folderId = childContainer.getFullPath().toOSString();
				paraFAdata.append("<tr id='" + curContainer.getFullPath().toOSString() + "' name='paras' " +
						"onmouseover= \"this.bgColor= '#F1F1FC'\" onmouseout= \"this.bgColor='#FFFFFF'\" bgcolor='#FFFFFF'>\n" +
						"<td class='folderTd' >\n" + "<a href='javascript:void(0)' id='" + folderId 
						+ "' name='paras' class='link' style='padding-left: " + paddLeft + "'"
						+ "title='" + Messages.getString("qa.all.fa.clickToShrink") + "' onclick='clickFolder(id, name)'>" +
								"<span id='" + folderId + "_span'>-</span> " + childContainer.getName() + "</a></td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotTransPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getNotTransParasRatio() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getTranslatedPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getTransParasRatio() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getLockedPara() + "</td>\n" 
						+ "<td class='folderTd' align='right'>" + faResult.getTotalParas() + "</td>\n" 
						+ "</tr>\n");
				paraSetInputData(childContainer, paraFAdata, paddLeft);
			}
		}
	}

}
