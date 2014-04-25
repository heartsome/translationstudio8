package net.sourceforge.nattable.columnChooser;


import java.util.List;

import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

public interface ISelectionTreeListener {

	void itemsSelected(List<ColumnEntry> addedItems);

	void itemsRemoved(List<ColumnEntry> removedItems);

	/**
	 * If columns moved are adjacent to each other, they are grouped together.
	 * @param direction
	 * @param selectedColumnGroupEntries
	 */
	void itemsMoved(MoveDirectionEnum direction, List<ColumnGroupEntry> selectedColumnGroupEntries, List<ColumnEntry> movedColumnEntries, List<List<Integer>> fromPositions, List<Integer> toPositions);

	void itemsExpanded(ColumnGroupEntry columnGroupEntry);

	void itemsCollapsed(ColumnGroupEntry columnGroupEntry);
}
