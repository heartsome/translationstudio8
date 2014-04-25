package net.sourceforge.nattable.edit;

import net.sourceforge.nattable.edit.command.UpdateDataCommand;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

public class SingleEditHandler implements ICellEditHandler {

	private final ICellEditor cellEditor;
	private final ILayer layer;
	private final int columnPosition;
	private final int rowPosition;

	public SingleEditHandler(ICellEditor cellEditor, ILayer layer, int columnPosition, int rowPosition) {
		this.cellEditor = cellEditor;
		this.layer = layer;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
	}
	
	/**
	 * {@inheritDoc}
 	 * Note: Assumes that the value is valid.<br/>
	 */
	public boolean commit(MoveDirectionEnum direction, boolean closeEditorAfterCommit) {
		Object canonicalValue = cellEditor.getCanonicalValue();
		switch (direction) {
		case LEFT:
			layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, 1, false, false));
			break;
		case RIGHT:
			layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, 1, false, false));
			break;
		}
		boolean committed = layer.doCommand(new UpdateDataCommand(layer, columnPosition, rowPosition, canonicalValue));
		if(committed && closeEditorAfterCommit){
			cellEditor.close();
			return true;
		}
		return committed;
	}
	
}