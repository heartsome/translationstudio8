package net.heartsome.cat.ts.ui.plugin.handler;

import net.heartsome.cat.ts.ui.plugin.dialog.CSV2TMXConverterDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * CSV to TMX Converter 的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class CSV2TMXConverterHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		CSV2TMXConverterDialog dialog = new CSV2TMXConverterDialog(HandlerUtil.getActiveShell(event));
		dialog.open();
		return null;
	}

}
