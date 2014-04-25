package net.sourceforge.nattable.viewport.event;

import java.util.Collection;

import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralRefreshEvent;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ScrollEvent extends StructuralRefreshEvent {

	public ScrollEvent(ViewportLayer viewportLayer) {
		super(viewportLayer);
	}
	
	protected ScrollEvent(ScrollEvent event) {
		super(event);
	}
	
	public ScrollEvent cloneEvent() {
		return new ScrollEvent(this);
	}
	
	public Collection<StructuralDiff> getColumnDiffs() {
		// TODO this is bogus - should have a horiz/vert scroll event instead that are multi col/row structural changes
		return null;
	}
	
	public Collection<StructuralDiff> getRowDiffs() {
		// TODO this is bogus - should have a horiz/vert scroll event instead that are multi col/row structural changes
		return null;
	}
	
}