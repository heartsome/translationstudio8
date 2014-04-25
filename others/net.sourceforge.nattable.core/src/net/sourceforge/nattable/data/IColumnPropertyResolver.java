package net.sourceforge.nattable.data;

/**
 * Maps between the column property name in the backing data bean
 * and its corresponding column index.
 */
public interface IColumnPropertyResolver {
	
	/**
	 * @param columnIndex i.e the order of the column in the backing bean
	 */
	public String getColumnProperty(int columnIndex);

	/**
	 * @param propertyName i.e the name of the column in the backing bean
	 */
	public int getColumnIndex(String propertyName);
	
}
