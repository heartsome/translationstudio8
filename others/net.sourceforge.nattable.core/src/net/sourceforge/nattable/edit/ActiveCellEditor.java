package net.sourceforge.nattable.edit;

import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ActiveCellEditor {

	private static ICellEditor cellEditor;
	private static ICellEditHandler editHandler;
	private static IDataValidator dataValidator;
	private static Control activeCellEditorControl;
	private static int columnPosition = -1;
	private static int rowPosition = -1;
	private static int columnIndex = -1;
	private static int rowIndex = -1;
	
	public static void activate(ICellEditor cellEditor, Composite parent, Object originalCanonicalValue, Character initialEditValue, IDisplayConverter displayConverter, IStyle cellStyle, IDataValidator dataValidator, ICellEditHandler editHandler, int columnPosition, int rowPosition, int columnIndex, int rowIndex) {
		close();
		
		ActiveCellEditor.cellEditor = cellEditor;
		ActiveCellEditor.editHandler = editHandler;
		ActiveCellEditor.dataValidator = dataValidator;
		ActiveCellEditor.columnPosition = columnPosition;
		ActiveCellEditor.rowPosition = rowPosition;
		ActiveCellEditor.columnIndex = columnIndex;
		ActiveCellEditor.rowIndex = rowIndex;

		activeCellEditorControl = cellEditor.activateCell(parent, originalCanonicalValue, initialEditValue, displayConverter, cellStyle, dataValidator, editHandler, columnIndex, rowIndex);
	}
	
	public static void commit() {
		if (isValid() && validateCanonicalValue()) {
			editHandler.commit(MoveDirectionEnum.NONE, true);
		}
		close();
	}
	
	public static void close() {
		if (cellEditor != null && !cellEditor.isClosed()) {
			cellEditor.close();
		}
		cellEditor = null;

		editHandler = null;
		
		dataValidator = null;
		
		if (activeCellEditorControl != null && !activeCellEditorControl.isDisposed()) {
			activeCellEditorControl.dispose();
		}
		activeCellEditorControl = null;
		
		columnPosition = -1;
		rowPosition = -1;
		columnIndex = -1;
		rowIndex = -1;
	}

	public static ICellEditor getCellEditor() {
		return cellEditor;
	}
	
	public static Control getControl() {
		if (isValid()) {
			return activeCellEditorControl;
		} else {
			return null;
		}
	}
	
	public static int getColumnPosition() {
		return columnPosition;
	}
	
	public static int getRowPosition() {
		return rowPosition;
	}
	
	public static int getColumnIndex() {
		return columnIndex;
	}
	
	public static int getRowIndex() {
		return rowIndex;
	}

	public static Object getCanonicalValue() {
		if (isValid()) {
			return cellEditor.getCanonicalValue();
		} else {
			return null;
		}
	}

	public static boolean validateCanonicalValue() {
		if (dataValidator != null) {
			return dataValidator.validate(columnIndex, rowIndex, getCanonicalValue());
		} else {
			return true;
		}
	}

	public static boolean isValid() {
		return cellEditor != null && !cellEditor.isClosed();
	}

}
