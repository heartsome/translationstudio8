package net.sourceforge.nattable.coordinate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents an Range of numbers.
 * Example a Range of selected rows: 1 - 100
 * Ranges are inclusive of their start value and not inclusive of their end value, i.e. start <= x < end
 */
public class Range {

	public int start = 0;
	public int end = 0;

	public Range(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int size() {
		return end - start;
	}
	
	/**
	 * @return TRUE if the range contains the given row position
	 */
	public boolean contains(int position) {
		return position >= start && position < end;
	}

	public boolean overlap(Range range) {
		return this.contains(range.start);
	}

	public Set<Integer> getMembers() {
		Set<Integer> members = new HashSet<Integer>();
		for (int i = start; i < end; i++) {
			members.add(Integer.valueOf(i));
		}
		return members;
	}

	@Override
	public String toString() {
		return "Range[" + start + "," + end + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Range)) {
			return false;
		}

		Range range2 = (Range) obj;
		return (start == range2.start) && (end == range2.end);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public static void sortByStart(List<Range> ranges) {
		Collections.sort(ranges, new Comparator<Range>() {
			public int compare(Range range1, Range range2) {
				return Integer.valueOf(range1.start).compareTo(
						Integer.valueOf(range2.start));
			}
		});
	}

}
