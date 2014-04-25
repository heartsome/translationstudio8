package net.sourceforge.nattable.edit.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.edit.InlineCellEditController;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.eclipse.swt.widgets.Composite;

public class EditCellCommandHandler extends AbstractLayerCommandHandler<EditCellCommand> {

	public Class<EditCellCommand> getCommandClass() {
		return EditCellCommand.class;
	}

	@Override
	public boolean doCommand(EditCellCommand command) {
		LayerCell cell = command.getCell();
		Composite parent = command.getParent();
		IConfigRegistry configRegistry = command.getConfigRegistry();


		return InlineCellEditController.editCellInline(cell, null, parent, configRegistry);
	}

}