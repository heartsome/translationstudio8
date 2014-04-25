package net.sourceforge.nattable.selection.command;

import net.sourceforge.nattable.command.AbstractMultiRowCommand;
import net.sourceforge.nattable.command.LayerCommandUtil;
import net.sourceforge.nattable.coordinate.ColumnPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.util.ArrayUtil;

public class SelectRowsCommand extends AbstractMultiRowCommand {

	private ColumnPositionCoordinate columnPositionCoordinate;
	private final boolean withShiftMask;
	private final boolean withControlMask;

	public SelectRowsCommand(ILayer layer, int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		this(layer, columnPosition, ArrayUtil.asIntArray(rowPosition), withShiftMask, withControlMask);
	}

	public SelectRowsCommand(ILayer layer, int columnPosition, int[] rowPositions, boolean withShiftMask, boolean withControlMask) {
		super(layer, rowPositions);
		this.columnPositionCoordinate = new ColumnPositionCoordinate(layer, columnPosition);
		this.withControlMask = withControlMask;
		this.withShiftMask = withShiftMask;
	}

	protected SelectRowsCommand(SelectRowsCommand command) {
		super(command);
		this.columnPositionCoordinate = command.columnPositionCoordinate;
		this.withShiftMask = command.withShiftMask;
		this.withControlMask = command.withControlMask;
	}

	@Override
	public boolean convertToTargetLayer(ILayer targetLayer) {
		super.convertToTargetLayer(targetLayer);
		this.columnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer);
		return columnPositionCoordinate != null && columnPositionCoordinate.getColumnPosition() >= 0;
	}

	public int getColumnPosition() {
		return columnPositionCoordinate.getColumnPosition();
	}

	public boolean isWithShiftMask() {
		return withShiftMask;
	}

	public boolean isWithControlMask() {
		return withControlMask;
	}

	public SelectRowsCommand cloneCommand() {
		return new SelectRowsCommand(this);
	}
}
