package net.sourceforge.nattable.columnRename;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.persistence.IPersistable;
import net.sourceforge.nattable.util.PersistenceUtils;

public class RenameColumnHelper implements IPersistable {

	public static final String PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS = ".renamedColumnHeaders";

	private final ColumnHeaderLayer columnHeaderLayer;

	/** Tracks the renamed labels provided by the users */
	private Map<Integer, String> renamedColumnsLabelsByIndex = new TreeMap<Integer, String>();

	public RenameColumnHelper(ColumnHeaderLayer columnHeaderLayer) {
		this.columnHeaderLayer = columnHeaderLayer;
	}

	/**
	 * Rename the column at the given position.<br/>
	 * Note: This does not change the underlying column name.
	 *
	 * @return
	 */
	public boolean renameColumnPosition(int columnPosition, String customColumnName) {
		int index = columnHeaderLayer.getColumnIndexByPosition(columnPosition);
		if (index >= 0) {
			if (customColumnName == null) {
				renamedColumnsLabelsByIndex.remove(index);
			} else {
				renamedColumnsLabelsByIndex.put(index, customColumnName);
			}
			return true;
		}
		return false;
	}

	/**
	 * @return the custom label for this column as specified by the user
	 * 	Null if the columns is not renamed
	 */
	public String getRenamedColumnLabel(int columnIndex) {
		return renamedColumnsLabelsByIndex.get(columnIndex);
	}

	/**
	 * @return TRUE if the column has been renamed
	 */
	public boolean isColumnRenamed(int columnIndex) {
		return renamedColumnsLabelsByIndex.get(columnIndex) != null;
	}

	public boolean isAnyColumnRenamed() {
		return renamedColumnsLabelsByIndex.size() > 0;
	}

	public void loadState(String prefix, Properties properties) {
		Object property = properties.get(prefix + PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS);

		try {
			renamedColumnsLabelsByIndex = PersistenceUtils.parseString(property);
		} catch (Exception e) {
			System.err.println("Error while restoring renamed column headers: " + e.getMessage());
			System.err.println("Skipping restore.");
			renamedColumnsLabelsByIndex.clear();
		}
	}

	public void saveState(String prefix, Properties properties) {
		String string = PersistenceUtils.mapAsString(renamedColumnsLabelsByIndex);
		if (!isEmpty(string)) {
			properties.put(prefix + PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS, string);
		}
	}
}
