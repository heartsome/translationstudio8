package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import java.util.ArrayList;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 翻译菜单下，锁定文本段－－＞锁定文本段的 handler
 * @author robert 2012-05-02 修改
 * @version
 * @since JDK1.6
 */
public class LockSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 这是之前的实现方式，这是锁定文本段。锁定全部文本段，锁定可编辑文本段的共同 handler，--robert 2012-05-02
		// String lockSegment = event.getParameter("lockSegment");
		// if (lockSegment == null) {
		// return null;
		// }
		// State state = event.getCommand().getState(RegistryToggleState.STATE_ID);
		// boolean isSelect = (Boolean) (state.getValue());
		// state.setValue(!isSelect);
		// IEditorPart editor = HandlerUtil.getActiveEditor(event);
		// if (editor instanceof XLIFFEditorImplWithNatTable) {
		// XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
		//
		// // 批准可编辑文本段时要初始化 rowIds ，批准所有文本段则不需要
		// if (lockSegment.equals("lockDuplicateSegment")) {
		// // TODO 解决如何高效获取重复文本段后再做
		//
		// } else {
		// ArrayList<String> rowIds = null;
		// if (lockSegment.equals("lockEditableSegment")) {
		// rowIds = xliffEditor.getXLFHandler().getRowIds();
		// } else if (lockSegment.equals("lockAllSegment")) {
		// rowIds = null;
		// }
		// NattableUtil util = new NattableUtil(xliffEditor);
		// util.lockTransUnits(rowIds, !isSelect);
		// // 改变单元格编辑器模式为“只读”
		// util.changeCellEditorMode(rowIds, null);
		// // TODO 入库
		//
		// }
		//
		// }

		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			XLFHandler handler = xliffEditor.getXLFHandler();

			ArrayList<String> rowIds = (ArrayList<String>) xliffEditor.getSelectedRowIds();
			ArrayList<String> needLockRowIds = new ArrayList<String>();
			// 先判断所有的选择文本段的是否锁定状态
			for (String rowId : rowIds) {
				if (!handler.isLocked(rowId)) {
					needLockRowIds.add(rowId);
				}
			}
			NattableUtil util = NattableUtil.getInstance(xliffEditor);
			// 如果都是锁定状态的，那么把它们都变成未锁定
			if (needLockRowIds.size() <= 0) {
				util.lockTransUnits(rowIds, false);
			} else {
				util.lockTransUnits(needLockRowIds, true);
				// 改变单元格编辑器模式为“只读”
				// util.changeCellEditorMode(rowIds, null);
			}
			HsMultiActiveCellEditor.refrushCellsEditAbility();
		}
		return null;
	}

}
