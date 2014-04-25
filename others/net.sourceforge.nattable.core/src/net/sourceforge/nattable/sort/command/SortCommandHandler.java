package net.sourceforge.nattable.sort.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.sort.ISortModel;
import net.sourceforge.nattable.sort.SortDirectionEnum;
import net.sourceforge.nattable.sort.SortHeaderLayer;
import net.sourceforge.nattable.sort.event.SortColumnEvent;

import org.eclipse.swt.custom.BusyIndicator;

/**
 * Handle sort commands
 */
public class SortCommandHandler<T> extends AbstractLayerCommandHandler<SortColumnCommand> {

	private final ISortModel sortModel;
	private final SortHeaderLayer<T> sortHeaderLayer;

	public SortCommandHandler(ISortModel sortModel, SortHeaderLayer<T> sortHeaderLayer) {
		this.sortModel = sortModel;
		this.sortHeaderLayer = sortHeaderLayer;
	}

	@Override
	public boolean doCommand(final SortColumnCommand command) {

		final int columnIndex = command.getLayer().getColumnIndexByPosition(command.getColumnPosition());
		final SortDirectionEnum newSortDirection = sortModel.getSortDirection(columnIndex).getNextSortDirection();

		// Fire command - with busy indicator
		Runnable sortRunner = new Runnable() {
			public void run() {
				sortModel.sort(columnIndex, newSortDirection, command.isAccumulate());
			}
		};
		BusyIndicator.showWhile(null, sortRunner);

		// Fire event
		SortColumnEvent sortEvent = new SortColumnEvent(sortHeaderLayer, command.getColumnPosition());
		sortHeaderLayer.fireLayerEvent(sortEvent);

		return true;
	}

	public Class<SortColumnCommand> getCommandClass() {
		return SortColumnCommand.class;
	}

}