package net.sourceforge.nattable.selection;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

/**
 * Indicates an {@link ILayer} that supports the selection of individual cells. Classes should implement this interface
 * if they need to customize selection logic. 
 */
public interface Selectable {

    /**
     * Determine if a cell at a given position is selected. 
     * 
     * @param p cell to query
     * @return <code>true</code> if the given cell is selected
     */
    public boolean isSelected(PositionCoordinate p);
}
