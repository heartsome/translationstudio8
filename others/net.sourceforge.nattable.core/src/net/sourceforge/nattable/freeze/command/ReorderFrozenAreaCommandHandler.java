package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;

public class ReorderFrozenAreaCommandHandler extends AbstractLayerCommandHandler<ReorderFrozenAreaCommand> {

	public boolean doCommand(ReorderFrozenAreaCommand command) {
		return false;
	}

	public Class<ReorderFrozenAreaCommand> getCommandClass() {
		return ReorderFrozenAreaCommand.class;
	}

}
