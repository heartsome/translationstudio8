package net.heartsome.cat.ts.ui.handlers;

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工具栏新建菜单的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NewHandler extends AbstractHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NewHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String parameter = event.getParameter("wizardParameter");
		Command command = null;
		ICommandService commandService = (ICommandService) HandlerUtil.getActiveSite(event).getService(
				ICommandService.class);
		if (parameter == null || parameter.equalsIgnoreCase("project")) {
			command = commandService.getCommand("net.heartsome.cat.ts.ui.command.newProject");
		} else if (parameter.equalsIgnoreCase("folder")) {
			command = commandService.getCommand("net.heartsome.cat.ts.ui.command.newFolder");
		} else if (parameter.equalsIgnoreCase("tm")) {
			command = commandService.getCommand("net.heartsome.cat.database.ui.tm.command.newTM");
		} else if (parameter.equalsIgnoreCase("tb")) {
			command = commandService.getCommand("net.heartsome.cat.database.ui.tb.command.newTB");
		}
		if (command == null) {
			return null;
		}
		IEvaluationService evalService = (IEvaluationService) HandlerUtil.getActiveSite(event).getService(
				IEvaluationService.class);
		IEvaluationContext currentState = evalService.getCurrentState();
		ExecutionEvent executionEvent = new ExecutionEvent(command, Collections.EMPTY_MAP, this, currentState);
		try {
			command.executeWithChecks(executionEvent);
		} catch (NotDefinedException e) {
			LOGGER.error("", e);
		} catch (NotEnabledException e) {
			LOGGER.error("", e);
		} catch (NotHandledException e) {
			LOGGER.error("", e);
		}

		return null;
	}

}
