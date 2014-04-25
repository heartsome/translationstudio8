package net.sourceforge.nattable.search;

import java.util.Comparator;

/**
 * The comparator will base its comparison on the display value of a cell.  The 
 * display value is assumed to be a string.
 *
 */
public class CellValueAsStringComparator<T extends Comparable<String>> implements Comparator<T> {
	
	public CellValueAsStringComparator() {
	}
	
	public int compare(T firstValue, T secondValue) {
		String firstCellValue = firstValue.toString();
		String secondCellValue = secondValue.toString();
		return firstCellValue.compareTo(secondCellValue);
	}
}
