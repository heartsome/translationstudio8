package net.sourceforge.nattable.data.convert;

import static net.sourceforge.nattable.util.ObjectUtils.isNotEmpty;
import static net.sourceforge.nattable.util.ObjectUtils.isNotNull;

/**
 * Converts the display value to a double and vice versa.
 */
public class DefaultDoubleDisplayConverter implements IDisplayConverter {

	public Object canonicalToDisplayValue(Object canonicalValue) {
		try {
			if (isNotNull(canonicalValue)) {
				return canonicalValue.toString();
			}
			return null;
		} catch (Exception e) {
			return canonicalValue;
		}
	}

	public Object displayToCanonicalValue(Object displayValue) {
		try {
			if (isNotNull(displayValue) && isNotEmpty(displayValue.toString())) {
				return Double.valueOf(displayValue.toString());
			}
			return null;
		} catch (Exception e) {
			return displayValue;
		}
	}
}
