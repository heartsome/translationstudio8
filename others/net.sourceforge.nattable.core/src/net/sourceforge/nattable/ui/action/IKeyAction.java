package net.sourceforge.nattable.ui.action;

import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.events.KeyEvent;

public interface IKeyAction {

	public void run(NatTable natTable, KeyEvent event);
	
}
