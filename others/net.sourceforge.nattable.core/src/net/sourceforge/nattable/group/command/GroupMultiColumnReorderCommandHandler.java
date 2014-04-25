package net.sourceforge.nattable.group.command;

import java.util.List;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupReorderLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommand;

public class GroupMultiColumnReorderCommandHandler extends AbstractLayerCommandHandler<MultiColumnReorderCommand> {

	private final ColumnGroupReorderLayer columnGroupReorderLayer;

	public GroupMultiColumnReorderCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
		this.columnGroupReorderLayer = columnGroupReorderLayer;
	}
	
	public Class<MultiColumnReorderCommand> getCommandClass() {
		return MultiColumnReorderCommand.class;
	}

	@Override
	protected boolean doCommand(MultiColumnReorderCommand command) {
		int toColumnPosition = command.getToColumnPosition();
		
		ILayer underlyingLayer = columnGroupReorderLayer.getUnderlyingLayer();
		int toColumnIndex = underlyingLayer.getColumnIndexByPosition(toColumnPosition);

		List<Integer> fromColumnPositions = command.getFromColumnPositions();
		
		ColumnGroupModel model = columnGroupReorderLayer.getModel();
		
		if (updateModel(underlyingLayer, toColumnIndex, fromColumnPositions, model)) {
			return underlyingLayer.doCommand(command);
		} else {
			return false;
		}
	}

	private boolean updateModel(ILayer underlyingLayer, int toColumnIndex, List<Integer> fromColumnPositions, ColumnGroupModel model) {
		// Moving INTO a group
		if (model.isPartOfAGroup(toColumnIndex)) {
			String toGroupName = model.getColumnGroupNameForIndex(toColumnIndex);
			if (model.isPartOfAnUnbreakableGroup(toColumnIndex)) {
				return false;
			}
			
			for (Integer fromColumnPosition : fromColumnPositions) {
				int fromColumnIndex = underlyingLayer.getColumnIndexByPosition(fromColumnPosition.intValue());

				// If 'from' index not already present in the 'to' group
				if (!toGroupName.equals(model.getColumnGroupNameForIndex(fromColumnIndex))) {
					model.removeColumnFromGroup(fromColumnIndex);
					model.addColumnsIndexesToGroup(toGroupName, fromColumnIndex);
				}
			}
			return true;
		}
		
		// Moving OUT OF a group
		if (!model.isPartOfAGroup(toColumnIndex)) {
			for (Integer fromColumnPosition : fromColumnPositions) {
				// Remove from model - if present
				int fromColumnIndex = underlyingLayer.getColumnIndexByPosition(fromColumnPosition.intValue());
				
				if (model.isPartOfAGroup(fromColumnIndex) && !model.removeColumnFromGroup(fromColumnIndex)) {
					return false;
				}
			}
			return true;
		}
		
		return true;
	}
}
