package net.heartsome.cat.convert.ui.handler;

import java.util.Arrays;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.wizard.ConversionWizard;
import net.heartsome.cat.convert.ui.wizard.ConversionWizardDialog;
import net.heartsome.cat.convert.ui.wizard.ReverseConversionWizard;
import net.heartsome.cat.converter.Converter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ConversionHandler extends AbstractHandler {

	// private final static Logger logger = LoggerFactory.getLogger(ConversionHandler.class.getName());

	/**
	 * The constructor.
	 */
	public ConversionHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String commandId = event.getCommand().getId();
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection.isEmpty()) {
			return null;
		}
		if (currentSelection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;

			Object object = structuredSelection.getFirstElement();
			// 通过 adapter manager 获得 conversion item
			Object adapter = Platform.getAdapterManager().getAdapter(object, IConversionItem.class);
			if (adapter instanceof IConversionItem) {
				// open the config conversion dialog
				ConverterViewModel converterViewModel = null;
				IConversionItem sourceItem = (IConversionItem) adapter;
				if (commandId.equals("net.heartsome.cat.convert.ui.commands.convertCommand")) {
					converterViewModel = new ConverterViewModel(Activator.getContext(), Converter.DIRECTION_POSITIVE);
					// 记住所选择的文件
					converterViewModel.setConversionItem(sourceItem);

					IWizard wizard = new ConversionWizard(Arrays.asList(converterViewModel), null);
					TSWizardDialog dialog = new ConversionWizardDialog(window.getShell(), wizard);
					int result = dialog.open();
					if (result == IDialogConstants.OK_ID) {
						converterViewModel.convert();
					}
				} else {
					converterViewModel = new ConverterViewModel(Activator.getContext(), Converter.DIRECTION_REVERSE);
					converterViewModel.setConversionItem(sourceItem);

					IWizard wizard = new ReverseConversionWizard(Arrays.asList(converterViewModel), null);
					TSWizardDialog dialog = new ConversionWizardDialog(window.getShell(), wizard);
					int result = dialog.open();
					if (result == IDialogConstants.OK_ID) {
						converterViewModel.convert();
					}
				}
			}
		}
		return null;
	}
}
