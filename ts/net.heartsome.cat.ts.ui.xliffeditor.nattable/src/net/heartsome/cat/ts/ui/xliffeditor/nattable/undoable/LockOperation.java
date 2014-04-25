package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import java.util.List;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.sourceforge.nattable.NatTable;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class LockOperation extends AbstractOperation {

	private List<String> rowIdList;

	private NatTable table;

	private XLFHandler handler;

	private boolean lock;

	/**
	 * 
	 * @param label
	 * @param natTable
	 * @param rowIdList
	 * @param handler
	 * @param lock
	 */
	public LockOperation(String label, NatTable natTable, List<String> rowIdList, XLFHandler handler,
			boolean lock) {
		super(label);
		IUndoContext context = (IUndoContext) natTable.getData(IUndoContext.class.getName());
		addContext(context);
		this.table = natTable;
		this.rowIdList = rowIdList;
		this.handler = handler;
		this.lock = lock;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		changeTransUnitBeanProperties(this.lock);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		changeTransUnitBeanProperties(!this.lock);
		return Status.OK_STATUS;
	}

	/**
	 * 设置TU对象的属性以及Target的属性
	 * @param tu
	 * @param tuProps
	 * @param tgtProps
	 *            ;
	 */
	private void changeTransUnitBeanProperties(boolean lock) {
		if (rowIdList == null) {
			handler.lockAllTransUnits(lock);
		} else {
			handler.lockTransUnits(rowIdList, lock);
		}
		table.redraw();
	}

}
