package net.heartsome.cat.database.ui.tb.handler;

import net.heartsome.cat.database.ui.tb.wizard.NewTermDbWizard;
import net.heartsome.cat.database.ui.tb.wizard.TermDbManagerImportWizardDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 新建术语库的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NewTBHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		NewTermDbWizard wizard = new NewTermDbWizard();
		TermDbManagerImportWizardDialog dialog = new TermDbManagerImportWizardDialog(window.getShell(), wizard);
		dialog.open();
		return null;
	}

}
