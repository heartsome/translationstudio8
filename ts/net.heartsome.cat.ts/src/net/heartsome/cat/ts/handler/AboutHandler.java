package net.heartsome.cat.ts.handler;

import net.heartsome.cat.ts.dialog.AboutDialog;
import net.heartsome.cat.ts.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 关于...
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class AboutHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		String version = System.getProperty("TSEdition");
		String version2 = System.getProperty("TSVersionDate");
		if (version == null || version2 == null || version.equals("") || version2.equals("")) {
			MessageDialog.openInformation(shell, Messages.getString("dialog.AboutDialog.msgTitle"),
					Messages.getString("dialog.AboutDialog.msg"));
			PlatformUI.getWorkbench().close();
		} else {
			AboutDialog dialog = new AboutDialog(shell);
			dialog.open();
		}
		return null;
	}

}
