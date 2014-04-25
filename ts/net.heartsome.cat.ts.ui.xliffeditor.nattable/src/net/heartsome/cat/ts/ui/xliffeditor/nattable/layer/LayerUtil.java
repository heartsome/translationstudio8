package net.heartsome.cat.ts.ui.xliffeditor.nattable.layer;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class LayerUtil extends net.sourceforge.nattable.layer.LayerUtil {

	private static int layoutX;

	private static int layoutY;

	public static void setBodyLayerPosition(int layoutX, int layoutY) {
		LayerUtil.layoutX = layoutX;
		LayerUtil.layoutY = layoutY;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ILayer> T getLayer(NatTable table, Class<T> targetLayerClass) {
		if (targetLayerClass.equals(NatTable.class)) {
			return (T) table;
		}
		ILayer layer = table.getUnderlyingLayerByPosition(0, 0); // 得到 CompositeLayer
		if (layer instanceof CompositeLayer) {
			if (targetLayerClass.equals(CompositeLayer.class)) {
				return (T) layer;
			}
			return getLayer((CompositeLayer) layer, targetLayerClass);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ILayer> T getLayer(CompositeLayer compositeLayer, Class<T> targetLayerClass) {
		if (targetLayerClass.equals(CompositeLayer.class)) {
			return (T) compositeLayer;
		}
		ILayer layer = compositeLayer.getChildLayerByLayoutCoordinate(layoutX, layoutY);
		while (layer != null && !(targetLayerClass.isInstance(layer))) {
			layer = layer.getUnderlyingLayerByPosition(0, 0);
		}
		return (T) layer;
	}

	// AbstractLayerTransform

	public static <T extends IUniqueIndexLayer> int getLowerLayerRowPosition(NatTable table, int sourceRowPosition,
			Class<T> targetLayerClass) {
		ViewportLayer viewportLayer = getLayer(table, ViewportLayer.class);
		int originRowPosition = viewportLayer.getOriginRowPosition();
		sourceRowPosition -= originRowPosition + 1;
		T t = getLayer(table, targetLayerClass);
		return convertRowPosition(viewportLayer, sourceRowPosition, t);
	}

	public static <T extends ILayer> int getUpperLayerRowPosition(NatTable table, int sourceRowPosition,
			Class<T> sourceLayerClass) {
		ViewportLayer viewportLayer = getLayer(table, ViewportLayer.class);
		int originRowPosition = viewportLayer.getOriginRowPosition();
		T t = getLayer(table, sourceLayerClass);
		sourceRowPosition = convertRowPosition(t, sourceRowPosition, viewportLayer);
		sourceRowPosition += originRowPosition + 1;
		return sourceRowPosition;
	}

	// public static int getRowPosition(ILayer sourceLayer, int sourceRowPosition,
	// ILayer targetLayer) {
	// Assert.isNotNull(sourceLayer);
	// Assert.isNotNull(targetLayer);
	//
	// if (sourceLayer.equals(targetLayer)) {
	// return sourceRowPosition;
	// }
	// if (targetLayer instanceof IUniqueIndexLayer) {
	// return convertRowPosition(sourceLayer, sourceRowPosition, (IUniqueIndexLayer) targetLayer);
	// } else if (targetLayer instanceof NatTable) {
	// ViewportLayer viewportLayer = getLayer((NatTable) targetLayer, ViewportLayer.class);
	// int originRowPosition = viewportLayer.getOriginRowPosition();
	// int targetRowPostion = convertRowPosition(sourceLayer, sourceRowPosition, viewportLayer);
	// return targetRowPostion + originRowPosition + 1;
	// } else {
	// throw new IllegalArgumentException("无法识别的参数：参数必须为接口 IUniqueIndexLayer 的实例或 NatTable 的实例。");
	// }
	// }

	/**
	 * 通过列索引得到列位置
	 * @param table
	 *            NatTable对象
	 * @param columnIndex
	 *            列索引
	 * @return 列位置;
	 */
	public static int getColumnPositionByIndex(NatTable table, int columnIndex) {
		int columnCount = table.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			if (table.getColumnIndexByPosition(i) == columnIndex) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 通过行索引得到行位置
	 * @param table
	 *            NatTable对象
	 * @param rowIndex
	 *            列索引
	 * @return 行位置;
	 */
	public static int getRowPositionByIndex(NatTable table, int rowIndex) {
		int rowCount = table.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			if (table.getRowIndexByPosition(i) == rowIndex) {
				return i;
			}
		}
		return -1;
	}
}
