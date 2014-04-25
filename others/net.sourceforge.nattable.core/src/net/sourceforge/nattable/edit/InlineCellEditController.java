package net.sourceforge.nattable.edit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.style.CellStyleProxy;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class InlineCellEditController {

	private static Map<ILayer, ILayerListener> layerListenerMap = new HashMap<ILayer, ILayerListener>();

	public static boolean editCellInline(LayerCell cell, Character initialEditValue, Composite parent, IConfigRegistry configRegistry) {
		try {
			ActiveCellEditor.commit();

			final List<String> configLabels = cell.getConfigLabels().getLabels();
			Rectangle cellBounds = cell.getBounds();

			ILayer layer = cell.getLayer();

			int columnPosition = layer.getColumnPositionByX(cellBounds.x);
			int columnIndex = layer.getColumnIndexByPosition(columnPosition);
			int rowPosition = layer.getRowPositionByY(cellBounds.y);
			int rowIndex = layer.getRowIndexByPosition(rowPosition);

			boolean editable = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, DisplayMode.EDIT, configLabels).isEditable(columnIndex, rowIndex);
			if (!editable) {
				return false;
			}

			ICellEditor cellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, configLabels);
			IDisplayConverter displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.EDIT, configLabels);
			IStyle cellStyle = new CellStyleProxy(configRegistry, DisplayMode.EDIT, configLabels);
			IDataValidator dataValidator = configRegistry.getConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, DisplayMode.EDIT, configLabels);

			ICellEditHandler editHandler = new SingleEditHandler(
					cellEditor,
					layer,
					columnPosition,
					rowPosition);

			final Rectangle editorBounds = layer.getLayerPainter().adjustCellBounds(new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height));

			Object originalCanonicalValue = cell.getDataValue();

			ActiveCellEditor.activate(cellEditor, parent, originalCanonicalValue, initialEditValue, displayConverter, cellStyle, dataValidator, editHandler, columnPosition, rowPosition, columnIndex, rowIndex);
			Control editorControl = ActiveCellEditor.getControl();

			if (editorControl != null) {
				editorControl.setBounds(editorBounds);
				ILayerListener layerListener = layerListenerMap.get(layer);
				if (layerListener == null) {
					layerListener = new InlineCellEditLayerListener(layer);
					layerListenerMap.put(layer, layerListener);

					layer.addLayerListener(layerListener);
				}
			}
		} catch (Exception e) {
			if(cell == null){
				System.err.println("Cell being edited is no longer available. " + "Character: " + initialEditValue);
			} else {
				System.err.println("Error while editing cell (inline): " + "Cell: " + cell + "; Character: " + initialEditValue);
				e.printStackTrace(System.err);
			}
		}

		return true;
	}

	public static void dispose() {
		layerListenerMap.clear();
	}

	static class InlineCellEditLayerListener implements ILayerListener {

		private final ILayer layer;

		InlineCellEditLayerListener(ILayer layer) {
			this.layer = layer;
		}

		public void handleLayerEvent(ILayerEvent event) {
//			if (ActiveCellEditor.isValid()) {
//				int editorColumnPosition = ActiveCellEditor.getColumnPosition();
//				int editorRowPosition = ActiveCellEditor.getRowPosition();
//				int editorColumnIndex = ActiveCellEditor.getColumnIndex();
//				int editorRowIndex = ActiveCellEditor.getRowIndex();
//				Control editorControl = ActiveCellEditor.getControl();
//
//				int columnIndex = layer.getColumnIndexByPosition(editorColumnPosition);
//				int rowIndex = layer.getRowIndexByPosition(editorRowPosition);
//
//				if (columnIndex != editorColumnIndex || rowIndex != editorRowIndex) {
//					ActiveCellEditor.close();
//				} else if (editorControl != null && !editorControl.isDisposed()) {
//					Rectangle cellBounds = layer.getBoundsByPosition(editorColumnPosition, editorRowPosition);
//					Rectangle adjustedCellBounds = layer.getLayerPainter().adjustCellBounds(cellBounds);
//					editorControl.setBounds(adjustedCellBounds);
//				}
//			}
		}

	}

}
