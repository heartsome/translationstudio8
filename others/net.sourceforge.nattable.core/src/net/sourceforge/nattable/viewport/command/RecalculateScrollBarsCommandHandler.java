package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class RecalculateScrollBarsCommandHandler extends AbstractLayerCommandHandler<RecalculateScrollBarsCommand> {
	
	private final ViewportLayer viewportLayer;

	public RecalculateScrollBarsCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<RecalculateScrollBarsCommand> getCommandClass() {
		return RecalculateScrollBarsCommand.class;
	}

	@Override
	protected boolean doCommand(RecalculateScrollBarsCommand command) {
		viewportLayer.recalculateScrollBars();
		return true;
	}

}
