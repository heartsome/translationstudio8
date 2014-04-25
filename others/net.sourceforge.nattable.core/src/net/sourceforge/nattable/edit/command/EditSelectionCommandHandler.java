package net.sourceforge.nattable.edit.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.edit.MultiCellEditController;
import net.sourceforge.nattable.selection.SelectionLayer;

import org.eclipse.swt.widgets.Composite;

public class EditSelectionCommandHandler extends AbstractLayerCommandHandler<EditSelectionCommand> {

	private SelectionLayer selectionLayer;
	
	public EditSelectionCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}
	
	public Class<EditSelectionCommand> getCommandClass() {
		return EditSelectionCommand.class;
	}
	
	public boolean doCommand(EditSelectionCommand command) {
		Composite parent = command.getParent();
		IConfigRegistry configRegistry = command.getConfigRegistry();
		Character initialValue = command.getCharacter();
		
		return MultiCellEditController.editSelectedCells(selectionLayer, initialValue, parent, configRegistry);
	}
}
