package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Paints the background of the cell using the color from the cell style.<br/>
 * If no background color is registered in the {@link ConfigRegistry} the painting
 * is skipped.<br/>
 *
 * Example: The {@link TextPainter} inherits this and uses the paint method<br/>
 * in this class to paint the background of the cell.
 *
 * Can be used as a cell painter or a decorator.
 */
public class BackgroundPainter extends CellPainterWrapper {

	public BackgroundPainter() {}

	public BackgroundPainter(ICellPainter painter) {
		super(painter);
	}

	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		Color backgroundColor = CellStyleUtil.getCellStyle(cell, configRegistry).getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		if (backgroundColor != null) {
			Color originalBackground = gc.getBackground();

			gc.setBackground(backgroundColor);
			gc.fillRectangle(bounds);

			gc.setBackground(originalBackground);
		}

		super.paintCell(cell, gc, bounds, configRegistry);
	}

}
