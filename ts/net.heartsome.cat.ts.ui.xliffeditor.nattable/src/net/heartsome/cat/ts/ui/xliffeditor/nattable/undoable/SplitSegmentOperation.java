package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import java.util.Arrays;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.ActiveCellRegion;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.event.RowSelectionEvent;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class SplitSegmentOperation extends AbstractOperation {

	private XLIFFEditorImplWithNatTable xliffEditor;

	private XLFHandler handler;

	private int offset;

	private String tuFragment;

	private int rowIndex;

	private String rowId;

	public SplitSegmentOperation(String label, XLIFFEditorImplWithNatTable xliffEditor, XLFHandler handler,
			int rowIndex, int offset) {
		super(label);
		this.xliffEditor = xliffEditor;
		IUndoContext context = (IUndoContext) xliffEditor.getTable().getData(IUndoContext.class.getName());
		addContext(context);
		this.handler = handler;
		this.offset = offset;
		this.rowIndex = rowIndex;
		this.rowId = handler.getRowId(rowIndex);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		tuFragment = handler.splitSegment(rowId, offset);
		refreshNatTable();
		xliffEditor.jumpToRow(rowId + "-1");
		xliffEditor.refresh();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		handler.resetSplitSegment(rowId, tuFragment);
		refreshNatTable();
		xliffEditor.jumpToRow(rowId);
		xliffEditor.refresh();
		return Status.OK_STATUS;
	}

	private void refreshNatTable() {
		SelectionLayer selectionLayer = LayerUtil.getLayer(xliffEditor.getTable(), SelectionLayer.class);
		int rowPosition = selectionLayer.getRowPositionByIndex(rowIndex);
		selectionLayer.fireLayerEvent(new RowSelectionEvent(selectionLayer, Arrays.asList(new Integer[] { rowPosition,
				rowPosition + 1 })));
		xliffEditor.autoResizeNotColumn();
		ActiveCellRegion.setActiveCellRegion(null);
	}
}
