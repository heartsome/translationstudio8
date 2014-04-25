package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.command;

import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.bean.XliffEditorParameter;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.CellRegion;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.event.FindReplaceEvent;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.DefaultCellSearchStrategy;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.ICellSearchStrategy;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.ISearchStrategy;
import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.jface.text.IRegion;

public class FindReplaceCommandHandler implements ILayerCommandHandler<FindReplaceCommand> {

	private final SelectionLayer selectionLayer;
	private CellRegion searchResultCellRegion;

	public FindReplaceCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public Class<FindReplaceCommand> getCommandClass() {
		return FindReplaceCommand.class;
	};

	public boolean doCommand(ILayer targetLayer, FindReplaceCommand findReplaceCommand) {
		findReplaceCommand.convertToTargetLayer(targetLayer);

		ISearchStrategy searchStrategy = findReplaceCommand.getSearchStrategy();
		if (findReplaceCommand.getSearchEventListener() != null) {
			selectionLayer.addLayerListener(findReplaceCommand.getSearchEventListener());
		}
		PositionCoordinate anchor = selectionLayer.getSelectionAnchor();
		if (anchor.columnPosition < 0 || anchor.rowPosition < 0) {
			anchor = new PositionCoordinate(selectionLayer, 0, 0);
		}
		searchStrategy.setContextLayer(targetLayer);
		Object dataValueToFind = null;
		if ((dataValueToFind = findReplaceCommand.getSearchText()) == null) {
			dataValueToFind = selectionLayer.getDataValueByPosition(anchor.columnPosition, anchor.rowPosition);
		}

		ICellSearchStrategy cellSearchStrategy = findReplaceCommand.getCellSearchStrategy();
		searchStrategy.setCellSearchStrategy(cellSearchStrategy);

		DefaultCellSearchStrategy defaultCellSearchStrategy = null;
		if (cellSearchStrategy instanceof DefaultCellSearchStrategy) {
			defaultCellSearchStrategy = (DefaultCellSearchStrategy) cellSearchStrategy;
		}
		searchResultCellRegion = searchStrategy.executeSearch(dataValueToFind);

		if (searchResultCellRegion != null) {
			PositionCoordinate searchResultCellCoordinate = searchResultCellRegion.getPositionCoordinate();
			int rowPosition = searchResultCellCoordinate.rowPosition;
			XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();

			ViewportLayer viewportLayer = LayerUtil.getLayer(editor.getTable(), ViewportLayer.class);

			HsMultiActiveCellEditor.commit(true);

			if (!editor.isHorizontalLayout()) {
				viewportLayer.doCommand(new SelectCellCommand(selectionLayer,
						searchResultCellCoordinate.columnPosition, rowPosition / 2 * 2, false, false));
			} else {
				viewportLayer.doCommand(new SelectCellCommand(selectionLayer,
						searchResultCellCoordinate.columnPosition, rowPosition, false, false));
			}

			HsMultiCellEditorControl.activeSourceAndTargetCell(editor);
			HsMultiActiveCellEditor.setCellEditorForceFocusByIndex(searchResultCellCoordinate.columnPosition,
					rowPosition);
			StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
			if (cellEditor != null) {
				String dataValue = cellEditor.getSegmentViewer().getDocument().get();
				if (dataValue != null) {
					int startOffset = -1;
					if (defaultCellSearchStrategy != null) {
						startOffset = defaultCellSearchStrategy.getStartOffset();
					}
					defaultCellSearchStrategy.setStartOffset(startOffset);
					String findString = dataValueToFind.toString();
					if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
						findString = findString.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
						findString = findString.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
						findString = findString.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
					}
					IRegion region = defaultCellSearchStrategy.executeSearch(findString, dataValue);
					if (region != null) {
						HsMultiActiveCellEditor.setSelectionText(cellEditor, region.getOffset(), region.getLength());
					}
					defaultCellSearchStrategy.setStartOffset(-1);
				}
			}
		}

		selectionLayer.fireLayerEvent(new FindReplaceEvent(searchResultCellRegion));
		if (findReplaceCommand.getSearchEventListener() != null) {
			selectionLayer.removeLayerListener(findReplaceCommand.getSearchEventListener());
		}

		return true;
	}
}
