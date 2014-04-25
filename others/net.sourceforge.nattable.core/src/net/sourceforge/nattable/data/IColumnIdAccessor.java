package net.sourceforge.nattable.data;

/**
 * Maps between column indexes and id(s).
 * 	A column id is a unique identifier for a column.
 */
public interface IColumnIdAccessor {

	public String getColumnId(int columnIndex);

	public int getColumnIndex(String columnId);

}
