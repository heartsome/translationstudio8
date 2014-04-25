package net.sourceforge.nattable.grid.command;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.print.command.TurnViewportOffCommand;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.resize.MaxCellBoundsHelper;
import net.sourceforge.nattable.resize.command.AutoResizeRowsCommand;
import net.sourceforge.nattable.resize.command.MultiRowResizeCommand;
import net.sourceforge.nattable.util.ObjectUtils;

/**
 * @see AutoResizeColumnCommandHandler
 */
public class AutoResizeRowCommandHandler implements ILayerCommandHandler<AutoResizeRowsCommand> {

	private final GridLayer gridLayer;

	public AutoResizeRowCommandHandler(GridLayer gridLayer) {
		this.gridLayer = gridLayer;
	}

	public Class<AutoResizeRowsCommand> getCommandClass() {
		return AutoResizeRowsCommand.class;
	}

	public boolean doCommand(ILayer targetLayer, AutoResizeRowsCommand command) {
		// Need to resize selected rows even if they are outside the viewport
		targetLayer.doCommand(new TurnViewportOffCommand());

		int[] rowPositions = ObjectUtils.asIntArray(command.getRowPositions());
		int[] gridRowPositions = convertFromSelectionToGrid(rowPositions);
		
		int[] gridRowHeights = MaxCellBoundsHelper.getPreferedRowHeights(
                                                    command.getConfigRegistry(), 
                                                    command.getGC(), 
                                                    gridLayer,
                                                    gridRowPositions);

		gridLayer.doCommand(new MultiRowResizeCommand(gridLayer, gridRowPositions, gridRowHeights));
		
		targetLayer.doCommand(new TurnViewportOnCommand());

		return true;
	}

	private int[] convertFromSelectionToGrid(int[] rowPositions) {
		int[] gridRowPositions = new int[rowPositions.length];

		for (int i = 0; i < rowPositions.length; i++) {
			// Since the viewport is turned off - body layer can be used as the underlying layer
			gridRowPositions[i] = gridLayer.underlyingToLocalRowPosition(gridLayer.getBodyLayer(), rowPositions[i]);
		}
		return gridRowPositions;
	}
}
