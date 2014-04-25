package net.sourceforge.nattable.copy.command;

import org.eclipse.swt.dnd.Clipboard;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

public class CopyDataToClipboardCommand extends AbstractContextFreeCommand {

	private final Clipboard clipboard;
	private final String cellDelimeter;
	private final String rowDelimeter;

	public CopyDataToClipboardCommand(Clipboard clipboard, String cellDelimeter, String rowDelimeter) {
		this.clipboard = clipboard;
		this.cellDelimeter = cellDelimeter;
		this.rowDelimeter = rowDelimeter;
		
	}
	
	public Clipboard getClipboard() {
		return clipboard;
	}
	
	public String getCellDelimeter() {
		return cellDelimeter;
	}
	
	public String getRowDelimeter() {
		return rowDelimeter;
	}
}
