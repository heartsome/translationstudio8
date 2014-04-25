package net.heartsome.cat.database.ui.tm.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.database.ui.tm.wizard.UpdateTMWizard;
import net.heartsome.cat.database.ui.tm.wizard.UpdateTMWizardDialog;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目 > 更新记忆库的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class UpdateTMHandler extends AbstractHandler {

	public static final Logger LOGGER = LoggerFactory.getLogger(UpdateTMHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		String partId = HandlerUtil.getActivePartId(event);
		ArrayList<IFile> lstXliff = new ArrayList<IFile>();
//		boolean isShowCurrentLangBtn = true;
		if (partId.equals("net.heartsome.cat.common.ui.navigator.view")) {
			// 导航视图处于激活状态
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView("net.heartsome.cat.common.ui.navigator.view");
			StructuredSelection selection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
					.getSelection();
			// ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
				List<?> lstObj = ((IStructuredSelection) selection).toList();
				for (Object obj : lstObj) {
					if (obj instanceof IFile) {
						IFile file = (IFile) obj;
						// Linux 下的文本文件无扩展名，因此要先判断扩展名是否为空
						if (file.getFileExtension() != null && CommonFunction.validXlfExtension(file.getFileExtension())) {
							lstXliff.add(file);
						}
					} else if (obj instanceof IFolder) {
						try {
							ResourceUtils.getXliffs((IFolder) obj, lstXliff);
						} catch (CoreException e) {
							LOGGER.error(Messages.getString("handler.UpdateTMHandler.logger1"), e);
							MessageDialog.openInformation(shell,
									Messages.getString("handler.UpdateTMHandler.msgTitle"),
									Messages.getString("handler.UpdateTMHandler.msg1"));
						}
					} else if (obj instanceof IProject) {
						try {
							ResourceUtils.getXliffs((IProject) obj, lstXliff);
						} catch (CoreException e) {
							LOGGER.error(Messages.getString("handler.UpdateTMHandler.logger2"), e);
							MessageDialog.openInformation(shell,
									Messages.getString("handler.UpdateTMHandler.msgTitle"),
									Messages.getString("handler.UpdateTMHandler.msg2"));
						}
					}
				}
				
				CommonFunction.removeRepeateSelect(lstXliff);

				if (lstXliff.size() == 0) {
					MessageDialog.openInformation(shell, Messages.getString("handler.UpdateTMHandler.msgTitle"),
							Messages.getString("handler.UpdateTMHandler.msg3"));
					return null;
				}

				Iterator<IFile> iterator = lstXliff.iterator();

				while (iterator.hasNext()) {
					IFile file = iterator.next();
					FileEditorInput editorInput = new FileEditorInput(file);
					IEditorPart editorPart = page.findEditor(editorInput);
					if (editorPart == null || (editorPart != null && !(editorPart instanceof IXliffEditor))) {
//						isShowCurrentLangBtn = false;
						break;
					}
				}
			} else {
				MessageDialog.openInformation(shell, Messages.getString("handler.UpdateTMHandler.msgTitle"),
						Messages.getString("handler.UpdateTMHandler.msg3"));
				return null;
			}
		} else if (partId.equals("net.heartsome.cat.ts.ui.xliffeditor.nattable.editor")) {
			// nattable 处于激活状态
			IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
			IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
			IFile iFile = (IFile) editorInput.getAdapter(IFile.class);
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			IXliffEditor xliffEditor = (IXliffEditor) editor;

			if (xliffEditor.isMultiFile()) {
				List<String> lstFile = new XLFHandler().getMultiFiles(iFile);
				if (lstFile.size() > 0) {
					for (String filePath : lstFile) {
						lstXliff.add(ResourceUtils.fileToIFile(filePath));
					}
				}
			} else if (iFile.getFileExtension() != null && CommonFunction.validXlfExtension(iFile.getFileExtension())) {
				lstXliff.add(iFile);
			}
		}
		if (lstXliff.size() > 0) {
			if (lstXliff.size() > 1) {
				String projectPath = lstXliff.get(0).getProject().getFullPath().toOSString();
				for (int i = 1; i < lstXliff.size(); i++) {
					String projectPath2 = lstXliff.get(i).getProject().getFullPath().toOSString();
					if (!projectPath.equals(projectPath2)) {
						MessageDialog.openInformation(shell, Messages.getString("handler.UpdateTMHandler.msgTitle"),
								Messages.getString("handler.UpdateTMHandler.msg4"));
						return null;
					}
				}
			}
			ArrayList<IFile> lstFiles = new ArrayList<IFile>();
			XLFValidator.resetFlag();
			for (IFile iFile : lstXliff) {
				if (!XLFValidator.validateXliffFile(iFile)) {
					lstFiles.add(iFile);
				}
			}
			XLFValidator.resetFlag();
			lstXliff.removeAll(lstFiles);
			if (lstXliff.size() == 0) {
				return null;
			}
			
			ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(lstXliff.get(0).getProject());
			if (projectConfig.getDefaultTMDb() == null) {
				MessageDialog.openInformation(shell, Messages.getString("handler.UpdateTMHandler.msgTitle"),
						Messages.getString("handler.UpdateTMHandler.msg5"));
				return null;
			}

			UpdateTMWizard wizard = new UpdateTMWizard(lstXliff);
			TSWizardDialog dialog = new UpdateTMWizardDialog(shell, wizard);
//			UpdateTMDialog dialog = new UpdateTMDialog(shell, isShowCurrentLangBtn, lstXliff);
			dialog.open();
		}
		return null;
	}

}
