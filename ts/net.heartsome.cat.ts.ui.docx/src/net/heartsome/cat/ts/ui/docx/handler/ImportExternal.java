/**
 * ImportExternal.java
 *
 * Version information :
 *
 * Date:2013-10-31
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.docx.handler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.docx.dialog.ImportExternalDialog;
import net.heartsome.cat.ts.ui.docx.resource.Messages;
import net.heartsome.cat.ts.ui.external.ImportConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class ImportExternal extends AbstractHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImportExternal.class);

	@SuppressWarnings("deprecation")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		XLIFFEditorImplWithNatTable xliffEditor = null;
		final Shell shell = HandlerUtil.getActiveShell(event);
		String partId = HandlerUtil.getActivePartId(event);
		IFile file = null;
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			xliffEditor = (XLIFFEditorImplWithNatTable) editor;
		}
		if (partId.equals("net.heartsome.cat.common.ui.navigator.view")) {
			// 导航视图处于激活状态
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView("net.heartsome.cat.common.ui.navigator.view");
			StructuredSelection selection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
					.getSelection();
			if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
				List<?> lstObj = ((IStructuredSelection) selection).toList();
				ArrayList<IFile> lstXliff = new ArrayList<IFile>();
				for (Object obj : lstObj) {
					if (obj instanceof IFile) {
						IFile tempFile = (IFile) obj;
						// Linux 下的文本文件无扩展名，因此要先判断扩展名是否为空
						if (tempFile.getFileExtension() != null
								&& CommonFunction.validXlfExtension(tempFile.getFileExtension())) {
							lstXliff.add(tempFile);
						}
					}
				}
				if (lstXliff.size() > 1) {
					MessageDialog.openInformation(shell, Messages.getString("all.dialog.ok.title"),
							Messages.getString("ImportDocxHandler.msg1"));
					return null;
				}
				if (lstXliff.size() == 1) {
					file = lstXliff.get(0);
				}
			}
		} else if (partId.equals("net.heartsome.cat.ts.ui.xliffeditor.nattable.editor")) {
			// nattable 处于激活状态
			IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
			IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
			IFile iFile = (IFile) editorInput.getAdapter(IFile.class);

			if (xliffEditor.isMultiFile()) {
				MessageDialog.openInformation(shell, Messages.getString("all.dialog.ok.title"),
						Messages.getString("ImportDocxHandler.msg2"));
				return null;
			} else if (iFile.getFileExtension() != null && CommonFunction.validXlfExtension(iFile.getFileExtension())) {
				file = iFile;
			}
		}
		if (file != null) {
			XLFValidator.resetFlag();
			if (!XLFValidator.validateXliffFile(file)) {
				return null;
			}
			XLFValidator.resetFlag();
		}

		final ImportConfig config = new ImportConfig();
		config.setShell(shell);
		config.set_xliff(file == null ? "" : file.getFullPath().toOSString());
		config.setXliffEditor(xliffEditor);
		config.setXliffFile(file == null ? "" : ResourceUtils.iFileToOSPath(file));
		HsMultiActiveCellEditor.commit(true);
		ImportExternalDialog dialog = new ImportExternalDialog(shell, xliffEditor, config);
		if (Dialog.OK == dialog.open()) {
			config.doImport();
			if (xliffEditor != null) {
				// reopen if need
				if (xliffEditor.getXLFHandler().getVnMap().get(config.getXliffFile()) != null) {
					Map<String, Object> resultMap = xliffEditor.getXLFHandler().openFile(config.getXliffFile());
					if (resultMap == null
							|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap
									.get(Constant.RETURNVALUE_RESULT)) {
						// 打开文件失败。
						MessageDialog.openWarning(
								shell,
								Messages.getString("all.dialog.warning"),
								MessageFormat.format(Messages.getString("ImportDocxDialog.ok.parseError"),
										config.get_xliff()));
						LOGGER.error(MessageFormat.format(Messages.getString("ImportDocxDialog.ok.parseError"),
								config.get_xliff()));
						return null;
					}
					xliffEditor.reloadData();
					HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
				}
			}
		}
		return null;
	}
}
