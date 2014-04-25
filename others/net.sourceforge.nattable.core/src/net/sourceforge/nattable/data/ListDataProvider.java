package net.sourceforge.nattable.data;

import java.util.List;

/**
 * Enables the use of a {@link List} containing POJO(s) as a backing data source.
 * 
 * By default a bean at position 'X' in the list is displayed in 
 * row 'X' in the table. The properties of the bean are used to 
 * populate the columns. A {@link IColumnPropertyResolver} is used to
 * retrieve column data from the bean properties. 
 *
 * @param <T> type of the Objects in the backing list.
 * @see IColumnPropertyResolver
 */
public class ListDataProvider<T> implements IRowDataProvider<T> {

	protected List<T> list;
	protected IColumnAccessor<T> columnAccessor;
	
	public ListDataProvider(List<T> list, IColumnAccessor<T> columnAccessor) {
		this.list = list;
		this.columnAccessor = columnAccessor;
	}
	
	public int getColumnCount() {
		return columnAccessor.getColumnCount();
	}
	
	public int getRowCount() {
		return list.size();
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		T rowObj = list.get(rowIndex);
		return columnAccessor.getDataValue(rowObj, columnIndex);
	}
	
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		T rowObj = list.get(rowIndex);
		columnAccessor.setDataValue(rowObj, columnIndex, newValue);
	}

	public T getRowObject(int rowIndex) {
		return list.get(rowIndex);
	}
	
	public int indexOfRowObject(T rowObject) {
		return list.indexOf(rowObject);
	}
	
	public List<T> getList() {
		return list;
	}
	
}
