package net.sourceforge.nattable.ui.util;

import static net.sourceforge.nattable.ui.util.CellEdgeEnum.BOTTOM;
import static net.sourceforge.nattable.ui.util.CellEdgeEnum.LEFT;
import static net.sourceforge.nattable.ui.util.CellEdgeEnum.NONE;
import static net.sourceforge.nattable.ui.util.CellEdgeEnum.RIGHT;
import static net.sourceforge.nattable.ui.util.CellEdgeEnum.TOP;
import static net.sourceforge.nattable.util.GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class CellEdgeDetectUtil {

	/**
	 * Calculate the column position to resize depending on the cursor's position
	 * on the left/right edges of the cell.
	 * Does <i>not</i> take into account columns which are not allowed to be resized.
	 */
	public static int getColumnPositionToResize(ILayer layer, Point clickPoint) {
		int columnPosition = layer.getColumnPositionByX(clickPoint.x);
		if (columnPosition >= 0) {
			switch (getHorizontalCellEdge(layer, clickPoint, DEFAULT_RESIZE_HANDLE_SIZE)) {
			case LEFT:
				if (columnPosition == 1) {
					// can't resize left edge of first column
					break;
				}
				return columnPosition - 1;
			case RIGHT:
				return columnPosition;
			}
		}
		return -1;
	}
	
	/**
	 * Calculate the row position to resize depending on the cursor's position
	 * on the top/bottom edges of the cell.
	 * Does not take into account rows which are not allowed to be resized.
	 */
	public static int getRowPositionToResize(ILayer layer, Point clickPt) {
		int rowPosition = layer.getRowPositionByY(clickPt.y);
		if (rowPosition >= 0) {
			switch (getVerticalCellEdge(layer, clickPt, DEFAULT_RESIZE_HANDLE_SIZE)) {
			case TOP:
				if (rowPosition == 1) {
					// can't resize top edge of first row
					break;
				}
				return rowPosition - 1;
			case BOTTOM:
				return rowPosition;
			}
		}
		return -1;
	}
	
	/**
	 * Gets the edge (left/right) of the cell which is closer to the click point.  
	 * @param cellBounds bounds of the cell containing the click
	 * @param clickPt usually the coordinates of a mouse click
	 */
	public static CellEdgeEnum getHorizontalCellEdge(Rectangle cellBounds, Point clickPt) {
		return getHorizontalCellEdge(cellBounds, clickPt, -1);
	}

	public static CellEdgeEnum getHorizontalCellEdge(ILayer layer, Point clickPt) {
		return getHorizontalCellEdge(layer, clickPt, -1);
	}
	
	public static CellEdgeEnum getHorizontalCellEdge(ILayer layer, Point clickPt, int handleWidth) {
		LayerCell cell = layer.getCellByPosition(
				layer.getColumnPositionByX(clickPt.x),
				layer.getRowPositionByY(clickPt.y)
		);
		if (cell != null) {
			return getHorizontalCellEdge(cell.getBounds(), clickPt, handleWidth);
		} else {
			return CellEdgeEnum.NONE;
		}
	}

	/**
	 * Figure out if the click point is closer to the left/right edge of the cell.
	 * @param cellBounds of the table cell containing the click
	 * @param clickPt
	 * @param distanceFromEdge distance from the edge to qualify as <i>close</i> to the cell edge
	 */
	public static CellEdgeEnum getHorizontalCellEdge(Rectangle cellBounds, Point clickPt, int distanceFromEdge) {
		if (distanceFromEdge < 0) {
			distanceFromEdge = cellBounds.width / 2;
		}

		Rectangle left = new Rectangle(cellBounds.x, cellBounds.y, distanceFromEdge, cellBounds.height);
		Rectangle right = new Rectangle(cellBounds.x + cellBounds.width - distanceFromEdge, cellBounds.y, 
		        distanceFromEdge, cellBounds.height);

		if (left.contains(clickPt)) {
			return LEFT;
		} else if (right.contains(clickPt)) {
			return RIGHT;
		} else {
			return NONE;
		}
	}

	public static CellEdgeEnum getVerticalCellEdge(ILayer layer, Point clickPt, int handleHeight) {
		LayerCell cell = layer.getCellByPosition(
				layer.getColumnPositionByX(clickPt.x), 
				layer.getRowPositionByY(clickPt.y)
		);
		return getVerticalCellEdge(cell.getBounds(), clickPt, handleHeight);
	}

	/**
	 * @see CellEdgeDetectUtil#getHorizontalCellEdge(Rectangle, Point, int)
	 */
	private static CellEdgeEnum getVerticalCellEdge(Rectangle cellBounds, Point clickPt, int distanceFromEdge) {
		if (distanceFromEdge < 0) {
			distanceFromEdge = cellBounds.height / 2;
		}

		Rectangle top = new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, distanceFromEdge);
		Rectangle bottom = new Rectangle(cellBounds.x, cellBounds.y + cellBounds.height - distanceFromEdge, cellBounds.width, distanceFromEdge);

		if (top.contains(clickPt)) {
			return TOP;
		} else if (bottom.contains(clickPt)) {
			return BOTTOM;
		} else {
			return NONE;
		}
	}
}
