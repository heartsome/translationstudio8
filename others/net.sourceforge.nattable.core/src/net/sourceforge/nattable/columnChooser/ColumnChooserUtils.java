package net.sourceforge.nattable.columnChooser;

import static net.sourceforge.nattable.util.ObjectUtils.asIntArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.hideshow.command.MultiColumnHideCommand;
import net.sourceforge.nattable.hideshow.command.MultiColumnShowCommand;
import net.sourceforge.nattable.layer.DataLayer;

public class ColumnChooserUtils {

	public static final String RENAMED_COLUMN_INDICATOR = "*";

	public static void hideColumnEntries(List<ColumnEntry> removedItems, ColumnHideShowLayer hideShowLayer) {
		MultiColumnHideCommand hideCommand = new MultiColumnHideCommand(
				hideShowLayer, asIntArray(getColumnEntryPositions(removedItems)));
		hideShowLayer.doCommand(hideCommand);
	}

	public static void hideColumnPositions(List<Integer> removedPositions, ColumnHideShowLayer hideShowLayer) {
		MultiColumnHideCommand hideCommand = new MultiColumnHideCommand(hideShowLayer, asIntArray(removedPositions));
		hideShowLayer.doCommand(hideCommand);
	}

	public static void showColumnEntries(List<ColumnEntry> addedItems, ColumnHideShowLayer hideShowLayer) {
		hideShowLayer.doCommand(new MultiColumnShowCommand(asIntArray(getColumnEntryIndexes(addedItems))));
	}

	public static void showColumnIndexes(List<Integer> addedColumnIndexes, ColumnHideShowLayer hideShowLayer) {
		hideShowLayer.doCommand(new MultiColumnShowCommand(asIntArray(addedColumnIndexes)));
	}

	public static List<ColumnEntry> getHiddenColumnEntries(ColumnHideShowLayer columnHideShowLayer, ColumnHeaderLayer columnHeaderLayer, DataLayer columnHeaderDataLayer) {
		Collection<Integer> hiddenColumnIndexes = columnHideShowLayer.getHiddenColumnIndexes();
		ArrayList<ColumnEntry> hiddenColumnEntries= new ArrayList<ColumnEntry>();

		for (Integer hiddenColumnIndex : hiddenColumnIndexes) {
			String label = getColumnLabel(columnHeaderLayer, columnHeaderDataLayer, hiddenColumnIndex);
			ColumnEntry columnEntry = new ColumnEntry(label, hiddenColumnIndex, Integer.valueOf(-1));
			hiddenColumnEntries.add(columnEntry);
		}

		return hiddenColumnEntries;
	}

	/**
	 * @return The renamed column header name for the given column index (if the column has been renamed),<br/>
	 * 	the original column name otherwise.
	 */
	public static String getColumnLabel(ColumnHeaderLayer columnHeaderLayer, DataLayer columnHeaderDataLayer, Integer columnIndex) {
		String label = "";
		if (columnHeaderLayer.isColumnRenamed(columnIndex)) {
			label = columnHeaderLayer.getRenamedColumnLabelByIndex(columnIndex) + RENAMED_COLUMN_INDICATOR;
		} else {
			int position = columnHeaderDataLayer.getColumnPositionByIndex(columnIndex.intValue());
			label = columnHeaderDataLayer.getDataValueByPosition(position, 0).toString();
		}
		return label;
	}

	/**
	 * Get all visible columns from the selection layer and the corresponding labels in the header
	 */
	public static List<ColumnEntry> getVisibleColumnsEntries(ColumnHideShowLayer columnHideShowLayer, ColumnHeaderLayer columnHeaderLayer, DataLayer columnHeaderDataLayer) {
		int visibleColumnCount = columnHideShowLayer.getColumnCount();
		ArrayList<ColumnEntry> visibleColumnEntries= new ArrayList<ColumnEntry>();

		for (int i = 0; i < visibleColumnCount; i++) {
			int index = columnHideShowLayer.getColumnIndexByPosition(i);
			String label = getColumnLabel(columnHeaderLayer, columnHeaderDataLayer, index);
			ColumnEntry columnEntry = new ColumnEntry(label, Integer.valueOf(index), Integer.valueOf(i));
			visibleColumnEntries.add(columnEntry);
		}
		return visibleColumnEntries;
	}

	/**
	 * Search the collection for the entry with the given index.
	 */
	public static ColumnEntry find(List<ColumnEntry> entries, int indexToFind) {
		for (ColumnEntry columnEntry : entries) {
			if(columnEntry.getIndex().equals(indexToFind)){
				return columnEntry;
			}
		}
		return null;
	}

	/**
	 * Get ColumnEntry positions for the ColumnEntry objects.
	 */
	public static List<Integer> getColumnEntryPositions(List<ColumnEntry> columnEntries) {
		List<Integer> columnEntryPositions = new ArrayList<Integer>();
		for (ColumnEntry columnEntry : columnEntries) {
			columnEntryPositions.add(columnEntry.getPosition());
		}
		return columnEntryPositions;
	}

	/**
	 * Get ColumnEntry positions for the ColumnEntry objects.
	 */
	public static List<Integer> getColumnEntryIndexes(List<ColumnEntry> columnEntries) {
		List<Integer> columnEntryIndexes = new ArrayList<Integer>();
		for (ColumnEntry columnEntry : columnEntries) {
			columnEntryIndexes.add(columnEntry.getIndex());
		}
		return columnEntryIndexes;
	}

	/**
	 * @return TRUE if the list contains an entry with the given index
	 */
	public static boolean containsIndex(List<ColumnEntry> entries, int indexToFind) {
		return find(entries, indexToFind) != null;
	}

}
