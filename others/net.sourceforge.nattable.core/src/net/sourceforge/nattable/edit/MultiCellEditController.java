package net.sourceforge.nattable.edit;

import java.util.List;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.command.EditUtils;
import net.sourceforge.nattable.edit.command.UpdateDataCommand;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.edit.event.InlineCellEditEvent;
import net.sourceforge.nattable.edit.gui.MultiCellEditDialog;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.style.CellStyleProxy;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.tickupdate.ITickUpdateHandler;
import net.sourceforge.nattable.tickupdate.TickUpdateConfigAttributes;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;

public class MultiCellEditController {

	public static boolean editSelectedCells(SelectionLayer selectionLayer, Character initialEditValue, Composite parent, IConfigRegistry configRegistry) {
		LayerCell lastSelectedCell = EditUtils.getLastSelectedCell(selectionLayer);
		
		// IF cell is selected
		if (lastSelectedCell != null) {
			final List<String> lastSelectedCellLabelsArray = lastSelectedCell.getConfigLabels().getLabels();
			
			PositionCoordinate[] selectedCells = selectionLayer.getSelectedCells();
			// AND selected cell count > 1
			if (selectedCells.length > 1) { 
				
				ICellEditor lastSelectedCellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, lastSelectedCellLabelsArray);
				
				// AND all selected cells are of the same editor type
				// AND all selected cells are editable
				if (EditUtils.isEditorSame(selectionLayer, configRegistry, lastSelectedCellEditor) 
						&& EditUtils.allCellsEditable(selectionLayer, configRegistry)) {
					
					// THEN use multi commit handler and populate editor in popup
					ICellEditor cellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, lastSelectedCellLabelsArray);
					IDisplayConverter dataTypeConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.EDIT, lastSelectedCellLabelsArray);
					IStyle cellStyle = new CellStyleProxy(configRegistry, DisplayMode.EDIT, lastSelectedCellLabelsArray);
					IDataValidator dataValidator = configRegistry.getConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, DisplayMode.EDIT, lastSelectedCellLabelsArray);

					Object originalCanonicalValue = lastSelectedCell.getDataValue();
					for (PositionCoordinate selectedCell : selectedCells) {
						Object cellValue = selectionLayer.getCellByPosition(selectedCell.columnPosition, selectedCell.rowPosition).getDataValue();
						if (!cellValue.equals(originalCanonicalValue)) {
							originalCanonicalValue = null;
							break;
						}
					}                        
					
					ITickUpdateHandler tickUpdateHandler = configRegistry.getConfigAttribute(TickUpdateConfigAttributes.UPDATE_HANDLER, DisplayMode.EDIT, lastSelectedCellLabelsArray);
					boolean allowIncrementDecrement = tickUpdateHandler != null && tickUpdateHandler.isApplicableFor(originalCanonicalValue);
					
					MultiCellEditDialog dialog = new MultiCellEditDialog(parent.getShell(), cellEditor, dataTypeConverter, cellStyle, dataValidator, originalCanonicalValue, initialEditValue, allowIncrementDecrement);

					int returnValue = dialog.open();
					
					ActiveCellEditor.close();
					
					if (returnValue == Dialog.OK) {
						Object editorValue = dialog.getEditorValue();
						Object newValue = editorValue;
						
						if (allowIncrementDecrement) {
							switch (dialog.getEditType()) {
							case INCREASE:
								newValue = tickUpdateHandler.getIncrementedValue(originalCanonicalValue);
								break;
							case DECREASE:
								newValue = tickUpdateHandler.getDecrementedValue(originalCanonicalValue);
								break;
							}
						}
						
						for (PositionCoordinate selectedCell : selectedCells) {
							selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, selectedCell.columnPosition, selectedCell.rowPosition, newValue));
						}
					}
				}
			} else {
				// ELSE use single commit handler and populate editor inline in cell rectangle
				selectionLayer.fireLayerEvent(new InlineCellEditEvent(selectionLayer, new PositionCoordinate(selectionLayer, lastSelectedCell.getColumnPosition(), lastSelectedCell.getRowPosition()), parent, configRegistry, initialEditValue));
			}
			return true;
		}
		return false;
	}
	
}
