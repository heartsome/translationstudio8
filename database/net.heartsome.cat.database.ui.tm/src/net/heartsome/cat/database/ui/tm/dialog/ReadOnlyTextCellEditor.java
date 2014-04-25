package net.heartsome.cat.database.ui.tm.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.editor.CellEditorBase;
import de.jaret.util.ui.table.editor.ICellEditor;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * 使用此 Editor 只能选择编辑框中的文本，不能修改
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ReadOnlyTextCellEditor extends CellEditorBase implements ICellEditor, FocusListener {
	protected boolean _multi = true;

	/** control used for editing. */
	protected Text _text;

	private int _maxrows = 6;

	public ReadOnlyTextCellEditor(boolean multi) {
		_multi = multi;
	}

	protected String convertValue(IRow row, IColumn column) {
		Object value = column.getValue(row);
		return value != null ? value.toString() : null;
	}

	protected void storeValue(IRow row, IColumn column) {
		String value = _text.getText();
		_column.setValue(_row, value);
	}

	/**
	 * Create the control to be used when editing.
	 * @param table
	 *            table is the parent control
	 */
	private void createControl(JaretTable table) {
		if (_text == null) {
			_table = table;
			if (!_multi) {
				_text = new Text(table, SWT.BORDER);
			} else {
				_text = new Text(table, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			}

			_text.addFocusListener(this);
			_text.setEditable(false);
			// 屏蔽右键菜单
//			_text.setMenu(new Menu(_text.getShell()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getEditorControl(JaretTable table, IRow row, IColumn column, char typedKey) {
		super.getEditorControl(table, row, column, typedKey);
		createControl(table);
		if (typedKey != 0) {
			_text.setText("" + typedKey);
			_text.setSelection(1);
		} else {
			String value = convertValue(row, column);
			_text.setText(value != null ? value : "");
			_text.selectAll();
		}
		return _text;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPreferredHeight() {
		if (_text == null) {
			return -1;
		}
		int lheight = _text.getLineHeight();
		int lcount = _text.getLineCount();
		if (lcount > _maxrows + 1) {
			lcount = _maxrows;
		}
		return (lcount + 1) * lheight;

	}

	public void focusGained(FocusEvent arg0) {
	}

	public void focusLost(FocusEvent arg0) {
		_table.stopEditing(true);
	}

	public void dispose() {
		super.dispose();
		if (_text != null && !_text.isDisposed()) {
			_text.removeFocusListener(this);
			_text.dispose();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void stopEditing(boolean storeInput) {
		if (storeInput) {
			storeValue(_row, _column);
		}
		_text.setVisible(false);
	}

	/**
	 * @return the maxrows
	 */
	public int getMaxrows() {
		return _maxrows;
	}

	/**
	 * @param maxrows
	 *            the maxrows to set
	 */
	public void setMaxrows(int maxrows) {
		_maxrows = maxrows;
	}

}
