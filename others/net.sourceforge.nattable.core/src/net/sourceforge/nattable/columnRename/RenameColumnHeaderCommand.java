package net.sourceforge.nattable.columnRename;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.layer.ILayer;

/**
 * Command fired to rename a column header
 *
 * @see RenameColumnHeaderCommandHandler
 */
public class RenameColumnHeaderCommand extends AbstractColumnCommand {

	private final String customColumnName;

	public RenameColumnHeaderCommand(ILayer layer, int columnPosition, String customColumnName) {
		super(layer, columnPosition);
		this.customColumnName = customColumnName;
	}

	public ILayerCommand cloneCommand() {
		return new RenameColumnHeaderCommand(getLayer(), getColumnPosition(), customColumnName);
	}

	public String getCustomColumnName() {
		return customColumnName;
	}

}
