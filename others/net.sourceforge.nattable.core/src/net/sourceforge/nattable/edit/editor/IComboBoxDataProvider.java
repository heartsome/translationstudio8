package net.sourceforge.nattable.edit.editor;

import java.util.List;

import net.sourceforge.nattable.data.convert.IDisplayConverter;

public interface IComboBoxDataProvider {

	/**
	 * List of values to used as a data source in a {@link ComboBoxCellEditor}.
	 * 	Note: these will be converted using the {@link IDisplayConverter} for display
	 */
	public List<?> getValues();
}
