package net.sourceforge.nattable.columnChooser.command;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.command.AbstractContextFreeCommand;

public class DisplayColumnChooserCommand extends AbstractContextFreeCommand {

	private final NatTable natTable;

	public DisplayColumnChooserCommand(NatTable natTable) {
		this.natTable = natTable;
	}
	
	public NatTable getNatTable() {
		return natTable;
	}
	
}
