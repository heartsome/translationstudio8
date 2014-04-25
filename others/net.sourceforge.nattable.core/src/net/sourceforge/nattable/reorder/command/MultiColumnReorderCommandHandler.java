package net.sourceforge.nattable.reorder.command;

import java.util.List;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.reorder.ColumnReorderLayer;

public class MultiColumnReorderCommandHandler extends AbstractLayerCommandHandler<MultiColumnReorderCommand> {

	private final ColumnReorderLayer columnReorderLayer;

	public MultiColumnReorderCommandHandler(ColumnReorderLayer columnReorderLayer) {
		this.columnReorderLayer = columnReorderLayer;
	}
	
	public Class<MultiColumnReorderCommand> getCommandClass() {
		return MultiColumnReorderCommand.class;
	}

	@Override
	protected boolean doCommand(MultiColumnReorderCommand command) {
		List<Integer> fromColumnPositions = command.getFromColumnPositions();
		int toColumnPosition = command.getToColumnPosition();
		
		columnReorderLayer.reorderMultipleColumnPositions(fromColumnPositions, toColumnPosition);
		
		return true;
	}

}
