package net.sourceforge.nattable.ui.binding;

import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public interface IUiBindingRegistry {
	
	public IKeyAction getKeyEventAction(KeyEvent event);
	
	public IDragMode getDragMode(MouseEvent event);
	
	public IMouseAction getMouseMoveAction(MouseEvent event);
	
	public IMouseAction getMouseDownAction(MouseEvent event);
	
	public IMouseAction getSingleClickAction(MouseEvent event);
	
	public IMouseAction getDoubleClickAction(MouseEvent event);

}
