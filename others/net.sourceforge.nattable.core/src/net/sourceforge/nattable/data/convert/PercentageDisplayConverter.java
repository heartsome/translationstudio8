package net.sourceforge.nattable.data.convert;


public class PercentageDisplayConverter implements IDisplayConverter {

	public Object canonicalToDisplayValue(Object canonicalValue) {
		double percentageValue = ((Double) canonicalValue).doubleValue();
		int displayInt = (int) (percentageValue * 100);
		return String.valueOf(displayInt) + "%";
	}

	public Object displayToCanonicalValue(Object displayValue) {
		String displayString = (String) displayValue;
		displayString = displayString.trim();
		if (displayString.endsWith("%")) {
			displayString = displayString.substring(0, displayString.length() - 1);
		}
		displayString = displayString.trim();
		int displayInt = Integer.valueOf(displayString).intValue();
		double percentageValue = (double) displayInt / 100;
		return Double.valueOf(percentageValue);
	}

}
