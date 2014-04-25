package net.heartsome.cat.database.ui.tm.handler;

import net.heartsome.cat.database.ui.tm.wizard.ImportTmxWizardDialog;
import net.heartsome.cat.database.ui.tm.wizard.NewTmDbWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 新建记忆库的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NewTMHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		NewTmDbWizard wizard = new NewTmDbWizard();
		ImportTmxWizardDialog dialog = new ImportTmxWizardDialog(window.getShell(), wizard);
		dialog.open();
		return null;
	}

}
