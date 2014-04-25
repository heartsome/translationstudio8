package net.sourceforge.nattable.painter.layer;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class GridLineCellLayerPainter extends CellLayerPainter {

	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle rectangle, IConfigRegistry configRegistry) {
		//Draw GridLines
		drawGridLines(natLayer, gc, rectangle);

		super.paintLayer(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);
	}
	
	@Override
	public Rectangle adjustCellBounds(Rectangle bounds) {
		return new Rectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
	}
	
	protected void drawGridLines(ILayer natLayer, GC gc, Rectangle rectangle) {
		gc.setForeground(GUIHelper.COLOR_GRAY);

		drawHorizontalLines(natLayer, gc, rectangle);
		drawVerticalLines(natLayer, gc, rectangle);
		
		// paint far bottom left corner pixel
		gc.drawPoint(natLayer.getWidth() - 1, natLayer.getHeight() - 1);
	}

	private void drawHorizontalLines(ILayer natLayer, GC gc, Rectangle rectangle) {
		int gridWidth = Math.min(natLayer.getWidth() - 1, rectangle.width);
		
		int rowPositionByY = natLayer.getRowPositionByY(rectangle.y + rectangle.height);
		int maxRowPosition = rowPositionByY > 0 ? Math.min(natLayer.getRowCount(), rowPositionByY) : natLayer.getRowCount();
		for (int rowPosition = natLayer.getRowPositionByY(rectangle.y); rowPosition < maxRowPosition; rowPosition++) {
			int y = natLayer.getStartYOfRowPosition(rowPosition) + natLayer.getRowHeightByPosition(rowPosition) - 1;
			gc.drawLine(rectangle.x, y, rectangle.x + gridWidth, y);
		}
	}

	private void drawVerticalLines(ILayer natLayer, GC gc, Rectangle rectangle) {
		int gridHeight = Math.min(natLayer.getHeight() - 1, rectangle.height);
		
		int columnPositionByX = natLayer.getColumnPositionByX(rectangle.x + rectangle.width);
		int maxColumnPosition = columnPositionByX > 0 ? Math.min(natLayer.getColumnCount(), columnPositionByX) : natLayer.getColumnCount();
		for (int columnPosition = natLayer.getColumnPositionByX(rectangle.x); columnPosition < maxColumnPosition; columnPosition++) {
			int x = natLayer.getStartXOfColumnPosition(columnPosition) + natLayer.getColumnWidthByPosition(columnPosition) - 1;
			gc.drawLine(x, rectangle.y, x, rectangle.y + gridHeight);
		}
	}

}
