package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.layer.ILayer;

public class FreezeColumnCommand extends AbstractColumnCommand implements IFreezeCommand {
	
	public FreezeColumnCommand(ILayer layer, int columnPosition) {
		super(layer, columnPosition);
	}
	
	protected FreezeColumnCommand(FreezeColumnCommand command) {
		super(command);
	}
	
	public ILayerCommand cloneCommand() {
		return new FreezeColumnCommand(this);
	}
	
}