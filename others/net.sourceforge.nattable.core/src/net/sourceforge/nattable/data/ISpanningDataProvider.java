package net.sourceforge.nattable.data;

import net.sourceforge.nattable.layer.cell.DataCell;

public interface ISpanningDataProvider extends IDataProvider {

	public DataCell getCellByPosition(int columnPosition, int rowPosition);
	
}
