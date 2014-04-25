package net.sourceforge.nattable.freeze.command;

import net.sourceforge.nattable.coordinate.PositionCoordinate;

interface IFreezeCoordinatesProvider {
	
	public PositionCoordinate getTopLeftPosition();
	
	public PositionCoordinate getBottomRightPosition();
	
}
