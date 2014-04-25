package net.sourceforge.nattable.resize.event;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.ui.util.CellEdgeDetectUtil;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

public class ColumnResizeEventMatcher extends MouseEventMatcher {

	public ColumnResizeEventMatcher(int stateMask, int button) {
		super(stateMask, GridRegion.COLUMN_HEADER, button);
	}

	@Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
		return super.matches(natTable, event, regionLabels) && isColumnResizable(natTable, event);
	}

	private boolean isColumnResizable(ILayer natLayer, MouseEvent event) {
		int columnPosition = 
			CellEdgeDetectUtil.getColumnPositionToResize(natLayer, new Point(event.x, event.y));
		
		if (columnPosition < 0) {
			return false;
		} else {
			return natLayer.isColumnPositionResizable(columnPosition);
		}
	}
	
}
