package net.sourceforge.nattable.data.convert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.nattable.util.ObjectUtils;

/**
 * Converts a java.util.Date object to a given format and vice versa
 */
public class DefaultDateDisplayConverter implements IDisplayConverter {

	private DateFormat dateFormat;

	/**
	 * @param dateFormat as specified in {@link SimpleDateFormat}
	 */
	public DefaultDateDisplayConverter(String dateFormat) {
		this.dateFormat = new SimpleDateFormat(dateFormat);
	}

	/**
	 * Convert {@link Date} to {@link String} using the default format from {@link SimpleDateFormat}
	 */
	public DefaultDateDisplayConverter() {
		this.dateFormat = new SimpleDateFormat();
	}

	public Object canonicalToDisplayValue(Object canonicalValue) {
		try {
			if (ObjectUtils.isNotNull(canonicalValue)) {
				return dateFormat.format(canonicalValue);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return canonicalValue;
	}

	public Object displayToCanonicalValue(Object displayValue) {
		try {
			return dateFormat.parse(displayValue.toString());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return displayValue;
	}

}
