package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ShowColumnInViewportCommand extends AbstractColumnCommand {

	public ShowColumnInViewportCommand(ILayer layer, int columnPosition) {
		super(layer, columnPosition);
	}
	
	protected ShowColumnInViewportCommand(ShowColumnInViewportCommand command) {
		super(command);
	}
	
	public ShowColumnInViewportCommand cloneCommand() {
		return new ShowColumnInViewportCommand(this);
	}
	
}
