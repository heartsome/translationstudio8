package net.sourceforge.nattable.painter.layer;

import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.ICellPainter;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class CellLayerPainter implements ILayerPainter {

	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle pixelRectangle, IConfigRegistry configRegistry) {
		if (pixelRectangle.width <= 0 || pixelRectangle.height <= 0) {
			return;
		}
		
		Collection<LayerCell> spannedCells = new HashSet<LayerCell>();
		
		Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
		
		for (int columnPosition = positionRectangle.x; columnPosition < positionRectangle.x + positionRectangle.width; columnPosition++) {
			for (int rowPosition = positionRectangle.y; rowPosition < positionRectangle.y + positionRectangle.height; rowPosition++) {
				LayerCell cell = natLayer.getCellByPosition(columnPosition, rowPosition);
				if (cell != null) {
					if (cell.isSpannedCell()) {
						spannedCells.add(cell);
					} else {
						paintCell(cell, gc, configRegistry);
					}
				}
			}
		}
		
		for (LayerCell cell : spannedCells) {
			paintCell(cell, gc, configRegistry);
		}
	}
	
	public Rectangle adjustCellBounds(Rectangle cellBounds) {
		return cellBounds;
	}
	
	protected Rectangle getPositionRectangleFromPixelRectangle(ILayer natLayer, Rectangle pixelRectangle) {
		int columnPositionOffset = natLayer.getColumnPositionByX(pixelRectangle.x);
		int rowPositionOffset = natLayer.getRowPositionByY(pixelRectangle.y);
		int numColumns = natLayer.getColumnPositionByX(Math.min(natLayer.getWidth(), pixelRectangle.x + pixelRectangle.width) - 1) - columnPositionOffset + 1;
		int numRows = natLayer.getRowPositionByY(Math.min(natLayer.getHeight(), pixelRectangle.y + pixelRectangle.height) - 1) - rowPositionOffset + 1;
		
		return new Rectangle(columnPositionOffset, rowPositionOffset, numColumns, numRows);
	}

	protected void paintCell(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		ICellPainter cellPainter = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_PAINTER, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		Rectangle adjustedCellBounds = adjustCellBounds(cell.getBounds());
		cellPainter.paintCell(cell, gc, adjustedCellBounds, configRegistry);
	}

}
