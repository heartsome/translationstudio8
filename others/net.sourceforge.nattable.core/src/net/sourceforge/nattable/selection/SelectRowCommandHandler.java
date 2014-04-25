package net.sourceforge.nattable.selection;

import static net.sourceforge.nattable.selection.SelectionUtils.bothShiftAndControl;
import static net.sourceforge.nattable.selection.SelectionUtils.isControlOnly;
import static net.sourceforge.nattable.selection.SelectionUtils.isShiftOnly;
import static net.sourceforge.nattable.selection.SelectionUtils.noShiftOrControl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.command.SelectRowsCommand;
import net.sourceforge.nattable.selection.event.RowSelectionEvent;

import org.eclipse.swt.graphics.Rectangle;

public class SelectRowCommandHandler implements ILayerCommandHandler<SelectRowsCommand> {

	private final SelectionLayer selectionLayer;

	public SelectRowCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(ILayer targetLayer, SelectRowsCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			selectRows(command.getColumnPosition(), command.getRowPositions(), command.isWithShiftMask(), command.isWithControlMask());
			return true;
		}
		return false;
	}

	protected void selectRows(int columnPosition, Collection<Integer> rowPositions, boolean withShiftMask, boolean withControlMask) {
		Set<Range> changedRowRanges = new HashSet<Range>();
		
		for (int rowPosition : rowPositions) {
			changedRowRanges.addAll(internalSelectRow(columnPosition, rowPosition, withShiftMask, withControlMask));
		}

		Set<Integer> changedRows = new HashSet<Integer>();
		for (Range range : changedRowRanges) {
			for (int i = range.start; i < range.end; i++) {
				changedRows.add(Integer.valueOf(i));
			}
		}
		selectionLayer.fireLayerEvent(new RowSelectionEvent(selectionLayer, changedRows));
	}

	private Set<Range> internalSelectRow(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		Set<Range> changedRowRanges = new HashSet<Range>();
		
		if (noShiftOrControl(withShiftMask, withControlMask)) {
			changedRowRanges.addAll(selectionLayer.getSelectedRows());
			selectionLayer.clear();
			selectionLayer.selectCell(0, rowPosition, withShiftMask, withControlMask);
			selectionLayer.selectRegion(0, rowPosition, selectionLayer.getColumnCount(), 1);
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
			changedRowRanges.add(new Range(rowPosition, rowPosition + 1));
		} else if (bothShiftAndControl(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithShiftKey(rowPosition));
		} else if (isShiftOnly(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithShiftKey(rowPosition));
		} else if (isControlOnly(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithCtrlKey(columnPosition, rowPosition));
		}

		selectionLayer.lastSelectedCell.columnPosition = selectionLayer.getColumnCount() - 1;
		selectionLayer.lastSelectedCell.rowPosition = rowPosition;
		
		return changedRowRanges;
	}

	private Range selectRowWithCtrlKey(int columnPosition, int rowPosition) {
		Rectangle selectedRowRectangle = new Rectangle(0, rowPosition, selectionLayer.getColumnCount(), 1);

		if (selectionLayer.isRowFullySelected(rowPosition)) {
			selectionLayer.clearSelection(selectedRowRectangle);
			if (selectionLayer.lastSelectedRegion != null && selectionLayer.lastSelectedRegion.equals(selectedRowRectangle)) {
				selectionLayer.lastSelectedRegion = null;
			}
		} else {
			// Preserve last selected region
			if (selectionLayer.lastSelectedRegion != null) {
				selectionLayer.selectionModel.addSelection(
						new Rectangle(selectionLayer.lastSelectedRegion.x,
								selectionLayer.lastSelectedRegion.y,
								selectionLayer.lastSelectedRegion.width,
								selectionLayer.lastSelectedRegion.height));
			}
			selectionLayer.selectRegion(0, rowPosition, selectionLayer.getColumnCount(), 1);
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
		}
		
		return new Range(rowPosition, rowPosition + 1);
	}

	private Range selectRowWithShiftKey(int rowPosition) {
		int numOfRowsToIncludeInRegion = 1;
		int startRowPosition = rowPosition;

		if (selectionLayer.lastSelectedRegion != null) {
			numOfRowsToIncludeInRegion = Math.abs(selectionLayer.selectionAnchor.rowPosition - rowPosition) + 1;
			if (startRowPosition < selectionLayer.selectionAnchor.rowPosition) {
				// Selecting above
				startRowPosition = rowPosition;
			} else {
				// Selecting below
				startRowPosition = selectionLayer.selectionAnchor.rowPosition;
			}
		}
		selectionLayer.selectRegion(0, startRowPosition, selectionLayer.getColumnCount(), numOfRowsToIncludeInRegion);
		
		return new Range(startRowPosition, startRowPosition + numOfRowsToIncludeInRegion);
	}

	public Class<SelectRowsCommand> getCommandClass() {
		return SelectRowsCommand.class;
	}

}
