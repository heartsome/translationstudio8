package net.sourceforge.nattable.grid.layer;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.layer.DataLayer;

public class DefaultRowHeaderDataLayer extends DataLayer {

	public DefaultRowHeaderDataLayer(IDataProvider rowHeaderDataProvider) {
		super(rowHeaderDataProvider, 40, 40);
	}
	
}
