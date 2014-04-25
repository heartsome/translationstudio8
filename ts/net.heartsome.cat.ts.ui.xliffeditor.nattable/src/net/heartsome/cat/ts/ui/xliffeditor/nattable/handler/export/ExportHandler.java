package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.export;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.ui.wizard.TSWizardDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportHandler extends AbstractHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportHandler.class);

	private static final String PARAMETER_ID = "ExportWizardClassName";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = getSelectionToUse(event);
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IExportWizard wizard = getExportWizard(event);
		wizard.init(window.getWorkbench(), currentSelection);

		TSWizardDialog dialog = new TSWizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.open();
		return null;
	}

	/**
	 * 得到选中项
	 * @param event
	 * @return ;
	 */
	protected IStructuredSelection getSelectionToUse(ExecutionEvent event) {
		String partId = HandlerUtil.getActivePartId(event);
		if (Constant.NAVIGATOR_VIEW_ID.equals(partId)) {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * 得到导出向导
	 * @param event
	 * @return ;
	 */
	protected IExportWizard getExportWizard(ExecutionEvent event) {
		String wizardClassName = event.getParameter(PARAMETER_ID);
		try {
			Object obj = Class.forName(wizardClassName).newInstance();
			if (IExportWizard.class.isInstance(obj)) {
				return (IExportWizard) obj;
			}
		} catch (ClassNotFoundException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
	}
}
