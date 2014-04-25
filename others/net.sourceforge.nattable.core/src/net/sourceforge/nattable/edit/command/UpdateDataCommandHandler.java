package net.sourceforge.nattable.edit.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.event.CellVisualChangeEvent;

public class UpdateDataCommandHandler extends AbstractLayerCommandHandler<UpdateDataCommand> {

	private final DataLayer dataLayer;

	public UpdateDataCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<UpdateDataCommand> getCommandClass() {
		return UpdateDataCommand.class;
	}

	@Override
	protected boolean doCommand(UpdateDataCommand command) {
		try {
			int columnPosition = command.getColumnPosition();
			int rowPosition = command.getRowPosition();
			dataLayer.getDataProvider().setDataValue(columnPosition, rowPosition, command.getNewValue());
			dataLayer.fireLayerEvent(new CellVisualChangeEvent(dataLayer, columnPosition, rowPosition));
			return true;
		} catch (UnsupportedOperationException e) {
			e.printStackTrace(System.err);
			System.err.println("Failed to update value to: "+command.getNewValue());
			return false;
		}
	}

}
