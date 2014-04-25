package org.eclipse.ui.internal.navigator.resources.actions;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenSystemEditorAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.navigator.resources.resource.WorkbenchNavigatorMessages;
import org.eclipse.ui.part.FileEditorInput;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * 打开xliff文件，并且验证，该类参照OpenFileAction来写的，并代替OpenFileAction。
 * @author robert 2012-06-07
 */
@SuppressWarnings("restriction")
public class OpenFileWithValidAction extends OpenSystemEditorAction {

	private IWorkbenchPage page;
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenFileWithValidAction";//$NON-NLS-1$
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	/**
	 * The editor to open.
	 */
	private IEditorDescriptor editorDescriptor;

	/**
	 * Creates a new action that will open editors on the then-selected file resources. Equivalent to
	 * <code>OpenFileAction(page,null)</code>.
	 * @param page
	 *            the workbench page in which to open the editor
	 */
	public OpenFileWithValidAction(IWorkbenchPage page) {
		this(page, null);
		this.page = page;
	}

	/**
	 * Creates a new action that will open instances of the specified editor on the then-selected file resources.
	 * @param page
	 *            the workbench page in which to open the editor
	 * @param descriptor
	 *            the editor descriptor, or <code>null</code> if unspecified
	 */
	public OpenFileWithValidAction(IWorkbenchPage page, IEditorDescriptor descriptor) {
		super(page);
		setText(descriptor == null ? IDEWorkbenchMessages.OpenFileAction_text : descriptor.getLabel());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.OPEN_FILE_ACTION);
		setToolTipText(IDEWorkbenchMessages.OpenFileAction_toolTip);
		setId(ID);
		this.editorDescriptor = descriptor;
	}

	/**
	 * Ensures that the contents of the given file resource are local.
	 * @param file
	 *            the file resource
	 * @return <code>true</code> if the file is local, and <code>false</code> if it could not be made local for some
	 *         reason
	 */
	boolean ensureFileLocal(final IFile file) {
		// Currently fails due to Core PR. Don't do it for now
		// 1G5I6PV: ITPCORE:WINNT - IResource.setLocal() attempts to modify immutable tree
		// file.setLocal(true, IResource.DEPTH_ZERO);
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		try {
			XLFValidator.resetFlag();
			Iterator itr = getSelectedResources().iterator();
			while (itr.hasNext()) {
				IResource resource = (IResource) itr.next();
				if (resource instanceof IFile) {
					openFile1((IFile) resource);
				}
			}
			XLFValidator.resetFlag();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens an editor on the given file resource.
	 * @param file
	 *            the file resource
	 */
	void openFile1(IFile file) throws Exception{
		try {
			boolean activate = OpenStrategy.activateOnOpen();
			if (editorDescriptor == null) {
				// 如果是nattble打开的，就验证其是否已经被打开
				if (!validIsopened(file)) {
					return;
				}
				String filePath = file.getFullPath().toOSString();
				// Bug #2519
				if (CommonFunction.validXlfExtensionByFileName(file.getName())
						|| (filePath.startsWith(File.separator + file.getProject().getName() + File.separator
								+ "Intermediate" + File.separator + "Report") && file.getName().endsWith(".html"))) {
					// 之前的这块验证 xliff 文件的代码是放在 nattable 打开时，现在改为在调用 nattable 打开之前
					// html 是不会验证路径的
					if (!file.getFullPath().getFileExtension().equals("html")) {
						if (XLFValidator.validateXliffFile(file)) {
							IDE.openEditor(page, file, activate);
						}
					}else {
						IDE.openEditor(page, file, activate);
					}
				} else {
					boolean openReult = Program.launch(file.getLocation().toOSString());
					if (!openReult) {
						MessageDialog.openWarning(page.getWorkbenchWindow().getShell(), WorkbenchNavigatorMessages.navigator_all_dialog_warning,
								MessageFormat.format(WorkbenchNavigatorMessages.actions_OpenFileWithValidAction_notFindProgram, new Object[]{file.getLocation().getFileExtension()}));
					}
				}
			} else {
				if (ensureFileLocal(file)) {
					if (!file.getFullPath().getFileExtension().equals("html")) {
						if (XLFValidator.validateXliffFile(file)) {
							page.openEditor(new FileEditorInput(file), editorDescriptor.getId(), activate);
						}
					}else {
						page.openEditor(new FileEditorInput(file), editorDescriptor.getId(), activate);
					}
				}
			}
		} catch (PartInitException e) {
			DialogUtil.openError(page.getWorkbenchWindow().getShell(),
					IDEWorkbenchMessages.OpenFileAction_openFileShellTitle, e.getMessage(), e);
		}
	}

	/**
	 * 验证是否已经被打开
	 * @param iFile
	 * @return 如果当前要打开的文件未被打开，那么验证通过，返回true,否则返回false。
	 */
	private boolean validIsopened(IFile iFile) throws Exception{
		String extention = iFile.getFileExtension();

		if (IDE.getDefaultEditor(iFile, true) != null
				&& XLIFF_EDITOR_ID.equals(IDE.getDefaultEditor(iFile, true).getId())
				&& CommonFunction.validXlfExtension(extention)) {
			IEditorReference[] editorRes = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getEditorReferences();
			IEditorPart editor = null;
			IFile openIFile = null;
			String needOpenIfileLc = iFile.getLocation().toOSString();
			for (int i = 0; i < editorRes.length; i++) {
				if (editorRes[i].getId().equals(XLIFF_EDITOR_ID)) {
					// 备注：这里的两行代码已经被注释了，原因是因为 getEditor 方法会让每一个 editor 都初始一次，这并不是本次需求所要的。注意。	2013-01-04
					//editor = editorRes[i].getEditor(true);
					//openIFile = ((IFileEditorInput) editor.getEditorInput()).getFile();
					openIFile = ((IFileEditorInput) editorRes[i].getEditorInput()).getFile();
					// 判断是否是合并打开，后缀名为xlp为合并打开，后缀名为xlf为单一打开
					if ("xlp".equals(openIFile.getFileExtension())) {
						// 开始解析这个合并打开临时文件，获取合并打开的文件。
						VTDGen vg = new VTDGen();
						if (vg.parseFile(openIFile.getLocation().toOSString(), true)) {
							VTDNav vn = vg.getNav();
							AutoPilot ap = new AutoPilot(vn);
							try {
								ap.selectXPath("/mergerFiles/mergerFile/@filePath");
								int index = -1;
								while ((index = ap.evalXPath()) != -1) {
									String fileLC = vn.toString(index + 1);
									if (fileLC != null && !"".equals(fileLC)) {
										if (fileLC.equals(needOpenIfileLc)) {
											editor = editorRes[i].getEditor(true);
											page.activate(editor);
											return false;
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
