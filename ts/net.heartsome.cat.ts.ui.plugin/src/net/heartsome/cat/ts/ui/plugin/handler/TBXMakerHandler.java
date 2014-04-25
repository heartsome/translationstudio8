package net.heartsome.cat.ts.ui.plugin.handler;

import net.heartsome.cat.ts.ui.plugin.dialog.TBXMakerDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * TBXMaker 的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class TBXMakerHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		TBXMakerDialog dialog = new TBXMakerDialog(HandlerUtil.getActivePart(event).getSite().getShell());
		dialog.open();
		return null;
	}

}
