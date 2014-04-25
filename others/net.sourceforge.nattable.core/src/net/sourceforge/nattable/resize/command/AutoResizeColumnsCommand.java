package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractMultiColumnCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.IConfigRegistry;

import org.eclipse.swt.graphics.GC;

/**
 * Command indicating that all selected columns have to be auto resized i.e made
 * wide enough to just fit the widest cell. This should also take the column
 * header into account
 * 
 * Note: The {@link InitializeAutoResizeColumnsCommand} has to be fired first
 * when autoresizing columns.
 */

public class AutoResizeColumnsCommand extends AbstractMultiColumnCommand {

	private final IConfigRegistry configRegistry;
	private final GC gc;

	public AutoResizeColumnsCommand(InitializeAutoResizeColumnsCommand initCommand) {
		super(initCommand.getSourceLayer(), initCommand.getColumnPositions());
		this.configRegistry = initCommand.getConfigRegistry();
		this.gc = initCommand.getGC();
	}

	protected AutoResizeColumnsCommand(AutoResizeColumnsCommand command) {
		super(command);
		this.configRegistry = command.configRegistry;
		this.gc = command.gc;
	}

	public ILayerCommand cloneCommand() {
		return new AutoResizeColumnsCommand(this);
	}

	// Accessors

	public GC getGC() {
		return gc;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
}