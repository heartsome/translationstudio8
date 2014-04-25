package net.sourceforge.nattable.config;

public class DefaultEditableRule implements IEditableRule {

	private boolean defaultEditable;
	
	public DefaultEditableRule(boolean defaultEditable) {
		this.defaultEditable = defaultEditable;
	}
	
	public boolean isEditable(int columnIndex, int rowIndex) {
		return defaultEditable;
	}

}
