package net.sourceforge.nattable.data.convert;


public class DefaultDisplayConverter implements IDisplayConverter {

	public Object canonicalToDisplayValue(Object sourceValue) {
		return sourceValue != null ? sourceValue.toString() : "";
	}

	public Object displayToCanonicalValue(Object destinationValue) {
		if (destinationValue == null || destinationValue.toString().length() == 0){
			return null;
		} else {
			return destinationValue.toString();
		}
	}
}
