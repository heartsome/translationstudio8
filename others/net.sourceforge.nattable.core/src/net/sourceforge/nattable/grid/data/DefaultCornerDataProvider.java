package net.sourceforge.nattable.grid.data;

import net.sourceforge.nattable.data.IDataProvider;

public class DefaultCornerDataProvider implements IDataProvider {
	
	private final IDataProvider columnHeaderDataProvider;
	private final IDataProvider rowHeaderDataProvider;

	public DefaultCornerDataProvider(IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider) {
		this.columnHeaderDataProvider = columnHeaderDataProvider;
		this.rowHeaderDataProvider = rowHeaderDataProvider;
	}
	
	public int getColumnCount() {
		return rowHeaderDataProvider.getColumnCount();
	}

	public int getRowCount() {
		return columnHeaderDataProvider.getRowCount();
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		return null;
	}
	
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}

}
