package net.sourceforge.nattable.layer;

public class LayerUtil {
	
	public static final int getColumnPositionByX(ILayer layer, int x) {
		int width = layer.getWidth();
		
		if (x < 0 || x >= width) {
			return -1;
		}

		return findColumnPosition(0, 0, layer, x, width, layer.getColumnCount());
	}
	
	protected static final int findColumnPosition(int xOffset, int columnOffset, ILayer layer, int x, int totalWidth, int columnCount) {
		double size = (double) (totalWidth - xOffset) / (columnCount - columnOffset);
		int columnPosition = columnOffset + (int) ((x - xOffset) / size);
		
		int startX = layer.getStartXOfColumnPosition(columnPosition);
		int endX = startX + layer.getColumnWidthByPosition(columnPosition);
		if (x < startX) {
			return findColumnPosition(xOffset, columnOffset, layer, x, startX, columnPosition);
		} else if (x >= endX) {
			return findColumnPosition(endX, columnPosition + 1, layer, x, totalWidth, columnCount);
		} else {
			return columnPosition;
		}
	}
	
	public static final int getRowPositionByY(ILayer layer, int y) {
		int height = layer.getHeight();
		
		if (y < 0 || y >= height) {
			return -1;
		}
		
		return findRowPosition(0, 0, layer, y, height, layer.getRowCount());
	}
	
	protected static final int findRowPosition(int yOffset, int rowOffset, ILayer layer, int y, int totalHeight, int rowCount) {
		double size = (double) (totalHeight - yOffset) / (rowCount - rowOffset);
		int rowPosition = rowOffset + (int) ((y - yOffset) / size);
		
		int startY = layer.getStartYOfRowPosition(rowPosition);
		int endY = startY + layer.getRowHeightByPosition(rowPosition);
		if (y < startY) {
			return findRowPosition(yOffset, rowOffset, layer, y, startY, rowPosition);
		} else if (y >= endY) {
			return findRowPosition(endY, rowPosition + 1, layer, y, totalHeight, rowCount);
		} else {
			return rowPosition;
		}
	}
	
	/**
	 * Convert column position from the source layer to the target layer
	 * @param sourceLayer source layer
	 * @param sourceColumnPosition column position in the source layer
	 * @param targetLayer layer to convert the from position to 
	 * @return converted column position
	 */
	public static final int convertColumnPosition(ILayer sourceLayer, int sourceColumnPosition, IUniqueIndexLayer targetLayer) {
		if (targetLayer == sourceLayer) {
			return sourceColumnPosition;
		}
		int columnIndex = sourceLayer.getColumnIndexByPosition(sourceColumnPosition);
		return targetLayer.getColumnPositionByIndex(columnIndex);
	}
	
	/**
	 * Convert row position from the source layer to the target layer
	 * @param sourceLayer source layer
	 * @param sourceRowPosition position in the source layer
	 * @param targetLayer layer to convert the from position to 
	 * @return converted row position
	 */
	public static final int convertRowPosition(ILayer sourceLayer, int sourceRowPosition, IUniqueIndexLayer targetLayer) {
		if (targetLayer == sourceLayer) {
			return sourceRowPosition;
		}
		int rowIndex = sourceLayer.getRowIndexByPosition(sourceRowPosition);
		return targetLayer.getRowPositionByIndex(rowIndex);
	}
	
}
