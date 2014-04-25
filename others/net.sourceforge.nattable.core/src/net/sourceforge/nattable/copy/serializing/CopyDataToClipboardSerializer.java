package net.sourceforge.nattable.copy.serializing;

import net.sourceforge.nattable.copy.command.CopyDataToClipboardCommand;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.serializing.ISerializer;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class CopyDataToClipboardSerializer implements ISerializer {

	private final LayerCell[][] copiedCells;
	private final CopyDataToClipboardCommand command;

	public CopyDataToClipboardSerializer(LayerCell[][] copiedCells, CopyDataToClipboardCommand command) {
		this.copiedCells = copiedCells;
		this.command = command;
	}
	
	public void serialize() {
		final Clipboard clipboard = command.getClipboard();
		final String cellDelimeter = command.getCellDelimeter();
		final String rowDelimeter = command.getRowDelimeter();
		
		final TextTransfer textTransfer = TextTransfer.getInstance();
		final StringBuilder textData = new StringBuilder();
		int currentRow = 0;
		for (LayerCell[] cells : copiedCells) {
			int currentCell = 0;
			for (LayerCell cell : cells) {
				final String delimeter = ++currentCell < cells.length ? cellDelimeter : "";
				if (cell != null) {
					textData.append(cell.getDataValue() + delimeter);
				} else {
					textData.append(delimeter);
				} 
			}
			if (++currentRow < copiedCells.length) {
				textData.append(rowDelimeter);
			}
		}
		clipboard.setContents(new Object[]{textData.toString()}, new Transfer[]{textTransfer});
	}
}
