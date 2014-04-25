package net.sourceforge.nattable.coordinate;

import static net.sourceforge.nattable.util.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PositionUtil {

	/**
	 * Finds contiguous numbers in a group of numbers.
	 * @see ColumnChooserDialogTest#getGroupedByContiguous()
	 */
	public static List<List<Integer>> getGroupedByContiguous(Collection<Integer> numberCollection) {
		List<Integer> numbers = new ArrayList<Integer>(numberCollection);
		Collections.sort(numbers);

		List<Integer> contiguous = new ArrayList<Integer>();
		List<List<Integer>> grouped =  new ArrayList<List<Integer>>();

		for(int i = 0; i < numbers.size()-1; i++) {
			if(numbers.get(i).intValue()+1 != numbers.get(i+1).intValue()){
				contiguous.add(numbers.get(i));
				grouped.add(contiguous);
				contiguous = new ArrayList<Integer>();
			} else {
				contiguous.add(numbers.get(i));
			}
		}
		if(isNotEmpty(numbers)){
			contiguous.add(numbers.get(numbers.size()-1));
		}
		grouped.add(contiguous);
		return grouped;
	}

	/**
	 * Creates {@link Range}s out of list of numbers.<br/>
	 * The contiguous numbers are grouped together in Ranges.<br/>
	 *
	 * Example: 0, 1, 2, 4, 5, 6 will return [[Range(0 - 3)][Range(4 - 7)]]<br/>
	 * The last number in the Range is not inclusive.
	 */
	public static List<Range> getRanges(Collection<Integer> numbers) {
		List<Range> ranges = new ArrayList<Range>();

		if(isNotEmpty(numbers)){
			for (List<Integer> number : PositionUtil.getGroupedByContiguous(numbers)) {
				int start = number.get(0).intValue();
				int end = number.get(number.size() - 1).intValue() + 1;

				ranges.add(new Range(start, end));
			}
		}

		return ranges;
	}

}
