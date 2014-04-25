package net.sourceforge.nattable.layer.event;

import net.sourceforge.nattable.layer.ILayer;

/**
 * @see ColumnStructuralRefreshEvent
 */
public class RowStructuralRefreshEvent extends StructuralRefreshEvent {

	public RowStructuralRefreshEvent(ILayer layer) {
		super(layer);
	}
	
	@Override
	public boolean isVerticalStructureChanged() {
		return true;
	}
	
	@Override
	public boolean isHorizontalStructureChanged() {
		return false;
	}
}
