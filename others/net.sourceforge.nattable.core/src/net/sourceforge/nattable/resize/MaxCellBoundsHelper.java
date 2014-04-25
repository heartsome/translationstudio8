package net.sourceforge.nattable.resize;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IConfiguration;
import net.sourceforge.nattable.grid.command.AutoResizeColumnCommandHandler;
import net.sourceforge.nattable.grid.command.AutoResizeRowCommandHandler;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.ICellPainter;

import org.eclipse.swt.graphics.GC;

/**
 * Does the calculations needed for auto resizing feature
 * Helper class for {@link AutoResizeColumnCommandHandler} and {@link AutoResizeRowCommandHandler}
 */
public class MaxCellBoundsHelper {
	
	/**
	 * @return Preferred widths for columns. Preferred width is the minimum width
	 *    required to horizontally fit all the contents of the column (including header)
	 */
	public static int[] getPreferedColumnWidths(IConfigRegistry configRegistry, GC gc, ILayer layer, int[]columnPositions) {
		int[] columnWidths = new int[columnPositions.length];
		
		for (int i = 0; i < columnPositions.length; i++) {
			columnWidths[i] = getPreferredColumnWidth(layer, columnPositions[i], configRegistry, gc);
		}
		return columnWidths;
	}

	/**
	 * Calculates the minimum width (in pixels) required to display the complete 
	 *    contents of the cells in a column. Takes into account the font settings 
	 *    and display type conversion. 
	 */
	public static int getPreferredColumnWidth(ILayer layer, int columnPosition, IConfigRegistry configRegistry, GC gc) {
		ICellPainter painter;
		int maxWidth = 0;
		LayerCell cell;
		
		for (int rowPosition = 0; rowPosition < layer.getRowCount(); rowPosition++) {
			cell = layer.getCellByPosition(columnPosition, rowPosition);
			if (cell != null) {
				painter = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_PAINTER, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
				if (painter != null) {
					int preferedWidth = painter.getPreferredWidth(cell, gc, configRegistry);
					maxWidth = (preferedWidth > maxWidth) ? preferedWidth : maxWidth;
				}
			}
		}
		
		return maxWidth;
	}
	
	/**
	 * @see MaxCellBoundsHelper#getPreferedColumnWidths(IConfiguration, GC, ILayer, int[])
	 */
	public static int[] getPreferedRowHeights(IConfigRegistry configRegistry, GC gc, ILayer layer, int[]rows) {
		int[] rowHeights = new int[rows.length];
		
		for (int i = 0; i < rows.length; i++) {
			rowHeights[i] = getPreferredRowHeight(layer, rows[i], configRegistry, gc);
		}
		return rowHeights;
	}

	public static int getPreferredRowHeight(ILayer layer, int rowPosition, IConfigRegistry configRegistry, GC gc) {
		int maxHeight = 0;
		ICellPainter painter;
		LayerCell cell;
		
		for (int columnPosition = 0; columnPosition < layer.getColumnCount(); columnPosition++) {
			cell = layer.getCellByPosition(columnPosition, rowPosition);
			if (cell != null) {
				painter = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_PAINTER, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
				if (painter != null) {
					int preferedHeight = painter.getPreferredHeight(cell, gc, configRegistry);
					maxHeight = (preferedHeight > maxHeight) ? preferedHeight : maxHeight;
				}
			}
		}
		
		return maxHeight;
	}

	/**
	 * Traverse the two arrays and return the greater element in each index position. 
	 */
	public static int[] greater(int[] array1, int[] array2) {
		int resultSize = (array1.length < array2.length) ? array1.length : array2.length;
		int[] result = new int[resultSize];
		
		for(int i=0; i<resultSize; i++){
			result[i] = (array1[i] > array2[i]) ? array1[i] : array2[i];
		}
		return result;
	}
}
