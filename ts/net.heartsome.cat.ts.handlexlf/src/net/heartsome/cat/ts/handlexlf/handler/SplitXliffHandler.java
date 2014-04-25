package net.heartsome.cat.ts.handlexlf.handler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.handlexlf.split.SplitOrMergeXlfModel;
import net.heartsome.cat.ts.handlexlf.wizard.NattableWizardDialog;
import net.heartsome.cat.ts.handlexlf.wizard.SplitXliffWizard;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
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
 * @author robert 2011-10-17 切割Xliff文件
 */
public class SplitXliffHandler extends AbstractHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SplitXliffHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String navegatorID = "net.heartsome.cat.common.ui.navigator.view";
		final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final Shell shell = window.getShell();

		IFile selectFile = null;
		XLIFFEditorImplWithNatTable xliffEditor = null;
		List<Integer> splitXlfPointsIndex = new LinkedList<Integer>();
		List<String> splitXlfPointsRowId = new LinkedList<String>();
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
								Messages.getString("handler.SplitXliffHandler.msgTitle"),
								Messages.getString("handler.SplitXliffHandler.msg1"));
						return null;
					}
				}
			}

		} else if (activePart instanceof IEditorPart) {
			if (XLIFF_EDITOR_ID.equals(activePart.getSite().getId())) {
				xliffEditor = (XLIFFEditorImplWithNatTable) activePart;
				selectFile = ((FileEditorInput) xliffEditor.getEditorInput()).getFile();
			}
		}

		// 根据每个tu节点的rowId获取其具体的位置，才好进行排序
		Map<Integer, String> pointIndexRowIdMap = new HashMap<Integer, String>();
		for (String rowId : xliffEditor.getSplitXliffPoints()) {
			// 获取指定tu节点所处其他结点的序列号
			int tuPostion = xliffEditor.getXLFHandler().getTUPositionByRowId(rowId);
			if (tuPostion >= 1) {
				splitXlfPointsIndex.add(tuPostion);
				pointIndexRowIdMap.put(tuPostion, rowId);
			}
		}

		if (splitXlfPointsIndex.size() <= 0) {
			MessageDialog.openInformation(shell,
					Messages.getString("handler.SplitXliffHandler.msgTitle"),
					Messages.getString("handler.SplitXliffHandler.msg2"));
			return null;
		}

		// 对切割点集合进行排序
		for (int i = 0; i < splitXlfPointsIndex.size(); i++) {
			int point1 = splitXlfPointsIndex.get(i);
			for (int j = i + 1; j < splitXlfPointsIndex.size(); j++) {
				int point2 = splitXlfPointsIndex.get(j);
				if (point1 > point2) {
					splitXlfPointsIndex.set(i, point2);
					splitXlfPointsIndex.set(j, point1);
					point1 = point2;
				}
			}
		}
		// 向存储rowId的list存放数据，这样的话，所存储的rowId就是经过排序了的。
		for (int i = 0; i < splitXlfPointsIndex.size(); i++) {
			splitXlfPointsRowId.add(pointIndexRowIdMap.get(splitXlfPointsIndex.get(i)));
		}

		SplitOrMergeXlfModel model = new SplitOrMergeXlfModel();
		model.setSplitFile(selectFile);
		model.setSplitXlfPointsIndex(splitXlfPointsIndex);
		model.setSplitXlfPointsRowId(splitXlfPointsRowId);
		model.setXliffEditor(xliffEditor);
		model.setShell(shell);

		SplitXliffWizard wizard = new SplitXliffWizard(model);
		final TSWizardDialog dialog = new NattableWizardDialog(shell, wizard);
		dialog.open();

		return null;
	}

}
