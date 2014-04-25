package net.sourceforge.nattable.layer;

import net.sourceforge.nattable.data.ISpanningDataProvider;
import net.sourceforge.nattable.layer.cell.DataCell;
import net.sourceforge.nattable.layer.cell.LayerCell;

public class SpanningDataLayer extends DataLayer {
	
	public SpanningDataLayer(ISpanningDataProvider dataProvider) {
		super(dataProvider);
	}
	
	public SpanningDataLayer(ISpanningDataProvider dataProvider, int defaultColumnWidth, int defaultRowHeight) {
		super(dataProvider, defaultColumnWidth, defaultRowHeight);
	}
	
	protected SpanningDataLayer() {
		super();
	}

	protected SpanningDataLayer(int defaultColumnWidth, int defaultRowHeight) {
		super(defaultColumnWidth, defaultRowHeight);
	}

	@Override
	public ISpanningDataProvider getDataProvider() {
		return (ISpanningDataProvider) super.getDataProvider();
	}
	
	@Override
	public LayerCell getCellByPosition(int columnPosition, int rowPosition) {
		if (columnPosition < 0 || columnPosition >= getColumnCount()
				|| rowPosition < 0 || rowPosition >= getRowCount()) {
			return null;
		}
		
		DataCell dataCell = getDataProvider().getCellByPosition(columnPosition, rowPosition);
		
		return new LayerCell(this, columnPosition, rowPosition, dataCell);
	}
	
}
