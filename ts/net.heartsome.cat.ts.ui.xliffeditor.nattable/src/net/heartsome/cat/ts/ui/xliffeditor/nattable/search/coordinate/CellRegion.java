package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate;

import net.sourceforge.nattable.coordinate.PositionCoordinate;

import org.eclipse.jface.text.IRegion;

public class CellRegion {

	public CellRegion(PositionCoordinate positionCoordinate, IRegion region) {
		this.positionCoordinate = positionCoordinate;
		this.region = region;
	}

	private PositionCoordinate positionCoordinate;

	private IRegion region;

	public void setPositionCoordinate(PositionCoordinate positionCoordinate) {
		this.positionCoordinate = positionCoordinate;
	}

	public PositionCoordinate getPositionCoordinate() {
		return positionCoordinate;
	}

	public void setRegion(IRegion region) {
		this.region = region;
	}

	public IRegion getRegion() {
		return region;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PositionCoordinate)) {
			return false;
		}
		CellRegion coordinate = (CellRegion) obj;
		try {
			return this.positionCoordinate.equals(coordinate.positionCoordinate)
					&& this.region.equals(coordinate.region);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return positionCoordinate.toString() + " | " + region.toString();
	}
}
