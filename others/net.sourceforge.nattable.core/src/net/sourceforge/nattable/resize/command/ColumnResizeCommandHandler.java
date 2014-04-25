package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.DataLayer;

public class ColumnResizeCommandHandler extends AbstractLayerCommandHandler<ColumnResizeCommand> {

	private final DataLayer dataLayer;

	public ColumnResizeCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<ColumnResizeCommand> getCommandClass() {
		return ColumnResizeCommand.class;
	}

	@Override
	protected boolean doCommand(ColumnResizeCommand command) {
		final int newColumnWidth = command.getNewColumnWidth();
		dataLayer.setColumnWidthByPosition(command.getColumnPosition(), newColumnWidth);
		return true;
	}

}
