package net.sourceforge.nattable.edit.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.eclipse.swt.widgets.Composite;

public class EditCellCommand extends AbstractContextFreeCommand {
	
	private final IConfigRegistry configRegistry;
	private final Composite parent;
	private LayerCell cell;

	public EditCellCommand(Composite parent, IConfigRegistry configRegistry, LayerCell cell) {
		this.configRegistry = configRegistry;
		this.parent = parent;
		this.cell = cell;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public Composite getParent() {
		return parent;
	}

	public LayerCell getCell() {
		return cell;
	}
	
}
