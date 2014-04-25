package net.sourceforge.nattable.selection;

import static net.sourceforge.nattable.selection.SelectionUtils.bothShiftAndControl;
import static net.sourceforge.nattable.selection.SelectionUtils.isControlOnly;
import static net.sourceforge.nattable.selection.SelectionUtils.isShiftOnly;
import static net.sourceforge.nattable.selection.SelectionUtils.noShiftOrControl;

import org.eclipse.swt.graphics.Rectangle;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.command.SelectColumnCommand;
import net.sourceforge.nattable.selection.event.ColumnSelectionEvent;

public class SelectColumnCommandHandler implements ILayerCommandHandler<SelectColumnCommand> {

	private final SelectionLayer selectionLayer;

	public SelectColumnCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(ILayer targetLayer, SelectColumnCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			selectColumn(command.getColumnPosition(), command.getRowPosition(), command.isWithShiftMask(), command.isWithControlMask());
			return true;
		}
		return false;
	}

	protected void selectColumn(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		if (noShiftOrControl(withShiftMask, withControlMask)) {
			selectionLayer.clear();
			selectionLayer.selectCell(columnPosition, 0, false, false);
			selectionLayer.selectRegion(columnPosition, 0, 1, selectionLayer.getRowCount());
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
		} else if (bothShiftAndControl(withShiftMask, withControlMask)) {
			selectColumnWithShiftKey(columnPosition);
		} else if (isShiftOnly(withShiftMask, withControlMask)) {
			selectColumnWithShiftKey(columnPosition);
		} else if (isControlOnly(withShiftMask, withControlMask)) {
			selectColumnWithCtrlKey(columnPosition, rowPosition);
		}

		// Set last selected column position to the recently clicked column
		selectionLayer.lastSelectedCell.columnPosition = columnPosition;
		selectionLayer.lastSelectedCell.rowPosition = selectionLayer.getRowCount() - 1;

		selectionLayer.fireLayerEvent(new ColumnSelectionEvent(selectionLayer, columnPosition));
	}

	private void selectColumnWithCtrlKey(int columnPosition, int rowPosition) {
		Rectangle selectedColumnRectangle = new Rectangle(columnPosition, 0, 1, selectionLayer.getRowCount());

		if (selectionLayer.isColumnFullySelected(columnPosition)) {
			selectionLayer.clearSelection(selectedColumnRectangle);
			if(selectionLayer.lastSelectedRegion != null && selectionLayer.lastSelectedRegion.equals(selectedColumnRectangle)){
				selectionLayer.lastSelectedRegion = null;
			}
		}else{
			if(selectionLayer.lastSelectedRegion != null){
				selectionLayer.selectionModel.addSelection(
						new Rectangle(selectionLayer.lastSelectedRegion.x,
								selectionLayer.lastSelectedRegion.y,
								selectionLayer.lastSelectedRegion.width,
								selectionLayer.lastSelectedRegion.height));
			}
			selectionLayer.selectRegion(columnPosition, 0, 1, selectionLayer.getRowCount());
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
		}
	}

	private void selectColumnWithShiftKey(int columnPosition) {
		int numOfColumnsToIncludeInRegion = 1;
		int startColumnPosition = columnPosition;
		 
		if (selectionLayer.lastSelectedRegion != null) {
			
			// Negative when we move left, but we are only concerned with the num. of columns  
			numOfColumnsToIncludeInRegion = Math.abs(selectionLayer.selectionAnchor.columnPosition - columnPosition) + 1;
			
			// Select to the Left
			if (columnPosition < selectionLayer.selectionAnchor.columnPosition) {
				startColumnPosition = columnPosition;
			} else {
				startColumnPosition = selectionLayer.selectionAnchor.columnPosition;
			}
		}
		selectionLayer.selectRegion(startColumnPosition, 0, numOfColumnsToIncludeInRegion, selectionLayer.getRowCount());
	}
	
	public Class<SelectColumnCommand> getCommandClass() {
		return SelectColumnCommand.class;
	}

}
