package net.sourceforge.nattable.ui.action;

import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.events.MouseEvent;

public interface IDragMode {

	public void mouseDown(NatTable natTable, MouseEvent event);
	
	public void mouseMove(NatTable natTable, MouseEvent event);
	
	public void mouseUp(NatTable natTable, MouseEvent event);
	
}
