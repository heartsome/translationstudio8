package net.sourceforge.nattable.hideshow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.RowStructuralRefreshEvent;

public abstract class AbstractColumnHideShowLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	private List<Integer> cachedVisibleColumnIndexOrder;

	private Map<Integer, Integer> cachedHiddenColumnIndexToPositionMap;

	private final Map<Integer, Integer> startXCache = new HashMap<Integer, Integer>();

	public AbstractColumnHideShowLayer(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IStructuralChangeEvent) {
			IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
			if (structuralChangeEvent.isHorizontalStructureChanged()) {
				invalidateCache();
			}
		}
		super.handleLayerEvent(event);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		fireLayerEvent(new RowStructuralRefreshEvent(this));
	}

	// Horizontal features

	// Columns

	@Override
	public int getColumnCount() {
		return getCachedVisibleColumnIndexes().size();
	}

	@Override
	public int getColumnIndexByPosition(int columnPosition) {
		if (columnPosition < 0 || columnPosition >= getColumnCount()) {
			return -1;
		}

		Integer columnIndex = getCachedVisibleColumnIndexes().get(columnPosition);
		if (columnIndex != null) {
			return columnIndex.intValue();
		} else {
			return -1;
		}
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return getCachedVisibleColumnIndexes().indexOf(Integer.valueOf(columnIndex));
	}

	@Override
	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		int columnIndex = getColumnIndexByPosition(localColumnPosition);
		return ((IUniqueIndexLayer) getUnderlyingLayer()).getColumnPositionByIndex(columnIndex);
	}

	@Override
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		int columnIndex = getUnderlyingLayer().getColumnIndexByPosition(underlyingColumnPosition);
		int columnPosition = getColumnPositionByIndex(columnIndex);
		if (columnPosition >= 0) {
			return columnPosition;
		} else {
			Integer hiddenColumnPosition = cachedHiddenColumnIndexToPositionMap.get(Integer.valueOf(columnIndex));
			if (hiddenColumnPosition != null) {
				return hiddenColumnPosition.intValue();
			} else {
				return -1;
			}
		}
	}

	@Override
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		Collection<Range> localColumnPositionRanges = new ArrayList<Range>();

		for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
			int startColumnPosition = getAdjustedUnderlyingToLocalStartPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.start, underlyingColumnPositionRange.end);
			int endColumnPosition = getAdjustedUnderlyingToLocalEndPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.end, underlyingColumnPositionRange.start);

			// teichstaedt: fixes the problem that ranges where added even if the
			// corresponding startPosition weren't found in the underlying layer.
			// Without that fix a bunch of ranges of kind Range [-1, 180] which
			// causes strange behaviour in Freeze- and other Layers were returned.
			if (startColumnPosition > -1) {
				localColumnPositionRanges.add(new Range(startColumnPosition, endColumnPosition));
			}
		}

		return localColumnPositionRanges;
	}

	private int getAdjustedUnderlyingToLocalStartPosition(ILayer sourceUnderlyingLayer, int startUnderlyingPosition, int endUnderlyingPosition) {
		int localStartColumnPosition = underlyingToLocalColumnPosition(sourceUnderlyingLayer, startUnderlyingPosition);
		int offset = 0;
		while (localStartColumnPosition < 0 && (startUnderlyingPosition + offset < endUnderlyingPosition)) {
			localStartColumnPosition = underlyingToLocalColumnPosition(sourceUnderlyingLayer, startUnderlyingPosition + offset++);
		}
		return localStartColumnPosition;
	}

	private int getAdjustedUnderlyingToLocalEndPosition(ILayer sourceUnderlyingLayer, int endUnderlyingPosition, int startUnderlyingPosition) {
		int localEndColumnPosition = underlyingToLocalColumnPosition(sourceUnderlyingLayer, endUnderlyingPosition - 1);
		int offset = 0;
		while (localEndColumnPosition < 0 && (endUnderlyingPosition - offset > startUnderlyingPosition)) {
			localEndColumnPosition = underlyingToLocalColumnPosition(sourceUnderlyingLayer, endUnderlyingPosition - offset++);
		}
		return localEndColumnPosition + 1;
	}

	// Width

	@Override
	public int getWidth() {
		int lastColumnPosition = getColumnCount() - 1;
		return getStartXOfColumnPosition(lastColumnPosition) + getColumnWidthByPosition(lastColumnPosition);
	}

	// X

	@Override
	public int getColumnPositionByX(int x) {
		return LayerUtil.getColumnPositionByX(this, x);
	}

	@Override
	public int getStartXOfColumnPosition(int localColumnPosition) {
		Integer cachedStartX = startXCache.get(Integer.valueOf(localColumnPosition));
		if (cachedStartX != null) {
			return cachedStartX.intValue();
		}

		IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
		int underlyingPosition = localToUnderlyingColumnPosition(localColumnPosition);
		int underlyingStartX = underlyingLayer.getStartXOfColumnPosition(underlyingPosition);

		for (Integer hiddenIndex : getHiddenColumnIndexes()) {
			int hiddenPosition = underlyingLayer.getColumnPositionByIndex(hiddenIndex.intValue());
			if (hiddenPosition <= underlyingPosition) {
				underlyingStartX -= underlyingLayer.getColumnWidthByPosition(hiddenPosition);
			}
		}

		startXCache.put(Integer.valueOf(localColumnPosition), Integer.valueOf(underlyingStartX));
		return underlyingStartX;
	}

	// Vertical features

	// Rows

	public int getRowPositionByIndex(int rowIndex) {
		return ((IUniqueIndexLayer) getUnderlyingLayer()).getRowPositionByIndex(rowIndex);
	}

	// Hide/show

	public abstract boolean isColumnIndexHidden(int columnIndex);

	public abstract Collection<Integer> getHiddenColumnIndexes();

	// Cache

	protected void invalidateCache() {
		cachedVisibleColumnIndexOrder = null;
		startXCache.clear();
	}

	private List<Integer> getCachedVisibleColumnIndexes() {
		if (cachedVisibleColumnIndexOrder == null) {
			cacheVisibleColumnIndexes();
		}
		return cachedVisibleColumnIndexOrder;
	}

	private void cacheVisibleColumnIndexes() {
		cachedVisibleColumnIndexOrder = new ArrayList<Integer>();
		cachedHiddenColumnIndexToPositionMap = new HashMap<Integer, Integer>();
		startXCache.clear();

		ILayer underlyingLayer = getUnderlyingLayer();
		int columnPosition = 0;
		for (int parentColumnPosition = 0; parentColumnPosition < underlyingLayer.getColumnCount(); parentColumnPosition++) {
			int columnIndex = underlyingLayer.getColumnIndexByPosition(parentColumnPosition);

			if (!isColumnIndexHidden(columnIndex)) {
				cachedVisibleColumnIndexOrder.add(Integer.valueOf(columnIndex));
				columnPosition++;
			} else {
				cachedHiddenColumnIndexToPositionMap.put(Integer.valueOf(columnIndex), Integer.valueOf(columnPosition));
			}
		}
	}

}
