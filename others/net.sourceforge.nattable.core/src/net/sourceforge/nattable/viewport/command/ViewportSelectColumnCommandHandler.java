package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.selection.command.SelectColumnCommand;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ViewportSelectColumnCommandHandler extends AbstractLayerCommandHandler<ViewportSelectColumnCommand> {

	private final ViewportLayer viewportLayer;

	public ViewportSelectColumnCommandHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
		
	}
	
	public Class<ViewportSelectColumnCommand> getCommandClass() {
		return ViewportSelectColumnCommand.class;
	}

	@Override
	protected boolean doCommand(ViewportSelectColumnCommand command) {
		IUniqueIndexLayer scrollableLayer = viewportLayer.getScrollableLayer();
		int scrollableColumnPosition = viewportLayer.localToUnderlyingColumnPosition(command.getColumnPosition());
		int scrollableRowPosition = viewportLayer.getOriginRowPosition();
		
		scrollableLayer.doCommand(new SelectColumnCommand(scrollableLayer, scrollableColumnPosition, scrollableRowPosition, command.isWithShiftMask(), command.isWithControlMask()));
		return true;
	}

}
