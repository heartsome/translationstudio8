package net.sourceforge.nattable.hideshow.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;

public class MultiColumnHideCommandHandler extends AbstractLayerCommandHandler<MultiColumnHideCommand> {

	private final ColumnHideShowLayer columnHideShowLayer;

	public MultiColumnHideCommandHandler(ColumnHideShowLayer columnHideShowLayer) {
		this.columnHideShowLayer = columnHideShowLayer;
	}
	
	public Class<MultiColumnHideCommand> getCommandClass() {
		return MultiColumnHideCommand.class;
	}

	@Override
	protected boolean doCommand(MultiColumnHideCommand command) {
		columnHideShowLayer.hideColumnPositions(command.getColumnPositions());
		return true;
	}

}
