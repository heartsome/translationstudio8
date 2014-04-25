package net.sourceforge.nattable.command;

import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractLayerCommandHandler<T extends ILayerCommand> implements ILayerCommandHandler<T> {

	public final boolean doCommand(ILayer targetLayer, T command) {
		if (command.convertToTargetLayer(targetLayer)) {
			return doCommand(command);
		}
		return false;
	}
	
	protected abstract boolean doCommand(T command);
	
}
