package net.sourceforge.nattable.config;

import java.io.Serializable;
import java.util.Comparator;

/**
 * GlazedLists require that the comparator be set to 'null' if a column is not sortable.<br/>
 * If a null value is set in the {@link IConfigRegistry} it will attempt to find<br/>
 * other matching values.<br/>
 * This comparator can be set in the {@link ConfigRegistry} to indicate that the column can not be sorted.
 *
 * @see SortableGridExample
 */
public class NullComparator implements Comparator<Object>, Serializable {

	private static final long serialVersionUID = -6945858872109267371L;

	public int compare(Object o1, Object o2) {
		return 0;
	}

}
