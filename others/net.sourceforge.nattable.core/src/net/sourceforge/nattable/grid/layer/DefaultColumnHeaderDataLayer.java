package net.sourceforge.nattable.grid.layer;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.layer.DataLayer;

public class DefaultColumnHeaderDataLayer extends DataLayer {

	public DefaultColumnHeaderDataLayer(IDataProvider columnHeaderDataProvider) {
		super(columnHeaderDataProvider, 100, 20);
	}
	
}
