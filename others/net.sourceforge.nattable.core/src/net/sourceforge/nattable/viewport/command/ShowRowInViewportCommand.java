package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractRowCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ShowRowInViewportCommand extends AbstractRowCommand {

	public ShowRowInViewportCommand(ILayer layer, int rowPosition) {
		super(layer, rowPosition);
	}
	
	protected ShowRowInViewportCommand(ShowRowInViewportCommand command) {
		super(command);
	}
	
	public ShowRowInViewportCommand cloneCommand() {
		return new ShowRowInViewportCommand(this);
	}
	
}
