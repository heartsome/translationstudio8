package net.sourceforge.nattable.resize.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class ColumnResizeCursorAction implements IMouseAction {

	private Cursor columnResizeCursor;

	public void run(NatTable natTable, MouseEvent event) {
		if (columnResizeCursor == null) {
			columnResizeCursor = new Cursor(Display.getDefault(), SWT.CURSOR_SIZEWE);
			
			natTable.addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					columnResizeCursor.dispose();
				}
				
			});
		}
		
		natTable.setCursor(columnResizeCursor);
	}
	
}
