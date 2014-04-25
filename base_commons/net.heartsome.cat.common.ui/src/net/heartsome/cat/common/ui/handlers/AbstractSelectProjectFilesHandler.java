package net.heartsome.cat.common.ui.handlers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.resource.Messages;
import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理多选（在同一个项目中的）文件。支持选中项目、文件夹、文件。
 * @author weachy
 * @version
 * @since JDK1.5
 */
public abstract class AbstractSelectProjectFilesHandler extends AbstractHandler {

	public static final Logger LOGGER = LoggerFactory.getLogger(AbstractSelectProjectFilesHandler.class);

	protected Shell shell;
	/** 所选中的是否是编辑器 */
	protected boolean isEditor;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		shell = HandlerUtil.getActiveShell(event);
		isEditor = false;
		// UNDO 如果焦点在其他视图上时，获取的文件错误。
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection == null || !(selection instanceof StructuredSelection) || selection.isEmpty()) {
			MessageDialog.openInformation(shell,
					Messages.getString("handlers.AbstractSelectProjectFilesHandler.msgTitle"),
					Messages.getString("handlers.AbstractSelectProjectFilesHandler.msg1"));
			return null;
		}
		StructuredSelection structuredSelection = (StructuredSelection) selection;

		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		String partId = HandlerUtil.getActivePartIdChecked(event);
		if (part instanceof IEditorPart) { // 当前焦点在编辑器
			IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
			IFile iFile = (IFile) editorInput.getAdapter(IFile.class);
			isEditor = true;
			ArrayList<IFile> list = new ArrayList<IFile>();
			list.add(iFile);	//代替 Arrays.asList(iFile)
			return execute(event, list);
		} else if ("net.heartsome.cat.common.ui.navigator.view".equals(partId)) { // 当前焦点在导航视图
			ArrayList<IFile> list = new ArrayList<IFile>();
			ArrayList<IFile> wrongFiles = new ArrayList<IFile>();
			String projectName = null;
			@SuppressWarnings("unchecked")
			Iterator<IResource> iterator = structuredSelection.iterator();
			while (iterator.hasNext()) {
				IResource resource = iterator.next();
				if (projectName == null) {
					projectName = resource.getProject().getName();
				} else {
					if (!projectName.equals(resource.getProject().getName())) {
						MessageDialog.openInformation(shell,
								Messages.getString("handlers.AbstractSelectProjectFilesHandler.msgTitle"),
								Messages.getString("handlers.AbstractSelectProjectFilesHandler.msg2"));
						return null;
					}
				}
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String fileExtension = file.getFileExtension();
					if (getLegalFileExtensions() == null || getLegalFileExtensions().length == 0) { // 未限制后缀名的情况
						list.add(file);
					} else { // 限制了后缀名的情况
						if (fileExtension == null) { // 无后缀名的文件
							fileExtension = "";
						}
						if (CommonFunction.containsIgnoreCase(getLegalFileExtensions(), fileExtension)) {
							list.add(file);
						} else {
							wrongFiles.add(file);
						}
					}
				} else if (resource instanceof IContainer) { // IContainer 包含 IFolder、IPorject。
					try {
						ResourceUtils.getFiles((IContainer) resource, list, getLegalFileExtensions());
					} catch (CoreException e) {
						LOGGER.error(MessageFormat.format(Messages
								.getString("handlers.AbstractSelectProjectFilesHandler.msg3"), resource.getFullPath()
								.toOSString()), e);
						e.printStackTrace();
					}
				}
			}

			if (!wrongFiles.isEmpty()) {
				String msg = Messages.getString("handlers.AbstractSelectProjectFilesHandler.msg4");
				StringBuffer arg = new StringBuffer();
				for (IFile iFile : wrongFiles) {
					arg.append("\n").append(iFile.getFullPath().toOSString());
				}
				if (!MessageDialog.openConfirm(shell,
						Messages.getString("handlers.AbstractSelectProjectFilesHandler.msgTitle"), MessageFormat.format(msg.toString(), arg.toString()))) {
					return null;
				}
			}

			return execute(event, list);
		}

		return null;
	}

	/**
	 * 获取合法的文件后缀名
	 * @return ;
	 */
	public abstract String[] getLegalFileExtensions();

	/**
	 * 同 {@link #execute(ExecutionEvent)}
	 * @param event
	 * @param list
	 *            选中的文件列表。
	 * @return ;
	 */
	public abstract Object execute(ExecutionEvent event, List<IFile> list);

}
