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
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * 此类未使用，因此未做国际化
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ApproveOperation extends AbstractOperation {

	private List<String> rowIdList;

	private NatTable table;

	private XLFHandler handler;

	private boolean approve;

	/**
	 * “批准”操作
	 * @param label
	 *            操作名，标识
	 * @param natTable
	 *            NatTable对象
	 * @param rowIdList
	 *            RowId集合
	 * @param handler
	 *            XLFHandler对象
	 * @param approve
	 *            true:批准,false:取消批准
	 */
	public ApproveOperation(String label, NatTable natTable, List<String> rowIdList, XLFHandler handler, boolean approve) {
		super(label);
		IUndoContext context = (IUndoContext) natTable.getData(IUndoContext.class.getName());
		addContext(context);
		this.table = natTable;
		this.rowIdList = rowIdList;
		this.handler = handler;
		this.approve = approve;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		changeTransUnitBeanProperties(this.approve);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		changeTransUnitBeanProperties(!this.approve);
		return Status.OK_STATUS;
	}

	/**
	 * 设置TU对象的属性以及Target的属性
	 * @param approve
	 *            true:批准,false:取消批准;
	 */
	private void changeTransUnitBeanProperties(boolean approve) {
		List<String> rowIds;
		if (rowIdList == null) {
			rowIds = handler.approveAllTransUnits(approve);
		} else {
			rowIds = handler.approveTransUnits(rowIdList, approve);
		}
		if (rowIds.size() > 0) {
			String message;
			if (rowIdList != null && rowIdList.size() == 1) {
				message = "当前翻译的长度不在允许范围内。是否仍要批准？";
			} else {
				message = "有 " + rowIds.size() + " 个翻译的长度不在允许范围内。是否仍要批准？";
			}
			boolean res = MessageDialog.openQuestion(table.getShell(), null, message);
			if (res) {
				handler.approveTransUnits(rowIds, approve, false);
			}
		}
		table.redraw();
	}
}
