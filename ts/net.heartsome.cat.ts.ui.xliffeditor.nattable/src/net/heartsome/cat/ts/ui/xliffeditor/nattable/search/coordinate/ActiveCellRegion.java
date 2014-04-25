package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate;

public class ActiveCellRegion {

	private static CellRegion activeCellRegion;
	
	public static synchronized CellRegion getActiveCellRegion() {
		return activeCellRegion;
	}
	
	public static synchronized void setActiveCellRegion(CellRegion region) {
		activeCellRegion = region;
	}
}
