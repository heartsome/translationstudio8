package net.sourceforge.nattable.grid;

/**
 * A region is simply an area on the Grid.
 * Diving the table/grid into regions makes it easier to manage areas with similar behavior.
 *
 * For example all the cells in the column header are painted differently
 * and can respond to sorting actions.
 */
public interface GridRegion {

	public static final String CORNER = "CORNER";
	public static final String COLUMN_HEADER = "COLUMN_HEADER";
	public static final String COLUMN_GROUP_HEADER = "COLUMN_GROUP_HEADER";
	public static final String ROW_HEADER = "ROW_HEADER";
	public static final String BODY = "BODY";
	public static final String DATAGRID = "DATAGRID";
	public static final String FILTER_ROW = "FILTER_ROW";
}
