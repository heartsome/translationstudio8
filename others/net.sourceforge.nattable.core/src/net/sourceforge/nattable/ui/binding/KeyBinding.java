package net.sourceforge.nattable.ui.binding;

import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.matcher.IKeyEventMatcher;

public class KeyBinding {

	private IKeyEventMatcher keyEventMatcher;
	
	private IKeyAction action;
	
	public KeyBinding(IKeyEventMatcher keyEventMatcher, IKeyAction action) {
		this.keyEventMatcher = keyEventMatcher;
		this.action = action;
	}
	
	public IKeyEventMatcher getKeyEventMatcher() {
		return keyEventMatcher;
	}
	
	public IKeyAction getAction() {
		return action;
	}
	
}
