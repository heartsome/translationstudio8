package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.DataLayer;

public class MultiRowResizeCommandHandler extends AbstractLayerCommandHandler<MultiRowResizeCommand> {

	private final DataLayer dataLayer;

	public MultiRowResizeCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<MultiRowResizeCommand> getCommandClass() {
		return MultiRowResizeCommand.class;
	}

	@Override
	protected boolean doCommand(MultiRowResizeCommand command) {
		for (int rowPosition : command.getRowPositions()) {
			dataLayer.setRowHeightByPosition(rowPosition, command.getRowHeight(rowPosition));
		}
		return true;
	}

}
