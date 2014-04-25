package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public abstract class CellPainterWrapper implements ICellPainter {

	private ICellPainter wrappedPainter;

	public CellPainterWrapper() {}

	public CellPainterWrapper(ICellPainter painter) {
		this.wrappedPainter = painter;
	}

	public void setWrappedPainter(ICellPainter painter) {
		this.wrappedPainter = painter;
	}

	public ICellPainter getWrappedPainter() {
		return wrappedPainter;
	}

	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return wrappedPainter != null ? wrappedPainter.getPreferredWidth(cell, gc, configRegistry) : 0;
	}

	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return wrappedPainter != null ? wrappedPainter.getPreferredHeight(cell, gc, configRegistry) : 0;
	}

	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		if (wrappedPainter != null) {
			wrappedPainter.paintCell(cell, gc, bounds, configRegistry);
		}
	}

}
