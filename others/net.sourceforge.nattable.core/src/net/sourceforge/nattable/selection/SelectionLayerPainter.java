package net.sourceforge.nattable.selection;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.layer.GridLineCellLayerPainter;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class SelectionLayerPainter extends GridLineCellLayerPainter {

	private int columnPositionOffset;
	
	private int rowPositionOffset;
	
	private Map<PositionCoordinate, LayerCell> cells;
	
	@Override
	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle pixelRectangle, IConfigRegistry configRegistry) {
		Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
		columnPositionOffset = positionRectangle.x;
		rowPositionOffset = positionRectangle.y;
		cells = new HashMap<PositionCoordinate, LayerCell>();
		
		super.paintLayer(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);
		
		// Save gc settings
		int originalLineStyle = gc.getLineStyle();
		Color originalForeground = gc.getForeground();
		
		// Apply border settings
		gc.setLineStyle(SWT.LINE_CUSTOM);
		gc.setLineDash(new int[] { 1, 1 });
		gc.setForeground(GUIHelper.COLOR_BLACK);
		
		// Draw horizontal borders
		boolean selectedMode = false;
		for (int columnPosition = columnPositionOffset; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++) {
			LayerCell cell = null;
			for (int rowPosition = rowPositionOffset; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++) {
				cell = cells.get(new PositionCoordinate(natLayer, columnPosition, rowPosition));
				if (cell != null) {
					if (selectedMode != isSelected(cell)) {
						selectedMode = !selectedMode;
						Rectangle cellBounds = cell.getBounds();
						// Draw top edge
						gc.drawLine(
								cellBounds.x - 1,
								cellBounds.y - 1,
								cellBounds.x + cellBounds.width - 1,
								cellBounds.y - 1
						);
					}
				}
			}
			if (selectedMode && cell != null) {
				// If last cell is selected, draw its bottom edge
				Rectangle cellBounds = cell.getBounds();
				gc.drawLine(
						cellBounds.x - 1,
						cellBounds.y + cellBounds.height - 1,
						cellBounds.x + cellBounds.width - 1,
						cellBounds.y + cellBounds.height - 1
				);
			}
			selectedMode = false;
		}
		
		// Draw vertical borders
		for (int rowPosition = rowPositionOffset; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++) {
			LayerCell cell = null;
			for (int columnPosition = columnPositionOffset; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++) {
				cell = cells.get(new PositionCoordinate(natLayer, columnPosition, rowPosition));
				if (cell != null) {
					if (selectedMode != isSelected(cell)) {
						selectedMode = !selectedMode;
						Rectangle cellBounds = cell.getBounds();
						// Draw left edge
						gc.drawLine(
								cellBounds.x - 1,
								cellBounds.y - 1,
								cellBounds.x - 1,
								cellBounds.y + cellBounds.height - 1
						);
					}
				}
			}
			if (selectedMode && cell != null) {
				// If last cell is selected, draw its right edge
				Rectangle cellBounds = cell.getBounds();
				gc.drawLine(
						cellBounds.x + cellBounds.width - 1,
						cellBounds.y - 1,
						cellBounds.x + cellBounds.width - 1,
						cellBounds.y + cellBounds.height - 1
				);
			}
			selectedMode = false;
		}
		
		// Restore original gc settings
		gc.setLineStyle(originalLineStyle);
		gc.setForeground(originalForeground);
	}
	
	@Override
	protected void paintCell(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		for (int columnPosition = cell.getColumnPosition(); columnPosition < cell.getColumnPosition() + cell.getColumnSpan(); columnPosition++) {
			for (int rowPosition = cell.getRowPosition(); rowPosition < cell.getRowPosition() + cell.getRowSpan(); rowPosition++) {
				cells.put(new PositionCoordinate(cell.getLayer(), columnPosition, rowPosition), cell);
			}
		}
		
		super.paintCell(cell, gc, configRegistry);
	}
	
	private boolean isSelected(LayerCell cell) {
		return cell.getDisplayMode() == DisplayMode.SELECT;
	}
	
}
