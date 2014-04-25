package net.sourceforge.nattable.group.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupReorderLayer;
import net.sourceforge.nattable.group.ColumnGroupUtils;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.reorder.command.ColumnReorderCommand;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Handles updating of the Column Group Model when a column belonging to
 * a group is reordered. The actual reordering of the column is delegated to the lower layers.
 */
public class GroupColumnReorderCommandHandler extends AbstractLayerCommandHandler<ColumnReorderCommand> {

	private final ColumnGroupReorderLayer columnGroupReorderLayer;

	private final ColumnGroupModel model;

	public GroupColumnReorderCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
		this.columnGroupReorderLayer = columnGroupReorderLayer;
		this.model = columnGroupReorderLayer.getModel();
	}

	public Class<ColumnReorderCommand> getCommandClass() {
		return ColumnReorderCommand.class;
	}

	@Override
	protected boolean doCommand(ColumnReorderCommand command) {
		int fromColumnPosition = command.getFromColumnPosition();
		int toColumnPosition = command.getToColumnPosition();

		if (fromColumnPosition == -1 || toColumnPosition == -1) {
			System.err.println("Invalid reorder positions, fromPosition: " + fromColumnPosition + ", toPosition: " + toColumnPosition);
		}
		ILayer underlyingLayer = columnGroupReorderLayer.getUnderlyingLayer();
		int fromColumnIndex = underlyingLayer.getColumnIndexByPosition(fromColumnPosition);
		int toColumnIndex = underlyingLayer.getColumnIndexByPosition(toColumnPosition);

		MoveDirectionEnum moveDirection = ColumnGroupUtils.getMoveDirection(fromColumnPosition, toColumnPosition);

		String leftEdgeGroupName = null;
		String rightEdgeGroupName = null;

		if (MoveDirectionEnum.RIGHT == moveDirection) {
			rightEdgeGroupName = movedToLeftEdgeOfAGroup(toColumnPosition, toColumnIndex);
		}
		if (MoveDirectionEnum.LEFT == moveDirection) {
			leftEdgeGroupName = movedToRightEdgeOfAGroup(toColumnPosition, toColumnIndex);
		}

		if(updateModel(fromColumnIndex, toColumnIndex, leftEdgeGroupName, rightEdgeGroupName)){
			return underlyingLayer.doCommand(command);
		}else{
			return false;
		}
	}

	private boolean updateModel(int fromColumnIndex, int toColumnIndex, String leftEdgeGroupName, String rightEdgeGroupName) {

		// If moved to the RIGHT edge of a group - remove from group
		if (rightEdgeGroupName != null) {
			return (model.isPartOfAGroup(fromColumnIndex)) ? model.removeColumnFromGroup(fromColumnIndex) : true;
		}

		// If moved to the LEFT edge of a column group - include in the group
		if (leftEdgeGroupName != null) {
			boolean removed = true;
			if (model.isPartOfAGroup(fromColumnIndex)){
				removed = model.removeColumnFromGroup(fromColumnIndex);
			}
			return removed && model.insertColumnIndexes(leftEdgeGroupName, fromColumnIndex);
		}

		// Move column INTO a group
		if (model.isPartOfAGroup(toColumnIndex) && !model.isPartOfAGroup(fromColumnIndex)) {
			String groupName = model.getColumnGroupNameForIndex(toColumnIndex);
			return model.insertColumnIndexes(groupName, fromColumnIndex);
		}

		// Move column OUT of a group
		if (model.isPartOfAGroup(fromColumnIndex) && !model.isPartOfAGroup(toColumnIndex)) {
			return model.removeColumnFromGroup(fromColumnIndex);
		}

		// Move column BETWEEN groups
		if (model.isPartOfAGroup(toColumnIndex) && model.isPartOfAGroup(fromColumnIndex)) {
			String toGroupName = model.getColumnGroupNameForIndex(toColumnIndex);
			String fromGroupName = model.getColumnGroupNameForIndex(fromColumnIndex);

			if (fromGroupName.equals(toGroupName)) {
				return true;
			} else {
				return model.removeColumnFromGroup(fromColumnIndex) && model.insertColumnIndexes(toGroupName, fromColumnIndex);
			}
		}
		return true;
	}

	private String movedToRightEdgeOfAGroup(int dropColumnPosition, int dropColumnIndex){
		if(ColumnGroupUtils.isRightEdgeOfAColumnGroup(columnGroupReorderLayer, dropColumnPosition, dropColumnIndex, model)){
			return model.getColumnGroupNameForIndex(dropColumnIndex);
		}
		return null;
	}

	private String movedToLeftEdgeOfAGroup(int dropColumnPosition, int dropColumnIndex){
		if(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(columnGroupReorderLayer, dropColumnPosition, dropColumnIndex, model)){
			return model.getColumnGroupNameForIndex(dropColumnIndex);
		}
		return null;
	}

}
