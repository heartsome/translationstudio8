package net.sourceforge.nattable.group.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.group.command.ColumnGroupExpandCollapseCommand;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.events.MouseEvent;

public class ColumnGroupExpandCollapseAction implements IMouseAction{

	public void run(NatTable natTable, MouseEvent event) {
		ColumnGroupExpandCollapseCommand command = new ColumnGroupExpandCollapseCommand(natTable, natTable.getColumnPositionByX(event.x));
		natTable.doCommand(command);
	}

}
