/**
 * HsMultiActiveCellEditor.java
 *
 * Version information :
 *
 * Date:2012-12-17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.print.command.TurnViewportOffCommand;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HsMultiActiveCellEditor {
	private static NatTable parent;
	private static HsMultiCellEditor sourceEditor;
	private static HsMultiCellEditor targetEditor;

	public static int sourceRowPosition = -1;
	public static int sourceRowIndex = -1;

	public static int targetRowPosition = -1;
	public static int targetRowIndex = -1;

	/**
	 * @param cellEditors
	 */
	public static void activateCellEditors(HsMultiCellEditor srcCellEditor, HsMultiCellEditor tgtCellEditor,
			NatTable natTable) {
		parent = natTable;
		sourceEditor = srcCellEditor;
		sourceRowIndex = srcCellEditor.getRowIndex();
		sourceRowPosition = srcCellEditor.getRowPosition();

		targetEditor = tgtCellEditor;
		targetRowIndex = tgtCellEditor.getRowIndex();
		targetRowPosition = tgtCellEditor.getRowPosition();

		sourceEditor.activeCurrentEditor(natTable);

		targetEditor.activeCurrentEditor(natTable);

		SelectionLayer sLayer = LayerUtil.getLayer(natTable, SelectionLayer.class);
		PositionCoordinate p = sLayer.getLastSelectedCellPosition();
		int colPosition = p.columnPosition;
		if (colPosition == targetEditor.getColumnIndex()) {
			targetEditor.forceFocus();
		} else if (colPosition == sourceEditor.getColumnIndex()) {
			sourceEditor.forceFocus();
		}
	}

	public static void commit(boolean closeEditorAfterCommit) {
		if (sourceEditor != null && sourceEditor.isValid()) {
			sourceEditor.getEditHandler().commit(MoveDirectionEnum.NONE, closeEditorAfterCommit);
		}

		if (targetEditor != null && targetEditor.isValid()) {
			targetEditor.getEditHandler().commit(MoveDirectionEnum.NONE, closeEditorAfterCommit);
		}
		if (closeEditorAfterCommit) {
			close();
		}
	}

	private static void close() {
		parent = null;

		if (sourceEditor != null) {
			sourceEditor.close();
			sourceEditor = null;
		}
		if (targetEditor != null) {
			targetEditor.close();
			targetEditor = null;
		}

		sourceRowPosition = -1;
		sourceRowIndex = -1;

		targetRowPosition = -1;
		targetRowIndex = -1;
	}

	public static StyledTextCellEditor getFocusCellEditor() {
		if (sourceEditor != null && sourceEditor.isFocus() && sourceEditor.isValid()) {
			return sourceEditor.getCellEditor();
		} else if (targetEditor != null && targetEditor.isFocus() && sourceEditor.isValid()) {
			return targetEditor.getCellEditor();
		}
		return null;
	}

	public static void setCellEditorForceFocusByIndex(int colIndex, int rowIndex) {
		if (sourceEditor != null && sourceEditor.isValid() && sourceEditor.getColumnIndex() == colIndex
				&& sourceEditor.getRowIndex() == rowIndex) {
			sourceEditor.forceFocus();
		} else if (targetEditor != null && targetEditor.isValid() && targetEditor.getColumnIndex() == colIndex
				&& targetEditor.getRowIndex() == rowIndex) {
			targetEditor.forceFocus();
		}
	}

	public static void setCellEditorForceFocus(int colPosition, int rowPosition) {
		if (sourceEditor != null && sourceEditor.isValid() && sourceEditor.getColumnPosition() == colPosition
				&& sourceEditor.getRowPosition() == rowPosition) {
			sourceEditor.forceFocus();
		} else if (targetEditor != null && targetEditor.isValid() && targetEditor.getColumnPosition() == colPosition
				&& targetEditor.getRowPosition() == rowPosition) {
			targetEditor.forceFocus();
		}
	}

	public static void setSelectionText(StyledTextCellEditor editor, int start, int length) {
		if (editor != null && !editor.isClosed()) {
			StyledText text = editor.viewer.getTextWidget();
			text.setSelection(start, start + length);
		}
	}

	public static void synchronizeRowHeight() {
		if (parent == null || sourceEditor == null || !sourceEditor.isValid() || targetEditor == null
				|| !targetEditor.isValid()) {
			return;
		}
		if (sourceRowIndex != targetRowIndex) { // 垂直模式
			StyledTextCellEditor focusCell = getFocusCellEditor();
			if (sourceEditor.getCellEditor() == focusCell) {
				// edit source
				int srcHeight = sourceEditor.computeSize().y;
				int srcColPosition = sourceEditor.getColumnPosition();
				int srcRowPosition = sourceEditor.getRowPosition();
				int srcRowIndex = sourceEditor.getRowIndex();
				Rectangle srcBounds = parent.getBoundsByPosition(srcColPosition, srcRowPosition);
				srcHeight += 3;
				if (srcBounds != null && srcBounds.height != srcHeight) {
					Rectangle srcEditorBounds = sourceEditor.getEditorBounds();
//					int preSrcH = srcEditorBounds.height;
					srcEditorBounds.height = srcHeight;
					int cellStartY = srcBounds.y;
					int cellEndY = cellStartY + srcHeight;
					Rectangle tgtEditorBounds = targetEditor.getEditorBounds();
					int srcAndTgtEndY = cellEndY + tgtEditorBounds.height;
					Rectangle clientArea = parent.getClientAreaProvider().getClientArea();
					int clientAreaEndY = clientArea.y + clientArea.height;
					if (srcAndTgtEndY > clientAreaEndY) {
						srcEditorBounds.height = clientAreaEndY - tgtEditorBounds.height - cellStartY - 3;
					}
					CompositeLayer comlayer = LayerUtil.getLayer(parent, CompositeLayer.class);
					DataLayer dataLayer = LayerUtil.getLayer(parent, DataLayer.class);
					comlayer.doCommand(new TurnViewportOffCommand());
					dataLayer.setRowHeightByPosition(dataLayer.getRowPositionByIndex(srcRowIndex),
							srcEditorBounds.height);
					comlayer.doCommand(new TurnViewportOnCommand());
//					sourceEditor.setEditorBounds(srcEditorBounds, true);
//					tgtEditorBounds.y = tgtEditorBounds.y + (srcEditorBounds.height - preSrcH);
//					targetEditor.setEditorBounds(tgtEditorBounds, true);
					recalculateCellsBounds();
				}
			} else {
				// edit target
				int tgtHeight = targetEditor.computeSize().y;
				int tgtColPosition = targetEditor.getColumnPosition();
				int tgtRowPosition = targetEditor.getRowPosition();
				int tgtRowIndex = targetEditor.getRowIndex();
				Rectangle bounds = parent.getBoundsByPosition(tgtColPosition, tgtRowPosition);
				tgtHeight += 3;
				if (bounds != null && bounds.height != tgtHeight) {
					Rectangle tgtBounds = targetEditor.getEditorBounds();
					tgtBounds.height = tgtHeight;

					int cellStartY = tgtBounds.y;
					int cellEndY = cellStartY + tgtBounds.height;
					Rectangle clientArea = parent.getClientAreaProvider().getClientArea();
					int clientAreaEndY = clientArea.y + clientArea.height;
					if (cellEndY > clientAreaEndY) {
						tgtBounds.height = clientAreaEndY - cellStartY;
					}

					CompositeLayer comlayer = LayerUtil.getLayer(parent, CompositeLayer.class);
					DataLayer dataLayer = LayerUtil.getLayer(parent, DataLayer.class);
					comlayer.doCommand(new TurnViewportOffCommand());
					dataLayer.setRowHeightByPosition(dataLayer.getRowPositionByIndex(tgtRowIndex), tgtBounds.height);
					comlayer.doCommand(new TurnViewportOnCommand());
//					targetEditor.setEditorBounds(tgtBounds, true);
					recalculateCellsBounds();
				}
			}
		} else { // 水平模式
			int srcHeight = sourceEditor.computeSize().y;
			int tgtHeight = targetEditor.computeSize().y;

			int newHeight = srcHeight > tgtHeight ? srcHeight : tgtHeight;
			int colPosition = sourceEditor.getColumnPosition();
			int rowPosition = sourceEditor.getRowPosition();
			int rowIndex = sourceEditor.getRowIndex();
			Rectangle bounds = parent.getBoundsByPosition(colPosition, rowPosition);
			newHeight += 3;
			if (bounds != null && bounds.height == newHeight) {
				return;
			}
			// 加上编辑模式下，StyledTextCellEditor的边框
			Rectangle srcBounds = sourceEditor.getEditorBounds();
			Rectangle tgtBounds = targetEditor.getEditorBounds();
			srcBounds.height = newHeight;
			tgtBounds.height = newHeight;

			int cellStartY = srcBounds.y;
			int cellEndY = cellStartY + srcBounds.height;
			Rectangle clientArea = parent.getClientAreaProvider().getClientArea();
			int clientAreaEndY = clientArea.y + clientArea.height;
			if (cellEndY > clientAreaEndY) {
				srcBounds.height = clientAreaEndY - cellStartY;
				tgtBounds.height = srcBounds.height;
			}
			CompositeLayer comlayer = LayerUtil.getLayer(parent, CompositeLayer.class);
			DataLayer dataLayer = LayerUtil.getLayer(parent, DataLayer.class);
			comlayer.doCommand(new TurnViewportOffCommand());
			dataLayer.setRowHeightByPosition(dataLayer.getRowPositionByIndex(rowIndex), tgtBounds.height);
			comlayer.doCommand(new TurnViewportOnCommand());
//			HorizontalViewportLayer viewLayer = LayerUtil.getLayer(parent, HorizontalViewportLayer.class);
//			int newRowPosition = viewLayer.getRowPositionByIndex(rowIndex) + 1;
//			if (newRowPosition != rowPosition) {
//				sourceEditor.setRowPosition(newRowPosition);
//				targetEditor.setRowPosition(newRowPosition);
//				Rectangle newSrcBounds = parent.getBoundsByPosition(colPosition, newRowPosition);
//				newSrcBounds.height = srcBounds.height;
//				Rectangle newTgtBounds = parent.getBoundsByPosition(targetEditor.getColumnIndex(), newRowPosition);
//				sourceEditor.setEditorBounds(newSrcBounds, true);
//				targetEditor.setEditorBounds(newTgtBounds, true);
//			} else {
//				sourceEditor.setEditorBounds(srcBounds, true);
//				targetEditor.setEditorBounds(tgtBounds, true);
			
				recalculateCellsBounds();
//			}

		}
	}

	public static void recalculateCellsBounds() {
		if (parent == null) {
			return;
		}
		ViewportLayer vLayer = LayerUtil.getLayer(parent, ViewportLayer.class);
		if (vLayer == null) {
			return;
		}
		StyledTextCellEditor fc = getFocusCellEditor();
		if (sourceEditor != null && sourceEditor.isValid()) {
			int rowPosition = vLayer.getRowPositionByIndex(sourceEditor.getRowIndex());
			int columnPosition = vLayer.getColumnPositionByIndex(sourceEditor.getColumnIndex());
			rowPosition += 1;
			if (rowPosition < 1) {
				return;
			}
			LayerCell cell = parent.getCellByPosition(columnPosition, rowPosition);
			if (cell == null) {
				return;
			}
			Rectangle cellBounds = cell.getBounds();
			if (cellBounds != null) {
				Rectangle adjustedCellBounds = parent.getLayerPainter().adjustCellBounds(cellBounds);
				sourceEditor.setEditorBounds(adjustedCellBounds, true);
				sourceEditor.setColumnPosition(columnPosition);
				sourceEditor.setRowPosition(rowPosition);
				if(fc == sourceEditor.getCellEditor()){
					sourceEditor.forceFocus();
				}
			}
		}
		if (targetEditor != null && targetEditor.isValid()) {
			int rowPosition = vLayer.getRowPositionByIndex(targetEditor.getRowIndex());
			int columnPosition = vLayer.getColumnPositionByIndex(targetEditor.getColumnIndex());
			rowPosition += 1;
			if (rowPosition < 1) {
				return;
			}
			LayerCell cell = parent.getCellByPosition(columnPosition, rowPosition);
			if (cell == null) {
				return;
			}
			Rectangle cellBounds = cell.getBounds();
			if (cellBounds != null) {
				Rectangle adjustedCellBounds = parent.getLayerPainter().adjustCellBounds(cellBounds);
				targetEditor.setEditorBounds(adjustedCellBounds, true);
				targetEditor.setColumnPosition(columnPosition);
				targetEditor.setRowPosition(rowPosition);
				if(fc == targetEditor.getCellEditor()){
					targetEditor.forceFocus();
				}
			}
		}
	}

	public static StyledTextCellEditor getTargetStyledEditor() {
		if (targetEditor != null && targetEditor.isValid()) {
			return targetEditor.getCellEditor();
		}
		return null;
	}

	public static StyledTextCellEditor getSourceStyledEditor() {
		if (sourceEditor != null && sourceEditor.isValid()) {
			return sourceEditor.getCellEditor();
		}
		return null;
	}

	public static void refrushCellsEditAbility() {
		StyledTextCellEditor editor = getTargetStyledEditor();
		if (editor != null) {
			editor.getEditableManager().judgeEditable();
		}

		editor = getSourceStyledEditor();
		if (editor != null) {
			editor.getEditableManager().judgeEditable();
		}
	}

	/** @return the sourceEditor */
	public static HsMultiCellEditor getSourceEditor() {
		return sourceEditor;
	}

	/** @return the targetEditor */
	public static HsMultiCellEditor getTargetEditor() {
		return targetEditor;
	}

	/** @return the parent */
	public static NatTable getParent() {
		return parent;
	}

}
