package net.sourceforge.nattable.ui.matcher;

import org.eclipse.swt.events.KeyEvent;

/**
 * Determines if a SWT {@link KeyEvent} matches the given criteria.
 */
public interface IKeyEventMatcher {

	public boolean matches(KeyEvent event);
	
}
