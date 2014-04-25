package net.sourceforge.nattable.group.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.group.command.OpenCreateColumnGroupDialog;
import net.sourceforge.nattable.ui.action.IKeyAction;

import org.eclipse.swt.events.KeyEvent;

public class CreateColumnGroupAction implements IKeyAction {

	private OpenCreateColumnGroupDialog dialogCommand;
	
	public void run(NatTable natTable, KeyEvent event) {
		if (dialogCommand == null) {
			// Create dialog
			dialogCommand = new OpenCreateColumnGroupDialog(natTable.getShell());
		}
		natTable.doCommand(dialogCommand);
	}
	
}