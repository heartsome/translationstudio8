package net.sourceforge.nattable.selection;

import java.util.List;
import java.util.Set;

import net.sourceforge.nattable.coordinate.Range;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Tracks the selections made in the table.
 */
public interface ISelectionModel {

	public boolean isCellPositionSelected(int columnPosition, int rowPosition);
	
	public boolean isRowPositionSelected(int rowPosition);

	/**
	 * @see #isColumnFullySelected(int, int)
	 */
	public boolean isRowFullySelected(int rowPosition, int rowWidth);

	/**
	 * Is a cell in this column selected ?
	 */
	public boolean isColumnPositionSelected(int columnPosition);

	/**
	 * Are all cells in this column selected ? 
	 * Different selection rectangles might aggregate to cover the entire column.
	 * We need to take into account any overlapping selections or any selection rectangles
	 * contained within each other.
	 * 
	 * @see the related tests for a better understanding.
	 */
	public boolean isColumnFullySelected(int columnPosition, int fullySelectedColumnRowCount);

	public void addSelection(int columnPosition, int rowPosition);

	public void addSelection(final Rectangle range);

	public void clearSelection();

	public void removeSelection(Rectangle removedSelection);

	public void removeSelection(int columnPosition, int rowPosition);

	public boolean isEmpty();

	/**
	 * @return all selected rows in the model
	 */
	public Set<Range> getSelectedRows();

	public int getSelectedRowCount();

	public int[] getFullySelectedRows(int rowWidth);

	public int[] getSelectedColumns();

	/**
	 * Get the positions of all fully selected columns.
	 * @param fullySelectedColumnRowCount the number of rows in a fully selected column
	 * @return
	 */
	public int[] getFullySelectedColumns(int fullySelectedColumnRowCount);

	public List<Rectangle> getSelections();
	
}
