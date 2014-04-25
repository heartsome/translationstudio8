package net.sourceforge.nattable.edit.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.edit.command.EditSelectionCommand;
import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;

import org.eclipse.swt.events.KeyEvent;

public class KeyEditAction implements IKeyAction {

	public void run(NatTable natTable, KeyEvent event) {
		Character character = null;
		if (LetterOrDigitKeyEventMatcher.isLetterOrDigit(event.character)) {
			character = Character.valueOf(event.character);
		}
		natTable.doCommand(new EditSelectionCommand(natTable, natTable.getConfigRegistry(), character));
	}

}