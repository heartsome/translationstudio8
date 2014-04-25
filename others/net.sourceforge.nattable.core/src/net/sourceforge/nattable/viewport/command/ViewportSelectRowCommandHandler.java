package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.selection.command.SelectRowsCommand;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ViewportSelectRowCommandHandler extends AbstractLayerCommandHandler<ViewportSelectRowCommand> {

	private final ViewportLayer viewportLayer;

	public ViewportSelectRowCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<ViewportSelectRowCommand> getCommandClass() {
		return ViewportSelectRowCommand.class;
	}

	@Override
	protected boolean doCommand(ViewportSelectRowCommand command) {
		IUniqueIndexLayer scrollableLayer = viewportLayer.getScrollableLayer();
		int scrollableColumnPosition = viewportLayer.getOriginColumnPosition();
		int scrollableRowPosition = viewportLayer.localToUnderlyingRowPosition(command.getRowPosition());
		
		scrollableLayer.doCommand(new SelectRowsCommand(scrollableLayer, scrollableColumnPosition, new int[] { scrollableRowPosition }, command.isWithShiftMask(), command.isWithControlMask()));
		
		return true;
	}

}
