package net.sourceforge.nattable.data.convert;

/**
 * Converts between two different data representations.
 *
 * The normal data representation is known as the <i>canonical representation</i>
 * The representation displayed on the UI is called the <i>display representation</i>.
 *
 * For example, the canonical representation might be a Date object,
 * whereas the target representation could be a formatted String.
 */
public interface IDisplayConverter {

	/**
	 * Convert backing data value -> value to be displayed<br/>
	 * Typically converted to a String for display.
	 */
	public Object canonicalToDisplayValue(Object canonicalValue);

	/**
	 * Convert from display value -> value in the backing data structure<br/>
	 * NOTE: The type the display value is converted to <i>must</i> match the type
	 * in the setter of the backing bean/row object
	 */
	public Object displayToCanonicalValue(Object displayValue);

}
