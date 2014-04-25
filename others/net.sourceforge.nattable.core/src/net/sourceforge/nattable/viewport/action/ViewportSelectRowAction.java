package net.sourceforge.nattable.viewport.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.viewport.command.ViewportSelectRowCommand;

import org.eclipse.swt.events.MouseEvent;

/**
 * Event fired when the <i>ctrl</i> key is pressed and the row header is clicked.
 * Note: Fires command in NatTable coordinates.
 * 
 * @see NatTable#configureMouseBindings()
 */
public class ViewportSelectRowAction implements IMouseAction {
	
	private final boolean withShiftMask;
	private final boolean withControlMask;

	public ViewportSelectRowAction(boolean withShiftMask, boolean withControlMask) {
		this.withShiftMask = withShiftMask;
		this.withControlMask = withControlMask;
	}
	
	public void run(NatTable natTable, MouseEvent event) {
		natTable.doCommand(new ViewportSelectRowCommand(natTable, natTable.getRowPositionByY(event.y), withShiftMask, withControlMask));
	}
	
}
