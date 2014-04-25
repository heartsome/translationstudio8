package net.heartsome.cat.convert.ui.command;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConverterCommandTrigger {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterCommandTrigger.class);

	/**
	 * 打开正转换对话框
	 * @param window
	 * @param file
	 *            ;
	 */
	public static void openConversionDialog(IWorkbenchWindow window, IFile file) {
		openDialog(window, file, "net.heartsome.cat.convert.ui.commands.openConvertDialogCommand");
	}

	/**
	 * 打开逆转换对话框
	 * @param window
	 * @param file
	 *            ;
	 */
	public static void openReverseConversionDialog(IWorkbenchWindow window, IFile file) {
		openDialog(window, file, "net.heartsome.cat.convert.ui.commands.openReverseConvertDialogCommand");
	}

	private static void openDialog(IWorkbenchWindow window, IFile file, String commandId) {
		IWorkbench workbench = window.getWorkbench();
		ICommandService commandService = (ICommandService) workbench.getService(ICommandService.class);
		Command command = commandService.getCommand(commandId);
		try {
			EvaluationContext context = new EvaluationContext(null, IEvaluationContext.UNDEFINED_VARIABLE);
			context.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, window);
			if (file != null) {				
				StructuredSelection selection = new StructuredSelection(file);
				context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
			}
			command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, context));
		} catch (ExecutionException e) {
			LOGGER.error("", e);
		} catch (NotDefinedException e) {
			LOGGER.error("", e);
		} catch (NotEnabledException e) {
			LOGGER.error("", e);
		} catch (NotHandledException e) {
			LOGGER.error("", e);
		}
	}
}
