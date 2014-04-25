package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;

public class DeleteAltTransOperation extends AbstractOperation {

	private final XLIFFEditorImplWithNatTable xliffEditor;

	private final XLFHandler handler;

	private final List<String> rowIds;

	private final Map<String, String> segmentCache;

	/**
	 * “批准”操作
	 * @param natTable
	 *            NatTable对象
	 * @param rowIdList
	 *            RowId集合
	 * @param handler
	 *            XLFHandler对象
	 * @param approve
	 *            true:批准,false:取消批准
	 */
	public DeleteAltTransOperation(XLIFFEditorImplWithNatTable xliffEditor, XLFHandler handler, List<String> rowIds) {
		super("Delete alt-trans");
		IUndoContext context = (IUndoContext) xliffEditor.getTable().getData(IUndoContext.class.getName());
		addContext(context);
		this.xliffEditor = xliffEditor;
		this.handler = handler;
		this.rowIds = rowIds;
		segmentCache = handler.getTuNodes(rowIds);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		handler.deleteAltTrans(rowIds);
		refreshViews();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		handler.resetTuNodes(segmentCache);
		refreshViews();
		return Status.OK_STATUS;
	}

	/**
	 * 刷新其他视图。 ;
	 */
	private void refreshViews() {
		ArrayList<Integer> rowList = new ArrayList<Integer>();
		int[] rows = xliffEditor.getSelectedRows();
		for (int i : rows) {			
			rowList.add(i);
		}
		StructuredSelection selection = new StructuredSelection(rowList);
		xliffEditor.getSite().getSelectionProvider().setSelection(selection);
	}

}
