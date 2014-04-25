package net.sourceforge.nattable.group.command;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupReorderLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommand;

public class ReorderColumnsAndGroupsCommandHandler extends AbstractLayerCommandHandler<ReorderColumnsAndGroupsCommand> {

	private final ColumnGroupReorderLayer columnGroupReorderLayer;

	public ReorderColumnsAndGroupsCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
		this.columnGroupReorderLayer = columnGroupReorderLayer;
	}
	
	public Class<ReorderColumnsAndGroupsCommand> getCommandClass() {
		return ReorderColumnsAndGroupsCommand.class;
	}

	/**
	 * Check if any column belongs to a group. If yes, add all columns in that group.
	 * Assumes that the 'toLocation' is not inside another group
	 */
	@Override
	protected boolean doCommand(ReorderColumnsAndGroupsCommand command) {
		final ILayer underlyingLayer = columnGroupReorderLayer.getUnderlyingLayer();
		List<String> groupsProcessed = new ArrayList<String>();
		
		List<Integer> fromColumnPositions = command.getFromColumnPositions();
		List<Integer> fromColumnPositionsWithGroupColumns = new ArrayList<Integer>();
		
		for (Integer fromColumnPosition : fromColumnPositions) {
			int fromColumnIndex = underlyingLayer.getColumnIndexByPosition(fromColumnPosition.intValue());
			
			ColumnGroupModel model = columnGroupReorderLayer.getModel();
			if (model.isPartOfAGroup(fromColumnIndex)) {
				String groupName = model.getColumnGroupNameForIndex(fromColumnIndex);
				if (!groupsProcessed.contains(groupName)) {
					groupsProcessed.add(groupName);
					fromColumnPositionsWithGroupColumns.addAll(columnGroupReorderLayer.getColumnGroupPositions(fromColumnIndex));
				}
			} else {
				fromColumnPositionsWithGroupColumns.add(fromColumnPosition);
			}
		}
		
		return underlyingLayer.doCommand(new MultiColumnReorderCommand(columnGroupReorderLayer, fromColumnPositionsWithGroupColumns, command.getToColumnPosition()));
	}

}
