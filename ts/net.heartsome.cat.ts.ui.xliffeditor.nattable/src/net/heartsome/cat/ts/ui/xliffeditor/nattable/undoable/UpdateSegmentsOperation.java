package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.sourceforge.nattable.NatTable;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateSegmentsOperation extends AbstractOperation {

	/** 日志 */
	Logger LOGGER = LoggerFactory.getLogger(UpdateSegmentsOperation.class);

	/** XLIFF 编辑器 */
	private final XLIFFEditorImplWithNatTable xliffEditor;

	/** NatTable */
	private final NatTable table;

	/** XLIFF 处理类 */
	private final XLFHandler handler;

	/** 缓存的未修改前的文本段 */
	private final Map<String, String> segmentCache;

	/** 做修改的行的 ID 集合 */
	private final List<String> rowIds;

	/** Key：修改的行的 Id；value：新值 */
	private Map<String, String> map;

	/** 新值 */
	private String newValue;

	private String matchType;

	private String quality;

	/** 是否需要批准文本段 */
	private final boolean approved;

	/**
	 * 修改文本段的操作
	 * @param xliffEditor
	 *            XLIFF 编辑器
	 * @param handler
	 *            XLIFF 文件的处理类
	 * @param map
	 *            Key：修改的行的 Id；value：新值
	 * @param columnIndex
	 *            列索引
	 * @param approved
	 *            是否需要批准文本段
	 */
	public UpdateSegmentsOperation(XLIFFEditorImplWithNatTable xliffEditor, XLFHandler handler,
			Map<String, String> map, int columnIndex, boolean approved, String matchType, String quality) {
		this(xliffEditor, handler, new ArrayList<String>(map.keySet()), columnIndex, approved, matchType, quality);
		this.map = map;
	}

	/**
	 * 修改文本段的操作
	 * @param xliffEditor
	 *            XLIFF 编辑器
	 * @param handler
	 *            XLIFF 文件的处理类
	 * @param rowIds
	 *            做修改的行的 ID 集合
	 * @param columnIndex
	 *            列索引
	 * @param newValue
	 *            新值
	 * @param approved
	 *            是否需要批准文本段
	 */
	public UpdateSegmentsOperation(XLIFFEditorImplWithNatTable xliffEditor, XLFHandler handler, List<String> rowIds,
			int columnIndex, String newValue, boolean approved, String matchType, String quality) {
		this(xliffEditor, handler, rowIds, columnIndex, approved, matchType, quality);
		this.newValue = newValue;
	}

	private UpdateSegmentsOperation(XLIFFEditorImplWithNatTable xliffEditor, XLFHandler handler, List<String> rowIds,
			int column, boolean approved, String matchType, String quality) {
		super("Update Segments");
		this.xliffEditor = xliffEditor;
		this.table = xliffEditor.getTable();
		IUndoContext undoContext = (IUndoContext) table.getData(IUndoContext.class.getName());
		addContext(undoContext); // 绑定上下文
		this.handler = handler;
		this.rowIds = rowIds;
		this.approved = approved;
		segmentCache = handler.getTuNodes(rowIds); // 缓存未修改前的文本段
		this.matchType = matchType;
		this.quality = quality;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		// updateCellEditor();
		Assert.isNotNull(rowIds, Messages.getString("undoable.UpdateSegmentsOperation.msg1"));
		if (rowIds.size() == 0 || segmentCache == null || segmentCache.size() == 0) {
			return Status.CANCEL_STATUS;
		}

		if (newValue != null) {
			handler.changeTgtTextValue(rowIds, newValue, matchType, quality);
		} else if (map != null) {
			handler.changeTgtTextValue(map, matchType, quality);
		} else {
			return Status.CANCEL_STATUS;
		}
		if (approved) { // 批准文本段
			List<String> rowids = handler.approveTransUnits(rowIds, true);
			if (rowids.size() > 0) {
				boolean res = MessageDialog.openQuestion(table.getShell(), null, MessageFormat.format(
						Messages.getString("undoable.UpdateSegmentsOperation.msg2"), rowIds.size()));
				if (res) {
					handler.approveTransUnits(rowIds, true, false);
				}
			}
		}
		int[] selectedRowIndexs = xliffEditor.getSelectedRows();
		// 修改
		if (!xliffEditor.isHorizontalLayout()) {
			int[] temp = new int[selectedRowIndexs.length * 2];
			int j = 0;
			for (int i = 0; i < selectedRowIndexs.length; i++) {
				int sel = selectedRowIndexs[i] + 1;
				temp[j++] = sel * 2;
				temp[j++] = sel * 2 + 1;
			}
//			table.doCommand(new AutoResizeCurrentRowsCommand(table, temp, table.getConfigRegistry()));
		} else {
			int[] temp = new int[selectedRowIndexs.length];
			int j = 0;
			for (int i = 0; i < selectedRowIndexs.length; i++) {
				int sel = selectedRowIndexs[i] + 1;
				temp[j++] = sel;
			}
//			table.doCommand(new AutoResizeCurrentRowsCommand(table, temp, table.getConfigRegistry()));
			
//			HsMultiActiveCellEditor.recalculateCellsBounds();
		}
		table.redraw();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if(table == null || table.isDisposed()){
			return Status.CANCEL_STATUS;
		}
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Assert.isNotNull(segmentCache, Messages.getString("undoable.UpdateSegmentsOperation.msg3"));

		if(table == null || table.isDisposed()){
			return Status.CANCEL_STATUS;
		}
		handler.resetTuNodes(segmentCache); // 重置为缓存的未修改前的文本段
		xliffEditor.refresh();
		return Status.OK_STATUS;
	}

	// 同步修改 Cell Editor 中的值，暂时保留
	// /**
	// * 修改单元格编辑器中的文本。
	// * @param newValue
	// * @return ;
	// */
	// private boolean updateCellEditor() {
	// int rowIndex = ActiveCellEditor.getRowIndex();
	// if (rowIndex == -1) {
	// return false;
	// }
	// ICellEditor cellEditor = ActiveCellEditor.getCellEditor();
	// if (cellEditor == null) {
	// return false;
	// }
	// if (cellEditor instanceof StyledTextCellEditor) {
	// StyledTextCellEditor editor = (StyledTextCellEditor) cellEditor;
	// if (!editor.isClosed()) {
	// String rowId = handler.getRowId(rowIndex);
	// if (newValue != null && rowIds.contains(rowId)) {
	// editor.setCanonicalValue(newValue);
	// return true;
	// } else if (map != null) {
	// editor.setCanonicalValue(map.get(rowId));
	// return true;
	// }
	// }
	// }
	// return false;
	// }
}
