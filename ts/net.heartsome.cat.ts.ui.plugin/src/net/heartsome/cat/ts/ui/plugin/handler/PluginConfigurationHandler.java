package net.heartsome.cat.ts.ui.plugin.handler;

import net.heartsome.cat.ts.ui.plugin.dialog.PluginConfigurationDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class PluginConfigurationHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		PluginConfigurationDialog dialog = new PluginConfigurationDialog(shell);
		dialog.open();
		return null;
	}
}
