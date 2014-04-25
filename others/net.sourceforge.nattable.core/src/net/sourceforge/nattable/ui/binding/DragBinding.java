package net.sourceforge.nattable.ui.binding;

import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.ui.matcher.IMouseEventMatcher;

public class DragBinding {
	
	private IMouseEventMatcher mouseEventMatcher;
	
	private IDragMode dragMode;
	
	public DragBinding(IMouseEventMatcher mouseEventMatcher, IDragMode dragMode) {
		this.mouseEventMatcher = mouseEventMatcher;
		this.dragMode = dragMode;
	}
	
	public IMouseEventMatcher getMouseEventMatcher() {
		return mouseEventMatcher;
	}
	
	public IDragMode getDragMode() {
		return dragMode;
	}
	
}
