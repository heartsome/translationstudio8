package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ShowColumnInViewportCommandHandler extends AbstractLayerCommandHandler<ShowColumnInViewportCommand> {
	
	private final ViewportLayer viewportLayer;

	public ShowColumnInViewportCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<ShowColumnInViewportCommand> getCommandClass() {
		return ShowColumnInViewportCommand.class;
	}

	@Override
	protected boolean doCommand(ShowColumnInViewportCommand command) {
		viewportLayer.moveColumnPositionIntoViewport(command.getColumnPosition(), false);
		return true;
	}

}
