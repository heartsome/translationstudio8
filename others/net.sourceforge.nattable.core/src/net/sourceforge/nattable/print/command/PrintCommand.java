package net.sourceforge.nattable.print.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;
import net.sourceforge.nattable.config.IConfigRegistry;

import org.eclipse.swt.widgets.Shell;

public class PrintCommand extends AbstractContextFreeCommand {

	private final IConfigRegistry configRegistry;
	private Shell shell;

	public PrintCommand(IConfigRegistry configRegistry, Shell shell) {
		this.configRegistry = configRegistry;
		this.shell = shell;
	}
	
	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}

	public Shell getShell() {
		return shell;
	}
}
