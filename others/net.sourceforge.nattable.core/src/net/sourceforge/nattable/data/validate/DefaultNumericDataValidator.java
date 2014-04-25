package net.sourceforge.nattable.data.validate;


public class DefaultNumericDataValidator implements IDataValidator {

	public boolean validate(int columnIndex, int rowIndex, Object newValue) {
		try {
			new Double(newValue.toString());
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
