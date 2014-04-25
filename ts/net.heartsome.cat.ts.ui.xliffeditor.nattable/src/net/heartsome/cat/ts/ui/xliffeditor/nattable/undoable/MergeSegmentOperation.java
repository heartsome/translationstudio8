package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 给合并文本段添加撤销与重做功能
 * @author robert	2012-11-06
 */
public class MergeSegmentOperation extends AbstractOperation {

	private XLIFFEditorImplWithNatTable xliffEditor;

	private XLFHandler handler;

	/** 要进行合并的所有文本段的 rowId 的集合 */
	private List<String> rowIdList;
	
	private Map<String, String> oldSegFragMap;

	public MergeSegmentOperation(String label, XLIFFEditorImplWithNatTable xliffEditor, XLFHandler handler, List<String> rowIdList) {
		super(label);
		this.xliffEditor = xliffEditor;
		NatTable table = xliffEditor.getTable();
		IUndoContext context = (IUndoContext) table.getData(IUndoContext.class.getName());
		addContext(context);
		this.rowIdList = rowIdList;
		this.handler = handler;
		oldSegFragMap = new HashMap<String, String>();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		// 先获取合并前所有文本段的 content
		HsMultiActiveCellEditor.commit(true);
		for(String rowId : rowIdList){
			oldSegFragMap.put(rowId, handler.getTUFragByRowId(rowId));
		}
		
		for (int i = rowIdList.size() - 2; i >= 0; i--) {
			handler.mergeSegment(rowIdList.get(i), rowIdList.get(i + 1));
		}
		
		// Bug #2373:选择全部文本段合并后，无显示内容
//		xliffEditor.refresh();
		
		// 合并文本段后自动调整大小
		xliffEditor.autoResizeNotColumn();
		xliffEditor.jumpToRow(rowIdList.get(0));//[13-04-24]:austen 定位
		xliffEditor.refresh();
		xliffEditor.updateStatusLine();

		//[13-04-24]R8中无显示模式，合并文本段后定位不准猜测是由其引起的。故删除--> 见 readmine #2982
		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		handler.resetMergeSegment(oldSegFragMap);
		xliffEditor.autoResizeNotColumn();
		return Status.OK_STATUS;
	}

}
