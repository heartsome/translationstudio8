package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Implementations are responsible for painting a cell.<br/>
 * 
 * Custom {@link ICellPainter} can be registered in the {@link IConfigRegistry}.
 * This is a mechanism for plugging in custom cell painting.
 * 
 * @see PercentageBarCellPainter
 */
public interface ICellPainter {
	
	/**
	 * 
	 * @param gc SWT graphics context used to draw the cell
	 * @param rectangle cell bounds
	 * @param natTable :-)
	 * @param cellRenderer
	 * @param rowIndex of the cell to paint
	 * @param colIndex of the cell to paint
	 * @param selected is the cell selected ?
	 */
	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry);

	/**
	 * Get the preferred width of the cell when rendered by this painter. Used for auto-resize.
	 * @param cell
	 * @param gc
	 * @param configRegistry
	 * @return
	 */
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry);

	/**
	 * Get the preferred height of the cell when rendered by this painter. Used for auto-resize.
	 * @param cell
	 * @param gc
	 * @param configRegistry
	 * @return
	 */
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry);
	
}
