package net.sourceforge.nattable.ui.menu;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;

public class PopupMenuAction implements IMouseAction {
	
	private Menu menu;
	
	public PopupMenuAction(Menu menu) {
		this.menu = menu;
	}
	
	public void run(NatTable natTable, MouseEvent event) {
		menu.setData(event.data);
		menu.setVisible(true);
	}
}