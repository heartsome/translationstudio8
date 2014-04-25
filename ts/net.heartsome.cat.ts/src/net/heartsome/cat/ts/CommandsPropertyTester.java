package net.heartsome.cat.ts;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.services.IServiceLocator;

public class CommandsPropertyTester extends PropertyTester {

	public CommandsPropertyTester() {
		// TODO Auto-generated constructor stub
	}

	public static final String NAMESPACE = "org.eclipse.core.commands"; //$NON-NLS-1$
	public static final String PROPERTY_BASE = NAMESPACE + '.';
	public static final String TOGGLE_PROPERTY_NAME = "toggle"; //$NON-NLS-1$
	public static final String TOGGLE_PROPERTY = PROPERTY_BASE
			+ TOGGLE_PROPERTY_NAME;

	public boolean test(final Object receiver, final String property,
			final Object[] args, final Object expectedValue) {
		if (receiver instanceof IServiceLocator && args.length == 1
				&& args[0] instanceof String) {
			final IServiceLocator locator = (IServiceLocator) receiver;
			if (TOGGLE_PROPERTY_NAME.equals(property)) {
				final String commandId = args[0].toString();
				final ICommandService commandService = (ICommandService) locator
						.getService(ICommandService.class);
				final Command command = commandService.getCommand(commandId);
				final State state = command
						.getState(RegistryToggleState.STATE_ID);
				if (state != null) {
					return state.getValue().equals(expectedValue);
				}
			}
		}
		return false;
	}

}
