package net.sourceforge.nattable.config;

import java.util.Comparator;

@SuppressWarnings("unchecked")
public class DefaultComparator implements Comparator<Object> {

	private static DefaultComparator singleton;

	public static final DefaultComparator getInstance() {
		if (singleton == null) {
			singleton = new DefaultComparator();
		}
		return singleton;
	}

	public int compare(final Object o1, final Object o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (o2 == null) {
			return 1;
		} else if (o1 instanceof Comparable && o2 instanceof Comparable) {
			return ((Comparable) o1).compareTo((Comparable) o2);
		} else {
			return 0;
		}
	}

}
