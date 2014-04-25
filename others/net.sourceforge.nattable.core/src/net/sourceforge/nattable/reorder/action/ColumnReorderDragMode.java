package net.sourceforge.nattable.reorder.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.IOverlayPainter;
import net.sourceforge.nattable.reorder.command.ColumnReorderCommand;
import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.ui.util.CellEdgeDetectUtil;
import net.sourceforge.nattable.ui.util.CellEdgeEnum;
import net.sourceforge.nattable.util.GUIHelper;
import net.sourceforge.nattable.viewport.command.ViewportSelectColumnCommand;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Default {@link IDragMode} invoked for 'left click + drag' on the column header.<br/>
 * It does the following when invoked: <br/>
 * <ol>
 *    <li>Fires a column reorder command, to move columns</li>
 *    <li>Overlays a black line indicating the new column position</li>
 * </ol>
 */
public class ColumnReorderDragMode implements IDragMode {

	protected int dragFromGridColumnPosition = -1;
	protected int dragToGridColumnPosition = -1;
	protected int dragToColumnHandleX = -1;
	protected ColumnReorderOverlayPainter overlayPainter = new ColumnReorderOverlayPainter();
	protected LabelStack regionLabels;
	protected boolean isValidCoordinate = false;
	private CellEdgeEnum moveDirection = null;

	public void mouseDown(NatTable natTable, MouseEvent event) {
		natTable.forceFocus();
		regionLabels = natTable.getRegionLabelsByXY(event.x, event.y);
		dragFromGridColumnPosition = natTable.getColumnPositionByX(event.x);
		dragToGridColumnPosition = -1;
		dragToColumnHandleX = -1;
		selectDragFocusColumn(natTable, event, dragFromGridColumnPosition);
        natTable.addOverlayPainter(overlayPainter);
	}

	public void mouseMove(NatTable natTable, MouseEvent event) {
		if (event.x > natTable.getWidth()) {
			return;
		}
		Point dragPt = new Point(event.x, event.y);

	    int gridColumnPosition = natTable.getColumnPositionByX(event.x);

		if (gridColumnPosition >= 0) {
			int gridRowPosition = natTable.getRowPositionByY(event.y);
			LayerCell cell = natTable.getCellByPosition(gridColumnPosition, gridRowPosition);
			if (cell == null) {
				return;
			}
			Rectangle selectedColumnHeaderRect = cell.getBounds();

			int tmpDragToGridColumnPosition = 0;
			moveDirection = CellEdgeDetectUtil.getHorizontalCellEdge(selectedColumnHeaderRect, dragPt);
			switch (moveDirection) {
			case LEFT:
				tmpDragToGridColumnPosition = gridColumnPosition;
				if ((isValidCoordinate = isValidTargetColumnPosition(natTable, dragFromGridColumnPosition,tmpDragToGridColumnPosition, event))) {
					dragToGridColumnPosition = tmpDragToGridColumnPosition;
					dragToColumnHandleX = selectedColumnHeaderRect.x;
				}else {
					dragToColumnHandleX = 0;
				}
				break;
			case RIGHT:
				tmpDragToGridColumnPosition = gridColumnPosition + 1;
				if ((isValidCoordinate = isValidTargetColumnPosition(natTable, dragFromGridColumnPosition, tmpDragToGridColumnPosition, event))) {
					dragToGridColumnPosition = tmpDragToGridColumnPosition;
					dragToColumnHandleX = selectedColumnHeaderRect.x + selectedColumnHeaderRect.width;
				} else {
					dragToColumnHandleX = 0;
				}
				break;
			}
			natTable.redraw(0, 0, natTable.getWidth(), natTable.getHeight(), false);
		}
	}

	protected boolean isValidTargetColumnPosition(ILayer natLayer, int dragFromColumnPosition, int dragToGridColumnPosition, MouseEvent event) {
		return true;
	}

	public void mouseUp(NatTable natTable, MouseEvent event) {
		natTable.removeOverlayPainter(overlayPainter);
		if (dragFromGridColumnPosition >= 0 && dragToGridColumnPosition >= 0 && isValidCoordinate) {

			fireMoveCommand(natTable);

			if(CellEdgeEnum.RIGHT == moveDirection) {
				selectDragFocusColumn(natTable, event, dragToGridColumnPosition - 1);
			} else {
				selectDragFocusColumn(natTable, event, dragToGridColumnPosition);
			}
		}
	}

	protected void fireMoveCommand(NatTable natTable) {
		natTable.doCommand(new ColumnReorderCommand(natTable, dragFromGridColumnPosition, dragToGridColumnPosition));
	}

	protected void selectDragFocusColumn(ILayer natLayer, MouseEvent event, int focusedColumnPosition) {
		boolean shiftMask = (SWT.SHIFT & event.stateMask) != 0;
		boolean controlMask = (SWT.CONTROL & event.stateMask) != 0;

		natLayer.doCommand(new ViewportSelectColumnCommand(natLayer, focusedColumnPosition, shiftMask, controlMask));
	}

	private class ColumnReorderOverlayPainter implements IOverlayPainter {

		public void paintOverlay(GC gc, ILayer layer) {
			if (dragFromGridColumnPosition >= 0) {
				Color orgBgColor = gc.getBackground();
				gc.setBackground(GUIHelper.COLOR_DARK_GRAY);

				gc.fillRectangle(dragToColumnHandleX - 1, 0, 2, layer.getHeight());

				gc.setBackground(orgBgColor);
			}

		}

	}

}
