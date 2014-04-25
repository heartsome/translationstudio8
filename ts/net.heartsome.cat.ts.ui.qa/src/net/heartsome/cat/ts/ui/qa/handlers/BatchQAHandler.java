package net.heartsome.cat.ts.ui.qa.handlers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.QualityAssurance;
import net.heartsome.cat.ts.ui.qa.dialogs.BatchQADialog;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.qa.views.QAResultViewPart;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 批量检查的handler
 * @author robert 2011-11-09
 */
public class BatchQAHandler extends AbstractHandler {
	private QualityAssurance quality;
	private IPreferenceStore preferenceStore;
	private QAModel model;
	/** 针对选择当前 nattable 编辑器是否合并打开 */
	private boolean isMultiFile;
	public final static Logger logger = LoggerFactory.getLogger(BatchQAHandler.class.getName());
	
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		isMultiFile = false;
		preferenceStore = Activator.getDefault().getPreferenceStore();
		// UNDO 如果焦点在其他视图上时，获取的文件错误。
		IFile multiTempIFile = null;
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final Shell shell = window.getShell();
//		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView("net.heartsome.cat.common.ui.navigator.view");
		ArrayList<IFile> selectIFiles = new ArrayList<IFile>();
		if (HandlerUtil.getActivePart(event) instanceof IViewPart) {
			ISelection currentSelection = (StructuredSelection) viewPart.getSite().getSelectionProvider().getSelection();
			if (currentSelection != null && !currentSelection.isEmpty() && currentSelection instanceof IStructuredSelection) {

				IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;
				Iterator<Object> selectIt = structuredSelection.iterator();
				while (selectIt.hasNext()) {
					Object object = selectIt.next();
					if (object instanceof IFile) {
						IFile selectFile = (IFile) object;
						String fileExtension = selectFile.getFileExtension();
						// 如果后缀名不是xlf，那么就进行提示
						if (fileExtension == null || !CommonFunction.validXlfExtension(fileExtension)) {
							boolean isSure = MessageDialog.openConfirm(shell, Messages.getString("qa.all.dialog.warning"),
									MessageFormat.format(Messages.getString("qa.all.tip.notXliff"),
									new Object[] { selectFile.getFullPath() }));
							if (!isSure) {
								return null;
							}
							continue;
						}
						selectIFiles.add(selectFile);
					} else if (object instanceof IProject ) {
						IProject selectProject = (IProject) object;
						try {
							ResourceUtils.getXliffs(selectProject, selectIFiles);
						} catch (Exception e) {
							e.printStackTrace();
							logger.error(Messages.getString("qa.handlers.BatchQAHandler.log1"), e);
						}
					} else if (object instanceof IContainer) {
						IContainer selectContainer = (IContainer) object;
						try {
							ResourceUtils.getXliffs(selectContainer, selectIFiles);
						} catch (Exception e) {
							logger.error(Messages.getString("qa.handlers.BatchQAHandler.log1"), e);
							e.printStackTrace();
						}
					}
				}
			}
		}else {
			//如果左边未选择品质检查的类型，那么，获取nattable中打开的文件
			IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
			if (activeEditor != null && !activeEditor.getSite().getId().equals(XLIFF_EDITOR_ID)) {
				MessageDialog.openWarning(shell, Messages.getString("qa.all.dialog.warning"),
						Messages.getString("qa.handlers.BatchQAHandler.tip2"));
				return null;
			}
			XLIFFEditorImplWithNatTable nattable = (XLIFFEditorImplWithNatTable) activeEditor;
			isMultiFile = nattable.isMultiFile();
			multiTempIFile = ((FileEditorInput) nattable.getEditorInput()).getFile();
			if (isMultiFile) {
				List<String> multiFilesList = new XLFHandler().getMultiFiles(multiTempIFile);
				for (String multiFileStr : multiFilesList) {
					selectIFiles.add(ResourceUtils.fileToIFile(multiFileStr));
				}
			}else {
				selectIFiles.add(multiTempIFile);
			}
		}
		
		CommonFunction.removeRepeateSelect(selectIFiles);
		
		if (selectIFiles.size() == 0) {
			MessageDialog.openWarning(shell, Messages.getString("qa.all.dialog.warning"),
					Messages.getString("qa.handlers.BatchQAHandler.tip1"));
			return null;
		}
		
		List<IFile> lstFiles = new ArrayList<IFile>();
		XLFValidator.resetFlag();
		for (IFile iFile : selectIFiles) {
			if (!XLFValidator.validateXliffFile(iFile)) {
				lstFiles.add(iFile);
			}
		}
		XLFValidator.resetFlag();
		selectIFiles.removeAll(lstFiles);
		if (selectIFiles.size() == 0) {
			return null;
		}
		
		model = new QAModel();
		model.setQaXlfList(selectIFiles);
		quality = new QualityAssurance(model);
		
		BatchQADialog dialog = new BatchQADialog(shell, model, isMultiFile);
		int result = dialog.open();

		if (result == IDialogConstants.OK_ID) {
			// 先调用方法，查看品质检查结果视图是否处于显示状态，如果显示了的。删除数据
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = workbenchPage.findView(QAResultViewPart.ID);
			if (view != null) {
				// 运行时，将结果视图中列表的数据清除
				((QAResultViewPart) view).clearTableData();
			}

			QAResult qaResult = new QAResult();

			// 存储品质检查的检查项
			model.setBatchQAItemIdList(getBatchQAItems());
			// 存储品质检查的检查时不包括的文本段
			model.setNotInclude(getNotIncludePara());
			
			//给品质检查结果视图发出通告，本次检查对象为合并打开文件
//			qaResult.firePropertyChange(isMultiFile, new MultiFilesOper(selectIFiles.get(0).getProject(), selectIFiles, multiTempIFile));
			
			// 将当前所处理的文件传至 qaResult
			List<String> fileList = new ArrayList<String>();
			for(IFile iFIle : model.getQaXlfList()){
				fileList.add(iFIle.getLocation().toOSString());
			}
			qaResult.setFilePathList(fileList);
			
			HsMultiActiveCellEditor.commit(true);
			if (isMultiFile) {
				model.setMuliFiles(true);
				model.setMultiOper(new MultiFilesOper(selectIFiles.get(0).getProject(), selectIFiles, multiTempIFile));
				qaResult.setMultiOper(model.getMultiOper());
				quality.beginMultiFileQA(qaResult);
			}else {
				model.setMuliFiles(false);
				qaResult.setMultiOper(model.getMultiOper());
				quality.beginQA(qaResult);
			}
		}
		
		return null;
	}

	/**
	 * 从首选项中获取批量检查中要检查的项
	 * @return
	 */
	public LinkedList<String> getBatchQAItems() {
		LinkedList<String> itemsList = new LinkedList<String>();

		String itemsValue = preferenceStore.getString(QAConstant.QA_PREF_BATCH_QAITEMS);
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
}
