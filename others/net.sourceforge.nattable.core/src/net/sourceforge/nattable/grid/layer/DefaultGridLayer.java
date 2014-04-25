package net.sourceforge.nattable.grid.layer;

import java.util.List;
import java.util.Map;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.data.DefaultBodyDataProvider;
import net.sourceforge.nattable.grid.data.DefaultColumnHeaderDataProvider;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.data.DefaultRowHeaderDataProvider;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.stack.DefaultBodyLayerStack;
import net.sourceforge.nattable.selection.SelectionLayer;

public class DefaultGridLayer extends GridLayer {

	protected IUniqueIndexLayer bodyDataLayer;
	protected IUniqueIndexLayer columnHeaderDataLayer;
	protected IUniqueIndexLayer rowHeaderDataLayer;
	protected IUniqueIndexLayer cornerDataLayer;
	
	public <T> DefaultGridLayer(List<T> rowData, String[] propertyNames, Map<String, String> propertyToLabelMap) {
		this(rowData, propertyNames, propertyToLabelMap, true);
	}
	
	public <T> DefaultGridLayer(List<T> rowData, String[] propertyNames, Map<String, String> propertyToLabelMap, boolean useDefaultConfiguration) {
		super(useDefaultConfiguration);
		init(rowData, propertyNames, propertyToLabelMap);
	}
	
	public DefaultGridLayer(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider) {
		this(bodyDataProvider, columnHeaderDataProvider, true);
	}
	
	public DefaultGridLayer(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider, boolean useDefaultConfiguration) {
		super(useDefaultConfiguration);
		init(bodyDataProvider, columnHeaderDataProvider);
	}
	
	public DefaultGridLayer(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider) {
		this(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, true);
	}
	
	public DefaultGridLayer(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider, boolean useDefaultConfiguration) {
		super(useDefaultConfiguration);
		init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider);
	}
	
	public DefaultGridLayer(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider, IDataProvider cornerDataProvider) {
		this(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider, true);
	}
	
	public DefaultGridLayer(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider, IDataProvider cornerDataProvider, boolean useDefaultConfiguration) {
		super(useDefaultConfiguration);
		init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider);
	}
	
	public DefaultGridLayer(IUniqueIndexLayer bodyDataLayer, IUniqueIndexLayer columnHeaderDataLayer, IUniqueIndexLayer rowHeaderDataLayer, IUniqueIndexLayer cornerDataLayer) {
		this(bodyDataLayer, columnHeaderDataLayer, rowHeaderDataLayer, cornerDataLayer, true);
	}
	
	public DefaultGridLayer(IUniqueIndexLayer bodyDataLayer, IUniqueIndexLayer columnHeaderDataLayer, IUniqueIndexLayer rowHeaderDataLayer, IUniqueIndexLayer cornerDataLayer, boolean useDefaultConfiguration) {
		super(useDefaultConfiguration);
		init(bodyDataLayer, columnHeaderDataLayer, rowHeaderDataLayer, cornerDataLayer);
	}

	protected DefaultGridLayer(boolean useDefaultConfiguration) {
		super(useDefaultConfiguration);
	}
	
	protected <T> void init(List<T> rowData, String[] propertyNames, Map<String, String> propertyToLabelMap) {
		init(new DefaultBodyDataProvider<T>(rowData, propertyNames), new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap));
	}
	
	protected void init(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider) {
		init(bodyDataProvider, columnHeaderDataProvider, new DefaultRowHeaderDataProvider(bodyDataProvider));
	}

	protected void init(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider) {
		init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider));
	}
	
	protected void init(IDataProvider bodyDataProvider, IDataProvider columnHeaderDataProvider, IDataProvider rowHeaderDataProvider, IDataProvider cornerDataProvider) {
		init(new DataLayer(bodyDataProvider), new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), new DefaultRowHeaderDataLayer(rowHeaderDataProvider), new DataLayer(cornerDataProvider));
	}
	
	protected void init(IUniqueIndexLayer bodyDataLayer, IUniqueIndexLayer columnHeaderDataLayer, IUniqueIndexLayer rowHeaderDataLayer, IUniqueIndexLayer cornerDataLayer) {
		// Body
		this.bodyDataLayer = bodyDataLayer;
		DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(bodyDataLayer);
		
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

		// Column header
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, selectionLayer);
		
		// Row header
		this.rowHeaderDataLayer = rowHeaderDataLayer;
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, selectionLayer);
		
		// Corner
		this.cornerDataLayer = cornerDataLayer;
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		
		setBodyLayer(bodyLayer);
		setColumnHeaderLayer(columnHeaderLayer);
		setRowHeaderLayer(rowHeaderLayer);
		setCornerLayer(cornerLayer);
	}
	
	public IUniqueIndexLayer getBodyDataLayer() {
		return bodyDataLayer;
	}
	
	@Override
	public DefaultBodyLayerStack getBodyLayer() {
		return (DefaultBodyLayerStack) super.getBodyLayer();
	}
	
	public IUniqueIndexLayer getColumnHeaderDataLayer() {
		return columnHeaderDataLayer;
	}
	
	@Override
	public ColumnHeaderLayer getColumnHeaderLayer() {
		return (ColumnHeaderLayer) super.getColumnHeaderLayer();
	}
	
	public IUniqueIndexLayer getRowHeaderDataLayer() {
		return rowHeaderDataLayer;
	}
	
	@Override
	public RowHeaderLayer getRowHeaderLayer() {
		return (RowHeaderLayer) super.getRowHeaderLayer();
	}
	
	public IUniqueIndexLayer getCornerDataLayer() {
		return cornerDataLayer;
	}
	
	@Override
	public CornerLayer getCornerLayer() {
		return (CornerLayer) super.getCornerLayer();
	}
	
}
