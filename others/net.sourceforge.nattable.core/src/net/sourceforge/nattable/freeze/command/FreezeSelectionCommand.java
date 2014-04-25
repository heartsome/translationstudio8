package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.layer.ILayer;

/**
 * Will inform the handler to use the selection layer for its freeze coordinates.
 *
 */
public class FreezeSelectionCommand implements IFreezeCommand {

	public FreezeSelectionCommand() {
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		return true;
	}
	
	public FreezeSelectionCommand cloneCommand() {
		return this;
	}
	
}
