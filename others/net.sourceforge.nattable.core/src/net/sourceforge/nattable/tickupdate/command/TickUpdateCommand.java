package net.sourceforge.nattable.tickupdate.command;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;

public class TickUpdateCommand implements ILayerCommand {

	private final IConfigRegistry configRegistry;
	private final boolean increment;

	public TickUpdateCommand(IConfigRegistry configRegistry, boolean increment) {
		this.configRegistry = configRegistry;
		this.increment = increment;
	}

	protected TickUpdateCommand(TickUpdateCommand command) {
		this.configRegistry = command.configRegistry;
		this.increment = command.increment;
	}
	
	public TickUpdateCommand cloneCommand() {
		return new TickUpdateCommand(this);
	}

	public boolean convertToTargetLayer(ILayer targetLayer) {
		// No op.
		return true;
	}
	
	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public boolean isIncrement() {
		return increment;
	}
}
