package net.sourceforge.nattable.painter.cell;

import static net.sourceforge.nattable.util.ObjectUtils.isNotNull;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Paints the cell background using an image.<br/>
 * Image is repeated to cover the background. Similar to HTML table painting.
 */
public class BackgroundImagePainter extends CellPainterWrapper {

	public final Color separatorColor;
	private final Image bgImage;

	/**
	 * @param interiorPainter used for painting the cell contents
	 * @param bgImage to be used for painting the background
	 * @param separatorColor to be used for drawing left and right borders for the cell.
	 * 	Set to null if the borders are not required.
	 */
	public BackgroundImagePainter(ICellPainter interiorPainter, Image bgImage, Color separatorColor) {
		super(interiorPainter);
		this.bgImage = bgImage;
		this.separatorColor = separatorColor;
	}

	@Override
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return super.getPreferredWidth(cell, gc, configRegistry) + 4;
	}

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return super.getPreferredHeight(cell, gc, configRegistry) + 4;
	}

	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		// Save GC settings
		Color originalBackground = gc.getBackground();
		Color originalForeground = gc.getForeground();

		gc.setBackgroundPattern(new Pattern(Display.getCurrent(), bgImage));

		gc.fillRectangle(rectangle);
		gc.setBackgroundPattern(null);

		if (isNotNull(separatorColor)) {
			gc.setForeground(separatorColor);
			gc.drawLine(rectangle.x - 1, rectangle.y, rectangle.x - 1, rectangle.y + rectangle.height);
			gc.drawLine(rectangle.x - 1 + rectangle.width, rectangle.y, rectangle.x - 1 + rectangle.width, rectangle.y + rectangle.height);
		}

		// Restore original GC settings
		gc.setBackground(originalBackground);
		gc.setForeground(originalForeground);

		// Draw interior
		Rectangle interiorBounds = new Rectangle(rectangle.x + 2, rectangle.y + 2, rectangle.width - 4,	rectangle.height - 4);
		super.paintCell(cell, gc, interiorBounds, configRegistry);
	}

}
