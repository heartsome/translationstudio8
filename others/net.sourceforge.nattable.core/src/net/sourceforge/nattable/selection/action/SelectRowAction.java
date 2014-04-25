package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.SelectRowsCommand;

import org.eclipse.swt.events.MouseEvent;

/**
 * Action executed when the user selects any row in the grid.
 */
public class SelectRowAction extends AbstractMouseSelectionAction {

    @Override
	public void run(NatTable natTable, MouseEvent event) {
    	super.run(natTable, event);
        natTable.doCommand(new SelectRowsCommand(natTable, getGridColumnPosition(), getGridRowPosition(), isWithShiftMask(), isWithControlMask()));
    }

}
