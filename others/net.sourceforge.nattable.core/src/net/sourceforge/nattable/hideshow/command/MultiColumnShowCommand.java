package net.sourceforge.nattable.hideshow.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

public class MultiColumnShowCommand extends AbstractContextFreeCommand{

	private final int[] columnIndexes;

	public MultiColumnShowCommand(int[] columnIndexes) {
		this.columnIndexes = columnIndexes;
	}

	protected MultiColumnShowCommand(MultiColumnShowCommand command) {
		columnIndexes = new int[command.columnIndexes.length];
		System.arraycopy(command.columnIndexes, 0, columnIndexes, 0, command.columnIndexes.length);
	}

	public int[] getColumnIndexes() {
		return columnIndexes;
	}

}