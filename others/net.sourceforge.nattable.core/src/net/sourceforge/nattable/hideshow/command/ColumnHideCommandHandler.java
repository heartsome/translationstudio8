package net.sourceforge.nattable.hideshow.command;

import static java.util.Arrays.asList;
import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;

public class ColumnHideCommandHandler extends AbstractLayerCommandHandler<ColumnHideCommand> {

	private final ColumnHideShowLayer columnHideShowLayer;

	public ColumnHideCommandHandler(ColumnHideShowLayer columnHideShowLayer) {
		this.columnHideShowLayer = columnHideShowLayer;
	}

	public Class<ColumnHideCommand> getCommandClass() {
		return ColumnHideCommand.class;
	}

	@Override
	protected boolean doCommand(ColumnHideCommand command) {
		columnHideShowLayer.hideColumnPositions(asList(command.getColumnPosition()));
		return true;
	}

}
