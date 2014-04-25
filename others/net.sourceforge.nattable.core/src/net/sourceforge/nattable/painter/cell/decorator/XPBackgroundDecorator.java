package net.sourceforge.nattable.painter.cell.decorator;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.BackgroundPainter;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class XPBackgroundDecorator extends BackgroundPainter {

	public final Color separatorColor;
	
	public final Color gradientColor1;
	public final Color gradientColor2;
	public final Color gradientColor3;
	
	public final Color highlightColor1;
	public final Color highlightColor2;
	public final Color highlightColor3;

	public XPBackgroundDecorator(ICellPainter interiorPainter) {
		super(interiorPainter);
		
		separatorColor = GUIHelper.getColor(199, 197, 178);
		
		gradientColor1 = GUIHelper.getColor(226, 222, 205);
		gradientColor2 = GUIHelper.getColor(214, 210, 194);
		gradientColor3 = GUIHelper.getColor(203, 199, 184);
		
		highlightColor1 = GUIHelper.getColor(250, 171, 0);
		highlightColor2 = GUIHelper.getColor(252, 194, 71);
		highlightColor3 = GUIHelper.getColor(250, 178, 24);
	}

	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return super.getPreferredWidth(cell, gc, configRegistry) + 4;
	}
	
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return super.getPreferredHeight(cell, gc, configRegistry) + 4;
	}

	public void paintCell(LayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		// Draw background
		super.paintCell(cell, gc, rectangle, configRegistry);

		// Draw interior
		Rectangle interiorBounds = new Rectangle(rectangle.x + 2, rectangle.y + 2, rectangle.width - 4, rectangle.height - 4);
		super.paintCell(cell, gc, interiorBounds, configRegistry);
		
		// Save GC settings
		Color originalBackground = gc.getBackground();
		Color originalForeground = gc.getForeground();
		
		// Draw separator
		int x = rectangle.x;
		gc.setForeground(GUIHelper.COLOR_WHITE);
		gc.drawLine(x, rectangle.y + 3, x, rectangle.y + rectangle.height - 6);

		x = rectangle.x + rectangle.width - 1;
		gc.setForeground(separatorColor);
		gc.drawLine(x, rectangle.y + 3, x, rectangle.y + rectangle.height - 6);
		
		// Restore GC settings
		gc.setBackground(originalBackground);
		gc.setForeground(originalForeground);

		// Draw bottom edge
		boolean isHighlight = false;
		
		int y = rectangle.y + rectangle.height - 3;
		gc.setForeground(isHighlight ? highlightColor1 : gradientColor1);
		gc.drawLine(rectangle.x, y, rectangle.x + rectangle.width, y);
		
		y++;
		gc.setForeground(isHighlight ? highlightColor2 : gradientColor2);
		gc.drawLine(rectangle.x, y, rectangle.x + rectangle.width, y);

		y++;
		gc.setForeground(isHighlight ? highlightColor3 : gradientColor3);
		gc.drawLine(rectangle.x, y, rectangle.x + rectangle.width, y);
	}
	
}
