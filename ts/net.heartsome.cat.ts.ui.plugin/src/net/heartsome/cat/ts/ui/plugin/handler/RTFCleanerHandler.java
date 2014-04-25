package net.heartsome.cat.ts.ui.plugin.handler;

import net.heartsome.cat.ts.ui.plugin.dialog.RTFCleanerDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * RTFCleaner 的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class RTFCleanerHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		RTFCleanerDialog dialog = new RTFCleanerDialog(HandlerUtil.getActiveShell(event));
		dialog.open();
		return null;
	}

}
