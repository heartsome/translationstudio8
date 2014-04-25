package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ShowRowInViewportCommandHandler extends AbstractLayerCommandHandler<ShowRowInViewportCommand> {
	
	private final ViewportLayer viewportLayer;

	public ShowRowInViewportCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<ShowRowInViewportCommand> getCommandClass() {
		return ShowRowInViewportCommand.class;
	}

	@Override
	protected boolean doCommand(ShowRowInViewportCommand command) {
		viewportLayer.moveRowPositionIntoViewport(command.getRowPosition(), false);
		return true;
	}

}
