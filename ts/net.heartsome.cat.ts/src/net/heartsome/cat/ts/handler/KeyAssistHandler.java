package net.heartsome.cat.ts.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.ts.ui.Constants;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;

/**
 * 帮助 -> 快捷键功能的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
@SuppressWarnings("restriction")
public class KeyAssistHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		IBindingService bindingService = (IBindingService) workbench.getService(IBindingService.class);
		BindingService service = (BindingService) bindingService;
		ArrayList<Binding> lstBinding = new ArrayList<Binding>(Arrays.asList(bindingService.getBindings()));
		List<String> lstRemove = Constants.lstRemove;
		Iterator<Binding> it = lstBinding.iterator();
		while (it.hasNext()) {
			Binding binding = it.next();
			ParameterizedCommand pCommand = binding.getParameterizedCommand();
			if (pCommand == null || lstRemove.contains(pCommand.getCommand().getId())) {
				it.remove();
			}
		}
		service.getKeyboard().openKeyAssistShell(lstBinding);
		return null;
	}

}
