package net.sourceforge.nattable.coordinate;

public final class IndexCoordinate {
	
	public final int columnIndex;
	
	public final int rowIndex;

	public IndexCoordinate(int columnIndex, int rowIndex) {
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}
	
	public int getRowIndex() {
		return rowIndex;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + columnIndex + "," + rowIndex + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof IndexCoordinate == false) {
			return false;
		}
		
		IndexCoordinate that = (IndexCoordinate) obj;
		return this.getColumnIndex() == that.getColumnIndex()
			&& this.getRowIndex() == that.getRowIndex();
	}
	
	@Override
	public int hashCode() {
		int hash = 95;
		hash = 35 * hash + getColumnIndex();
		hash = 35 * hash + getRowIndex() + 87;
		return hash;
	}
}
