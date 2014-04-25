/**
 * ChangeXliffEditorModelHandler.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.ts.ui.handlers;

import net.heartsome.cat.ts.ui.editors.IHSEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ChangeXliffEditorModelHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public ChangeXliffEditorModelHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = workbenchPage.getActiveEditor();
		if (editor == null) {
			return null;
		}
		if (editor instanceof IHSEditor) {
			IHSEditor xliffEditor = (IHSEditor) editor;
			xliffEditor.changeModel();
		}
		return null;
	}

}
