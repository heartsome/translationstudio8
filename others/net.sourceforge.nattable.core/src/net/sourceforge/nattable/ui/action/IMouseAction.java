package net.sourceforge.nattable.ui.action;

import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.events.MouseEvent;

public interface IMouseAction {

	public void run(NatTable natTable, MouseEvent event);
	
}
