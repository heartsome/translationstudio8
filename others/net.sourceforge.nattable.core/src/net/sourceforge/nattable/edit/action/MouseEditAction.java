package net.sourceforge.nattable.edit.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.edit.command.EditCellCommand;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

public class MouseEditAction implements IMouseAction {

	public void run(NatTable natTable, MouseEvent event) {
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);

		boolean withShiftMask = (event.stateMask & SWT.SHIFT) != 0;
		boolean withCtrlMask = (event.stateMask & SWT.CTRL) != 0;

		natTable.doCommand(new SelectCellCommand(natTable, columnPosition, rowPosition, withShiftMask, withCtrlMask));

		natTable.doCommand(
				new EditCellCommand(
						natTable,
						natTable.getConfigRegistry(),
						natTable.getCellByPosition(columnPosition, rowPosition)));
	}
}