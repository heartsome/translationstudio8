package net.sourceforge.nattable.layer.stack;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.data.DefaultRowHeaderDataProvider;
import net.sourceforge.nattable.grid.data.DummyBodyDataProvider;
import net.sourceforge.nattable.grid.data.DummyColumnHeaderDataProvider;
import net.sourceforge.nattable.grid.layer.DefaultGridLayer;

public class DummyGridLayerStack extends DefaultGridLayer {

	public DummyGridLayerStack() {
		this(20, 20);
	}
	
	public DummyGridLayerStack(int columnCount, int rowCount) {
		super(true);
		IDataProvider bodyDataProvider = new DummyBodyDataProvider(columnCount, rowCount);
		IDataProvider columnHeaderDataProvider = new DummyColumnHeaderDataProvider(bodyDataProvider);
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		
		init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider);
	}
	
}
