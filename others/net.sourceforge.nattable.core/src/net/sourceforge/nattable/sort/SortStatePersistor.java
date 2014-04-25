package net.sourceforge.nattable.sort;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import net.sourceforge.nattable.persistence.IPersistable;

/**
 * Handles persisting of the sorting state.<br/>
 * The sorting state is read from and restored to the {@link ISortModel}.<br/>
 *
 * @param <T> Type of the Beans in the backing data source.
 */
public class SortStatePersistor<T> implements IPersistable {
	public static final String PERSISTENCE_KEY_SORTING_STATE = ".SortHeaderLayer.sortingState";
	private final SortHeaderLayer<T> sortHeaderLayer;
	private final ISortModel sortModel;

	public SortStatePersistor(SortHeaderLayer<T> sortHeaderLayer) {
		this.sortHeaderLayer = sortHeaderLayer;
		this.sortModel = sortHeaderLayer.getSortModel();
	}

	/**
	 * Save the sorting state in the properties file.<br/>
	 * Key:
	 * 	{@link #PERSISTENCE_KEY_SORTING_STATE}<br/>
	 *
	 * Format:<br/>
	 * 	column index : sort direction : sort order |
	 */
	public void saveState(String prefix, Properties properties) {
		int columnCount = sortHeaderLayer.getColumnCount();
		StringBuffer buffer = new StringBuffer();

		for (int columnPosition = 0; columnPosition < columnCount; columnPosition++) {
			int columnIndex = sortHeaderLayer.getColumnIndexByPosition(columnPosition);
			boolean isColumnSorted = sortModel.isColumnIndexSorted(columnIndex);

			if (isColumnSorted) {
				SortDirectionEnum sortDirection = sortModel.getSortDirection(columnIndex);
				int sortOrder = sortModel.getSortOrder(columnIndex);

				buffer.append(columnIndex);
				buffer.append(":");
				buffer.append(sortDirection.toString());
				buffer.append(":");
				buffer.append(sortOrder);
				buffer.append("|");
			}
		}
		if (isNotEmpty(buffer.toString())) {
			properties.put(prefix + PERSISTENCE_KEY_SORTING_STATE, buffer.toString());
		}
	}

	/**
	 * Parses the saved string and restores the state to the {@link ISortModel}.
	 */
	public void loadState(String prefix, Properties properties) {
		Object savedValue = properties.get(prefix + PERSISTENCE_KEY_SORTING_STATE);
		if(savedValue == null){
			return;
		}
		try{
			String savedState = savedValue.toString();
			String[] sortedColumns = savedState.split("\\|");
			List<SortState> stateInfo = new ArrayList<SortState>();

			// Parse string
			for (String token : sortedColumns) {
				stateInfo.add(getSortStateFromString(token));
			}

			// Order by the sort order
			Collections.sort(stateInfo, new SortStateComparator());

			// Restore to the model
			for (SortState state : stateInfo) {
				sortModel.sort(state.columnIndex, state.sortDirection, true);
			}
		}catch(Exception ex){
			sortModel.clear();
			System.err.println("Error while restoring sorting state. Skipping");
			ex.printStackTrace(System.err);
		}
	}

	/**
	 * Parse the string representation to extract the
	 * column index, sort direction and sort order
	 */
	protected SortState getSortStateFromString(String token) {
		String[] split = token.split(":");
		int columnIndex = Integer.parseInt(split[0]);
		SortDirectionEnum sortDirection = SortDirectionEnum.valueOf(split[1]);
		int sortOrder = Integer.parseInt(split[2]);

		return new SortState(columnIndex, sortDirection, sortOrder);
	}

	/**
	 * Encapsulation of the sort state of a column
	 */
	protected class SortState {
		public int columnIndex;
		public SortDirectionEnum sortDirection;
		public int sortOrder;

		public SortState(int columnIndex, SortDirectionEnum sortDirection, int sortOrder) {
			this.columnIndex = columnIndex;
			this.sortDirection = sortDirection;
			this.sortOrder = sortOrder;
		}
	}

	/**
	 * Helper class to order sorting state by the 'sort order'.
	 * The sorting state has be restored in the same sequence
	 * in which the original sort was applied.
	 */
	private class SortStateComparator implements Comparator<SortState> {

		public int compare(SortState state1, SortState state2) {
			return Integer.valueOf(state1.sortOrder).compareTo(Integer.valueOf(state2.sortOrder));
		}

	}
}
