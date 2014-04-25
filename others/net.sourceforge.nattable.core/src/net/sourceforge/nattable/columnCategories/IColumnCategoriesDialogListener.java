package net.sourceforge.nattable.columnCategories;

import java.util.List;

import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

public interface IColumnCategoriesDialogListener {

	void itemsSelected(List<Integer> addedColumnIndexes);

	void itemsRemoved(List<Integer> removedColumnPositions);

	void itemsMoved(MoveDirectionEnum direction, List<Integer> selectedPositions);

}
