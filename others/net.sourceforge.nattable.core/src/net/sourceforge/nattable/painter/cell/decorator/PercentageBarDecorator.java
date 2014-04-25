package net.sourceforge.nattable.painter.cell.decorator;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.CellPainterWrapper;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Draws a rectangular bar in cell proportional to the value of the cell.
 */
public class PercentageBarDecorator extends CellPainterWrapper {

	public PercentageBarDecorator(ICellPainter interiorPainter) {
		super(interiorPainter);
	}

	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		Color originalBackground = gc.getBackground();

		double factor = Math.min(1.0, ((Double) cell.getDataValue()).doubleValue());
		factor = Math.max(0.0, factor);

		Rectangle bar = new Rectangle(rectangle.x, rectangle.y, (int)(rectangle.width * factor), rectangle.height);
		Rectangle bounds = cell.getBounds();
		gc.setBackgroundPattern(new Pattern(Display.getCurrent(),
				bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
				GUIHelper.getColor(new RGB(187, 216, 254)),
				GUIHelper.getColor(new RGB(255, 255, 255))));
		gc.fillRectangle(bar);

		gc.setBackground(originalBackground);

		super.paintCell(cell, gc, rectangle, configRegistry);
	}

}
