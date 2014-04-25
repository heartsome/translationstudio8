package net.sourceforge.nattable.tickupdate.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.command.EditUtils;
import net.sourceforge.nattable.edit.command.UpdateDataCommand;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.tickupdate.ITickUpdateHandler;
import net.sourceforge.nattable.tickupdate.TickUpdateConfigAttributes;

public class TickUpdateCommandHandler extends AbstractLayerCommandHandler<TickUpdateCommand> {

	private SelectionLayer selectionLayer;

	public TickUpdateCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(TickUpdateCommand command) {
		PositionCoordinate[] selectedPositions = selectionLayer.getSelectedCells();
		IConfigRegistry configRegistry = command.getConfigRegistry();
		
		// Tick update for multiple cells in selection 
		if (selectedPositions.length > 1) {
			
			ICellEditor lastSelectedCellEditor = EditUtils.lastSelectedCellEditor(selectionLayer, configRegistry);
			// Can all cells be updated ?
			if (EditUtils.isEditorSame(selectionLayer, configRegistry, lastSelectedCellEditor) 
					&& EditUtils.allCellsEditable(selectionLayer, configRegistry)){
				
				for (PositionCoordinate position : selectedPositions) {
					updateSingleCell(command, position);
				}
			}
		} else {
			// Tick update for single selected cell
			updateSingleCell(command, selectionLayer.getLastSelectedCellPosition());
		}

		return true;
	}


	private void updateSingleCell(TickUpdateCommand command, PositionCoordinate selectedPosition) {
		LayerCell cell = selectionLayer.getCellByPosition(selectedPosition.columnPosition, selectedPosition.rowPosition);
		
		IEditableRule editableRule = command.getConfigRegistry().getConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, 
				DisplayMode.EDIT,
				cell.getConfigLabels().getLabels());
		
		if(editableRule.isEditable(selectedPosition.columnPosition, selectedPosition.rowPosition)){
			selectionLayer.doCommand(new UpdateDataCommand(
					selectionLayer,
					selectedPosition.columnPosition, 
					selectedPosition.rowPosition,
					getNewCellValue(command, cell)));
		}
	}

	private Object getNewCellValue(TickUpdateCommand command, LayerCell cell) {
		ITickUpdateHandler tickUpdateHandler = command.getConfigRegistry().getConfigAttribute(
				TickUpdateConfigAttributes.UPDATE_HANDLER,
				DisplayMode.EDIT, 
				cell.getConfigLabels().getLabels());

		Object dataValue = cell.getDataValue();

		if (tickUpdateHandler != null && tickUpdateHandler.isApplicableFor(dataValue)) {
			if (command.isIncrement()) {
				return tickUpdateHandler.getIncrementedValue(dataValue);
			} else {
				return tickUpdateHandler.getDecrementedValue(dataValue);
			}
		} else {
			return dataValue;
		}
	}

	public Class<TickUpdateCommand> getCommandClass() {
		return TickUpdateCommand.class;
	}
}
