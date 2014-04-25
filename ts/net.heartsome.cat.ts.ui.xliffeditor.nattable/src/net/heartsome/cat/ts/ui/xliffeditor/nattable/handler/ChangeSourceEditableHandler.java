package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.EditableManager;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.SourceEditMode;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.NatTableConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.style.DisplayMode;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 设置“是否允许编辑源文本”状态
 * @author weachy
 * @since JDK1.5
 */
public abstract class ChangeSourceEditableHandler extends AbstractHandler {

	private Listener listener = new Listener() {

		public void handleEvent(Event event) {
			StyledTextCellEditor sce = (StyledTextCellEditor) event.data;
			if (sce.getCellType().equals(NatTableConstant.SOURCE)) {
				EditableManager editableManager = sce.getEditableManager();
				if (editableManager.getSourceEditMode() == SourceEditMode.ONCE_EDITABLE) {
					editableManager.setSourceEditMode(SourceEditMode.DISEDITABLE);
					// TODO 设置默认模式图片
//					element.setIcon(Activator.getImageDescriptor(SourceEditMode.DISEDITABLE.getImagePath()));
				}
			}
		}
	};

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor != null && editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			ICellEditor cellEditor = xliffEditor.getTable().getConfigRegistry().getConfigAttribute(
					EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, XLIFFEditorImplWithNatTable.SOURCE_EDIT_CELL_LABEL);
			if (cellEditor == null || !(cellEditor instanceof StyledTextCellEditor)) {
				return null;
			}
			HsMultiActiveCellEditor.commit(false);
			StyledTextCellEditor sce = (StyledTextCellEditor) cellEditor;
			EditableManager editableManager = sce.getEditableManager();
//			SourceEditMode nextMode = editableManager.getSourceEditMode().getNextMode();
			SourceEditMode nextMode = getSourceEditMode(editableManager);
			editableManager.setSourceEditMode(nextMode);
//			element.setIcon(Activator.getImageDescriptor(nextMode.getImagePath()));
			if (!sce.isClosed()) {
				editableManager.judgeEditable();
//				更新全局 Action 的可用状态，主要是更新编辑-删除功能的可用状态。
				sce.getActionHandler().updateActionsEnableState();
			}
			
			sce.addClosingListener(listener);
		}
		return null;
	}
	
	public abstract SourceEditMode getSourceEditMode(EditableManager editableManager);
}
