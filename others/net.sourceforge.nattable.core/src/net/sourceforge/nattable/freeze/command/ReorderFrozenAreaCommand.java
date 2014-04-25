package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ReorderFrozenAreaCommand implements ILayerCommand {

	public boolean convertToTargetLayer(ILayer targetLayer) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public ReorderFrozenAreaCommand cloneCommand() {
		return this;
	}

}
