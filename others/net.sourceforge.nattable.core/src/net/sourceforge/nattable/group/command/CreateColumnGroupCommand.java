package net.sourceforge.nattable.group.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

public class CreateColumnGroupCommand extends AbstractContextFreeCommand implements IColumnGroupCommand {

	private final String columnGroupName;
	
	public CreateColumnGroupCommand(String columnGroupName) {
		this.columnGroupName = columnGroupName;	
	}
	
	public String getColumnGroupName() {
		return columnGroupName;
	}
	
}
