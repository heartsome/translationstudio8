package net.sourceforge.nattable.hideshow.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;

public class MultiColumnShowCommandHandler extends AbstractLayerCommandHandler<MultiColumnShowCommand> {

	private final ColumnHideShowLayer columnHideShowLayer;

	public MultiColumnShowCommandHandler(ColumnHideShowLayer columnHideShowLayer) {
		this.columnHideShowLayer = columnHideShowLayer;
	}
	
	public Class<MultiColumnShowCommand> getCommandClass() {
		return MultiColumnShowCommand.class;
	}

	@Override
	protected boolean doCommand(MultiColumnShowCommand command) {
		int[] columnIndexes = command.getColumnIndexes();
		columnHideShowLayer.showColumnIndexes(columnIndexes);
		return true;
	}

}
