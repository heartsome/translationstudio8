package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.freeze.FreezeLayer;

class FreezeColumnStrategy implements IFreezeCoordinatesProvider {

	private final FreezeLayer freezeLayer;
	
	private final int columnPosition;

	FreezeColumnStrategy(FreezeLayer freezeLayer, int columnPosition) {
		this.freezeLayer = freezeLayer;
		this.columnPosition = columnPosition;
	}

	public PositionCoordinate getTopLeftPosition() {
		return new PositionCoordinate(freezeLayer, 0, -1);
	}
	
	public PositionCoordinate getBottomRightPosition() {
		return new PositionCoordinate(freezeLayer, columnPosition, -1);
	}
	
}
