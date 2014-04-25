package net.heartsome.cat.ts.ui.xliffeditor.nattable.layer;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.grid.command.ClientAreaResizeCommand;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;
import net.sourceforge.nattable.selection.command.ScrollSelectionCommand;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * 垂直布局使用的ViewportLayer
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class VerticalViewportLayer extends ViewportLayer {

	private ScrollBar vBar; // 垂直滚动条

	public VerticalViewportLayer(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
	}

	RowHeightCalculator rowHeightCalculator;

	public void setRowHeightCalculator(RowHeightCalculator rowHeightCalculator) {
		this.rowHeightCalculator = rowHeightCalculator;
	}

	@Override
	protected MoveSelectionCommand scrollVerticallyByAPageCommand(ScrollSelectionCommand scrollSelectionCommand) {
		int rowCount = getRowCount();
		
		return new MoveSelectionCommand(scrollSelectionCommand.getDirection(), rowCount,
				scrollSelectionCommand.isShiftMask(), scrollSelectionCommand.isControlMask());
	}

	@Override
	public void moveRowPositionIntoViewport(int scrollableRowPosition, boolean forceEntireCellIntoViewport) {
		ILayer underlyingLayer = getUnderlyingLayer();
		if (underlyingLayer.getRowIndexByPosition(scrollableRowPosition) >= 0) {
			if (scrollableRowPosition >= getMinimumOriginRowPosition()) {
				int originRowPosition = getOriginRowPosition(); // 滚动条滚动过的行数

				if (scrollableRowPosition < originRowPosition) {
					// Move up
					if(scrollableRowPosition %2 != 0){
						scrollableRowPosition -=1;
					}
					setOriginRowPosition(scrollableRowPosition);
				} else {
					scrollableRowPosition = scrollableRowPosition / VerticalNatTableConfig.ROW_SPAN
							* VerticalNatTableConfig.ROW_SPAN;

					int clientAreaHeight = getClientAreaHeight(); // 编辑区的高（不包括滚动过的区域）
					int viewportEndY = underlyingLayer.getStartYOfRowPosition(originRowPosition) + clientAreaHeight; // 编辑区的高（包括滚动过的区域）

					int scrollableRowStartY = underlyingLayer.getStartYOfRowPosition(scrollableRowPosition); // 当前选中行的起始位置（包括滚动过的区域）

					if (clientAreaHeight <= 0) {
						return;
					}
					int currentRowHeight = 0;
					for (int i = 0; i < VerticalNatTableConfig.ROW_SPAN; i++) {
						if (rowHeightCalculator != null)
							rowHeightCalculator.recaculateRowHeight(scrollableRowPosition + i);
						int currentSubRowHeight = underlyingLayer.getRowHeightByPosition(scrollableRowPosition + i);
						currentRowHeight += currentSubRowHeight;
						if (currentRowHeight >= clientAreaHeight) {
							setOriginRowPosition(scrollableRowPosition + i);
							return;
						}
					}
					int scrollableRowEndY = scrollableRowStartY + currentRowHeight; // 当前选中行的结束位置，包括不可见的部分（包括滚动过的区域）

					if (viewportEndY < scrollableRowEndY) { // 选中行下半部分没有显示完全时
						if (currentRowHeight >= clientAreaHeight) { // 当前行高大于等于编辑区的高
							// Move up：设置起始行为当前行
							setOriginRowPosition(scrollableRowPosition);
						} else {
							// int targetOriginRowPosition = underlyingLayer.getRowPositionByY(scrollableRowEndY
							// - clientAreaHeight) + 1;
							// // targetOriginRowPosition = targetOriginRowPosition
							// // + (VerticalNatTableConfig.ROW_SPAN - targetOriginRowPosition %
							// VerticalNatTableConfig.ROW_SPAN);
							// // Move up：将当前选中行显示完全
							// setOriginRowPosition(targetOriginRowPosition);
							scrollableRowPosition = scrollableRowPosition
									+ (VerticalNatTableConfig.ROW_SPAN - scrollableRowPosition
											% VerticalNatTableConfig.ROW_SPAN);
							int ch = clientAreaHeight;
							int r = scrollableRowPosition;
							for (; r > 0; r--) {
								int h = rowHeightCalculator.recaculateRowHeight(r);
								ch -= h;
								if (ch < 0) {
									break;
								}
							}
							r += 1;
							if(r %2 == 0){
								r -=1;
							}
							setOriginRowPosition(r);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		boolean b = super.doCommand(command);
		if (command instanceof ClientAreaResizeCommand && command.convertToTargetLayer(this)) {
			ClientAreaResizeCommand clientAreaResizeCommand = (ClientAreaResizeCommand) command;
			if (vBar == null) {
				vBar = clientAreaResizeCommand.getScrollable().getVerticalBar(); // 垂直滚动条
				Listener[] listeners = vBar.getListeners(SWT.Selection);
				for (Listener listener : listeners) { // 清除默认的 Selection 监听（在类 ScrollBarHandlerTemplate 中添加的）
					vBar.removeListener(SWT.Selection, listener);
				}
				vBar.addListener(SWT.Selection, new Listener() {

					// private int lastPosition = 0;
					// private int lastSelection = 0;

					private IUniqueIndexLayer scrollableLayer = VerticalViewportLayer.this.getScrollableLayer();

					public void handleEvent(Event event) {

						// 滚动滚动条前提交当前处于编辑状态的文本段
						// ActiveCellEditor.commit();
						// ScrollBar scrollBar = (ScrollBar) event.widget;
						//
						// int selection = scrollBar.getSelection();
						// int position = scrollableLayer.getRowPositionByY(selection);
						//
						// if (lastSelection == selection) {
						// return;
						// }
						// HsMultiActiveCellEditor.commit(true);
						//
						// VerticalViewportLayer.this.invalidateVerticalStructure(); // 清除缓存
						// lastPosition = position / VerticalNatTableConfig.ROW_SPAN * VerticalNatTableConfig.ROW_SPAN;
						// VerticalViewportLayer.this.setOriginRowPosition(lastPosition);
						// vBar.setIncrement(getRowHeightByPosition(0) * VerticalNatTableConfig.ROW_SPAN); //
						// 设置滚动条增量为两倍行高
						//
						// lastSelection = selection;
						//
						HsMultiActiveCellEditor.commit(true);
						ScrollBar scrollBar = (ScrollBar) event.widget;
						int position = scrollableLayer.getRowPositionByY(scrollBar.getSelection());
						VerticalViewportLayer.this.invalidateVerticalStructure();
						VerticalViewportLayer.this.setOriginRowPosition(position);
						scrollBar.setIncrement(VerticalViewportLayer.this.getRowHeightByPosition(0));
						HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.getCurrent());
					}
				});
			}
		}
		return b;
	}

}
