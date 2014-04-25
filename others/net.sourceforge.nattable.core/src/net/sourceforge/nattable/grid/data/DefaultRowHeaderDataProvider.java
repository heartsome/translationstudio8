package net.sourceforge.nattable.grid.data;

import net.sourceforge.nattable.data.IDataProvider;

public class DefaultRowHeaderDataProvider implements IDataProvider {
	
	protected final IDataProvider bodyDataProvider;

	public DefaultRowHeaderDataProvider(IDataProvider bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}
	
	public int getColumnCount() {
		return 1;
	}

	public int getRowCount() {
		return bodyDataProvider.getRowCount();
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		return Integer.valueOf(rowIndex + 1);
	}

	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}
	
}
