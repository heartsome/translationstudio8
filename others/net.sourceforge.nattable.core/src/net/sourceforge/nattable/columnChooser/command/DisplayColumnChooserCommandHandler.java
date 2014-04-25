package net.sourceforge.nattable.columnChooser.command;

import net.sourceforge.nattable.columnChooser.ColumnChooser;
import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.group.ColumnGroupHeaderLayer;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.selection.SelectionLayer;

public class DisplayColumnChooserCommandHandler extends AbstractLayerCommandHandler<DisplayColumnChooserCommand> {

	private final ColumnHideShowLayer columnHideShowLayer;
	private final ColumnGroupHeaderLayer columnGroupHeaderLayer;
	private final ColumnGroupModel columnGroupModel;
	private final SelectionLayer selectionLayer;
	private final DataLayer columnHeaderDataLayer;
	private final ColumnHeaderLayer columnHeaderLayer;

	public DisplayColumnChooserCommandHandler(
			SelectionLayer selectionLayer,
			ColumnHideShowLayer columnHideShowLayer,
			ColumnHeaderLayer columnHeaderLayer,
			DataLayer columnHeaderDataLayer,
			ColumnGroupHeaderLayer cgHeader, ColumnGroupModel columnGroupModel) {

		this.selectionLayer = selectionLayer;
		this.columnHideShowLayer = columnHideShowLayer;
		this.columnHeaderLayer = columnHeaderLayer;
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		this.columnGroupHeaderLayer = cgHeader;
		this.columnGroupModel = columnGroupModel;
	}

	@Override
	public boolean doCommand(DisplayColumnChooserCommand command) {
		ColumnChooser columnChooser = new ColumnChooser(
				command.getNatTable().getShell(),
				selectionLayer,
				columnHideShowLayer,
				columnHeaderLayer,
				columnHeaderDataLayer,
				columnGroupHeaderLayer,
				columnGroupModel);

		columnChooser.openDialog();
		return true;
	}

	public Class<DisplayColumnChooserCommand> getCommandClass() {
		return DisplayColumnChooserCommand.class;
	}

}
