package net.sourceforge.nattable.data;

public interface IRowDataProvider<T> extends IDataProvider {

	public T getRowObject(int rowIndex);
	
	public int indexOfRowObject(T rowObject);
	
}
