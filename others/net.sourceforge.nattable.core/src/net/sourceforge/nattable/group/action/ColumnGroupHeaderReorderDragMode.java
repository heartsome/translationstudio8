package net.sourceforge.nattable.group.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupUtils;
import net.sourceforge.nattable.group.command.ReorderColumnGroupCommand;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.reorder.action.ColumnReorderDragMode;
import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.events.MouseEvent;

/**
 * Default {@link IDragMode} invoked for 'left click + drag' on the column group header.<br/>
 *
 * It overrides the isValidTargetColumnPosition() to calculate if a destination position is valid
 * for the column group to be reordered to.<br/>
 *
 * Example, a column group cannot only be reordered to be inside another column group.
 * @see ColumnGroupHeaderReorderDragModeTest
 */
public class ColumnGroupHeaderReorderDragMode extends ColumnReorderDragMode {

	private final ColumnGroupModel model;
	private MouseEvent event;

	public ColumnGroupHeaderReorderDragMode(ColumnGroupModel model) {
		this.model = model;
	}

	@Override
	protected boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition, MouseEvent event) {
		this.event = event;
		toGridColumnPosition = natLayer.getColumnPositionByX(event.x);
		return isValidTargetColumnPosition(natLayer, fromGridColumnPosition, toGridColumnPosition);
	}

	/**
	 * Work off the event coordinates since the drag {@link ColumnReorderDragMode} adjusts the
	 * 'to' column positions (for on screen semantics)
	 */
	protected boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition) {
		int toColumnIndex = natLayer.getColumnIndexByPosition(toGridColumnPosition);

		boolean betweenGroups = false;
		if(event != null){
			int minX = event.x -  GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			int maxX = event.x +  GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			betweenGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, model);
		}

		return (!model.isPartOfAGroup(toColumnIndex)) || betweenGroups;
	}

	/**
	 * Fire a {@link ReorderColumnGroupCommand} for the column group
	 */
	@Override
	protected void fireMoveCommand(NatTable natTable) {
		natTable.doCommand(new ReorderColumnGroupCommand(natTable, dragFromGridColumnPosition, dragToGridColumnPosition));
	}
}
