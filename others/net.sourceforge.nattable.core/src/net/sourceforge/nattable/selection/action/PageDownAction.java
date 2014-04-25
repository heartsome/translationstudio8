package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.ScrollSelectionCommand;

import org.eclipse.swt.events.KeyEvent;

public class PageDownAction extends AbstractKeySelectAction {

	public PageDownAction() {
		super(MoveDirectionEnum.DOWN);
	}

	public void run(NatTable natTable, KeyEvent event) {
		super.run(natTable, event);
		natTable.doCommand(new ScrollSelectionCommand(MoveDirectionEnum.DOWN, isShiftMask(), isControlMask()));
	}

}
