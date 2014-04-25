package net.sourceforge.nattable.ui.binding;

import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.matcher.IMouseEventMatcher;

public class MouseBinding {

	private IMouseEventMatcher mouseEventMatcher;
	
	private IMouseAction action;
	
	public MouseBinding(IMouseEventMatcher mouseEventMatcher, IMouseAction action) {
		this.mouseEventMatcher = mouseEventMatcher;
		this.action = action;
	}
	
	public IMouseEventMatcher getMouseEventMatcher() {
		return mouseEventMatcher;
	}
	
	public IMouseAction getAction() {
		return action;
	}
	
	@Override
    public String toString() {
	    return getClass().getSimpleName() + "[mouseEventMatcher=" + mouseEventMatcher + " action=" + action  + "]";
	}
}
