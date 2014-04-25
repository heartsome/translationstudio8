package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractMultiRowCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.IConfigRegistry;

import org.eclipse.swt.graphics.GC;

/**
 * @see AutoResizeColumnsCommand
 */

public class AutoResizeRowsCommand extends AbstractMultiRowCommand {
	
	private final IConfigRegistry configRegistry;
	private final GC gc;

	public AutoResizeRowsCommand(InitializeAutoResizeRowsCommand initCommand) {
		super(initCommand.getSourceLayer(), initCommand.getRowPositions());
		this.configRegistry = initCommand.getConfigRegistry();
		this.gc = initCommand.getGC();
	}

	protected AutoResizeRowsCommand(AutoResizeRowsCommand command) {
		super(command);
		this.configRegistry = command.configRegistry;
		this.gc = command.gc;
	}

	public ILayerCommand cloneCommand() {
		return new AutoResizeRowsCommand(this);
	}

	// Accessors

	public GC getGC() {
		return gc;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
}