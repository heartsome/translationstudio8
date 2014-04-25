/**
 * AbstractExportHandler.java
 *
 * Version information :
 *
 * Date:2013-10-11
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.docx.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.bean.XliffBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.docx.resource.Messages;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.external.ExportConfig;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public abstract class AbstractExportHandler extends AbstractHandler {

	// warning : 如果更改项目结构，需要改正此字段，甚至此类。
	private final String XLF = "XLIFF";
	private Set<String> fileSet = new HashSet<String>();
	protected ExportConfig config;

	public boolean initExportConfig(ExecutionEvent event) throws ExecutionException {
		config = new ExportConfig();
		Shell shell = HandlerUtil.getActiveShell(event);
		String partId = HandlerUtil.getActivePartId(event);

		if (partId.equals("net.heartsome.cat.common.ui.navigator.view")) {// 导航视图处于激活状态
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView("net.heartsome.cat.common.ui.navigator.view");
			StructuredSelection selection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
					.getSelection();
			if (selection != null && !selection.isEmpty()) {
				for (Object obj : selection.toList()) {
					if (obj instanceof IFile) {
						addXLFFile((IFile) obj);
					} else if (obj instanceof IFolder) {
						traversalFile((IFolder) obj);
					} else if (obj instanceof IProject) {
						IProject proj = (IProject) obj;
						traversalFile(proj.getFolder(XLF));
					}
				}
				if (config.getProjects() == null || config.getProjects().size() < 1) {
					MessageDialog.openInformation(shell, Messages.getString("all.dialog.ok.title"),
							Messages.getString("xlf2tmx.info.notfoundxlf"));
					return false;
				}
			}
		} else if (partId.equals("net.heartsome.cat.ts.ui.xliffeditor.nattable.editor")) {// nattable 处于激活状态
			IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
			IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
			IFile iFile = (IFile) editorInput.getAdapter(IFile.class);
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			IXliffEditor xliffEditor = (IXliffEditor) editor;

			if (xliffEditor.isMultiFile()) {
				MessageDialog.openInformation(shell, Messages.getString("all.dialog.ok.title"),
						Messages.getString("ExportDocxHandler.msg2"));
				return false;
			} else if (iFile.getFileExtension() != null && CommonFunction.validXlfExtension(iFile.getFileExtension())) {
				addXLFFile(iFile);
			}
		}

		return true;
	}

	private void traversalFile(IFolder folder) {
		IResource[] ress = null;
		try {
			if (folder == null) {
				return;
			}
			ress = folder.members();
		} catch (CoreException e) {
		} finally {
			if (ress == null) {
				return;
			}
		}

		for (IResource res : ress) {
			if (res instanceof IFile) {
				addXLFFile((IFile) res);
			} else if (res instanceof IFolder) {
				traversalFile((IFolder) res);
			} else { // do nothing
			}
		}
	}

	private void addXLFFile(IFile file) {
		if (!fileSet.contains(file.getFullPath())) {// 防止又选项目，又选文件夹，又选文件
			IPath path = file.getFullPath();
			if (path.segment(1).equals(XLF)) {// 必须存放在 XLIFF 中(should check??)
				if (file.getFileExtension() != null && CommonFunction.validXlfExtension(file.getFileExtension())) {
					fileSet.add(file.toString());
					XliffBean bean = getXlfBean(file);
					if (bean != null) {
						config.addXlfBean(file.getProject(), bean);
					}
				}
			}
		}
	}

	private XliffBean getXlfBean(IFile file) {
		XliffBean bean = null;
		XLFHandler xlfHandler = new XLFHandler();
		Map<String, Object> result = xlfHandler.openFile(file.getLocation().toOSString());
		Object obj = result.get(Constant.RETURNVALUE_RESULT);
		if (obj != null && (Integer) obj == Constant.RETURNVALUE_RESULT_SUCCESSFUL) {
			bean = xlfHandler.getXliffInfo().get(file.getLocation().toOSString()).get(0);
		}
		return bean;
	}

}
