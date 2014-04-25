package net.sourceforge.nattable.group.action;

import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupUtils;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.reorder.action.ColumnReorderDragMode;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.events.MouseEvent;

/**
 * Extends the regular column drag functionality to work with Column groups.<br/>
 * It does the following checks:<br/>
 * <ol>
 * <li>Checks that the destination is not part of a Unbreakable column group</li>
 * <li>Checks if the destination is between two adjoining column groups</li>
 * </ol>
 */
public class ColumnHeaderReorderDragMode extends ColumnReorderDragMode {

	private final ColumnGroupModel model;
	private MouseEvent event;

	public ColumnHeaderReorderDragMode(ColumnGroupModel model) {
		this.model = model;
	}

	public boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition) {
		int toColumnIndex = natLayer.getColumnIndexByPosition(toGridColumnPosition);
		int fromColumnIndex = natLayer.getColumnIndexByPosition(fromGridColumnPosition);

		// Allow moving within the unbreakable group
		if (model.isPartOfAnUnbreakableGroup(fromColumnIndex)){
			return ColumnGroupUtils.isInTheSameGroup(fromColumnIndex, toColumnIndex, model);
		}

		boolean betweenTwoGroups = false;
		if (event != null) {
			int minX = event.x - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			int maxX = event.x + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			betweenTwoGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, model);
		}

		return (!model.isPartOfAnUnbreakableGroup(toColumnIndex)) || betweenTwoGroups;
	}

	@Override
	public boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition, MouseEvent event) {
		this.event = event;
		toGridColumnPosition = natLayer.getColumnPositionByX(event.x);
		return isValidTargetColumnPosition(natLayer, fromGridColumnPosition, toGridColumnPosition);
	}
}
