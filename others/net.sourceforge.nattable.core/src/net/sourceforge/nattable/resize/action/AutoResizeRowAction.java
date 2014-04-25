package net.sourceforge.nattable.resize.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.resize.command.InitializeAutoResizeRowsCommand;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.util.CellEdgeDetectUtil;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class AutoResizeRowAction implements IMouseAction {
	
	private GC gc;

	public void run(NatTable natTable, MouseEvent event) {
		if (gc == null) {
			gc = new GC(natTable);
			
			natTable.addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					gc.dispose();
				}
				
			});
		}
		
		Point clickPoint = new Point(event.x, event.y);
		int row = CellEdgeDetectUtil.getRowPositionToResize(natTable, clickPoint);

		InitializeAutoResizeRowsCommand command = new InitializeAutoResizeRowsCommand(natTable, row, natTable.getConfigRegistry(), gc);
		natTable.doCommand(command);
	}
	
}