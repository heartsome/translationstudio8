package net.heartsome.cat.ts.ui.plugin.handler;

import net.heartsome.cat.ts.ui.plugin.dialog.Martif2TBXConverterDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * MARTIF to TBX Converter 的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class Martif2TBXConverterHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Martif2TBXConverterDialog dialog = new Martif2TBXConverterDialog(HandlerUtil.getActiveShell(event));
		dialog.open();
		return null;
	}

}
