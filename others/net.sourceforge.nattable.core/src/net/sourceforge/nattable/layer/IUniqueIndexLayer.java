package net.sourceforge.nattable.layer;

/**
 * A layer that has a set of column and row indexes that contain no duplicates,
 * such that there is only one corresponding column or row position for a row or
 * column index in the layer.
 */
public interface IUniqueIndexLayer extends ILayer {

	public int getColumnPositionByIndex(int columnIndex);

	public int getRowPositionByIndex(int rowIndex);

}
