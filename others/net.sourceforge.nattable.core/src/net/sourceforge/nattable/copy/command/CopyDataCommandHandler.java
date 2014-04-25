package net.sourceforge.nattable.copy.command;

import java.util.Set;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.copy.serializing.CopyDataToClipboardSerializer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.serializing.ISerializer;

public class CopyDataCommandHandler extends AbstractLayerCommandHandler<CopyDataToClipboardCommand> {

	private final SelectionLayer selectionLayer;
	private final ILayer columnHeaderLayer;
	private final ILayer rowHeaderLayer;

	public CopyDataCommandHandler(SelectionLayer selectionLayer) {
		this(selectionLayer, null, null);
	}
	
	public CopyDataCommandHandler(SelectionLayer selectionLayer, ILayer columnHeaderLayer, ILayer rowHeaderLayer) {
		this.selectionLayer = selectionLayer;
		this.columnHeaderLayer = columnHeaderLayer;
		this.rowHeaderLayer = rowHeaderLayer;
	}
	
	public boolean doCommand(CopyDataToClipboardCommand command) {
		ISerializer serializer = new CopyDataToClipboardSerializer(assembleCopiedDataStructure(), command);
		serializer.serialize();
		return true;
	}

	public Class<CopyDataToClipboardCommand> getCommandClass() {
		return CopyDataToClipboardCommand.class;
	}

	protected LayerCell[][] assembleCopiedDataStructure() {
		final Set<Range> selectedRows = selectionLayer.getSelectedRows();
		final int rowOffset = columnHeaderLayer != null ? columnHeaderLayer.getRowCount() : 0;
		// Add offset to rows, remember they need to include the column header as a row
		final LayerCell[][] copiedCells = new LayerCell[selectionLayer.getSelectedRowCount() + rowOffset][1];
		if (columnHeaderLayer != null) {
			copiedCells[0] = assembleColumnHeaders(selectionLayer.getSelectedColumns());
		}
		for (Range range : selectedRows) {
			for (int rowPosition = range.start; rowPosition < range.end; rowPosition++) {
				copiedCells[(rowPosition - range.start) + rowOffset] = assembleBody(rowPosition);
			}
		}
		return copiedCells;
	}

	/**
	 * FIXME When we implement column groups, keep in mind this method assumes the ColumnHeaderLayer is has only a height of 1 row.
	 * @return
	 */
	protected LayerCell[] assembleColumnHeaders(int... selectedColumnPositions) {
		final int columnOffset = rowHeaderLayer.getColumnCount();
		final LayerCell[] cells = new LayerCell[selectedColumnPositions.length + columnOffset];
		for (int columnPosition = 0; columnPosition < selectedColumnPositions.length; columnPosition++) {
			// Pad the width of the vertical layer
			cells[columnPosition + columnOffset] = columnHeaderLayer.getCellByPosition(selectedColumnPositions[columnPosition], 0);
		}
		return cells;
	}
	
	/**
	 * FIXME Assumes row headers have only one column.
	 * @param lastSelectedColumnPosition
	 * @param currentRowPosition
	 * @return
	 */
	protected LayerCell[] assembleBody(int currentRowPosition) {		
		final int[] selectedColumns = selectionLayer.getSelectedColumns();
		final int columnOffset = rowHeaderLayer != null ? rowHeaderLayer.getColumnCount() : 0;
		final LayerCell[] bodyCells = new LayerCell[selectedColumns.length + columnOffset];
		
		if (rowHeaderLayer != null) {
			bodyCells[0] = rowHeaderLayer.getCellByPosition(0, currentRowPosition);
		}
		
		for (int columnPosition = 0; columnPosition < selectedColumns.length; columnPosition++) {
			final int selectedColumnPosition = selectedColumns[columnPosition];
			if (selectionLayer.isCellPositionSelected(selectedColumnPosition, currentRowPosition)) {
				bodyCells[columnPosition + columnOffset] = selectionLayer.getCellByPosition(selectedColumnPosition, currentRowPosition);
			}
		}
		return bodyCells;
	}
	
}