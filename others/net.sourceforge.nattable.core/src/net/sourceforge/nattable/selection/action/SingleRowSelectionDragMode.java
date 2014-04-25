package net.sourceforge.nattable.selection.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.ClearAllSelectionsCommand;
import net.sourceforge.nattable.selection.config.RowOnlySelectionBindings;
import net.sourceforge.nattable.ui.action.IDragMode;

import org.eclipse.swt.events.MouseEvent;

/**
 * Selects the entire row when the mouse is dragged on the body. Only a
 * <i>single</i> row is selected at a given time. This is the row the mouse is
 * over.
 *
 * @see RowOnlySelectionBindings
 */
public class SingleRowSelectionDragMode extends RowSelectionDragMode implements IDragMode {

	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		natTable.doCommand(new ClearAllSelectionsCommand());

		if (event.x > natTable.getWidth()) {
			return;
		}
		int selectedColumnPosition = natTable.getColumnPositionByX(event.x);
		int selectedRowPosition = natTable.getRowPositionByY(event.y);

		if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
			fireSelectionCommand(natTable,
					selectedColumnPosition,
					selectedRowPosition,
					false,
					false);
		}
	}
}