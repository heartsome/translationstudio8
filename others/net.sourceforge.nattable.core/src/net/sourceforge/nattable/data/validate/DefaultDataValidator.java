package net.sourceforge.nattable.data.validate;


public class DefaultDataValidator implements IDataValidator {

	public boolean validate(int columnIndex, int rowIndex, Object newValue) {
		return true;
	}

}
