package net.sourceforge.nattable.grid.data;

import net.sourceforge.nattable.data.IDataProvider;

public class DummyColumnHeaderDataProvider implements IDataProvider {

	private final IDataProvider bodyDataProvider;

	public DummyColumnHeaderDataProvider(IDataProvider bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}
	
	public int getColumnCount() {
		return bodyDataProvider.getColumnCount();
	}

	public int getRowCount() {
		return 1;
	}
	
	public Object getDataValue(int columnIndex, int rowIndex) {
		return "Column " + (columnIndex + 1);
	}
	
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}

}
