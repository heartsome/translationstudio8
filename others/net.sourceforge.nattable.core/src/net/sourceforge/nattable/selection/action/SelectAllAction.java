package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.SelectAllCommand;
import net.sourceforge.nattable.ui.action.IKeyAction;

import org.eclipse.swt.events.KeyEvent;

public class SelectAllAction implements IKeyAction {
	
	public void run(NatTable natTable, KeyEvent event) {
		natTable.doCommand(new SelectAllCommand());
	}

}
