package net.sourceforge.nattable.ui.menu;

import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.widgets.Menu;

public interface IMenuItemProvider {
	
	/**
	 * Add an item to the popup menu.
	 * 
	 * @param natTable active table instance.
	 * @param popupMenu the SWT {@link Menu} which popus up. 
	 */
	public void addMenuItem(final NatTable natTable, final Menu popupMenu);

}
