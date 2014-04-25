package net.sourceforge.nattable.resize.mode;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.painter.IOverlayPainter;
import net.sourceforge.nattable.resize.command.ColumnResizeCommand;
import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.ui.util.CellEdgeDetectUtil;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Drag mode that will implement the column resizing process.
 */
public class ColumnResizeDragMode implements IDragMode {

	private static final int DEFAULT_COLUMN_WIDTH_MINIMUM = 25;

	private int columnPositionToResize;
	private int originalColumnWidth;
	private int startX;
	private int currentX;
	private int lastX = -1;
	private int gridColumnStartX;

	private final IOverlayPainter overlayPainter = new ColumnResizeOverlayPainter();

	public void mouseDown(NatTable natTable, MouseEvent event) {
		natTable.forceFocus();
		columnPositionToResize =
		    CellEdgeDetectUtil.getColumnPositionToResize(natTable, new Point(event.x, event.y));
		if (columnPositionToResize >= 0) {
		    gridColumnStartX = natTable.getStartXOfColumnPosition(columnPositionToResize);
		    originalColumnWidth = natTable.getColumnWidthByPosition(columnPositionToResize);
		    startX = event.x;
		    natTable.addOverlayPainter(overlayPainter);
		}
	}

	public void mouseMove(NatTable natTable, MouseEvent event) {
		if (event.x > natTable.getWidth()) {
			return;
		}
	    this.currentX = event.x;
	    if (currentX < gridColumnStartX + getColumnWidthMinimum()) {
	        currentX = gridColumnStartX + getColumnWidthMinimum();
	    } else {
	    	int overlayExtent = ColumnResizeOverlayPainter.COLUMN_RESIZE_OVERLAY_WIDTH / 2;

	    	Set<Integer> columnsToRepaint = new HashSet<Integer>();

	    	columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(currentX - overlayExtent)));
	    	columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(currentX + overlayExtent)));

	    	if (lastX >= 0) {
	    		columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(lastX - overlayExtent)));
	    		columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(lastX + overlayExtent)));
	    	}

	    	for (Integer columnToRepaint : columnsToRepaint) {
	    		natTable.repaintColumn(columnToRepaint.intValue());
	    	}

	        lastX = currentX;
	    }
	}

	public void mouseUp(NatTable natTable, MouseEvent event) {
	    natTable.removeOverlayPainter(overlayPainter);
		updateColumnWidth(natTable, event);
	}

	private void updateColumnWidth(ILayer natLayer, MouseEvent e) {
	    int dragWidth = e.x - startX;
        int newColumnWidth = originalColumnWidth + dragWidth;
        if (newColumnWidth < getColumnWidthMinimum()) newColumnWidth = getColumnWidthMinimum();
		natLayer.doCommand(new ColumnResizeCommand(natLayer, columnPositionToResize, newColumnWidth));
	}

	// XXX: This method must ask the layer what it's minimum width is!
	private int getColumnWidthMinimum() {
	    return DEFAULT_COLUMN_WIDTH_MINIMUM;
	}

	private class ColumnResizeOverlayPainter implements IOverlayPainter {

		static final int COLUMN_RESIZE_OVERLAY_WIDTH = 2;

	    public void paintOverlay(GC gc, ILayer layer) {
	        Color originalBackgroundColor = gc.getBackground();
	        gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
	        gc.fillRectangle(currentX - (COLUMN_RESIZE_OVERLAY_WIDTH / 2), 0, COLUMN_RESIZE_OVERLAY_WIDTH, layer.getHeight());
	        gc.setBackground(originalBackgroundColor);
	    }
	}
}
