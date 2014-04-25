/**
 * HsMultiCellEditorControl.java
 *
 * Version information :
 *
 * Date:2012-12-14
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.ts.core.bean.SingleWord;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.qa.RealTimeSpellCheckTrigger;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.style.CellStyleProxy;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HsMultiCellEditorControl {
	private static RealTimeSpellCheckTrigger spellTrigger;
	/** 当点以如下标点结尾时，触发实时拼写检查 robert */
	private static final String ENDREGEX = "[ ,.;!?，。；！？]";

	public static void activeSourceAndTargetCell(XLIFFEditorImplWithNatTable xliffEditor) {
		if (xliffEditor == null) {
			return;
		}
		int[] selectedRowIndexs = xliffEditor.getSelectedRows();
		if (selectedRowIndexs.length == 0) {
			return;
		}
		Arrays.sort(selectedRowIndexs);
		int rowIndex = selectedRowIndexs[selectedRowIndexs.length - 1];
		if (!xliffEditor.isHorizontalLayout()) {
			rowIndex = rowIndex * 2; // source index
		}

		NatTable natTable = xliffEditor.getTable();
		IConfigRegistry configRegistry = natTable.getConfigRegistry();
		ViewportLayer vLayer = LayerUtil.getLayer(natTable, ViewportLayer.class);
		int rowPosition = vLayer.getRowPositionByIndex(rowIndex);
		rowPosition += 1;
		if (rowPosition < 1) {
			return;
		}

		int columnIndex = xliffEditor.getSrcColumnIndex();
		HsMultiCellEditor srcCellEditor = activeCell(vLayer, xliffEditor, configRegistry, columnIndex, rowIndex,
				rowPosition, NatTableConstant.SOURCE);
		if (srcCellEditor == null) {
			return;
		}

		if (!xliffEditor.isHorizontalLayout()) {
			rowIndex = rowIndex + 1; // target
			rowPosition = vLayer.getRowPositionByIndex(rowIndex);
			rowPosition += 1;
			if (rowPosition < 1) {
				return;
			}
		}
		columnIndex = xliffEditor.getTgtColumnIndex();
		HsMultiCellEditor tgtCellEditor = activeCell(vLayer, xliffEditor, configRegistry, columnIndex, rowIndex,
				rowPosition, NatTableConstant.TARGET);
		if (tgtCellEditor == null) {
			return;
		}
		HsMultiActiveCellEditor.activateCellEditors(srcCellEditor, tgtCellEditor, natTable);

		// 目标文本段一进入焦点就进行一次拼写检查 robert 2013-01-22
		// UNDO 这里错误单词提示并没有修改颜色。
		String tgtLang = xliffEditor.getTgtColumnName();
		spellTrigger = RealTimeSpellCheckTrigger.getInstance();
		if (spellTrigger != null && spellTrigger.checkSpellAvailable(tgtLang)) {
			tgtTextFirstRealTimeSpellCheck(tgtLang, tgtCellEditor);
			tgtTextRealTimeSpellCheck(tgtLang, tgtCellEditor);
		}
		List<String> terms = xliffEditor.getTermsCache().get(selectedRowIndexs[0]);
		if (terms != null && terms.size() > 0) {
			srcCellEditor.highlightedTerms(terms);
		}
	}

	/**
	 * 当一个文本段初次获取焦点时，实时进行拼写检查，<div style='color:red'>该方法与{@link tgtTextRealTimeSpellCheck} 类似</div>
	 */
	private static void tgtTextFirstRealTimeSpellCheck(final String tgtLang, HsMultiCellEditor targetEditor) {
		final StyledTextCellEditor tgtEditor = targetEditor.getCellEditor();
		final StyledText text = tgtEditor.getSegmentViewer().getTextWidget();
		if (tgtLang == null) {
			return;
		}
		String tgtText = text.getText();
		if (tgtText == null || "".equals(tgtText.trim())) {
			return;
		}
		List<SingleWord> errorWordList = new LinkedList<SingleWord>();
		errorWordList = spellTrigger.getErrorWords(tgtText, tgtLang);
		if (errorWordList != null && errorWordList.size() > 0) {
			targetEditor.highLightedErrorWord(tgtText, errorWordList);
		} else {
			targetEditor.refreshErrorWordsStyle(null);
		}
	}

	/**
	 * 当文本处于正在编辑时，实时进行拼写检查，<div style='color:red'>该方法与{@link #tgtTextFirstRealTimeSpellCheck} 类似</div>
	 */
	private static void tgtTextRealTimeSpellCheck(final String tgtLang, final HsMultiCellEditor targetEditor) {
		final StyledTextCellEditor tgtEditor = targetEditor.getCellEditor();
		final StyledText text = tgtEditor.getSegmentViewer().getTextWidget();
		if (tgtLang == null) {
			return;
		}
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String tgtText = text.getText();

				if (tgtText.isEmpty()) {
					return;
				}

				String endStr = tgtText.substring(tgtText.length() - 1, tgtText.length());
				if (endStr.matches(ENDREGEX)) {
					List<SingleWord> errorWordList = new LinkedList<SingleWord>();
					errorWordList = spellTrigger.getErrorWords(tgtText, tgtLang);
					if (errorWordList != null && errorWordList.size() > 0) {
						targetEditor.highLightedErrorWord(tgtText, errorWordList);
					} else {
						targetEditor.refreshErrorWordsStyle(null);
					}
				}
			}
		});
	}

	private static HsMultiCellEditor activeCell(ViewportLayer vLayer, XLIFFEditorImplWithNatTable xliffEditor,
			IConfigRegistry configRegistry, int columnIndex, int rowIndex, int rowPosition, String cellType) {
		NatTable natTable = xliffEditor.getTable();
		int columnPosition = vLayer.getColumnPositionByIndex(columnIndex);

		LayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);
		if (cell == null) {
			return null;
		}
		Rectangle cellBounds = cell.getBounds();
		List<String> configLabels = cell.getConfigLabels().getLabels();
		if (!xliffEditor.isHorizontalLayout()) {
			if (cellType.equals(NatTableConstant.SOURCE)) {
				configLabels.remove(XLIFFEditorImplWithNatTable.TARGET_EDIT_CELL_LABEL);
			} else if (cellType.equals(NatTableConstant.TARGET)) {
				configLabels.remove(XLIFFEditorImplWithNatTable.SOURCE_EDIT_CELL_LABEL);
			}
		}
		ILayer layer = cell.getLayer();
		Object originalCanonicalValue = cell.getDataValue();

		IDisplayConverter displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				DisplayMode.EDIT, configLabels);
		IStyle cellStyle = new CellStyleProxy(configRegistry, DisplayMode.EDIT, configLabels);
		IDataValidator dataValidator = configRegistry.getConfigAttribute(EditConfigAttributes.DATA_VALIDATOR,
				DisplayMode.EDIT, configLabels);

		Rectangle editorBounds = layer.getLayerPainter().adjustCellBounds(
				new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height));

		int cellStartY = cellBounds.y;
		int cellEndY = cellStartY + cellBounds.height;
		Rectangle clientArea = natTable.getClientAreaProvider().getClientArea();
		int clientAreaEndY = clientArea.y + clientArea.height;
		if (cellEndY > clientAreaEndY) {
			editorBounds.height = clientAreaEndY - cellStartY;
		}

		StyledTextCellEditor cellEditor = (StyledTextCellEditor) configRegistry.getConfigAttribute(
				EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, configLabels);
		ICellEditHandler editHandler = new HsMultiCellEditorHandler(cellEditor, layer);

		HsMultiCellEditor hsCellEditor = new HsMultiCellEditor(cellType, cellEditor, editHandler, columnPosition,
				rowPosition, columnIndex, rowIndex, dataValidator, originalCanonicalValue, displayConverter, cellStyle,
				editorBounds);

		return hsCellEditor;
	}
}
