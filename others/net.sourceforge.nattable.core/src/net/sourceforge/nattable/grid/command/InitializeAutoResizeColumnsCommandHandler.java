package net.sourceforge.nattable.grid.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.resize.command.AutoResizeColumnsCommand;
import net.sourceforge.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import net.sourceforge.nattable.selection.SelectionLayer;

public class InitializeAutoResizeColumnsCommandHandler extends AbstractLayerCommandHandler<InitializeAutoResizeColumnsCommand> {

	private SelectionLayer selectionLayer;

	public InitializeAutoResizeColumnsCommandHandler(SelectionLayer selectionLayer) {
		super();
		this.selectionLayer = selectionLayer;
	}

	public Class<InitializeAutoResizeColumnsCommand> getCommandClass() {
		return InitializeAutoResizeColumnsCommand.class;
	}

	@Override
	protected boolean doCommand(InitializeAutoResizeColumnsCommand initCommand) {
		int columnPosition = initCommand.getColumnPosition();
		if (selectionLayer.isColumnFullySelected(columnPosition)) {
			initCommand.setSelectedColumnPositions(selectionLayer.getFullySelectedColumnPositions());
		} else {
			initCommand.setSelectedColumnPositions(new int[] { columnPosition });
		}

		// Fire command carrying the selected columns
		initCommand.getSourceLayer().doCommand(new AutoResizeColumnsCommand(initCommand));
		return true;
	}

}
