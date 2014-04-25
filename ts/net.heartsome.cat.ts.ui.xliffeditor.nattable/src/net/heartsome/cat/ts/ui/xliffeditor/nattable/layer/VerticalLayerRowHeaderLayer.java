package net.heartsome.cat.ts.ui.xliffeditor.nattable.layer;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.cell.LayerCell;

public class VerticalLayerRowHeaderLayer extends DataLayer {
	public VerticalLayerRowHeaderLayer(IDataProvider dataProvider, int defaultColumnWidth, int defaultRowHeight) {
		super(dataProvider,defaultColumnWidth,defaultRowHeight);
	}

	@Override
	public LayerCell getCellByPosition(int columnPosition, int rowPosition) {
		if (columnPosition < 0 || columnPosition >= getColumnCount() || rowPosition < 0 || rowPosition >= getRowCount()) {
			return null;
		}
		int rowSpan = 1;
		int columnSpan = 1;
		if (columnPosition == 0) {
			if((rowPosition+1)%3 == 1){
				rowSpan = 3;
			}
		}
		
		return new LayerCell(this, columnPosition, rowPosition, columnPosition, rowPosition, columnSpan, rowSpan);
	}

}
