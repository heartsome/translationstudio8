package net.sourceforge.nattable.grid.data;

import java.util.List;

import net.sourceforge.nattable.data.ListDataProvider;
import net.sourceforge.nattable.data.ReflectiveColumnPropertyAccessor;

public class DefaultBodyDataProvider<T> extends ListDataProvider<T> {

	public DefaultBodyDataProvider(List<T> rowData, String[] propertyNames) {
		super(rowData, new ReflectiveColumnPropertyAccessor<T>(propertyNames));
	}
	
}
