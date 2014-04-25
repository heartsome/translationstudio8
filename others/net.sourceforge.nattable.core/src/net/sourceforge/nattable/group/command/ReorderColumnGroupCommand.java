package net.sourceforge.nattable.group.command;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.reorder.command.ColumnReorderCommand;

public class ReorderColumnGroupCommand extends ColumnReorderCommand {

	public ReorderColumnGroupCommand(ILayer layer, int fromColumnPosition, int toColumnPosition) {
		super(layer, fromColumnPosition, toColumnPosition);
	}

	public ReorderColumnGroupCommand(ReorderColumnGroupCommand command) {
		super(command);
	}

	@Override
	public ReorderColumnGroupCommand cloneCommand() {
		return new ReorderColumnGroupCommand(this);
	}

}
