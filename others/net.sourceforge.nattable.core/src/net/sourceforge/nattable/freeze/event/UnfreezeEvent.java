package net.sourceforge.nattable.freeze.event;

import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralRefreshEvent;

public class UnfreezeEvent extends StructuralRefreshEvent {

	public UnfreezeEvent(ILayer layer) {
		super(layer);
	}
	
	protected UnfreezeEvent(UnfreezeEvent event) {
		super(event);
	}
	
	public UnfreezeEvent cloneEvent() {
		return new UnfreezeEvent(this);
	}
	
	public Collection<StructuralDiff> getColumnDiffs() {
		return null;
	}
	
	public Collection<StructuralDiff> getRowDiffs() {
		return null;
	}

}
