package net.sourceforge.nattable.reorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sourceforge.nattable.coordinate.PositionUtil;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.layer.event.ColumnStructuralRefreshEvent;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.reorder.command.ColumnReorderCommandHandler;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommandHandler;
import net.sourceforge.nattable.reorder.config.DefaultColumnReorderLayerConfiguration;
import net.sourceforge.nattable.reorder.event.ColumnReorderEvent;

/**
 * Adds functionality for reordering column(s)<br/>
 * Also responsible for saving/loading the column order state.
 * 
 * @see DefaultColumnReorderLayerConfiguration
 */
public class ColumnReorderLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	public static final String PERSISTENCE_KEY_COLUMN_INDEX_ORDER = ".columnIndexOrder";

	private final IUniqueIndexLayer underlyingLayer;

	// Position X in the List contains the index of column at position X
	private final List<Integer> columnIndexOrder = new ArrayList<Integer>();

	private final Map<Integer, Integer> startXCache = new HashMap<Integer, Integer>();

	public ColumnReorderLayer(IUniqueIndexLayer underlyingLayer) {
		this(underlyingLayer, true);
	}

	public ColumnReorderLayer(IUniqueIndexLayer underlyingLayer, boolean useDefaultConfiguration) {
		super(underlyingLayer);
		this.underlyingLayer = underlyingLayer;

		populateIndexOrder();

		registerCommandHandler(new ColumnReorderCommandHandler(this));
		registerCommandHandler(new MultiColumnReorderCommandHandler(this));

		if (useDefaultConfiguration) {
			addConfiguration(new DefaultColumnReorderLayerConfiguration());
		}
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IStructuralChangeEvent) {
			IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
			if (structuralChangeEvent.isHorizontalStructureChanged()) {
				Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getColumnDiffs();
				if (structuralDiffs == null) {
					// Assume everything changed
					columnIndexOrder.clear();
					populateIndexOrder();
				} else {
					for (StructuralDiff structuralDiff : structuralDiffs) {
						switch (structuralDiff.getDiffType()) {
						case ADD:
							columnIndexOrder.clear();
							populateIndexOrder();
							break;
						case DELETE:
							columnIndexOrder.clear();
							populateIndexOrder();
							break;
						}
					}
				}
				invalidateCache();
			}
		}
		super.handleLayerEvent(event);
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		super.saveState(prefix, properties);
		if (columnIndexOrder.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Integer index : columnIndexOrder) {
				strBuilder.append(index);
				strBuilder.append(',');
			}
			properties.setProperty(prefix + PERSISTENCE_KEY_COLUMN_INDEX_ORDER, strBuilder.toString());
		}
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		String property = properties.getProperty(prefix + PERSISTENCE_KEY_COLUMN_INDEX_ORDER);

		if (property != null) {
			List<Integer> newColumnIndexOrder = new ArrayList<Integer>();
			StringTokenizer tok = new StringTokenizer(property, ",");
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken();
				newColumnIndexOrder.add(Integer.valueOf(index));
			}
			
			if(isRestoredStateValid(newColumnIndexOrder)){
				columnIndexOrder.clear();
				columnIndexOrder.addAll(newColumnIndexOrder);
			}
		}
		fireLayerEvent(new ColumnStructuralRefreshEvent(this));
	}

	/**
	 * Ensure that columns haven't changed in the underlying data source
	 * @param newColumnIndexOrder restored from the properties file.
	 */
	protected boolean isRestoredStateValid(List<Integer> newColumnIndexOrder) {
		if (newColumnIndexOrder.size() != getColumnCount()){
			System.err.println(
				"Number of persisted columns (" + newColumnIndexOrder.size() + ") " +
				"is not the same as the number of columns in the data source (" + getColumnCount() + ").\n" +
				"Skipping restore of column ordering");
			return false;
		}
		
		for (Integer index : newColumnIndexOrder) {
			if(!columnIndexOrder.contains(index)){
				System.err.println(
					"Column index: " + index + " being restored, is not a available in the data soure.\n" +
					"Skipping restore of column ordering");
				return false;
			}
		}
		return true;
	}

	// Columns

	@Override
	public int getColumnIndexByPosition(int columnPosition) {
		if (columnPosition >= 0 && columnPosition < columnIndexOrder.size()) {
			return columnIndexOrder.get(columnPosition).intValue();
		} else {
			return -1;
		}
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return columnIndexOrder.indexOf(Integer.valueOf(columnIndex));
	}

	@Override
	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		int columnIndex = getColumnIndexByPosition(localColumnPosition);
		return underlyingLayer.getColumnPositionByIndex(columnIndex);
	}

	@Override
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		int columnIndex = underlyingLayer.getColumnIndexByPosition(underlyingColumnPosition);
		return getColumnPositionByIndex(columnIndex);
	}

	@Override
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		List<Integer> reorderedColumnPositions = new ArrayList<Integer>();
		for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
			for (int underlyingColumnPosition = underlyingColumnPositionRange.start; underlyingColumnPosition < underlyingColumnPositionRange.end; underlyingColumnPosition++) {
				int localColumnPosition = underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPositionRange.start);
				reorderedColumnPositions.add(Integer.valueOf(localColumnPosition));
			}
		}
		Collections.sort(reorderedColumnPositions);
		
		return PositionUtil.getRanges(reorderedColumnPositions);
	}
	
	// X

	@Override
	public int getColumnPositionByX(int x) {
		return LayerUtil.getColumnPositionByX(this, x);
	}

	@Override
	public int getStartXOfColumnPosition(int targetColumnPosition) {
		Integer cachedStartX = startXCache.get(Integer.valueOf(targetColumnPosition));
		if (cachedStartX != null) {
			return cachedStartX.intValue();
		}

		int aggregateWidth = 0;
		for (int columnPosition = 0; columnPosition < targetColumnPosition; columnPosition++) {
			aggregateWidth += underlyingLayer.getColumnWidthByPosition(localToUnderlyingColumnPosition(columnPosition));
		}

		startXCache.put(Integer.valueOf(targetColumnPosition), Integer.valueOf(aggregateWidth));
		return aggregateWidth;
	}

	private void populateIndexOrder() {
		ILayer underlyingLayer = getUnderlyingLayer();
		for (int columnPosition = 0; columnPosition < underlyingLayer.getColumnCount(); columnPosition++) {
			columnIndexOrder.add(Integer.valueOf(underlyingLayer.getColumnIndexByPosition(columnPosition)));
		}
	}

	// Vertical features

	// Rows

	public int getRowPositionByIndex(int rowIndex) {
		return underlyingLayer.getRowPositionByIndex(rowIndex);
	}

	/**
	 * Moves the column to the <i>LEFT</i> of the toColumnPosition
	 * @param fromColumnPosition column position to move
	 * @param toColumnPosition position to move the column to
	 */
	private void moveColumn(int fromColumnPosition, int toColumnPosition) {
		Integer fromColumnIndex = columnIndexOrder.get(fromColumnPosition);
		columnIndexOrder.add(toColumnPosition, fromColumnIndex);

		columnIndexOrder.remove(fromColumnPosition + (fromColumnPosition > toColumnPosition ? 1 : 0));
		invalidateCache();
	}

	public void reorderColumnPosition(int fromColumnPosition, int toColumnPosition) {
		moveColumn(fromColumnPosition, toColumnPosition);
		fireLayerEvent(new ColumnReorderEvent(this, fromColumnPosition, toColumnPosition));
	}

	public void reorderMultipleColumnPositions(List<Integer> fromColumnPositions, int toColumnPosition) {
		// Moving from left to right
		final int fromColumnPositionsCount = fromColumnPositions.size();

		if (toColumnPosition > fromColumnPositions.get(fromColumnPositionsCount - 1).intValue()) {
			int firstColumnPosition = fromColumnPositions.get(0).intValue();

			for (int columnCount = 0; columnCount < fromColumnPositionsCount; columnCount++) {
				final int fromColumnPosition = fromColumnPositions.get(0).intValue();
				moveColumn(fromColumnPosition, toColumnPosition);
				if (fromColumnPosition < firstColumnPosition) {
					firstColumnPosition = fromColumnPosition;
				}
			}
		} else if (toColumnPosition < fromColumnPositions.get(fromColumnPositionsCount - 1).intValue()) {
			// Moving from right to left
			int targetColumnPosition = toColumnPosition;
			for (Integer fromColumnPosition : fromColumnPositions) {
				final int fromColumnPositionInt = fromColumnPosition.intValue();
				moveColumn(fromColumnPositionInt, targetColumnPosition++);
			}
		}

		fireLayerEvent(new ColumnReorderEvent(this, fromColumnPositions, toColumnPosition));
	}

	private void invalidateCache() {
		startXCache.clear();
	}

}