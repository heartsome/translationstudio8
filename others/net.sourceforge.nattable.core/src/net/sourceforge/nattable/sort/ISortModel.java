package net.sourceforge.nattable.sort;

import net.sourceforge.nattable.sort.command.SortCommandHandler;

/**
 * Interface providing sorting functionality.
 */
public interface ISortModel {

	/**
	 * @return TRUE if the column with the given index is sorted at the moment.
	 */
	public boolean isColumnIndexSorted(int columnIndex);

	/**
	 * @return the direction in which the column with the given index is<br/>
	 * currently sorted
	 */
	public SortDirectionEnum getSortDirection(int columnIndex);

	/**
	 * @return when multiple columns are sorted, this returns the order of the<br/>
	 * column index in the sort<br/>
	 *
	 * Example: If column indexes 3, 6, 9 are sorted (in that order) the sort order<br/>
	 * for index 6 is 1.
	 */
	public int getSortOrder(int columnIndex);

	/**
	 * This method is called by the {@link SortCommandHandler} in response to a sort command.<br/>
	 * It is responsible for sorting the requested column. <br/>
	 *
	 * @param accumulate flag indicating if the column should added to a previous sort.
	 */
	public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate);

	/**
	 * Remove all sorting
	 */
	public void clear();

}
