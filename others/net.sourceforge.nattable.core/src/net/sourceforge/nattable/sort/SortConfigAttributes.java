package net.sourceforge.nattable.sort;

import java.util.Comparator;

import net.sourceforge.nattable.style.ConfigAttribute;

public interface SortConfigAttributes {
	
	public static final ConfigAttribute<Comparator<?>> SORT_COMPARATOR = new ConfigAttribute<Comparator<?>>();

}
