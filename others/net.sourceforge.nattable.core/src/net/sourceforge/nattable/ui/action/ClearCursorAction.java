package net.sourceforge.nattable.ui.action;

import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.events.MouseEvent;

public class ClearCursorAction implements IMouseAction {
	
	public void run(NatTable natTable, MouseEvent event) {
		natTable.setCursor(null);
	}

}
