package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.SelectCellCommand;

import org.eclipse.swt.events.MouseEvent;

/**
 * Action executed when the user selects any cell in the grid.
 */
public class SelectCellAction extends AbstractMouseSelectionAction {

    @Override
	public void run(NatTable natTable, MouseEvent event) {
    	super.run(natTable, event);
        natTable.doCommand(new SelectCellCommand(natTable, getGridColumnPosition(), getGridRowPosition(), isWithShiftMask(), isWithControlMask()));
    }

}
