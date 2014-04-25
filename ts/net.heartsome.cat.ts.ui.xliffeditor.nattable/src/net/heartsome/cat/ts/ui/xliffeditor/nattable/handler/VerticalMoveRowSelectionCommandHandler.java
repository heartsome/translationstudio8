package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.selection.MoveRowSelectionCommandHandler;
import net.sourceforge.nattable.selection.SelectionLayer;

/**
 * 垂直布局下，处理选择行移动的 Handler。 (用户通过键盘的方向键移动选中行时，以三行为一个单位移动)
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class VerticalMoveRowSelectionCommandHandler extends MoveRowSelectionCommandHandler {

	public VerticalMoveRowSelectionCommandHandler(SelectionLayer selectionLayer) {
		super(selectionLayer);
	}

	@Override
	protected void moveLastSelectedUp(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (stepSize > 0) {
			stepSize = stepSize * VerticalNatTableConfig.ROW_SPAN; // 增大到所跨行数倍
		}
		super.moveLastSelectedUp(stepSize, withShiftMask, withControlMask);
		XLIFFEditorImplWithNatTable.getCurrent().updateStatusLine();
	}

	@Override
	protected void moveLastSelectedDown(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (stepSize > 0) {
			stepSize = stepSize * VerticalNatTableConfig.ROW_SPAN; // 增大到所跨行数倍
		}
		super.moveLastSelectedDown(stepSize, withShiftMask, withControlMask);
		XLIFFEditorImplWithNatTable.getCurrent().updateStatusLine();
	}

}
