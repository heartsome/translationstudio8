package net.sourceforge.nattable.data;

/**
 * Maps the properties from the row object to the corresponding columns. 
 * @param <T> type of the bean used as a row object
 */
public interface IColumnAccessor<T> {

	public Object getDataValue(T rowObject, int columnIndex);
	
	public void setDataValue(T rowObject, int columnIndex, Object newValue);
	
	public int getColumnCount();
	
}
