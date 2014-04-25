package net.sourceforge.nattable.painter.cell.decorator;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.CellPainterWrapper;
import net.sourceforge.nattable.painter.cell.ICellPainter;

public class PaddingDecorator extends CellPainterWrapper {
	
	private final int topPadding;
	private final int rightPadding;
	private final int bottomPadding;
	private final int leftPadding;

	public PaddingDecorator(ICellPainter interiorPainter) {
		this(interiorPainter, 2);
	}
	
	public PaddingDecorator(ICellPainter interiorPainter, int padding) {
		this(interiorPainter, padding, padding, padding, padding);
	}
	
	public PaddingDecorator(ICellPainter interiorPainter, int topPadding, int rightPadding, int bottomPadding, int leftPadding) {
		super(interiorPainter);
		this.topPadding = topPadding;
		this.rightPadding = rightPadding;
		this.bottomPadding = bottomPadding;
		this.leftPadding = leftPadding;
	}

	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return leftPadding + super.getPreferredWidth(cell, gc, configRegistry) + rightPadding;
	}
	
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return topPadding + super.getPreferredHeight(cell, gc, configRegistry) + bottomPadding;
	}

	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		Rectangle interiorBounds =
			new Rectangle(
					bounds.x + leftPadding,
					bounds.y + topPadding,
					bounds.width - leftPadding - rightPadding,
					bounds.height - topPadding - bottomPadding
			);
		
		super.paintCell(cell, gc, interiorBounds, configRegistry);
	}
	
}
