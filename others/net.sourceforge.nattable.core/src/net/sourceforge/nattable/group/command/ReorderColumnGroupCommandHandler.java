package net.sourceforge.nattable.group.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.group.ColumnGroupReorderLayer;

public class ReorderColumnGroupCommandHandler extends AbstractLayerCommandHandler<ReorderColumnGroupCommand> {

	private final ColumnGroupReorderLayer columnGroupReorderLayer;

	public ReorderColumnGroupCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
		this.columnGroupReorderLayer = columnGroupReorderLayer;
	}
	
	public Class<ReorderColumnGroupCommand> getCommandClass() {
		return ReorderColumnGroupCommand.class;
	}

	@Override
	protected boolean doCommand(ReorderColumnGroupCommand command) {
		int fromColumnPosition = command.getFromColumnPosition();
		int toColumnPosition = command.getToColumnPosition();
		
		return columnGroupReorderLayer.reorderColumnGroup(fromColumnPosition, toColumnPosition);
	}

}
