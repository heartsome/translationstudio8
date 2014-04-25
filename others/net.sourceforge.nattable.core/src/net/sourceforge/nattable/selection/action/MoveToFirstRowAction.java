package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

import org.eclipse.swt.events.KeyEvent;

public class MoveToFirstRowAction extends AbstractKeySelectAction {

	public MoveToFirstRowAction() {
		super(MoveDirectionEnum.UP, false, false);
	}

	@Override
	public void run(NatTable natTable, KeyEvent event) {
		super.run(natTable, event);
		natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, SelectionLayer.MOVE_ALL, isShiftMask(), isControlMask()));
	}

}
