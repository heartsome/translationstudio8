package net.sourceforge.nattable.tickupdate.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.tickupdate.command.TickUpdateCommand;
import net.sourceforge.nattable.ui.action.IKeyAction;

import org.eclipse.swt.events.KeyEvent;

public class TickUpdateAction implements IKeyAction {

	private final boolean increment;

	public TickUpdateAction(boolean increment) {
		this.increment = increment;
	}

	public void run(NatTable natTable, KeyEvent event) {
		natTable.doCommand(new TickUpdateCommand(natTable.getConfigRegistry(), increment));
	}

}
