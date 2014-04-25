package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ShowCellInViewportCommandHandler extends AbstractLayerCommandHandler<ShowCellInViewportCommand> {
	
	private final ViewportLayer viewportLayer;

	public ShowCellInViewportCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<ShowCellInViewportCommand> getCommandClass() {
		return ShowCellInViewportCommand.class;
	}

	@Override
	protected boolean doCommand(ShowCellInViewportCommand command) {
		viewportLayer.moveCellPositionIntoViewport(command.getColumnPosition(), command.getRowPosition(), false);
		return true;
	}

}
