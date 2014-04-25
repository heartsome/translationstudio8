package net.sourceforge.nattable.freeze.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.freeze.command.UnFreezeGridCommand;
import net.sourceforge.nattable.ui.action.IKeyAction;

import org.eclipse.swt.events.KeyEvent;

public class UnFreezeGridAction implements IKeyAction {

	public void run(NatTable natTable, KeyEvent event) {
		natTable.doCommand(new UnFreezeGridCommand());
	}
}
