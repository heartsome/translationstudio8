package net.sourceforge.nattable.filterrow.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ClearFilterCommand extends AbstractColumnCommand {

	public ClearFilterCommand(ILayer layer, int columnPosition) {
		super(layer, columnPosition);
	}

	public ILayerCommand cloneCommand() {
		return new ClearFilterCommand(getLayer(), getColumnPosition());
	}

}
