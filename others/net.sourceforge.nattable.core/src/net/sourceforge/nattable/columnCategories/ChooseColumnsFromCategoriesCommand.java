package net.sourceforge.nattable.columnCategories;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.command.AbstractContextFreeCommand;

import org.eclipse.swt.widgets.Shell;

public class ChooseColumnsFromCategoriesCommand extends AbstractContextFreeCommand {

	private final NatTable natTable;

	public ChooseColumnsFromCategoriesCommand(NatTable natTable) {
		this.natTable = natTable;
	}

	public NatTable getNatTable() {
		return natTable;
	}

	public Shell getShell() {
		return natTable.getShell();
	}

}
