/**
 * OpenViewHandler.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.common.ui.handlers;

import java.util.Map;

import net.heartsome.cat.common.ui.Activator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * 打开视图的 Handler，视图 ID 作为参数在 plugin.xml 中指定。
 * 修改handler类，切换查看菜单的图标，--robert	2012-03-20
 */
public class OpenViewHandler extends AbstractHandler implements IElementUpdater {
	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String viewId = event.getParameter("ViewId");
		if (viewId == null) {
			return null;
		}
		
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = workbenchPage.findView(viewId);
		if (view == null) {
			try {
				workbenchPage.showView(viewId);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		} else {
			workbenchPage.hideView(view);
		}
//		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
//		commandService.refreshElements(event.getCommand().getId(), null);
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void updateElement(final UIElement element, Map parameters) {
		if (parameters.get("ViewId") == null) {
			return;
		}
		
		final String viewId = (String) parameters.get("ViewId");
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null) {
					return;
				}
				IWorkbenchPage workbenchPage = window.getActivePage();
				
				if (workbenchPage == null) {
					return;
				}
				IViewPart view = workbenchPage.findView(viewId);
				
				if (view == null) {
					element.setIcon(Activator.getImageDescriptor("icons/disabled_co.png"));
				} else {
					element.setIcon(Activator.getImageDescriptor("icons/enabled_co.png"));
				}
			}
		});
	}
}
