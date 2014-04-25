package net.sourceforge.nattable.grid.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.resize.command.AutoResizeRowsCommand;
import net.sourceforge.nattable.resize.command.InitializeAutoResizeRowsCommand;
import net.sourceforge.nattable.selection.SelectionLayer;

public class InitializeAutoResizeRowsCommandHandler extends AbstractLayerCommandHandler<InitializeAutoResizeRowsCommand> {

	private SelectionLayer selectionLayer;

	public InitializeAutoResizeRowsCommandHandler(SelectionLayer selectionLayer) {
		super();
		this.selectionLayer = selectionLayer;
	}

	public Class<InitializeAutoResizeRowsCommand> getCommandClass() {
		return InitializeAutoResizeRowsCommand.class;
	}

	@Override
	protected boolean doCommand(InitializeAutoResizeRowsCommand initCommand) {
		int rowPosition = initCommand.getRowPosition();
		
		if (selectionLayer.isRowFullySelected(rowPosition)) {
			initCommand.setSelectedRowPositions(selectionLayer.getFullySelectedRowPositions());
		} else {
			initCommand.setSelectedRowPositions(new int[] { rowPosition });
		}

		// Fire command carrying the selected columns
		initCommand.getSourceLayer().doCommand(new AutoResizeRowsCommand(initCommand));
		return true;
	}

}
