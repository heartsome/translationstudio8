package net.sourceforge.nattable.freeze.event;

import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralRefreshEvent;

public class FreezeEvent extends StructuralRefreshEvent {

	public FreezeEvent(ILayer layer) {
		super(layer);
	}
	
	protected FreezeEvent(FreezeEvent event) {
		super(event);
	}
	
	public FreezeEvent cloneEvent() {
		return new FreezeEvent(this);
	}
	
	public Collection<StructuralDiff> getColumnDiffs() {
		return null;
	}
	
	public Collection<StructuralDiff> getRowDiffs() {
		return null;
	}
	
}
