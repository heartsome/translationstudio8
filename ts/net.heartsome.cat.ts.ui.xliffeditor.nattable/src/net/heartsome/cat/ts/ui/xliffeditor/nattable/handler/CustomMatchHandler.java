package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog.CustomMatchConditionDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

public class CustomMatchHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CustomMatchConditionDialog dialog = new CustomMatchConditionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.open();
		return null;
	}
}
