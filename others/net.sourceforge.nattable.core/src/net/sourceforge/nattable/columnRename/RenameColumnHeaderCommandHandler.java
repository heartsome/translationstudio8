package net.sourceforge.nattable.columnRename;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;

/**
 * Handles renaming of columns.<br/>
 * Registered with the {@link ColumnHeaderLayer}.
 */
public class RenameColumnHeaderCommandHandler 
		extends AbstractLayerCommandHandler<RenameColumnHeaderCommand> {

	ColumnHeaderLayer columnHeaderLayer;
	
	public RenameColumnHeaderCommandHandler(ColumnHeaderLayer columnHeaderLayer) {
		this.columnHeaderLayer = columnHeaderLayer;
	}

	@Override
	protected boolean doCommand(RenameColumnHeaderCommand command) {
		return columnHeaderLayer.renameColumnPosition(command.getColumnPosition(), command.getCustomColumnName());
	}

	public Class<RenameColumnHeaderCommand> getCommandClass() {
		return RenameColumnHeaderCommand.class;
	}

}
