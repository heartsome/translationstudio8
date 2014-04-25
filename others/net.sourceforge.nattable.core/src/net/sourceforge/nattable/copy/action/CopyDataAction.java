package net.sourceforge.nattable.copy.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.copy.command.CopyDataToClipboardCommand;
import net.sourceforge.nattable.ui.action.IKeyAction;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;

public class CopyDataAction implements IKeyAction {

	public void run(NatTable natTable, KeyEvent event) {
		natTable.doCommand(new CopyDataToClipboardCommand(new Clipboard(event.display), "\t", "\n"));
	}	
}