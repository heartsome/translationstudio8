package net.sourceforge.nattable.data.convert;


/**
 * Data type converter for a Check Box. 
 * Assumes that the data value is stored as a boolean.
 */
public class DefaultBooleanDisplayConverter implements IDisplayConverter {

	public Object displayToCanonicalValue(Object displayValue) {
		return Boolean.valueOf(displayValue.toString());
	}

	public Object canonicalToDisplayValue(Object canonicalValue) {
		if (canonicalValue == null) {
			return null;
		} else {
			return canonicalValue.toString();
		}
	}

}
