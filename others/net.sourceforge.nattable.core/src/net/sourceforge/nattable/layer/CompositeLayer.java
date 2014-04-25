package net.sourceforge.nattable.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import net.sourceforge.nattable.layer.cell.IConfigLabelAccumulator;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.painter.layer.ILayerPainter;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.util.IClientAreaProvider;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A composite layer is a layer that is made up of a number of underlying child layers. This class assumes that the child
 * layers are laid out in a regular grid pattern where the child layers in each composite row all have the same number of
 * rows and the same height, and the child layers in each composite column each have the same number of columns and
 * the same width.
 */
public class CompositeLayer extends AbstractLayer {

	private final int layoutXCount;

	private final int layoutYCount;

	private final Map<ILayer, String> childLayerToRegionNameMap = new HashMap<ILayer, String>();

	private final Map<String, IConfigLabelAccumulator> regionNameToConfigLabelAccumulatorMap = new HashMap<String, IConfigLabelAccumulator>();

	private final Map<ILayer, LayoutCoordinate> childLayerToLayoutCoordinateMap = new HashMap<ILayer, LayoutCoordinate>();

	/** Data struct. for child Layers */
	private final ILayer[][] childLayerLayout;

	/** [X][Y] */
	private ChildLayerInfo[][] childLayerInfos;

	private final CompositeLayerPainter compositeLayerPainter = new CompositeLayerPainter();

	public CompositeLayer(int layoutXCount, int layoutYCount) {
		this.layoutXCount = layoutXCount;
		this.layoutYCount = layoutYCount;
		childLayerLayout = new ILayer[layoutXCount][layoutYCount];
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				ILayer childLayer = childLayerLayout[layoutX][layoutY];
				if (childLayer != null) {
					String regionName = childLayerToRegionNameMap.get(childLayer);
					childLayer.saveState(prefix + "." + regionName, properties);
				}
			}
		}
		super.saveState(prefix, properties);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				ILayer childLayer = childLayerLayout[layoutX][layoutY];
				if (childLayer != null) {
					String regionName = childLayerToRegionNameMap.get(childLayer);
					childLayer.loadState(prefix + "." + regionName, properties);
				}
			}
		}
		super.loadState(prefix, properties);
	}

	// Configuration

	@Override
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				childLayerLayout[layoutX][layoutY].configure(configRegistry, uiBindingRegistry);
			}
		}

		super.configure(configRegistry, uiBindingRegistry);
	}

	@Override
	public ILayerPainter getLayerPainter() {
		return compositeLayerPainter;
	}

	/**
	 * {@inheritDoc}
	 * Handle commands
	 */
	@Override
	public boolean doCommand(ILayerCommand command) {
		if (super.doCommand(command)) {
			return true;
		}
		return doCommandOnChildLayers(command);
	}

	protected boolean doCommandOnChildLayers(ILayerCommand command) {
		for (ILayer childLayer : childLayerToLayoutCoordinateMap.keySet()) {
			ILayerCommand childCommand = command.cloneCommand();
			if (childLayer.doCommand(childCommand)) {
				return true;
			}
		}
		return false;
	}

	// Events

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IStructuralChangeEvent) {
			childLayerInfos = null;
		}

		super.handleLayerEvent(event);
	}

	// Horizontal features

	// Columns

	/**
	 * @return total number of columns being displayed
	 *    Note: Works off the header layers.
	 */
	public int getColumnCount() {
		ChildLayerInfo lastChildLayerInfo = getChildLayerInfoByLayout(layoutXCount - 1, 0);
		return lastChildLayerInfo.getColumnPositionOffset() + lastChildLayerInfo.getLayer().getColumnCount();
	}

	public int getPreferredColumnCount() {
		int preferredColumnCount = 0;
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			preferredColumnCount += childLayerLayout[layoutX][0].getPreferredColumnCount();
		}
		return preferredColumnCount;
	}

	/**
	 * @param compositeColumnPosition Column position in the {@link CompositeLayer}
	 * @return column index in the underlying layer.
	 */
	public int getColumnIndexByPosition(int compositeColumnPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByColumnPosition(compositeColumnPosition);
		if (childLayerInfo == null) {
			return -1;
		}
		int childColumnPosition = compositeColumnPosition - childLayerInfo.getColumnPositionOffset();
		return childLayerInfo.getLayer().getColumnIndexByPosition(childColumnPosition);
	}

	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(localColumnPosition, 0);
		if (childLayerInfo == null) {
			return -1;
		}
		return localColumnPosition - childLayerInfo.getColumnPositionOffset();
	}

	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByChildLayer(sourceUnderlyingLayer);
		if (childLayerInfo == null) {
			return -1;
		}
		return childLayerInfo.columnPositionOffset + underlyingColumnPosition;
	}

	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByChildLayer(sourceUnderlyingLayer);
		if (childLayerInfo == null) {
			return null;
		}

		Collection<Range> localColumnPositionRanges = new ArrayList<Range>();

		int offset = childLayerInfo.columnPositionOffset;
		for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
			localColumnPositionRanges.add(new Range(offset + underlyingColumnPositionRange.start, offset + underlyingColumnPositionRange.end));
		}

		return localColumnPositionRanges;
	}

	// Width

    public int getWidth() {
		ChildLayerInfo lastChildLayerInfo = getChildLayerInfoByLayout(layoutXCount - 1, 0);
		return lastChildLayerInfo.getWidthOffset() + lastChildLayerInfo.getLayer().getWidth();
	}

	public int getPreferredWidth() {
		int preferredWidth = 0;
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			preferredWidth += childLayerLayout[layoutX][0].getPreferredWidth();
		}
		return preferredWidth;
	}

	/**
	 * @param compositeColumnPosition position in the composite layer.
	 */
    public int getColumnWidthByPosition(int compositeColumnPosition) {
    	ChildLayerInfo childLayerInfo = getChildLayerInfoByColumnPosition(compositeColumnPosition);
		if (childLayerInfo == null) {
			return -1;
		}
    	int childColumnPosition = compositeColumnPosition - childLayerInfo.getColumnPositionOffset();
		return childLayerInfo.getLayer().getColumnWidthByPosition(childColumnPosition);
    }

	// Column resize

    public boolean isColumnPositionResizable(int compositeColumnPosition) {
    	//Only looks at the header
    	ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(compositeColumnPosition, 0);
		if (childLayerInfo == null) {
			return false;
		}
		int childColumnPosition = compositeColumnPosition - childLayerInfo.getColumnPositionOffset();
		return childLayerInfo.getLayer().isColumnPositionResizable(childColumnPosition);
    }

    // X

    /**
     * @param x pixel position - starts from 0
     */
    public int getColumnPositionByX(int x) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByX(x);
		if (childLayerInfo == null) {
			return -1;
		}
		int childX = x - childLayerInfo.getWidthOffset();
		int childColumnPosition = childLayerInfo.getLayer().getColumnPositionByX(childX);
		return childLayerInfo.getColumnPositionOffset() + childColumnPosition;
	}

    public int getStartXOfColumnPosition(int columnPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByColumnPosition(columnPosition);
		if (childLayerInfo == null) {
			return -1;
		}
		int childColumnPosition = columnPosition - childLayerInfo.getColumnPositionOffset();
		return childLayerInfo.getWidthOffset() + childLayerInfo.getLayer().getStartXOfColumnPosition(childColumnPosition);
	}

    // Underlying

    public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();

		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, 0);
			if (columnPosition < childLayerInfo.getColumnPositionOffset() + childLayerInfo.getLayer().getColumnCount()) {
				break;
			}

			layoutX++;
		}

		if (layoutX >= layoutXCount) {
			return null;
		}

		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, layoutY);
			underlyingLayers.add(childLayerInfo.getLayer());
			layoutY++;
		}

		return underlyingLayers;
    }

	// Vertical features

	// Rows

	public int getRowCount() {
		ChildLayerInfo lastChildLayerInfo = getChildLayerInfoByLayout(0, layoutYCount - 1);
		return lastChildLayerInfo.getRowPositionOffset() + lastChildLayerInfo.getLayer().getRowCount();
	}

	public int getPreferredRowCount() {
		int preferredRowCount = 0;
		for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
			preferredRowCount += childLayerLayout[0][layoutY].getPreferredRowCount();
		}
		return preferredRowCount;
	}

	public int getRowIndexByPosition(int compositeRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByRowPosition(compositeRowPosition);
		if (childLayerInfo == null) {
			return -1;
		}
		int childRowPosition = compositeRowPosition - childLayerInfo.getRowPositionOffset();
		return childLayerInfo.getLayer().getRowIndexByPosition(childRowPosition);

	}

	public int localToUnderlyingRowPosition(int localRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(0, localRowPosition);
		if (childLayerInfo == null) {
			return -1;
		}
		return localRowPosition - childLayerInfo.getRowPositionOffset();
	}

	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByChildLayer(sourceUnderlyingLayer);
		if (childLayerInfo == null) {
			return -1;
		}
		return childLayerInfo.rowPositionOffset + underlyingRowPosition;
	}

	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByChildLayer(sourceUnderlyingLayer);
		if (childLayerInfo == null) {
			return null;
		}

		Collection<Range> localRowPositionRanges = new ArrayList<Range>();

		int offset = childLayerInfo.rowPositionOffset;
		for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
			localRowPositionRanges.add(new Range(offset + underlyingRowPositionRange.start, offset + underlyingRowPositionRange.end));
		}

		return localRowPositionRanges;
	}

	// Height

    public int getHeight() {
    	ChildLayerInfo lastChildLayerInfo = getChildLayerInfoByLayout(0, layoutYCount - 1);
    	return lastChildLayerInfo.getHeightOffset() + lastChildLayerInfo.getLayer().getHeight();
	}

	public int getPreferredHeight() {
		int preferredHeight = 0;
		for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
			preferredHeight += childLayerLayout[0][layoutY].getPreferredHeight();
		}
		return preferredHeight;
	}

    public int getRowHeightByPosition(int compositeRowPosition) {
    	ChildLayerInfo childLayerInfo = getChildLayerInfoByRowPosition(compositeRowPosition);
		if (childLayerInfo == null) {
			return -1;
		}
		int childRowPosition = compositeRowPosition - childLayerInfo.getRowPositionOffset();
		return childLayerInfo.getLayer().getRowHeightByPosition(childRowPosition);
    }

    // Row resize

    /**
     * @return false if the row position is out of bounds
     */
    public boolean isRowPositionResizable(int compositeRowPosition) {
    	ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(0, compositeRowPosition);
		if (childLayerInfo == null) {
			return false;
		}
    	int childRowPosition = compositeRowPosition - childLayerInfo.getRowPositionOffset();
		return childLayerInfo.getLayer().isRowPositionResizable(childRowPosition);
    }

	// Y

	/**
	 * Get the <i>row</i> position relative to the layer the containing coordinate y.
	 * @param x Mouse event Y position.
	 */
    public int getRowPositionByY(int y) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByXY(0, y);
		if (childLayerInfo == null) {
			return -1;
		}
		int childY = y - childLayerInfo.getHeightOffset();
		int childRowPosition = childLayerInfo.getLayer().getRowPositionByY(childY);
		return childLayerInfo.getRowPositionOffset() + childRowPosition;
	}

    public int getStartYOfRowPosition(int rowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByRowPosition(rowPosition);
		if (childLayerInfo == null) {
			return -1;
		}
		int childRowPosition = rowPosition - childLayerInfo.getRowPositionOffset();
		return childLayerInfo.getHeightOffset() + childLayerInfo.getLayer().getStartYOfRowPosition(childRowPosition);
	}

    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
		Collection<ILayer> underlyingLayers = new HashSet<ILayer>();

		int columnPosition = 0;
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(columnPosition, rowPosition);
		while (childLayerInfo != null) {
			ILayer childLayer = childLayerInfo.getLayer();
			underlyingLayers.add(childLayer);

			columnPosition += childLayer.getColumnCount();
			childLayerInfo = getChildLayerInfoByPosition(columnPosition, rowPosition);
		}

		return underlyingLayers;
    }

	// Cell features

	@Override
	public LayerCell getCellByPosition(int compositeColumnPosition, int compositeRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(compositeColumnPosition, compositeRowPosition);

		if (childLayerInfo == null) {
			return null;
		}

		ILayer childLayer = childLayerInfo.getLayer();
		int childColumnPosition = compositeColumnPosition - childLayerInfo.getColumnPositionOffset();
		int childRowPosition = compositeRowPosition - childLayerInfo.getRowPositionOffset();

		final LayerCell cell = childLayer.getCellByPosition(childColumnPosition, childRowPosition);

		if (cell != null) {
			cell.updatePosition(
					this,
					underlyingToLocalColumnPosition(childLayer, cell.getOriginColumnPosition()),
					underlyingToLocalRowPosition(childLayer, cell.getOriginRowPosition()),
					underlyingToLocalColumnPosition(childLayer, cell.getColumnPosition()),
					underlyingToLocalRowPosition(childLayer, cell.getRowPosition())
			);
		}

		return cell;
	}

	@Override
	public Rectangle getBoundsByPosition(int compositeColumnPosition, int compositeRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(compositeColumnPosition, compositeRowPosition);

		if (childLayerInfo == null) {
			return null;
		}

		ILayer childLayer = childLayerInfo.getLayer();
		int childColumnPosition = compositeColumnPosition - childLayerInfo.getColumnPositionOffset();
		int childRowPosition = compositeRowPosition - childLayerInfo.getRowPositionOffset();

		final Rectangle bounds = childLayer.getBoundsByPosition(childColumnPosition, childRowPosition);

		if (bounds != null) {
			bounds.x += childLayerInfo.widthOffset;
			bounds.y += childLayerInfo.heightOffset;
		}

		return bounds;
	}

	/**
	 * @return Rectangle bounding the cell position
	 *    x - pixel position of the top left of the rectangle
	 *    y - pixel position of the top left of the rectangle
	 *    width  - in pixels
	 *    height - in pixels
	 * Note - All values are -1 for a position out of bounds.
	 */
	public Rectangle getCellBounds(int compositeRowPosition, int compositeColumnPosition) {
		final Rectangle rectangle = new Rectangle(0, 0, 0, 0);

		rectangle.width = getColumnWidthByPosition(compositeColumnPosition);
		rectangle.height = getRowHeightByPosition(compositeRowPosition);

		rectangle.x = getStartXOfColumnPosition(compositeColumnPosition);
		rectangle.y = getStartYOfRowPosition(compositeRowPosition);

		return rectangle;
	}

	@Override
	public String getDisplayModeByPosition(int compositeColumnPosition, int compositeRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(compositeColumnPosition, compositeRowPosition);
		if (childLayerInfo == null) {
			return super.getDisplayModeByPosition(compositeColumnPosition, compositeRowPosition);
		}

		return childLayerInfo.getLayer().getDisplayModeByPosition(
				compositeColumnPosition - childLayerInfo.getColumnPositionOffset(),
				compositeRowPosition - childLayerInfo.getRowPositionOffset());
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int compositeColumnPosition, int compositeRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(compositeColumnPosition, compositeRowPosition);
		if (childLayerInfo == null) {
			return new LabelStack();
		}

		ILayer childLayer = childLayerInfo.getLayer();
		int childColumnPosition = compositeColumnPosition - childLayerInfo.getColumnPositionOffset();
		int childRowPosition = compositeRowPosition - childLayerInfo.getRowPositionOffset();
		LabelStack configLabels = childLayer.getConfigLabelsByPosition(childColumnPosition, childRowPosition);

		String regionName = childLayerToRegionNameMap.get(childLayer);
		IConfigLabelAccumulator configLabelAccumulator = regionNameToConfigLabelAccumulatorMap.get(regionName);
		if (configLabelAccumulator != null) {
			configLabelAccumulator.accumulateConfigLabels(configLabels, childColumnPosition, childRowPosition);
		}
		configLabels.addLabel(regionName);

		return configLabels;
	}

	public Object getDataValueByPosition(int compositeColumnPosition, int compositeRowPosition) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByPosition(compositeColumnPosition, compositeRowPosition);
		if (childLayerInfo == null) {
			return Integer.valueOf(-1);
		}

		return childLayerInfo.getLayer().getDataValueByPosition(
				compositeColumnPosition - childLayerInfo.getColumnPositionOffset(),
				compositeRowPosition - childLayerInfo.getRowPositionOffset());
	}

	// Child layer stuff

	public void setChildLayer(String regionName, ILayer childLayer, final int layoutX, final int layoutY) {
		if (childLayer == null) {
			throw new IllegalArgumentException("Cannot set null child layer");
		}

		childLayerToRegionNameMap.put(childLayer, regionName);

		childLayer.addLayerListener(this);
		childLayerToLayoutCoordinateMap.put(childLayer, new LayoutCoordinate(layoutX, layoutY));
		childLayerLayout[layoutX][layoutY] = childLayer;

		childLayer.setClientAreaProvider(new IClientAreaProvider() {
			public Rectangle getClientArea() {
				return getChildClientArea(layoutX, layoutY);
			}
		});
	}

	public IConfigLabelAccumulator getConfigLabelAccumulatorByRegionName(String regionName) {
		return regionNameToConfigLabelAccumulatorMap.get(regionName);
	}

	/**
	 * Sets the IConfigLabelAccumulator for the given named region. Replaces any existing IConfigLabelAccumulator.
	 */
	public void setConfigLabelAccumulatorForRegion(String regionName, IConfigLabelAccumulator configLabelAccumulator) {
		regionNameToConfigLabelAccumulatorMap.put(regionName, configLabelAccumulator);
	}

	/**
	 *  Adds the configLabelAccumulator to the existing label accumulators.
	 */
	public void addConfigLabelAccumulatorForRegion(String regionName, IConfigLabelAccumulator configLabelAccumulator) {
		IConfigLabelAccumulator existingConfigLabelAccumulator = regionNameToConfigLabelAccumulatorMap.get(regionName);
		AggregrateConfigLabelAccumulator aggregateAccumulator;
		if (existingConfigLabelAccumulator instanceof AggregrateConfigLabelAccumulator) {
			aggregateAccumulator = (AggregrateConfigLabelAccumulator) existingConfigLabelAccumulator;
		} else {
			aggregateAccumulator = new AggregrateConfigLabelAccumulator();
			aggregateAccumulator.add(existingConfigLabelAccumulator);
			regionNameToConfigLabelAccumulatorMap.put(regionName, aggregateAccumulator);
		}
		aggregateAccumulator.add(configLabelAccumulator);
	}

	private Rectangle getChildClientArea(final int layoutX, final int layoutY) {
		final ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, layoutY);

		final Rectangle compositeClientArea = getClientAreaProvider().getClientArea();

		final Rectangle childClientArea = new Rectangle(
				compositeClientArea.x + childLayerInfo.getWidthOffset(),
				compositeClientArea.y + childLayerInfo.getHeightOffset(),
				childLayerInfo.getLayer().getPreferredWidth(),
				childLayerInfo.getLayer().getPreferredHeight());

		final Rectangle intersection = compositeClientArea.intersection(childClientArea);

		return intersection;
	}

	/**
	 * @param layoutX col position in the CompositeLayer
	 * @param layoutY row position in the CompositeLayer
	 * @return child layer according to the Composite Layer Layout
	 */
	public ILayer getChildLayerByLayoutCoordinate(int layoutX, int layoutY) {
		return childLayerLayout[layoutX][layoutY];
	}

	/**
	 * Child layer at the specified pixel position
	 * @param x pixel value
	 * @param y pixel value
	 * @return <i>null</i> if the pixel position is out of bounds.
	 */
	public ILayer getChildLayerByXY(int x, int y) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByXY(x, y);
		return (childLayerInfo == null)
			? null : childLayerInfo.getLayer();
	}

	/**
	 * @param x pixel position
	 * @param y pixel position
	 * @return Region which the given position is in
	 */
	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		ChildLayerInfo childLayerInfo = getChildLayerInfoByXY(x, y);
		if (childLayerInfo == null) {

			return null;
		}

		ILayer childLayer = childLayerInfo.getLayer();

		int childX = x - childLayerInfo.getWidthOffset();
	    int childY = y - childLayerInfo.getHeightOffset();
		LabelStack regionLabels = childLayer.getRegionLabelsByXY(childX, childY);

		String regionName = childLayerToRegionNameMap.get(childLayer);
		regionLabels.addLabel(regionName);

		return regionLabels;
	}

	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		return getChildLayerInfoByPosition(columnPosition, rowPosition).getLayer();
	}

	// Child layer info

	private ChildLayerInfo getChildLayerInfoByXY(int x, int y) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, 0);
			if (childLayerInfo == null) {
				return null;
			}
			if (x < childLayerInfo.getWidthOffset() + childLayerInfo.getLayer().getWidth()) {
				break;
			}

			layoutX++;
		}

		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, layoutY);
			if (childLayerInfo == null) {
				return null;
			}
			if (y < childLayerInfo.getHeightOffset() + childLayerInfo.getLayer().getHeight()) {
				return childLayerInfo;
			}

			layoutY++;
		}

		return null;
	}

	private ChildLayerInfo getChildLayerInfoByX(int x) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, 0);
			if (childLayerInfo == null) {
				return null;
			}
			if (x < childLayerInfo.getWidthOffset() + childLayerInfo.getLayer().getWidth()) {
				return childLayerInfo;
			}

			layoutX++;
		}
		return null;
	}

	protected ChildLayerInfo getChildLayerInfoByColumnPosition(int compositeColumnPosition) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, 0);
			if (compositeColumnPosition < childLayerInfo.getColumnPositionOffset() + childLayerInfo.getLayer().getColumnCount()) {
				return childLayerInfo;
			}

			layoutX++;
		}
		return null;
	}

	protected ChildLayerInfo getChildLayerInfoByRowPosition(int compositeRowPosition) {
		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(0, layoutY);
			if (compositeRowPosition < childLayerInfo.getRowPositionOffset() + childLayerInfo.getLayer().getRowCount()) {
				return childLayerInfo;
			}

			layoutY++;
		}

		return null;
	}

	protected ChildLayerInfo getChildLayerInfoByPosition(int compositeColumnPosition, int compositeRowPosition) {
		int layoutX = 0;
		while (layoutX < layoutXCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, 0);
			if (compositeColumnPosition < childLayerInfo.getColumnPositionOffset() + childLayerInfo.getLayer().getColumnCount()) {
				break;
			}

			layoutX++;
		}

		if (layoutX >= layoutXCount) {
			return null;
		}

		int layoutY = 0;
		while (layoutY < layoutYCount) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByLayout(layoutX, layoutY);
			if (compositeRowPosition < childLayerInfo.getRowPositionOffset() + childLayerInfo.getLayer().getRowCount()) {
				return childLayerInfo;
			}

			layoutY++;
		}

		return null;
	}

	protected ChildLayerInfo getChildLayerInfoByLayout(int layoutX, int layoutY) {
		if (layoutX >= layoutXCount || layoutY >= layoutYCount) {
			return null;
		}
		if (childLayerInfos == null) {
			populateChildLayerInfos();
		}
		return childLayerInfos[layoutX][layoutY];
	}

	protected ChildLayerInfo getChildLayerInfoByChildLayer(ILayer childLayer) {
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				if (childLayer == childLayerLayout[layoutX][layoutY]) {
					return getChildLayerInfoByLayout(layoutX, layoutY);
				}
			}
		}

		return null;
	}

	/**
	 *
	 * @param isPositionMode flag indicating - search for child layer by position or X/Y values.
	 * @param compositeColumnPositionOrX Composite layer column position or X pixel value
	 * @param compositeRowPositionOrY Composite layer row position or Y pixel value
	 *
	 * @return <i>null</i> if the position or X/y values are outside layer bounds.
	 */
	protected void populateChildLayerInfos() {
		childLayerInfos = new ChildLayerInfo[layoutXCount][layoutYCount];

		int columnPositionOffset = 0;
		int widthOffset = 0;
		for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
			int rowPositionOffset = 0;
			int heightOffset = 0;
			for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
				ILayer childLayer = childLayerLayout[layoutX][layoutY];

				childLayerInfos[layoutX][layoutY] = new ChildLayerInfo(childLayer, columnPositionOffset, rowPositionOffset, widthOffset, heightOffset);

				if (layoutY < layoutYCount - 1) {
					rowPositionOffset += childLayer.getRowCount();
					heightOffset += childLayer.getHeight();
				}
			}

			if (layoutX < layoutXCount - 1) {
				ILayer childLayer = childLayerLayout[layoutX][0];
				columnPositionOffset += childLayer.getColumnCount();
				widthOffset += childLayer.getWidth();
			}
		}
	}

	protected static final class ChildLayerInfo {

		private final ILayer layer;

		private final int columnPositionOffset;
		private final int rowPositionOffset;
		private final int widthOffset;
		private final int heightOffset;

		public ChildLayerInfo(
				ILayer layer,
				int columnPositionOffset,
				int rowPositionOffset,
				int widthOffset,
		        int heightOffset) {
			this.layer = layer;
			this.columnPositionOffset = columnPositionOffset;
			this.rowPositionOffset = rowPositionOffset;
			this.widthOffset = widthOffset;
			this.heightOffset = heightOffset;
		}

		public ILayer getLayer() {
			return layer;
		}

		public int getColumnPositionOffset() {
			return columnPositionOffset;
		}

		public int getRowPositionOffset() {
			return rowPositionOffset;
		}

		public int getWidthOffset() {
			return widthOffset;
		}

		public int getHeightOffset() {
			return heightOffset;
		}

	}

	protected class CompositeLayerPainter implements ILayerPainter {

		public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle rectangle, IConfigRegistry configuration) {
			int x = xOffset;
			for (int layoutX = 0; layoutX < layoutXCount; layoutX++) {
				int y = yOffset;
				for (int layoutY = 0; layoutY < layoutYCount; layoutY++) {
					ILayer childLayer = childLayerLayout[layoutX][layoutY];

					Rectangle childLayerRectangle = new Rectangle(x, y, childLayer.getWidth(), childLayer.getHeight());

					childLayerRectangle = rectangle.intersection(childLayerRectangle);

					Rectangle originalClipping = gc.getClipping();
					gc.setClipping(childLayerRectangle);

					childLayer.getLayerPainter().paintLayer(natLayer, gc, x, y, childLayerRectangle, configuration);

					gc.setClipping(originalClipping);
					y += childLayer.getHeight();
				}

				x += childLayerLayout[layoutX][0].getWidth();
			}
		}

		public Rectangle adjustCellBounds(Rectangle cellBounds) {
			ChildLayerInfo childLayerInfo = getChildLayerInfoByXY(cellBounds.x, cellBounds.y);

			int widthOffset = childLayerInfo.getWidthOffset();
			int heightOffset = childLayerInfo.getHeightOffset();

			cellBounds.x -= widthOffset;
			cellBounds.y -= heightOffset;

			Rectangle adjustedChildCellBounds = childLayerInfo.getLayer().getLayerPainter().adjustCellBounds(cellBounds);

			adjustedChildCellBounds.x += widthOffset;
			adjustedChildCellBounds.y += heightOffset;

			return adjustedChildCellBounds;
		}

	}

}