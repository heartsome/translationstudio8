package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.DataLayer;

public class RowResizeCommandHandler extends AbstractLayerCommandHandler<RowResizeCommand> {

	private final DataLayer dataLayer;

	public RowResizeCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<RowResizeCommand> getCommandClass() {
		return RowResizeCommand.class;
	}

	@Override
	protected boolean doCommand(RowResizeCommand command) {
		final int rowPosition = command.getRowPosition();
		final int newRowHeight = command.getNewHeight();
		dataLayer.setRowHeightByPosition(rowPosition, newRowHeight);
		return true;
	}

}
