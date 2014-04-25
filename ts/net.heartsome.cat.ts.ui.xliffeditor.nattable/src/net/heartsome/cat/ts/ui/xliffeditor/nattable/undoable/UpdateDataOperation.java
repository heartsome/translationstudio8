package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import net.heartsome.cat.ts.ui.innertag.ISegmentViewer;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.UpdateDataBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.AutoResizeCurrentRowsCommand;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.UpdateDataAndAutoResizeCommand;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.event.CellVisualChangeEvent;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class UpdateDataOperation extends AbstractOperation {

	private final Object oldValue;

	private final DataLayer dataLayer;

	private final UpdateDataAndAutoResizeCommand command;

	private final NatTable table;

	private final ViewportLayer viewportLayer;

	public UpdateDataOperation(NatTable table, UpdateDataAndAutoResizeCommand command) {
		this(table, LayerUtil.getLayer(table, DataLayer.class), command);
	}

	public UpdateDataOperation(NatTable table, DataLayer dataLayer, UpdateDataAndAutoResizeCommand command) {
		super("Typing");
		IUndoContext undoContext = (IUndoContext) table.getData(IUndoContext.class.getName());
		addContext(undoContext);
		Object currentValue = dataLayer.getDataProvider().getDataValue(command.getColumnPosition(), command.getRowPosition());
//		Object currentValue = dataLayer.getDataValueByPosition(command2.getColumnPosition(), command2.getRowPosition());
		oldValue = currentValue == null ? new UpdateDataBean() : new UpdateDataBean((String) currentValue, null, null);
		this.dataLayer = dataLayer;
		this.command = command;
		this.table = table;
		viewportLayer = LayerUtil.getLayer(table, ViewportLayer.class);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Object newValue = command.getNewValue() == null ? new UpdateDataBean() : command.getNewValue();
		if (((UpdateDataBean) newValue).getText().equals(((UpdateDataBean) oldValue).getText())) { // 值相同，则取消操作
			int currentRow = command.getRowPosition() + 1 /* 列头占一行 */; // 修改行在当前一屏显示的几行中的相对位置
			table.doCommand(new AutoResizeCurrentRowsCommand(table, new int[] { currentRow }, table.getConfigRegistry()));
			return Status.CANCEL_STATUS;
		}		
		return refreshNatTable(newValue, false);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return refreshNatTable(command.getNewValue(), true);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return refreshNatTable(oldValue, true);
	}

	/**
	 * 更新 NatTable 的 UI
	 * @param value
	 *            单元格保存的值
	 * @param move
	 *            是否移动单元格到指定区域;
	 */
	private IStatus refreshNatTable(Object value, boolean move) {
		if (table == null || table.isDisposed()) {
			return Status.CANCEL_STATUS;
		}
		int rowIndex = command.getRowPosition();
		int columnIndex = command.getColumnPosition();
		int columnPosition = viewportLayer.getColumnPositionByIndex(columnIndex);
		int rowPosition = viewportLayer.getRowPositionByIndex(rowIndex);
		// 实质上 DataLayer 层的 index 和 position 是一致的，此方法可以对范围判断
		if (rowIndex == -1 || columnIndex == -1) {
			return Status.CANCEL_STATUS;
		}
		
		// 修改值并刷新 UI。
		dataLayer.getDataProvider().setDataValue(columnIndex, rowIndex, value);
		dataLayer.fireLayerEvent(new CellVisualChangeEvent(dataLayer, columnPosition, rowPosition));
		
		
		int currentRow = rowPosition + 1 /* 列头占一行 */; // 修改行在当前一屏显示的几行中的相对位置
		table.doCommand(new AutoResizeCurrentRowsCommand(table, new int[] { currentRow }, table.getConfigRegistry()));

		int selectedRow = XLIFFEditorImplWithNatTable.getCurrent().getSelectedRows()[0];
		if (value instanceof UpdateDataBean & rowIndex == selectedRow) {
			UpdateDataBean bean = (UpdateDataBean) value;
			StyledTextCellEditor sourceCellEditor = HsMultiActiveCellEditor.getSourceStyledEditor();
			StyledTextCellEditor targetCellEditor = HsMultiActiveCellEditor.getTargetStyledEditor();
			if(sourceCellEditor != null && sourceCellEditor.getRowIndex() == rowIndex && sourceCellEditor.getColumnIndex() == columnIndex){
				ISegmentViewer segviewer = sourceCellEditor.getSegmentViewer();
				if (segviewer != null) {
					segviewer.setText(bean.getText());
				}				
			} else if (targetCellEditor != null && targetCellEditor.getRowIndex() == rowIndex && targetCellEditor.getColumnIndex() == columnIndex){
				ISegmentViewer segviewer = targetCellEditor.getSegmentViewer();
				if (segviewer != null) {
					segviewer.setText(bean.getText());
				}	
			}
		}
		
		// 先记录下可见区域的范围
		int originRowPosition = viewportLayer.getOriginRowPosition();
		int rowCount = viewportLayer.getRowCount(); // 总行数

		int row = LayerUtil.convertRowPosition(dataLayer, rowPosition, viewportLayer);
		// 此操作会自动调整选中单元格进入可见区域

		if (move) { // 定位到屏幕第三行的位置
			if (rowPosition < originRowPosition || rowPosition > originRowPosition + rowCount) {
				HsMultiActiveCellEditor.commit(true);
				viewportLayer.doCommand(new SelectCellCommand(viewportLayer, columnPosition, row, false, false));
				HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.getCurrent());
			} else {
				XLIFFEditorImplWithNatTable.getCurrent().jumpToRow(rowIndex);
			}
		}
		return Status.OK_STATUS;
	}

}
