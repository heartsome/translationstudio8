package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.SelectRowsCommand;
import net.sourceforge.nattable.selection.config.RowOnlySelectionBindings;

/**
 * Selects the entire row when the mouse is dragged on the body.
 * <i>Multiple</i> rows are selected as the user drags.
 *
 * @see RowOnlySelectionBindings
 */
public class RowSelectionDragMode extends CellSelectionDragMode {

	@Override
	public void fireSelectionCommand(NatTable natTable, int columnPosition,	int rowPosition, boolean shiftMask, boolean controlMask) {
		natTable.doCommand(new SelectRowsCommand(natTable, columnPosition, rowPosition, shiftMask, controlMask));
	}

}