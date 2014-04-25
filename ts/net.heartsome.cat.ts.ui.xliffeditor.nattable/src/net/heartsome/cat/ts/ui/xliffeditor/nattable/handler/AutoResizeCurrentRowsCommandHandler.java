package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IConfiguration;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.print.command.TurnViewportOffCommand;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.resize.MaxCellBoundsHelper;
import net.sourceforge.nattable.resize.command.MultiRowResizeCommand;

import org.eclipse.swt.graphics.GC;

/**
 * 自适应当前显示行的高度的处理 Handler
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class AutoResizeCurrentRowsCommandHandler implements ILayerCommandHandler<AutoResizeCurrentRowsCommand> {

	private final CompositeLayer layer;

	public AutoResizeCurrentRowsCommandHandler(CompositeLayer compositeLayer) {
		this.layer = compositeLayer;
	}

	public boolean doCommand(ILayer targetLayer, AutoResizeCurrentRowsCommand command) {
		// Need to resize selected rows even if they are outside the viewport
		targetLayer.doCommand(new TurnViewportOffCommand());

		int[] gridRowPositions = command.getRows();
		int[] gridRowHeights = getPreferedRowHeights(command.getConfigRegistry(), layer,
				gridRowPositions);

		layer.doCommand(new MultiRowResizeCommand(layer, gridRowPositions, gridRowHeights));

		targetLayer.doCommand(new TurnViewportOnCommand());

		return true;
	}

	public Class<AutoResizeCurrentRowsCommand> getCommandClass() {
		return AutoResizeCurrentRowsCommand.class;
	}

	/**
	 * @see MaxCellBoundsHelper#getPreferedColumnWidths(IConfiguration, GC, ILayer, int[])
	 */
	private int[] getPreferedRowHeights(IConfigRegistry configRegistry, ILayer layer, int[] rows) {
		int[] rowHeights = new int[rows.length];

		// 获取编辑区高度（编辑器中，去除corner的高度）
		int clientAreaHeight = layer.getClientAreaProvider().getClientArea().height;
		for (int i = 0; i < rows.length; i++) {
			rowHeights[i] = getPreferredRowHeight(layer, rows[i], configRegistry, null, clientAreaHeight);
		}
		return rowHeights;
	}

	private int getPreferredRowHeight(ILayer layer, int rowPosition, IConfigRegistry configRegistry, GC gc,
			int clientAreaHeight) {
		int maxHeight = 0;
		ICellPainter painter;
		LayerCell cell;

		for (int columnPosition = 0; columnPosition < layer.getColumnCount(); columnPosition++) {
			cell = layer.getCellByPosition(columnPosition, rowPosition);
			if (cell != null) {
				painter = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_PAINTER, cell.getDisplayMode(),
						cell.getConfigLabels().getLabels());
				if (painter != null) {
					int preferedHeight = painter.getPreferredHeight(cell, gc, configRegistry);
					maxHeight = (preferedHeight > maxHeight) ? preferedHeight : maxHeight;
				}
			}
		}

		if (maxHeight > clientAreaHeight) {
			return clientAreaHeight;
		}
		return maxHeight;
	}
}
