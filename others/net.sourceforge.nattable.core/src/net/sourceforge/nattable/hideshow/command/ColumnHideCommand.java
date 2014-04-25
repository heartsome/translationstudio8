package net.sourceforge.nattable.hideshow.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ColumnHideCommand extends AbstractColumnCommand {

	public ColumnHideCommand(ILayer layer, int columnPosition) {
		super(layer, columnPosition);
	}
	
	protected ColumnHideCommand(ColumnHideCommand command) {
		super(command);
	}
	
	public ColumnHideCommand cloneCommand() {
		return new ColumnHideCommand(this);
	}

}
