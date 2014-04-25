package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.DataLayer;

public class MultiColumnResizeCommandHandler extends AbstractLayerCommandHandler<MultiColumnResizeCommand> {

	private final DataLayer dataLayer;

	public MultiColumnResizeCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<MultiColumnResizeCommand> getCommandClass() {
		return MultiColumnResizeCommand.class;
	}

	@Override
	protected boolean doCommand(MultiColumnResizeCommand command) {
		for (int columnPosition : command.getColumnPositions()) {
			dataLayer.setColumnWidthByPosition(columnPosition, command.getColumnWidth(columnPosition));
		}
		return true;
	}

}
