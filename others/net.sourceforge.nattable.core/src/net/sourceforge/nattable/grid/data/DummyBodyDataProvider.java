package net.sourceforge.nattable.grid.data;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

import net.sourceforge.nattable.data.IDataProvider;

public class DummyBodyDataProvider implements IDataProvider {

	private final int columnCount;
	
	private final int rowCount;

	private Map<Point, Object> values = new HashMap<Point, Object>();
	
	public DummyBodyDataProvider(int columnCount, int rowCount) {
		this.columnCount = columnCount;
		this.rowCount = rowCount;
	}
	
	public int getColumnCount() {
		return columnCount;
	}

	public int getRowCount() {
		return rowCount;
	}

	public Object getDataValue(int columnIndex, int rowIndex) {
		Point point = new Point(columnIndex, rowIndex);
		if (values.containsKey(point)) {
			return values.get(point);
		} else {
			return "Col: " + (columnIndex + 1) + ", Row: " + (rowIndex + 1);
		}
	}
	
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		values.put(new Point(columnIndex, rowIndex), newValue);
	}

}
