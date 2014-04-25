package net.sourceforge.nattable.group.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.group.command.UngroupColumnCommand;
import net.sourceforge.nattable.ui.action.IKeyAction;

import org.eclipse.swt.events.KeyEvent;

public class UngroupColumnsAction implements IKeyAction {

	public void run(NatTable natTable, KeyEvent event) {
		natTable.doCommand(new UngroupColumnCommand());
	}
}