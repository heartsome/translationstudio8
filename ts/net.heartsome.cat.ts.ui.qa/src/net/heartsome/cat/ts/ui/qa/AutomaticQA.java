package net.heartsome.cat.ts.ui.qa;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.qa.IAutomaticQA;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自动品质检查解发类
 * 若某个检查项的级别是错误，则弹出提示对话框
 * @author  robert	2012-02-13
 * @version 
 * @since   JDK1.6
 */
public class AutomaticQA implements IAutomaticQA{
	/** 品质检查运行时机，从首选项中获取, 当此值等于0时：为从不执行;等于1时，为入库时执行;等于2时，为批准文本段时执行 */
	private int runTime = -1;
	private IPreferenceStore preferenceStore;
	/** 保存某个检查项的实例 */
	private Map<String, QARealization> qaItemClassMap = new HashMap<String, QARealization>();
	private QAModel model;
	private IWorkbenchWindow window;
	private XLFHandler handler;
	private Map<String, Boolean> filterMap;
	private IWorkspaceRoot root;
	private IFile iFile;
	private QAXmlHandler xmlHandler;
	private QAResult qaResult;
	public final static Logger logger = LoggerFactory.getLogger(AutomaticQA.class.getName());
	private boolean isCancel = false;
	
	public AutomaticQA(){
		
	}
	
	public void setInitData(XLFHandler handler) {
		this.handler = handler;
		preferenceStore = Activator.getDefault().getPreferenceStore();
		model = new QAModel();
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		filterMap = getNotIncludePara();
		root = ResourcesPlugin.getWorkspace().getRoot();
		xmlHandler = new QAXmlHandler();
		
		// 存储品质检查的检查项
		model.setBatchQAItemIdList(getAutoQAItems());
		// 存储品质检查的检查时不包括的文本段
		model.setNotInclude(filterMap);
		model.setShell(window.getShell());
		
		XLIFFEditorImplWithNatTable nattable = XLIFFEditorImplWithNatTable.getCurrent();
		boolean isMultiFile = nattable.isMultiFile();
		if (isMultiFile) {
			model.setMuliFiles(true);
			IFile multiTempIFile = ((FileEditorInput) nattable.getEditorInput()).getFile();
			ArrayList<IFile> selectIFiles = (ArrayList<IFile>) ResourceUtils.filesToIFiles(nattable.getMultiFileList());
			model.setMultiOper(new MultiFilesOper(selectIFiles.get(0).getProject(), selectIFiles, multiTempIFile));
		}else {
			model.setMuliFiles(false);
		}
	}

	/**
	 * 开始进行自动品质检查
	 * @param isAddToDb 若为true,则是入库操作，若false,则为批准操作
	 * @return 若返回 ""，则表示没有检查到错误，若返回 null,则标识退出操作。
	 */
	public String beginAutoQa(boolean isAddToDb, String rowId, boolean needInitQAResultViewer) {
		runTime = preferenceStore.getInt(QAConstant.QA_PREF_AUTO_QARUNTIME);
		
		//从不执行
		if (runTime == QAConstant.QA_ZERO) {
			return "";
		}
		if (needInitQAResultViewer) {
			initQAResult();
		}
		
		//等于1时，为入库时执行
		if ((runTime == QAConstant.QA_FIRST || runTime == QAConstant.QA_THREE) && isAddToDb) {
			StringBuffer resultSB = new StringBuffer();
			autoQA(rowId, resultSB, isAddToDb);
			if (resultSB.length() > 0) {
				resultSB.append(MessageFormat.format(Messages.getString("qa.AutomaticQATrigger.tip5"), new Object[]{Messages.getString("qa.AutomaticQATrigger.name1")}));
			}
			return isCancel ? null : resultSB.toString();
		}
		//等于2时，为批准文本段时执行
		if ((runTime == QAConstant.QA_TWO  || runTime == QAConstant.QA_THREE) && !isAddToDb) {
			StringBuffer resultSB = new StringBuffer();
			autoQA(rowId, resultSB, isAddToDb);
			if (resultSB.length() > 0) {
				resultSB.append(MessageFormat.format(Messages.getString("qa.AutomaticQATrigger.tip5"), new Object[]{Messages.getString("qa.AutomaticQATrigger.name2")}));
			}
			return isCancel ? null : resultSB.toString();
		}
		
		return "";
	}
	
	public void informQAEndFlag(){
		if (qaResult != null) {
			qaResult.informQAEndFlag();
		}
	}
	
	public void autoQA(String rowId, StringBuffer resultSB, boolean isAddToDb){
		iFile = root.getFileForLocation(Path.fromOSString(RowIdUtil.getFileNameByRowId(rowId)));
		//先通过rowId获取品质检查所需要的数据
		QATUDataBean tuDataBean = handler.getAutoQAFilteredTUText(rowId, filterMap);
		if (!tuDataBean.isPassFilter()){
			return;
		}
		
		String lineNumber = "" + (handler.getRowIndex(rowId) + 1);
		tuDataBean.setLineNumber(lineNumber);
		tuDataBean.setRowId(rowId);
		tuDataBean.setFileName(iFile.getName());
		
		// 译文为空时，只有文本段完整性里面有检查，而，自动品质检查时已经翻译了的，因此不考虑此种情况
		if (tuDataBean.getTgtContent() == null || tuDataBean.getTgtContent().isEmpty()) {
			return;
		}
		
		resultSB.append(MessageFormat.format(
				Messages.getString("qa.AutomaticQATrigger.tip1"),
				new Object[] {
						lineNumber,
						isAddToDb ? Messages.getString("qa.AutomaticQATrigger.name1") : Messages
								.getString("qa.AutomaticQATrigger.name2") }));
		// 品质检查项的总数
		QARealization realization = null;
		boolean hasError = false;
		IProgressMonitor monitor = new NullProgressMonitor();
		for (int i = 0; i < model.getBatchQAItemIdList().size(); i++) {
			final String qaItemId = model.getBatchQAItemIdList().get(i);
			realization = getClassInstance(qaItemId, qaResult);
			// 若没有该项检查的实例，提示出错
			if (realization == null) {
				MessageDialog.openError(model.getShell(),
					Messages.getString("qa.AutomaticQATrigger.name3"),
					MessageFormat.format(Messages.getString("qa.AutomaticQATrigger.tip2"),
					new Object[] { model.getQaItemId_Name_Class().get(qaItemId).get(QAConstant.QA_ITEM_NAME) }));
			}
			
			// 开始进行该项文件的该项检查
			String result = realization.startQA(model, monitor, iFile, xmlHandler, tuDataBean);
			if (monitor.isCanceled()) {
				isCancel = true;
				return;
			}
			// 未配置术语库, 
			if (result == null) {
				model.getBatchQAItemIdList().remove(qaItemId);
				i --;
			}
			if (result != null && !"".equals(result)) {
				hasError = true;
				result = model.getQaItemId_Name_Class().get(result).get(QAConstant.QA_ITEM_NAME);
				resultSB.append("\t" + result).append(Messages.getString("qa.AutomaticQATrigger.tip3")).append("\n");
			}
		}
		// 这一步很重要，将数据传送至结果视图
		qaResult.sendDataToViewer(tuDataBean.getRowId());
		if (!hasError) {
			resultSB.delete(0, resultSB.length());
		}
		monitor.done();
		return;
	}
	
	/**
	 * 初始化品质检查结果视图， ;
	 */
	public void initQAResult() {
		qaResult = new QAResult();
		qaResult.setMultiOper(model.getMultiOper());
		qaResult.setAutoQA(true);
		qaResult.setFilePathList(handler.getFiles());
	}
	
	public void bringQAResultViewerToTop() {
		qaResult.bringQAResultViewerToTop();
	}
	
	/**
	 * 获取某个检查项实现类的实例
	 * @param qaItemId
	 * @return
	 */
	public QARealization getClassInstance(String qaItemId, QAResult qaResult) {
		if (qaItemClassMap.get(qaItemId) != null) {
			return (QARealization) qaItemClassMap.get(qaItemId);
		}

		try {
			HashMap<String, String> valueMap = model.getQaItemId_Name_Class().get(qaItemId);
			Object obj = null;
			try {
				obj = Class.forName(valueMap.get(QAConstant.QA_ITEM_CLASSNAME)).newInstance();
			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
			}
			if (QARealization.class.isInstance(obj)) {
				qaItemClassMap.put(qaItemId, (QARealization) obj);
				((QARealization) obj).setParentQaResult(qaResult);
				return (QARealization) obj;
			}
		} catch (Exception e) {
			logger.error(Messages.getString("qa.AutomaticQATrigger.tip4"), e);
		}
		return null;
	}
	
	/**
	 * 从首选项中获取自动检查中要检查的项
	 * @return
	 */
	public LinkedList<String> getAutoQAItems() {
		LinkedList<String> itemsList = new LinkedList<String>();

		String itemsValue = preferenceStore.getString(QAConstant.QA_PREF_AUTO_QAITEMS);
		List<String> itemsValList = new ArrayList<String>();
		String[] itemsValArray = itemsValue.split(",");
		for (int index = 0; index < itemsValArray.length; index++) {
			itemsValList.add(itemsValArray[index]);
		}
		
		//获取所有的品质检查项的标识符
		model.getQaItemId_Name_Class().keySet();
		Iterator<String> qaIt = model.getQaItemId_Name_Class().keySet().iterator();
		while (qaIt.hasNext()) {
			String qaItermId = qaIt.next();
			if (itemsValList.indexOf(qaItermId) >= 0) {
				itemsList.add(qaItermId);
			}
		}
		
		return itemsList;
	}
	
	
	/**
	 * 获取首选项中品质检查的不包括的文本段
	 * @return
	 */
	public Map<String, Boolean> getNotIncludePara() {
		Map<String, Boolean> notInclude = new HashMap<String, Boolean>();

		// 不包括上下文匹配
		notInclude.put(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE,
				preferenceStore.getBoolean(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE));
		// 不包括完成匹配
		notInclude.put(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE,
				preferenceStore.getBoolean(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE));
		// 不包括已锁文本段
		notInclude.put(QAConstant.QA_PREF_LOCKED_NOTINCLUDE,
				preferenceStore.getBoolean(QAConstant.QA_PREF_LOCKED_NOTINCLUDE));

		return notInclude;
	}
	
	/**
	 * 关闭数据库，只针对术语一致性查
	 *  ;
	 */
	public void closeDB(){
		if (qaItemClassMap.get(QAConstant.QA_TERM) != null) {
			QARealization termRealize = qaItemClassMap.get(QAConstant.QA_TERM);
			termRealize.closeDB();
		}
	}

	

}
