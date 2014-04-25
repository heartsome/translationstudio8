package net.sourceforge.nattable.grid.data;

import java.util.Map;

import net.sourceforge.nattable.data.IDataProvider;

public class DefaultColumnHeaderDataProvider implements IDataProvider {

	private final String[] propertyNames;

	private Map<String, String> propertyToLabelMap;

	public DefaultColumnHeaderDataProvider(final String[] columnLabels) {
		propertyNames = columnLabels;
	}

	public DefaultColumnHeaderDataProvider(final String[] propertyNames, Map<String, String> propertyToLabelMap) {
		this.propertyNames = propertyNames;
		this.propertyToLabelMap = propertyToLabelMap;
	}

	public String getColumnHeaderLabel(int columnIndex) {
		String propertyName = propertyNames[columnIndex];
		if (propertyToLabelMap != null) {
			String label = propertyToLabelMap.get(propertyName);
			if (label != null) {
				return label;
			}
		}
		return propertyName;
	}

	public int getColumnCount() {
		return propertyNames.length;
	}

	public int getRowCount() {
		return 1;
	}

	/**
	 * This class does not support multiple rows in the column header layer.
	 */
	public Object getDataValue(int columnIndex, int rowIndex) {
		return getColumnHeaderLabel(columnIndex);
	}

	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}

}
