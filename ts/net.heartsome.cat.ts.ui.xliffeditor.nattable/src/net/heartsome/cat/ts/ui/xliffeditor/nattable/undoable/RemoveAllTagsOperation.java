package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import java.util.HashMap;
import java.util.List;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.NatTable;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class RemoveAllTagsOperation extends AbstractOperation {

	private XLFHandler handler;
	
	private List<String> rowIds;
	
	private HashMap<String, String> map;

	private NatTable table;

	public RemoveAllTagsOperation(String label, NatTable table, XLFHandler handler, List<String> rowIds) {
		super(label);
		IUndoContext context = (IUndoContext) table.getData(IUndoContext.class.getName());
		addContext(context);
		this.table = table;
		this.handler = handler;
		this.rowIds = rowIds;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		map = handler.removeAllTags(rowIds);
		refreshNatTable();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		handler.resetRemoveAllTags(map);
		refreshNatTable();
		return Status.OK_STATUS;
	}

	private void refreshNatTable() {
		if (XLIFFEditorImplWithNatTable.getCurrent() != null) {
			XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
			xliffEditor.autoResize();
			xliffEditor.refresh();
		}
	}

}
