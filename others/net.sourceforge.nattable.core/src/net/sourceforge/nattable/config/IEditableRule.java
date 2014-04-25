package net.sourceforge.nattable.config;

public interface IEditableRule {
	
	public boolean isEditable(int columnIndex, int rowIndex);
	
	public static final IEditableRule ALWAYS_EDITABLE = new IEditableRule() {

		public boolean isEditable(int columnIndex, int rowIndex) {
			return true;
		}
		
	};
	
	public static final IEditableRule NEVER_EDITABLE = new IEditableRule() {

		public boolean isEditable(int columnIndex, int rowIndex) {
			return false;
		}
		
	};

}
