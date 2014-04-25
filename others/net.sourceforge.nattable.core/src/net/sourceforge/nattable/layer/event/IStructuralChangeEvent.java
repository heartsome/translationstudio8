package net.sourceforge.nattable.layer.event;

import java.util.Collection;

/**
 * An event indicating a structural change to the layer. A structural change is
 * defined as something that modifies the number of columns/rows in the layer or
 * their associated widths/heights.
 */
public interface IStructuralChangeEvent extends IVisualChangeEvent {

	public boolean isHorizontalStructureChanged();
	
	public Collection<StructuralDiff> getColumnDiffs();
	
	public boolean isVerticalStructureChanged();
	
	public Collection<StructuralDiff> getRowDiffs();
	
}
