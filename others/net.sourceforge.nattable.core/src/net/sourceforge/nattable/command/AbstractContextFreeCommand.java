package net.sourceforge.nattable.command;

import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractContextFreeCommand implements ILayerCommand {
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		return true;
	}
	
	public AbstractContextFreeCommand cloneCommand() {
		return this;
	}
	
}
