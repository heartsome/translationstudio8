package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractPositionCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ShowCellInViewportCommand extends AbstractPositionCommand {
	
	public ShowCellInViewportCommand(ILayer layer, int columnPosition, int rowPosition) {
		super(layer, columnPosition, rowPosition);
	}
	
	protected ShowCellInViewportCommand(ShowCellInViewportCommand command) {
		super(command);
	}
	
	public ShowCellInViewportCommand cloneCommand() {
		return new ShowCellInViewportCommand(this);
	}

}
