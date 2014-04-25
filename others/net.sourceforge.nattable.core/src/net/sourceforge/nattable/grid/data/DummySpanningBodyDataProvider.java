package net.sourceforge.nattable.grid.data;

import net.sourceforge.nattable.data.ISpanningDataProvider;
import net.sourceforge.nattable.layer.cell.DataCell;

public class DummySpanningBodyDataProvider extends DummyBodyDataProvider implements ISpanningDataProvider {
	
	private static final int BLOCK_SIZE = 4;

	private static final int CELL_SPAN = 2;
	
	public DummySpanningBodyDataProvider(int columnCount, int rowCount) {
		super(columnCount, rowCount);
	}
	
	public DataCell getCellByPosition(int columnPosition, int rowPosition) {
		int columnBlock = columnPosition / BLOCK_SIZE;
		int rowBlock = rowPosition / BLOCK_SIZE;
		
		boolean isSpanned = isEven(columnBlock + rowBlock) && (columnPosition % BLOCK_SIZE) < CELL_SPAN && (rowPosition % BLOCK_SIZE) < CELL_SPAN;
		int columnSpan = isSpanned ? CELL_SPAN : 1;
		int rowSpan = isSpanned ? CELL_SPAN : 1;
		
		int cellColumnPosition = columnPosition;
		int cellRowPosition = rowPosition;
		
		if (isSpanned) {
			cellColumnPosition -= columnPosition % BLOCK_SIZE;
			cellRowPosition -= rowPosition % BLOCK_SIZE;
		}
		
		return new DataCell(cellColumnPosition, cellRowPosition, columnSpan, rowSpan);
	}
	
	private boolean isEven(int i) {
		return i % 2 == 0;
	}
	
}
