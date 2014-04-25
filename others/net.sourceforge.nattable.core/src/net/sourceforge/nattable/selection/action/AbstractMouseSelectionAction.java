package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

public class AbstractMouseSelectionAction implements IMouseAction {

    private boolean withShiftMask;
    private boolean withControlMask;
    private int gridColumnPosition;
    private int gridRowPosition;

	public void run(NatTable natTable, MouseEvent event) {
    	withShiftMask = (event.stateMask & SWT.SHIFT) != 0;
    	withControlMask = (event.stateMask & SWT.CTRL) != 0;

    	gridColumnPosition = natTable.getColumnPositionByX(event.x);
    	gridRowPosition = natTable.getRowPositionByY(event.y);

    	natTable.forceFocus();
	}

	public boolean isWithShiftMask() {
		return withShiftMask;
	}

	public boolean isWithControlMask() {
		return withControlMask;
	}

	public int getGridColumnPosition() {
		return gridColumnPosition;
	}

	public int getGridRowPosition() {
		return gridRowPosition;
	}

}
