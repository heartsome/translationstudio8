package net.heartsome.cat.ts.handler;

import java.util.Map;

import net.heartsome.cat.ts.Activator;
import net.heartsome.cat.ts.ApplicationWorkbenchAdvisor;
import net.heartsome.cat.ts.ApplicationWorkbenchWindowAdvisor;
import net.heartsome.cat.ts.TsPreferencesConstant;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * 主界面状态栏的显示与隐藏
 * @author robert	2011-11-04
 */
public class OpenStatusBarHandler extends AbstractHandler implements IElementUpdater {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		boolean oldValue = preferenceStore.getBoolean(TsPreferencesConstant.TS_statusBar_status);
		
		ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.WorkbenchWindowAdvisor;
        configurer.setStatusVisible(!oldValue);
		
		preferenceStore.setValue(TsPreferencesConstant.TS_statusBar_status, !oldValue);
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(event.getCommand().getId(), null);
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void updateElement(UIElement element, Map parameters) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		boolean oldValue = preferenceStore.getBoolean(TsPreferencesConstant.TS_statusBar_status);
		element.setIcon(oldValue ? Activator.getImageDescriptor("icons/enabled_co.png") : Activator
				.getImageDescriptor("icons/disabled_co.png"));
	}
}
