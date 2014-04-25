package net.sourceforge.nattable.columnChooser;

import java.util.List;

import net.sourceforge.nattable.columnChooser.gui.ColumnChooserDialog;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.group.ColumnGroupHeaderLayer;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.command.ColumnGroupExpandCollapseCommand;
import net.sourceforge.nattable.group.command.ReorderColumnGroupCommand;
import net.sourceforge.nattable.group.command.ReorderColumnsAndGroupsCommand;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.reorder.command.ColumnReorderCommand;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommand;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

import org.eclipse.swt.widgets.Shell;

public final class ColumnChooser {

	private final ColumnChooserDialog columnChooserDialog;
	private final ColumnHideShowLayer columnHideShowLayer;
	private final DataLayer columnHeaderDataLayer;
	private final ColumnHeaderLayer columnHeaderLayer;
	private List<ColumnEntry> hiddenColumnEntries;
	private List<ColumnEntry> visibleColumnsEntries;
	private final ColumnGroupModel columnGroupModel;
	private final SelectionLayer selectionLayer;

	public ColumnChooser(Shell shell,
			SelectionLayer selectionLayer,
			ColumnHideShowLayer columnHideShowLayer,
			ColumnHeaderLayer columnHeaderLayer,
			DataLayer columnHeaderDataLayer,
			ColumnGroupHeaderLayer columnGroupHeaderLayer,
			ColumnGroupModel columnGroupModel) {
		this.selectionLayer = selectionLayer;
		this.columnHideShowLayer = columnHideShowLayer;
		this.columnHeaderLayer = columnHeaderLayer;
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		this.columnGroupModel = columnGroupModel;

		columnChooserDialog = new ColumnChooserDialog(shell, "Available Columns", "Selected Columns");
	}

	public void openDialog() {
		columnChooserDialog.create();

		hiddenColumnEntries = ColumnChooserUtils.getHiddenColumnEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);
		columnChooserDialog.populateAvailableTree(hiddenColumnEntries, columnGroupModel);

		visibleColumnsEntries = ColumnChooserUtils.getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);
		columnChooserDialog.populateSelectedTree(visibleColumnsEntries, columnGroupModel);

		columnChooserDialog.expandAllLeaves();

		addListenersOnColumnChooserDialog();
		columnChooserDialog.open();
	}

	private void addListenersOnColumnChooserDialog() {

		columnChooserDialog.addListener(new ISelectionTreeListener() {

			public void itemsRemoved(List<ColumnEntry> removedItems) {
				ColumnChooserUtils.hideColumnEntries(removedItems, columnHideShowLayer);
				refreshColumnChooserDialog();
			}

			public void itemsSelected(List<ColumnEntry> addedItems) {
				ColumnChooserUtils.showColumnEntries(addedItems, columnHideShowLayer);
				refreshColumnChooserDialog();
			}

			public void itemsMoved(MoveDirectionEnum direction, List<ColumnGroupEntry> movedColumnGroupEntries, List<ColumnEntry> movedColumnEntries, List<List<Integer>> fromPositions, List<Integer> toPositions) {
				moveItems(direction, movedColumnGroupEntries, movedColumnEntries, fromPositions, toPositions);
			}

			/**
			 * Fire appropriate commands depending on the events received from the column chooser dialog
			 * @param direction
			 * @param movedColumnGroupEntries
			 * @param movedColumnEntries
			 * @param fromPositions
			 * @param toPositions
			 */
			private void moveItems(MoveDirectionEnum direction, List<ColumnGroupEntry> movedColumnGroupEntries, List<ColumnEntry> movedColumnEntries, List<List<Integer>> fromPositions, List<Integer> toPositions) {

				for (int i = 0; i < fromPositions.size(); i++) {
					boolean columnGroupMoved = columnGroupMoved(fromPositions.get(i), movedColumnGroupEntries);
					boolean multipleColumnsMoved = fromPositions.get(i).size() > 1;

					ILayerCommand command = null;
					if (!columnGroupMoved && !multipleColumnsMoved) {
						int fromPosition = fromPositions.get(i).get(0).intValue();
						int toPosition = adjustToPosition(direction, toPositions.get(i).intValue());
						command = new ColumnReorderCommand(columnHideShowLayer, fromPosition, toPosition);
					} else if(columnGroupMoved && multipleColumnsMoved){
							command = new ReorderColumnsAndGroupsCommand(columnHideShowLayer, fromPositions.get(i), adjustToPosition(direction, toPositions.get(i)));
					} else if(!columnGroupMoved && multipleColumnsMoved){
						command = new MultiColumnReorderCommand(columnHideShowLayer, fromPositions.get(i), adjustToPosition(direction, toPositions.get(i)));
					} else if(columnGroupMoved && !multipleColumnsMoved){
						command = new ReorderColumnGroupCommand(columnHideShowLayer, fromPositions.get(i).get(0), adjustToPosition(direction, toPositions.get(i)));
					}
					columnHideShowLayer.doCommand(command);
				}

				refreshColumnChooserDialog();
				columnChooserDialog.setSelectionIncludingNested(ColumnChooserUtils.getColumnEntryIndexes(movedColumnEntries));
			}

			private int adjustToPosition(MoveDirectionEnum direction, Integer toColumnPosition) {
				if (MoveDirectionEnum.DOWN == direction) {
					return toColumnPosition + 1;
				} else {
					return toColumnPosition;
				}
			}

			private boolean columnGroupMoved(List<Integer> fromPositions, List<ColumnGroupEntry> movedColumnGroupEntries) {
				for (ColumnGroupEntry columnGroupEntry : movedColumnGroupEntries) {
					if(fromPositions.contains(columnGroupEntry.getFirstElementPosition())) return true;
				}
				return false;
			}

			public void itemsCollapsed(ColumnGroupEntry columnGroupEntry) {
				int index = columnGroupEntry.getFirstElementIndex().intValue();
				int position = selectionLayer.getColumnPositionByIndex(index);
				selectionLayer.doCommand(new ColumnGroupExpandCollapseCommand(selectionLayer, position));
			}

			public void itemsExpanded(ColumnGroupEntry columnGroupEntry) {
				int index = columnGroupEntry.getFirstElementIndex().intValue();
				int position = selectionLayer.getColumnPositionByIndex(index);
				selectionLayer.doCommand(new ColumnGroupExpandCollapseCommand(selectionLayer, position));
			}
		});
	}

	private void refreshColumnChooserDialog() {
		hiddenColumnEntries = ColumnChooserUtils.getHiddenColumnEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);
		visibleColumnsEntries = ColumnChooserUtils.getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);

		columnChooserDialog.removeAllLeaves();

		columnChooserDialog.populateSelectedTree(visibleColumnsEntries, columnGroupModel);
		columnChooserDialog.populateAvailableTree(hiddenColumnEntries, columnGroupModel);
		columnChooserDialog.expandAllLeaves();
	}

}
