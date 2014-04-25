package net.sourceforge.nattable.edit.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.edit.command.EditCellCommand;
import net.sourceforge.nattable.selection.action.CellSelectionDragMode;

import org.eclipse.swt.events.MouseEvent;

public class CellEditDragMode extends CellSelectionDragMode {

	private int originalColumnPosition;
	
	private int originalRowPosition;

	public void mouseDown(NatTable natTable, MouseEvent event) {
		super.mouseDown(natTable, event);
		
		originalColumnPosition = natTable.getColumnPositionByX(event.x);
		originalRowPosition = natTable.getRowPositionByY(event.y);
	}

	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		super.mouseMove(natTable, event);
		
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);
		
		if (columnPosition != originalColumnPosition || rowPosition != originalRowPosition) {
			// Left original cell, cancel edit
			originalColumnPosition = -1;
			originalRowPosition = -1;
		}
	}
	
	public void mouseUp(NatTable natTable, MouseEvent event) {
		super.mouseUp(natTable, event);
		
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);
		
		if (columnPosition == originalColumnPosition && rowPosition == originalRowPosition) {
			natTable.doCommand(
					new EditCellCommand(
							natTable,
							natTable.getConfigRegistry(),
							natTable.getCellByPosition(columnPosition, rowPosition)));
		}
	}

}
