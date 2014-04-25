package net.sourceforge.nattable.grid.command;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.print.command.TurnViewportOffCommand;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.resize.MaxCellBoundsHelper;
import net.sourceforge.nattable.resize.command.AutoResizeColumnsCommand;
import net.sourceforge.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import net.sourceforge.nattable.resize.command.MultiColumnResizeCommand;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.util.ObjectUtils;

/**
 * This command is triggered by the {@link InitializeAutoResizeColumnsCommand}.
 * The selected columns picked from the {@link SelectionLayer} by the above command.
 * This handler runs as a second step. This <i>must</i> run at the {@link GridLayer} level
 * since we need to pick up all the region labels which are applied at the grid level.
 * 
 * Additionally running at the grid layer level ensures that we include cells from the
 * headers in the width calculations.
 */
public class AutoResizeColumnCommandHandler implements ILayerCommandHandler<AutoResizeColumnsCommand> {

	private final GridLayer gridLayer;

	public AutoResizeColumnCommandHandler(GridLayer gridLayer) {
		this.gridLayer = gridLayer;
	}

	public Class<AutoResizeColumnsCommand> getCommandClass() {
		return AutoResizeColumnsCommand.class;
	}

	public boolean doCommand(ILayer targetLayer, AutoResizeColumnsCommand command) {
		// Need to resize selected columns even if they are outside the viewport
		targetLayer.doCommand(new TurnViewportOffCommand());

		int[] columnPositions = ObjectUtils.asIntArray(command.getColumnPositions());
		int[] gridColumnPositions = convertFromSelectionToGrid(columnPositions);

		int[] gridColumnWidths = MaxCellBoundsHelper.getPreferedColumnWidths(
                                                         command.getConfigRegistry(), 
                                                         command.getGC(), 
                                                         gridLayer,
                                                         gridColumnPositions);

		gridLayer.doCommand(new MultiColumnResizeCommand(gridLayer, gridColumnPositions, gridColumnWidths));
		targetLayer.doCommand(new TurnViewportOnCommand());

		return true;
	}

	private int[] convertFromSelectionToGrid(int[] columnPositions) {
		int[] gridColumnPositions = new int[columnPositions.length];

		for (int i = 0; i < columnPositions.length; i++) {
			// Since the viewport is turned off - body layer can be used as the underlying layer
			gridColumnPositions[i] = gridLayer.underlyingToLocalColumnPosition(gridLayer.getBodyLayer(), columnPositions[i]);
		}
		return gridColumnPositions;
	}

}
