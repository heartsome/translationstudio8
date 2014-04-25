package net.sourceforge.nattable.ui.mode;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

public interface IModeEventHandler extends KeyListener, MouseListener, MouseMoveListener, FocusListener {

	public void cleanup();
	
}
