package net.heartsome.cat.ts.handler;

import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 首选项的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class PreferenceHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window != null) {
			PreferenceUtil.openPreferenceDialog(window, null);
		}
		
		return null;
	}

}
