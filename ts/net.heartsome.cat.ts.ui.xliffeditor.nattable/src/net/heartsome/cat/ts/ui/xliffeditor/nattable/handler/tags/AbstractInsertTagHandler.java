package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.tags;

import net.heartsome.cat.ts.ui.innertag.ISegmentViewer;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 插入标记
 * @author weachy
 * @version
 * @since JDK1.5
 */
public abstract class AbstractInsertTagHandler extends AbstractHandler {

	/** 单元格标记器 */
	protected StyledTextCellEditor cellEditor;

	/** ExecutionEvent 对象 */
	protected ExecutionEvent event;

	/** source 内部标记中的最大索引 */
	protected int sourceMaxTagIndex;

	protected ExecutionEvent getEvent() {
		return event;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.event = event;
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
			return null; // 不是针对 XLIFFEditorImplWithNatTable 编辑器中的操作，则退出
		}
		cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
		if(cellEditor == null){
			return null;
		}
//		ICellEditor iCellEditor = HsMultiActiveCellEditor.getTargetEditor().getCellEditor();
//				//ActiveCellEditor.getCellEditor();
//		if (!(iCellEditor instanceof StyledTextCellEditor)) {
//			return null; // 不是 StyledTextCellEditor 实例，则退出
//		}
//
//		cellEditor = (StyledTextCellEditor) iCellEditor;
		if (!cellEditor.isEditable()) {
			cellEditor.showUneditableMessage(); // 显示不可编辑提示信息。
			return null; // 不可编辑，则退出
		}

		ISegmentViewer segmentViewer = cellEditor.getSegmentViewer();
		int caretOffset = segmentViewer.getTextWidget().getCaretOffset(); // 插入位置
		if (caretOffset < 0) {
			return null; // 文本框已经关闭，则退出
		}

		sourceMaxTagIndex = segmentViewer.getSourceMaxTagIndex(); // source 内部标记中的最大索引
		if (sourceMaxTagIndex <= 0) { // source 无内部标记。
			return null;
		}

		int num = getTagNum();
		if (num < 0 || num > sourceMaxTagIndex) {
			return null;
		}

		cellEditor.getSegmentViewer().insertInnerTag(num, caretOffset);
		return null;
	}

	protected abstract int getTagNum();
}
