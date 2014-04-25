package net.sourceforge.nattable.columnRename;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.layer.ILayer;

/**
 * Fire this command to pop-up the rename column dialog.
 */
public class DisplayColumnRenameDialogCommand extends AbstractColumnCommand {

	/**
	 * @param columnPosition of the column to be renamed 
	 */
	public DisplayColumnRenameDialogCommand(ILayer layer, int columnPosition) {
		super(layer, columnPosition);
	}

	public ILayerCommand cloneCommand() {
		return new DisplayColumnRenameDialogCommand(getLayer(), getColumnPosition());
	}

}
