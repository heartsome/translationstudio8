package net.sourceforge.nattable.edit.editor;

import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractCellEditor implements ICellEditor {

	private boolean closed;
	private Composite parent;
	private ICellEditHandler editHandler;
	private IDisplayConverter displayConverter;
	private IStyle cellStyle;
	private IDataValidator dataValidator;
	private int columnIndex;
	private int rowIndex;

	public final Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue,
			IDisplayConverter displayConverter, IStyle cellStyle, IDataValidator dataValidator,
			ICellEditHandler editHandler, int columnIndex, int rowIndex) {

		this.closed = false;
		this.parent = parent;
		this.editHandler = editHandler;
		this.displayConverter = displayConverter;
		this.cellStyle = cellStyle;
		this.dataValidator = dataValidator;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;

		return activateCell(parent, originalCanonicalValue, initialEditValue);
	}

	protected abstract Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue);

	protected boolean validateCanonicalValue() {
		if (dataValidator != null) {
			return dataValidator.validate(columnIndex, rowIndex, getCanonicalValue());
		} else {
			return true;
		}
	}

	protected IDisplayConverter getDataTypeConverter() {
		return displayConverter;
	}

	protected IStyle getCellStyle() {
		return cellStyle;
	}

	protected IDataValidator getDataValidator() {
		return dataValidator;
	}

	/**
	 * Commit and close editor.
	 * @see AbstractCellEditor#commit(MoveDirectionEnum, boolean)
	 */
	protected final boolean commit(MoveDirectionEnum direction) {
		return commit(direction, true);
	}
	
	/**
	 * Commit change - after validation.
	 * @param direction to move the selection in after a successful commit
	 * @param closeAfterCommit close the editor after a successful commit
	 */
	protected final boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit) {
		if (editHandler != null) {
			if (validateCanonicalValue()) {
				return (editHandler.commit(direction, closeAfterCommit));
			}
		}
		return false;
	}

	public void close() {
		if (parent != null && !parent.isDisposed()) {
			parent.forceFocus();
		}
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}
}
