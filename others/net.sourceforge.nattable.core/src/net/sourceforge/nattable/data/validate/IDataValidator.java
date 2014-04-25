package net.sourceforge.nattable.data.validate;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.edit.editor.TextCellEditor;

public interface IDataValidator {

	/**
	 *
	 * @param columnIndex Index of the colunm being validated
	 * @param rowIndex Index of the row being validated
	 * @param newValue Value entered through the edit control text box, combo box etc.
	 * 	Note: In case of the {@link TextCellEditor} the text typed in by the user
	 * 	will be converted to the canonical value using the {@link IDisplayConverter}
	 * 	before it hits this method
	 *
	 * @see IDataProvider#getDataValue(int, int)
	 *
	 * @return true is newValue is valid. False otherwise.
	 */
	public boolean validate(int columnIndex, int rowIndex, Object newValue);

	public static final IDataValidator ALWAYS_VALID = new IDataValidator() {

		public boolean validate(int columnIndex, int rowIndex, Object newValue) {
			return true;
		}

	};

	public static final IDataValidator NEVER_VALID = new IDataValidator() {

		public boolean validate(int columnIndex, int rowIndex, Object newValue) {
			return false;
		}

	};
}
