package net.heartsome.cat.ts.handler;

import java.util.Map;

import net.heartsome.cat.ts.Activator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.menus.UIElement;

@SuppressWarnings("restriction")
public class OpenToolBarHandler extends AbstractHandler implements IElementUpdater{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (activeWorkbenchWindow instanceof WorkbenchWindow) {
			WorkbenchWindow window = (WorkbenchWindow) activeWorkbenchWindow;
			window.toggleToolbarVisibility();
		}
		
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(event.getCommand().getId(), null);
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void updateElement(final UIElement element, Map parameters) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow instanceof WorkbenchWindow) {
					WorkbenchWindow window = (WorkbenchWindow) activeWorkbenchWindow;
					boolean coolbarVisible = window.getCoolBarVisible();
					element.setIcon(coolbarVisible ? Activator.getImageDescriptor("icons/enabled_co.png") : Activator
							.getImageDescriptor("icons/disabled_co.png"));
				}
			}
		});
		
	}
}
