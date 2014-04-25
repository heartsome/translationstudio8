package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

import org.eclipse.swt.events.KeyEvent;

public class MoveToLastColumnAction extends AbstractKeySelectAction {

	public MoveToLastColumnAction() {
		super(MoveDirectionEnum.RIGHT);
	}

	public void run(NatTable natTable, KeyEvent event) {
		super.run(natTable, event);
		natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, SelectionLayer.MOVE_ALL, isShiftMask(), isControlMask()));
	}

}
