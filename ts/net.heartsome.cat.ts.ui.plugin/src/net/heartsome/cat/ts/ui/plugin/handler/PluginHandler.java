package net.heartsome.cat.ts.ui.plugin.handler;

import net.heartsome.cat.ts.ui.plugin.dialog.JavaPropertiesViewerDialog;
import net.heartsome.cat.ts.ui.plugin.dialog.TMX2TXTConverterDialog;
import net.heartsome.cat.ts.ui.plugin.dialog.TMXValidatorDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 四个插件的菜单触发类(Java Properties Viewer, RTFCleaner, TMX to TXT Converter, TMXValidator)
 * @author robert 2012-03-09
 * @version
 * @since JDK1.6
 */
public class PluginHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		//插件的参数
		String pluginID = event.getParameter("net.heartsome.cat.ts.ui.plugin.pluginID");
		
		Shell shell = HandlerUtil.getActiveShell(event);
		
		//Java Properties Viewer插件
		if ("PropertiesViewer".equals(pluginID)) {
			JavaPropertiesViewerDialog dialog = new JavaPropertiesViewerDialog(shell);
			dialog.open();
		}else if ("TMX2TXTConverter".equals(pluginID)) {
			//TMX to TXT converter
			TMX2TXTConverterDialog dialog = new TMX2TXTConverterDialog(shell);
			dialog.open();
		}else if ("TMXValidator".equals(pluginID)) {
			TMXValidatorDialog dialog = new TMXValidatorDialog(shell);
			dialog.open();
		}
		
		return null;
	}

}
