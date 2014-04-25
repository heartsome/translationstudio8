package net.sourceforge.nattable.resize.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class RowResizeCursorAction implements IMouseAction {
	
	private Cursor rowResizeCursor;

	public void run(NatTable natTable, MouseEvent event) {
		if (rowResizeCursor == null) {
			 rowResizeCursor = new Cursor(Display.getDefault(), SWT.CURSOR_SIZENS);
			 
			 natTable.addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					rowResizeCursor.dispose();
				}
				
			});
		}
		
		natTable.setCursor(rowResizeCursor);
	}
	
}
