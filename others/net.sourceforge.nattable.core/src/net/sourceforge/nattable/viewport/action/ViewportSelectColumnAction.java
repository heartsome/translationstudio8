package net.sourceforge.nattable.viewport.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.viewport.command.ViewportSelectColumnCommand;

import org.eclipse.swt.events.MouseEvent;

/**
 * Action indicating that the user has specifically selected a column header. 
 */
public class ViewportSelectColumnAction implements IMouseAction {
	
	private final boolean withShiftMask;
	private final boolean withControlMask;

	public ViewportSelectColumnAction(boolean withShiftMask, boolean withControlMask) {
		this.withShiftMask = withShiftMask;
		this.withControlMask = withControlMask;
	}
	
	public void run(NatTable natTable, MouseEvent event) {
		natTable.doCommand(new ViewportSelectColumnCommand(natTable, natTable.getColumnPositionByX(event.x), withShiftMask, withControlMask));
	}
}
