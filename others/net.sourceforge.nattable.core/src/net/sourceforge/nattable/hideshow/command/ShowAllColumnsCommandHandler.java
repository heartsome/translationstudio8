package net.sourceforge.nattable.hideshow.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;

public class ShowAllColumnsCommandHandler extends AbstractLayerCommandHandler<ShowAllColumnsCommand> {

	private final ColumnHideShowLayer columnHideShowLayer;

	public ShowAllColumnsCommandHandler(ColumnHideShowLayer columnHideShowLayer) {
		this.columnHideShowLayer = columnHideShowLayer;
	}
	
	public Class<ShowAllColumnsCommand> getCommandClass() {
		return ShowAllColumnsCommand.class;
	}

	@Override
	protected boolean doCommand(ShowAllColumnsCommand command) {
		columnHideShowLayer.showAllColumns();
		return true;
	}

}
