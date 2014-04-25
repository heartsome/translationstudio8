package net.sourceforge.nattable.resize.command;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.nattable.command.AbstractMultiRowCommand;
import net.sourceforge.nattable.command.LayerCommandUtil;
import net.sourceforge.nattable.coordinate.RowPositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;

public class MultiRowResizeCommand extends AbstractMultiRowCommand {

	private int commonRowHeight = -1;
	protected Map<RowPositionCoordinate, Integer> rowPositionToHeight = new HashMap<RowPositionCoordinate, Integer>();

	/**
	 * All rows are being resized to the same height e.g. during a drag resize
	 */
	public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int commonRowHeight) {
		super(layer, rowPositions);
		this.commonRowHeight = commonRowHeight;
	}

	/**
	 * Each row is being resized to a different size e.g. during auto resize
	 */
	public MultiRowResizeCommand(ILayer layer, int[] rowPositions, int[] rowHeights) {
		super(layer, rowPositions);
		for (int i = 0; i < rowPositions.length; i++) {
			rowPositionToHeight.put(new RowPositionCoordinate(layer, rowPositions[i]), Integer.valueOf(rowHeights[i]));
		}
	}
	
	protected MultiRowResizeCommand(MultiRowResizeCommand command) {
		super(command);
		this.commonRowHeight = command.commonRowHeight;
		this.rowPositionToHeight = new HashMap<RowPositionCoordinate, Integer>(command.rowPositionToHeight);
	}

	public int getCommonRowHeight() {
		return commonRowHeight;
	}
	
	public int getRowHeight(int rowPosition) {
		for (RowPositionCoordinate rowPositionCoordinate : rowPositionToHeight.keySet()) {
			if (rowPositionCoordinate.getRowPosition() == rowPosition) {
				return rowPositionToHeight.get(rowPositionCoordinate).intValue();
			}
		}
		return commonRowHeight;
	}
	
	@Override
	public boolean convertToTargetLayer(ILayer targetLayer) {
		Map<RowPositionCoordinate, Integer> newRowPositionToHeight = new HashMap<RowPositionCoordinate, Integer>();
		
		for (RowPositionCoordinate rowPositionCoordinate : rowPositionToHeight.keySet()) {
			RowPositionCoordinate convertedRowPositionCoordinate = LayerCommandUtil.convertRowPositionToTargetContext(rowPositionCoordinate, targetLayer);
			if (convertedRowPositionCoordinate != null) {
				newRowPositionToHeight.put(convertedRowPositionCoordinate, rowPositionToHeight.get(rowPositionCoordinate));
			}
		}
		
		rowPositionToHeight = newRowPositionToHeight;
		
		return super.convertToTargetLayer(targetLayer);
	}
	
	public MultiRowResizeCommand cloneCommand() {
		return new MultiRowResizeCommand(this);
	}
	
}