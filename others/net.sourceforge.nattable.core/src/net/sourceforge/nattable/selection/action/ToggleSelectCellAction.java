package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.events.MouseEvent;

/**
 * NOTE - work in progress - NTBL-252
 */
public class ToggleSelectCellAction implements IMouseAction {

	private boolean withControlMask;
	private boolean withShiftMask;

	public ToggleSelectCellAction(boolean withShiftMask, boolean withControlMask) {
		this.withShiftMask = withShiftMask;
		this.withControlMask = withControlMask;
	}
	
	public void run(NatTable natTable, MouseEvent event) {
		new SelectCellCommand(
				natTable,
				natTable.getColumnPositionByX(event.x), 
				natTable.getRowPositionByY(event.y), 
				withShiftMask, 
				withControlMask);
	}
}
