package net.sourceforge.nattable.painter.cell.decorator;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.CellPainterWrapper;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class BeveledBorderDecorator extends CellPainterWrapper {

	public BeveledBorderDecorator(ICellPainter interiorPainter) {
		super(interiorPainter);
	}

	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return super.getPreferredWidth(cell, gc, configRegistry) + 4;
	}
	
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return super.getPreferredHeight(cell, gc, configRegistry) + 4;
	}

	public void paintCell(LayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		Rectangle interiorBounds = new Rectangle(rectangle.x + 2, rectangle.y + 2, rectangle.width - 4, rectangle.height - 4);
		super.paintCell(cell, gc, interiorBounds, configRegistry);
		
		// Save GC settings
		Color originalForeground = gc.getForeground();
		
		//TODO: Need to look at the border style
		
		// Up
		gc.setForeground(GUIHelper.COLOR_WIDGET_LIGHT_SHADOW);
		gc.drawLine(rectangle.x, rectangle.y, rectangle.x + rectangle.width - 1, rectangle.y);
		gc.drawLine(rectangle.x, rectangle.y, rectangle.x, rectangle.y + rectangle.height - 1);

		gc.setForeground(GUIHelper.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		gc.drawLine(rectangle.x + 1, rectangle.y + 1, rectangle.x + rectangle.width - 1, rectangle.y + 1);
		gc.drawLine(rectangle.x + 1, rectangle.y + 1, rectangle.x + 1, rectangle.y + rectangle.height - 1);

		// Down
		gc.setForeground(GUIHelper.COLOR_WIDGET_DARK_SHADOW);
		gc.drawLine(rectangle.x, rectangle.y + rectangle.height - 1, rectangle.x + rectangle.width - 1, rectangle.y + rectangle.height - 1);
		gc.drawLine(rectangle.x + rectangle.width - 1, rectangle.y, rectangle.x + rectangle.width - 1, rectangle.y + rectangle.height - 1);

		gc.setForeground(GUIHelper.COLOR_WIDGET_NORMAL_SHADOW);
		gc.drawLine(rectangle.x, rectangle.y + rectangle.height - 2, rectangle.x + rectangle.width - 1, rectangle.y + rectangle.height - 2);
		gc.drawLine(rectangle.x + rectangle.width - 2, rectangle.y, rectangle.x + rectangle.width - 2, rectangle.y + rectangle.height - 2);
		
		// Restore GC settings
		gc.setForeground(originalForeground);
	}
	
}
