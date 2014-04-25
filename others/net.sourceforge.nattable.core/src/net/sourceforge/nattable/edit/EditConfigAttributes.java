package net.sourceforge.nattable.edit;

import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.style.ConfigAttribute;

public interface EditConfigAttributes {
	
	public static final ConfigAttribute<IEditableRule> CELL_EDITABLE_RULE = new ConfigAttribute<IEditableRule>();
	
	public static final ConfigAttribute<ICellEditor> CELL_EDITOR = new ConfigAttribute<ICellEditor>();

	public static final ConfigAttribute<IDataValidator> DATA_VALIDATOR = new ConfigAttribute<IDataValidator>();

}
