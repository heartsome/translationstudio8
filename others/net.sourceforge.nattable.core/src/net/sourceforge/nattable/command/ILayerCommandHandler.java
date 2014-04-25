package net.sourceforge.nattable.command;

import net.sourceforge.nattable.layer.ILayer;

public interface ILayerCommandHandler <T extends ILayerCommand> {
	
	public Class<T> getCommandClass();
	
	/**
	 * @param command
	 * @return true if the command has been handled, false otherwise
	 */
	public boolean doCommand(ILayer targetLayer, T command);
	
}
