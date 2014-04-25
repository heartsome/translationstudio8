package net.sourceforge.nattable.columnRename;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;

import org.eclipse.swt.widgets.Shell;

public class DisplayColumnRenameDialogCommandHandler extends
		AbstractLayerCommandHandler<DisplayColumnRenameDialogCommand> {

	private final ColumnHeaderLayer columnHeaderLayer;

	public DisplayColumnRenameDialogCommandHandler(ColumnHeaderLayer columnHeaderLayer) {
		this.columnHeaderLayer = columnHeaderLayer;
	}

	@Override
	protected boolean doCommand(DisplayColumnRenameDialogCommand command) {
		int columnPosition = command.getColumnPosition();
		String originalLabel = columnHeaderLayer.getOriginalColumnLabel(columnPosition);
		String renamedLabel = columnHeaderLayer.getRenamedColumnLabel(columnPosition);

		ColumnRenameDialog dialog = new ColumnRenameDialog(new Shell(), originalLabel, renamedLabel);
		dialog.open();

		if (dialog.isCancelPressed()) {
			return true;
		}

		return columnHeaderLayer.renameColumnPosition(columnPosition, dialog.getNewColumnLabel());
	}

	public Class<DisplayColumnRenameDialogCommand> getCommandClass() {
		return DisplayColumnRenameDialogCommand.class;
	}

}
