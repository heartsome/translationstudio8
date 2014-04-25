package net.sourceforge.nattable.columnCategories;

import static net.sourceforge.nattable.columnChooser.ColumnChooserUtils.getHiddenColumnEntries;
import static net.sourceforge.nattable.columnChooser.ColumnChooserUtils.getVisibleColumnsEntries;
import static net.sourceforge.nattable.util.ObjectUtils.isNotNull;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.columnCategories.gui.ColumnCategoriesDialog;
import net.sourceforge.nattable.columnChooser.ColumnChooserUtils;
import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.coordinate.PositionUtil;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.reorder.command.ColumnReorderCommand;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommand;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.util.ObjectUtils;

public class ChooseColumnsFromCategoriesCommandHandler
	extends AbstractLayerCommandHandler<ChooseColumnsFromCategoriesCommand>
	implements IColumnCategoriesDialogListener {

	private final ColumnHideShowLayer columnHideShowLayer;
	private final ColumnHeaderLayer columnHeaderLayer;
	private final DataLayer columnHeaderDataLayer;
	private final ColumnCategoriesModel model;
	private ColumnCategoriesDialog dialog;

	public ChooseColumnsFromCategoriesCommandHandler(
				ColumnHideShowLayer columnHideShowLayer,
				ColumnHeaderLayer columnHeaderLayer,
				DataLayer columnHeaderDataLayer,
				ColumnCategoriesModel model) {
		super();
		this.columnHideShowLayer = columnHideShowLayer;
		this.columnHeaderLayer = columnHeaderLayer;
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		this.model = model;
	}

	@Override
	protected boolean doCommand(ChooseColumnsFromCategoriesCommand command) {
		dialog = new ColumnCategoriesDialog(
				command.getShell(),
				model,
				getHiddenColumnEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer),
				getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer));

		dialog.addListener(this);
		dialog.open();
		return true;
	}

	public Class<ChooseColumnsFromCategoriesCommand> getCommandClass() {
		return ChooseColumnsFromCategoriesCommand.class;
	}

	// Listen and respond to the dialog events

	public void itemsRemoved(List<Integer> removedColumnPositions) {
		ColumnChooserUtils.hideColumnPositions(removedColumnPositions, columnHideShowLayer);
		refreshDialog();
	}

	public void itemsSelected(List<Integer> addedColumnIndexes) {
		ColumnChooserUtils.showColumnIndexes(addedColumnIndexes, columnHideShowLayer);
		refreshDialog();
	}

	/**
	 * Moves the columns up or down by firing commands on the dialog.<br/>
	 *
	 * Individual columns are moved using the {@link ColumnReorderCommand}<br/>
	 * Contiguously selected columns are moved using the {@link MultiColumnReorderCommand}<br/>
	 */
	public void itemsMoved(MoveDirectionEnum direction, List<Integer> selectedPositions) {
		List<List<Integer>> fromPositions = PositionUtil.getGroupedByContiguous(selectedPositions);
		List<Integer> toPositions = getDestinationPositions(direction, fromPositions);

		for (int i = 0; i < fromPositions.size(); i++) {
			boolean multipleColumnsMoved = fromPositions.get(i).size() > 1;

			ILayerCommand command = null;
			if (!multipleColumnsMoved) {
				int fromPosition = fromPositions.get(i).get(0).intValue();
				int toPosition = toPositions.get(i);
				command = new ColumnReorderCommand(columnHideShowLayer, fromPosition, toPosition);
			} else if(multipleColumnsMoved){
				command = new MultiColumnReorderCommand(columnHideShowLayer, fromPositions.get(i), toPositions.get(i));
			}
			columnHideShowLayer.doCommand(command);
		}

		refreshDialog();
	}

	/**
	 * Calculates the destination positions taking into account the move direction
	 * and single/contiguous selection.
	 *
	 * @param selectedPositions grouped together if they are contiguous.
	 * 	Example: if 2,3,4, 9, 12 are selected, they are grouped as [[2, 3, 4], 9, 12]
	 * 		While moving up the destination position for [2, 3, 4] is 1
	 * 		While moving up the destination position for [2, 3, 4] is 6
	 */
	protected List<Integer> getDestinationPositions(MoveDirectionEnum direction, List<List<Integer>> selectedPositions) {
		List<Integer> destinationPositions = new ArrayList<Integer>();
		for (List<Integer> contiguousPositions : selectedPositions) {
			switch (direction) {
			case UP:
				destinationPositions.add(ObjectUtils.getFirstElement(contiguousPositions) - 1);
				break;
			case DOWN:
				destinationPositions.add(ObjectUtils.getLastElement(contiguousPositions) + 2);
			default:
				break;
			}
		}
		return destinationPositions;
	}

	private void refreshDialog() {
		if (isNotNull(dialog)) {
			dialog.refresh(
					ColumnChooserUtils.getHiddenColumnEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer),
					ColumnChooserUtils.getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer));
		}
	}

}
