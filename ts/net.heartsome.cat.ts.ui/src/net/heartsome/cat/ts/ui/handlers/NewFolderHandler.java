package net.heartsome.cat.ts.ui.handlers;

import java.util.List;

import net.heartsome.cat.ts.ui.dialog.NewFolderDialogOfHs;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 新建文件夹
 * @author peason
 * @version
 * @since JDK1.6
 */
public class NewFolderHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		String partId = HandlerUtil.getActivePartId(event);
		if (partId.equals("net.heartsome.cat.common.ui.navigator.view")) {
			// 导航视图处于激活状态
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart viewPart = page.findView("net.heartsome.cat.common.ui.navigator.view");
			StructuredSelection selection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
					.getSelection();
			if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
				List<?> lstObj = ((IStructuredSelection) selection).toList();
				if (lstObj == null || lstObj.size() != 1) {
					MessageDialog.openInformation(shell, Messages.getString("handlers.NewFolderHandler.msgTitle"),
							Messages.getString("handlers.NewFolderHandler.msg1"));
					return null;
				}
				IContainer container = null;
				for (Object obj : lstObj) {
					if (obj instanceof IFile) {
						IFile file = (IFile) obj;
						container = file.getParent();
					} else if (obj instanceof IFolder) {
						container = (IFolder) obj;
					} else if (obj instanceof IProject) {
						container = (IProject) obj;
					}
				}
				ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_DISABLE_LINKING, true);
				NewFolderDialogOfHs dialog = new NewFolderDialogOfHs(shell, container);
				dialog.open();
			} else {
				MessageDialog.openInformation(shell, Messages.getString("handlers.NewFolderHandler.msgTitle"),
						Messages.getString("handlers.NewFolderHandler.msg2"));
				return null;
			}
		} else {
			MessageDialog.openInformation(shell, Messages.getString("handlers.NewFolderHandler.msgTitle"),
					Messages.getString("handlers.NewFolderHandler.msg2"));
			return null;
		}
		return null;
	}

}
