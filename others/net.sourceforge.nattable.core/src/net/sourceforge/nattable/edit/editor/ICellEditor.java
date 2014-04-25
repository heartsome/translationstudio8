package net.sourceforge.nattable.edit.editor;

import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Implementations are responsible for capturing new cell value during cell edit. 
 */
public interface ICellEditor {
	
	/**
	 * Invoked by the action handlers to initialize the editor
	 * @param parent
	 * @param originalCanonicalValue of the cell being edited
	 * @param initialEditValue the initial key press char which triggered editing
	 * @return the SWT {@link Control} to be used for capturing the new cell value
	 */
	public Control activateCell(
			Composite parent,
			Object originalCanonicalValue,
			Character initialEditValue,
			IDisplayConverter displayConverter,
			IStyle cellStyle,
			IDataValidator dataValidator,
			ICellEditHandler editHandler,
			int colIndex,
			int rowIndex
	);

	/**
	 * @param canonicalValue the data value to be set in the backing bean.
	 * Note: This should be converted using the {@link IDisplayConverter} for display.
	 */
	public void setCanonicalValue(Object canonicalValue);
	
	public Object getCanonicalValue();
	
	/**
	 * Close/dispose the contained {@link Control}
	 */
	public void close();

	public boolean isClosed();
	
}
