package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.ui.action.IDragMode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * Fires commands to select a range of cells when the mouse is dragged in the viewport.
 */
public class CellSelectionDragMode implements IDragMode {

	private boolean shiftMask;
	private boolean controlMask;

	private Point lastDragInCellPosition = null;

	public void mouseDown(NatTable natTable, MouseEvent event) {
		natTable.forceFocus();

		shiftMask = ((event.stateMask & SWT.SHIFT) == SWT.SHIFT);
		controlMask = ((event.stateMask & SWT.CONTROL) == SWT.CONTROL);

		fireSelectionCommand(natTable, natTable.getColumnPositionByX(event.x), natTable.getRowPositionByY(event.y), shiftMask, controlMask);
	}

	public void mouseMove(NatTable natTable, MouseEvent event) {
		if (event.x > natTable.getWidth()) {
			return;
		}
		int selectedColumnPosition = natTable.getColumnPositionByX(event.x);
		int selectedRowPosition = natTable.getRowPositionByY(event.y);

		if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
			Point dragInCellPosition = new Point(selectedColumnPosition, selectedRowPosition);
			if(lastDragInCellPosition == null || !dragInCellPosition.equals(lastDragInCellPosition)){
				lastDragInCellPosition = dragInCellPosition;

				fireSelectionCommand(natTable, selectedColumnPosition, selectedRowPosition, true, false);
			}
		}
	}

	public void fireSelectionCommand(NatTable natTable, int columnPosition,	int rowPosition, boolean shiftMask, boolean controlMask) {
		natTable.doCommand(new SelectCellCommand(natTable, columnPosition, rowPosition, shiftMask, controlMask));
	}

	public void mouseUp(NatTable natTable, MouseEvent event) {
		endDrag();
	}

	private void endDrag(){
		lastDragInCellPosition = null;
	}
}