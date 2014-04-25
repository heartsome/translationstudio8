package net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable;

import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.sourceforge.nattable.NatTable;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class StateOperation extends AbstractOperation {

	private List<String> rowIdList;

	private NatTable table;

	private XLFHandler handler;

	private String state;

	private Map<String, String> oldState;

	/**
	 * “修改状态“操作
	 * @param label
	 *            操作名
	 * @param natTable
	 *            NatTable对象
	 * @param rowIdList
	 *            RowId集合
	 * @param handler
	 *            XLFHandler对象
	 * @param state
	 *            状态值（Target节点的state属性的值）
	 */
	public StateOperation(String label, NatTable natTable, List<String> rowIdList, XLFHandler handler, String state) {
		super(label);
		IUndoContext context = (IUndoContext) natTable.getData(IUndoContext.class.getName());
		addContext(context);
		this.table = natTable;
		this.rowIdList = rowIdList;
		this.handler = handler;
		this.state = state;
		this.oldState = handler.getTgtPropValue(rowIdList, "state");
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (rowIdList != null && rowIdList.size() > 0) {
			handler.changeTgtPropValue(rowIdList, "state", state);
			if(state.equals("translated") || state.equals("new")){ //切换到已翻译或草稿状态
				handler.deleteTuProp(rowIdList, "approved");
			}
			if(state.equals("signed-off")){
				handler.changeTuPropValue(rowIdList, "approved", "yes");
			}
			table.redraw();
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (oldState != null && oldState.size() > 0) {
			handler.changeTgtPropValue(oldState, "state");
			table.redraw();
		}
		return Status.OK_STATUS;
	}

}
