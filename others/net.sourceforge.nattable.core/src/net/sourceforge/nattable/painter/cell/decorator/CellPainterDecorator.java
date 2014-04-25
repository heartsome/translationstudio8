package net.sourceforge.nattable.painter.cell.decorator;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.ui.util.CellEdgeEnum;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Decorates a cell painter with another cell painter.
 */
public class CellPainterDecorator implements ICellPainter {

	private final ICellPainter baseCellPainter;
	private final CellEdgeEnum cellEdge;
	private final int spacing;
	private final ICellPainter decoratorCellPainter;

	public CellPainterDecorator(ICellPainter baseCellPainter, CellEdgeEnum cellEdge, ICellPainter decoratorCellPainter) {
		this(baseCellPainter, cellEdge, 2, decoratorCellPainter);
	}
	
	public CellPainterDecorator(ICellPainter baseCellPainter, CellEdgeEnum cellEdge, int spacing, ICellPainter decoratorCellPainter) {
		this.baseCellPainter = baseCellPainter;
		this.cellEdge = cellEdge;
		this.spacing = spacing;
		this.decoratorCellPainter = decoratorCellPainter;
	}

	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return baseCellPainter.getPreferredWidth(cell, gc, configRegistry) + spacing + decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry);
	}
	
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return baseCellPainter.getPreferredHeight(cell, gc, configRegistry) + spacing + decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry);
	}

	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		int x = bounds.x + (cellEdge == CellEdgeEnum.LEFT ? decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry) + spacing : 0);
		int y = bounds.y + (cellEdge == CellEdgeEnum.TOP ? decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry) + spacing : 0);
		Rectangle baseCellPainterBounds = new Rectangle(x, y, bounds.width, bounds.height).intersection(bounds);
		baseCellPainter.paintCell(cell, gc, baseCellPainterBounds, configRegistry);
		
		x = bounds.x + (cellEdge == CellEdgeEnum.RIGHT ? bounds.width - spacing - decoratorCellPainter.getPreferredWidth(cell, gc, configRegistry) : 0);
		y = bounds.y + (cellEdge == CellEdgeEnum.BOTTOM ? bounds.height - spacing - decoratorCellPainter.getPreferredHeight(cell, gc, configRegistry) : 0);
		Rectangle decoratorCellPainterBounds = new Rectangle(x, y, bounds.width, bounds.height).intersection(bounds);
		decoratorCellPainter.paintCell(cell, gc, decoratorCellPainterBounds, configRegistry);
	}
	
}
