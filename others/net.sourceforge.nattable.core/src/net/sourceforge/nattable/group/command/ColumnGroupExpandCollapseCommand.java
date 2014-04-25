package net.sourceforge.nattable.group.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ColumnGroupExpandCollapseCommand extends AbstractColumnCommand {

	public ColumnGroupExpandCollapseCommand(ILayer layer, int columnPosition) {
		super(layer, columnPosition);
	}
	
	protected ColumnGroupExpandCollapseCommand(ColumnGroupExpandCollapseCommand command) {
		super(command);
	}
	
	public ColumnGroupExpandCollapseCommand cloneCommand() {
		return new ColumnGroupExpandCollapseCommand(this);
	}

}
