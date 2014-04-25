package net.sourceforge.nattable.export.excel.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;
import net.sourceforge.nattable.config.IConfigRegistry;

import org.eclipse.swt.widgets.Shell;

public class ExportToExcelCommand extends AbstractContextFreeCommand {

	private IConfigRegistry configRegistry;
	private final Shell shell;

	public ExportToExcelCommand(IConfigRegistry configRegistry, Shell shell) {
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
