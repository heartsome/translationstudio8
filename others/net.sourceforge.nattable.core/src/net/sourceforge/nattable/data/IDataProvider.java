package net.sourceforge.nattable.data;

import net.sourceforge.nattable.layer.DataLayer;

/**
 * Provide data to the table.
 *
 * @see DataLayer
 * @see ListDataProvider
 */
public interface IDataProvider {

	/**
	 * Gets the value at the given column and row index.
	 *
	 * @param columnIndex
	 * @param rowIndex
	 * @return the data value associated with the specified cell
	 */
	public Object getDataValue(int columnIndex, int rowIndex);

	/**
	 * Sets the value at the given column and row index. Optional operation. Should throw UnsupportedOperationException
	 * if this operation is not supported.
	 *
	 * @param columnIndex
	 * @param rowIndex
	 * @param newValue
	 */
	public void setDataValue(int columnIndex, int rowIndex, Object newValue);

	public int getColumnCount();

	public int getRowCount();

}
