package net.sourceforge.nattable.sort.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.sort.command.SortColumnCommand;
import net.sourceforge.nattable.ui.NatEventData;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.events.MouseEvent;

public class SortColumnAction implements IMouseAction {
	
	private final boolean accumulate;

	public SortColumnAction(boolean accumulate) {
		this.accumulate = accumulate;
	}
	
	public void run(NatTable natTable, MouseEvent event) {
		int columnPosition = ((NatEventData)event.data).getColumnPosition();
		natTable.doCommand(new SortColumnCommand(natTable, columnPosition, accumulate));
	}
}
