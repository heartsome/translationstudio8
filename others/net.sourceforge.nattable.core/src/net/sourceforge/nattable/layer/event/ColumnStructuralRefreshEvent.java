package net.sourceforge.nattable.layer.event;

import net.sourceforge.nattable.layer.ILayer;

/**
 * General event indicating that columns cached by the layers need refreshing. <br/>
 * 
 * Note: As opposed to the the {@link ColumnStructuralChangeEvent} this event does not <br/>
 * indicate the specific columns which have changed. <br/>
 */
public class ColumnStructuralRefreshEvent extends StructuralRefreshEvent {

	public ColumnStructuralRefreshEvent(ILayer layer) {
		super(layer);
	}
	
	@Override
	public boolean isHorizontalStructureChanged() {
		return true;
	}
	
	@Override
	public boolean isVerticalStructureChanged() {
		return false;
	}
}
