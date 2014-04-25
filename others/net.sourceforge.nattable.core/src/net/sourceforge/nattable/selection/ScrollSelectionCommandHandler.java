package net.sourceforge.nattable.selection;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.selection.command.ScrollSelectionCommand;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ScrollSelectionCommandHandler extends AbstractLayerCommandHandler<ScrollSelectionCommand> {

	private final ViewportLayer viewportLayer;

	public ScrollSelectionCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<ScrollSelectionCommand> getCommandClass() {
		return ScrollSelectionCommand.class;
	}

	@Override
	protected boolean doCommand(ScrollSelectionCommand command) {
		viewportLayer.scrollVerticallyByAPage(command);
		return true;
	}

}
