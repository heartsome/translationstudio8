package net.sourceforge.nattable.search.strategy;

import net.sourceforge.nattable.coordinate.PositionCoordinate;

public interface ISearchStrategy {

	public PositionCoordinate executeSearch(Object valueToMatch);
	
}
