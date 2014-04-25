/**
 * HsMultiCellEditorHandler.java
 *
 * Version information :
 *
 * Date:2012-12-18
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.UpdateDataAndAutoResizeCommand;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HsMultiCellEditorHandler implements ICellEditHandler {

	private final StyledTextCellEditor cellEditor;
	private final ILayer layer;

	public HsMultiCellEditorHandler(StyledTextCellEditor cellEditor, ILayer layer) {
		this.cellEditor = cellEditor;
		this.layer = layer;
	}

	/**
	 * Just commit the data, will not close the editor control
	 * @see net.sourceforge.nattable.edit.ICellEditHandler#commit(net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum, boolean)
	 */
	public boolean commit(MoveDirectionEnum direction, boolean closeEditorAfterCommit) {
		switch (direction) {
		case LEFT:
			layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, 1, false, false));
			break;
		case RIGHT:
			layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, 1, false, false));
			break;
		}
		
		if (cellEditor.isEditable()) {
			Object canonicalValue = cellEditor.getCanonicalValue();
			DataLayer datalayer = LayerUtil.getLayer((CompositeLayer)layer, DataLayer.class);
			datalayer.doCommand(new UpdateDataAndAutoResizeCommand(layer, cellEditor.getColumnIndex(), cellEditor.getRowIndex(), canonicalValue));
//			layer.doCommand(new UpdateDataCommand(layer, cellEditor.getColumnPosition(), cellEditor.getRowPosition(), canonicalValue));
		}		
		return true;
	}

}
