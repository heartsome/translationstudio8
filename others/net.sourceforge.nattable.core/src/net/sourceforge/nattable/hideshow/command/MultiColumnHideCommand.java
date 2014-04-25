package net.sourceforge.nattable.hideshow.command;

import net.sourceforge.nattable.command.AbstractMultiColumnCommand;
import net.sourceforge.nattable.layer.ILayer;

public class MultiColumnHideCommand extends AbstractMultiColumnCommand {

	public MultiColumnHideCommand(ILayer layer, int columnPosition) {
		this(layer, new int[] { columnPosition });
	}

	public MultiColumnHideCommand(ILayer layer, int[] columnPositions) {
		super(layer, columnPositions);
	}
	
	protected MultiColumnHideCommand(MultiColumnHideCommand command) {
		super(command);
	}
	
	public MultiColumnHideCommand cloneCommand() {
		return new MultiColumnHideCommand(this);
	}
	
}