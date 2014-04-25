package net.heartsome.cat.ts.handlexlf.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.bean.SegPointBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.handlexlf.dialog.SetSplitPointByWordNumDialog;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 设置分割点.
 * @author robert	2013-10-14
 *
 */
public class SetSplitPointByWordNumHandler extends AbstractHandler{
	private Logger LOGGER = LoggerFactory.getLogger(SetSplitPointByWordNumHandler.class);
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String navegatorID = "net.heartsome.cat.common.ui.navigator.view";
		final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final Shell shell = window.getShell();

		IFile selectFile = null;
		XLIFFEditorImplWithNatTable xliffEditor = null;
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		// 如果是导航视图，那么就获取导航视图中选中的文件
		if (activePart instanceof IViewPart) {
			if (navegatorID.equals(activePart.getSite().getId())) {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.findView(navegatorID);
				ISelection currentSelection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
						.getSelection();
				if (currentSelection != null && !currentSelection.isEmpty()
						&& currentSelection instanceof IStructuredSelection) {

					IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;
					Object object = structuredSelection.getFirstElement();
					if (object instanceof IFile) {
						selectFile = (IFile) object;
						String fileExtension = selectFile.getFileExtension();
						// 如果后缀名不是xlf，那么就退出操作
						if (fileExtension == null || !CommonFunction.validXlfExtension(fileExtension)) {
							MessageDialog.openInformation(shell,
									Messages.getString("handler.SplitXliffHandler.msgTitle"),
									Messages.getString("handler.SplitXliffHandler.msg1"));
							return null;
						}

						FileEditorInput fileInput = new FileEditorInput(selectFile);

						IEditorReference[] editorRefer = window.getActivePage().findEditors(fileInput, XLIFF_EDITOR_ID,
								IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
						IEditorPart editorPart = null;

						if (editorRefer.length >= 1) {
							editorPart = editorRefer[0].getEditor(true);
							xliffEditor = (XLIFFEditorImplWithNatTable) editorPart;
							if (window.getActivePage().getActiveEditor() != editorPart) {
								window.getActivePage().activate(editorPart);
							}
						} else {
							try {
								xliffEditor = (XLIFFEditorImplWithNatTable) window.getActivePage().openEditor(
										fileInput, XLIFF_EDITOR_ID, true,
										IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
							} catch (PartInitException e) {
								LOGGER.error("", e);
								e.printStackTrace();
							}
						}
					} else {
						MessageDialog.openInformation(shell,
								Messages.getString("all.dialog.info"),
								Messages.getString("SetSplitPointHandler.cantSetPointToNotXlf"));
						return null;
					}
				}
			}

		} else if (activePart instanceof IEditorPart) {
			if (XLIFF_EDITOR_ID.equals(activePart.getSite().getId())) {
				xliffEditor = (XLIFFEditorImplWithNatTable) activePart;
				if (xliffEditor.isMultiFile()) {
					MessageDialog.openInformation(shell, Messages.getString("all.dialog.info"), Messages.getString("SetSplitPointByWOrdNumHandler.cantSetForMulty"));
					return null;
				}
				selectFile = ((FileEditorInput) xliffEditor.getEditorInput()).getFile();
			}
		}
		if (selectFile == null) {
			return null;
		}
		
		XLIFFEditorImplWithNatTable nattable = XLIFFEditorImplWithNatTable.getCurrent();
		final XLFHandler xlfHander = nattable.getXLFHandler();
		Map<String, Integer> rowWordNumMap = xlfHander.getSplitFileInfoForPointSetting(selectFile.getLocation().toOSString());
		
		if (rowWordNumMap.size() <= 1) {
			MessageDialog.openInformation(shell, Messages
					.getString("all.dialog.info"), Messages
					.getString("SetSplitPointHandler.cantSetPointForTuNum"));
			return null;
		}
		
		List<String> splitPointList = nattable.getSplitXliffPoints();
		// 排序之前先过滤重复
		HashSet<String> set = new HashSet<String>(splitPointList);
		splitPointList.clear();
		splitPointList.addAll(set);
		
		// 先对 splitPointList 进行排序
		Collections.sort(splitPointList, new Comparator<String>() {
			public int compare(String rowId1, String rowId2) {
				int rowIndex1 = xlfHander.getRowIndex(rowId1);
				int rowIndex2 = xlfHander.getRowIndex(rowId2);
				return rowIndex1 > rowIndex2 ? 1 : -1;
			}
		});
		
		List<SegPointBean> segPointList = new ArrayList<SegPointBean>();
		if (splitPointList.size() > 0) {
			int segWordNum = 0;
			int i = 0;
			String pointRowId = splitPointList.get(i);
			for(Entry<String, Integer> entry : rowWordNumMap.entrySet()){
				segWordNum += entry.getValue();
				if (pointRowId.equals(entry.getKey())) {
					segPointList.add(new SegPointBean(segWordNum));
					segWordNum = 0;
					i ++;
					if (i < splitPointList.size()) {
						pointRowId = splitPointList.get(i);
					}
				}
			}
			if (splitPointList.size() + 1 > segPointList.size()) {
				segPointList.add(new SegPointBean(segWordNum));
			}
		}
		
		SetSplitPointByWordNumDialog dialog = new SetSplitPointByWordNumDialog(shell, xlfHander, selectFile.getLocation().toOSString(), rowWordNumMap, segPointList);
		int openResult = dialog.open();
		if (openResult == IDialogConstants.OK_ID) {
			// 如果选择确定，那么在界面上生成分割点
			nattable.getSplitXliffPoints().clear();
			for(SegPointBean bean : segPointList){
				if (bean.getRowId() != null && !bean.getRowId().isEmpty()) {
					nattable.getSplitXliffPoints().add(bean.getRowId());
				}
			}
			nattable.refresh();
		}
		
		return null;
	}
	
	
	
}
