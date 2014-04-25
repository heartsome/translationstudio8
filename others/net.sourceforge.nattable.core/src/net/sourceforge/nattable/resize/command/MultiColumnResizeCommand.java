package net.sourceforge.nattable.resize.command;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.nattable.command.AbstractMultiColumnCommand;
import net.sourceforge.nattable.command.LayerCommandUtil;
import net.sourceforge.nattable.coordinate.ColumnPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public class MultiColumnResizeCommand extends AbstractMultiColumnCommand {

	private int commonColumnWidth = -1;
	protected Map<ColumnPositionCoordinate, Integer> colPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>();

	/**
	 * All columns are being resized to the same size e.g. during a drag resize
	 */
	public MultiColumnResizeCommand(ILayer layer, int[] columnPositions, int commonColumnWidth) {
		super(layer, columnPositions);
		this.commonColumnWidth = commonColumnWidth;
	}

	/**
	 * Each column is being resized to a different size e.g. during auto resize
	 */
	public MultiColumnResizeCommand(ILayer layer, int[] columnPositions, int[] columnWidths) {
		super(layer, columnPositions);
		for (int i = 0; i < columnPositions.length; i++) {
			colPositionToWidth.put(new ColumnPositionCoordinate(layer, columnPositions[i]), Integer.valueOf(columnWidths[i]));
		}
	}
	
	protected MultiColumnResizeCommand(MultiColumnResizeCommand command) {
		super(command);
		this.commonColumnWidth = command.commonColumnWidth;
		this.colPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>(command.colPositionToWidth);
	}

	public int getCommonColumnWidth() {
		return commonColumnWidth;
	}
	
	public int getColumnWidth(int columnPosition) {
		for (ColumnPositionCoordinate columnPositionCoordinate : colPositionToWidth.keySet()) {
			if (columnPositionCoordinate.getColumnPosition() == columnPosition) {
				return colPositionToWidth.get(columnPositionCoordinate).intValue();
			}
		}
		return commonColumnWidth;
	}
	
	/**
	 * Convert the column positions to the target layer.
	 * Ensure that the width associated with the column is now associated with the
	 * converted column position.
	 */
	@Override
	public boolean convertToTargetLayer(ILayer targetLayer) {
		Map<ColumnPositionCoordinate, Integer> newColPositionToWidth = new HashMap<ColumnPositionCoordinate, Integer>();
		
		for (ColumnPositionCoordinate columnPositionCoordinate : colPositionToWidth.keySet()) {
			ColumnPositionCoordinate convertedColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer);
			if (convertedColumnPositionCoordinate != null) {
				newColPositionToWidth.put(convertedColumnPositionCoordinate, colPositionToWidth.get(columnPositionCoordinate));
			}
		}
		
		colPositionToWidth = newColPositionToWidth;

		return super.convertToTargetLayer(targetLayer);
	}
	
	public MultiColumnResizeCommand cloneCommand() {
		return new MultiColumnResizeCommand(this);
	}
}