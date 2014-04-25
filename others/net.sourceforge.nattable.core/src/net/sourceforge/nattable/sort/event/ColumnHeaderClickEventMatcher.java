package net.sourceforge.nattable.sort.event;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.ui.util.CellEdgeDetectUtil;
import net.sourceforge.nattable.ui.util.CellEdgeEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * Matches a click on the column header, except if the click is on the column edge.<br/>
 */
public class ColumnHeaderClickEventMatcher extends MouseEventMatcher {

	public ColumnHeaderClickEventMatcher(int stateMask, int button) {
		super(stateMask, GridRegion.COLUMN_HEADER, button);
	}

	@Override
    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
		return super.matches(natTable, event, regionLabels) && isNearTheHeaderEdge(natTable, event);
	}

	private boolean isNearTheHeaderEdge(ILayer natLayer, MouseEvent event) {
		CellEdgeEnum cellEdge = CellEdgeDetectUtil.getHorizontalCellEdge(
                                                           natLayer, 
                                                           new Point(event.x, event.y), 
                                                           GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE);
		return cellEdge == CellEdgeEnum.NONE;
	}
	
}
