package net.sourceforge.nattable.reorder.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.reorder.ColumnReorderLayer;

public class ColumnReorderCommandHandler extends AbstractLayerCommandHandler<ColumnReorderCommand> {

	private final ColumnReorderLayer columnReorderLayer;

	public ColumnReorderCommandHandler(ColumnReorderLayer columnReorderLayer) {
		this.columnReorderLayer = columnReorderLayer;
	}
	
	public Class<ColumnReorderCommand> getCommandClass() {
		return ColumnReorderCommand.class;
	}

	@Override
	protected boolean doCommand(ColumnReorderCommand command) {
		int fromColumnPosition = command.getFromColumnPosition();
		int toColumnPosition = command.getToColumnPosition();
		
		columnReorderLayer.reorderColumnPosition(fromColumnPosition, toColumnPosition);
		
		return true;
	}

}
