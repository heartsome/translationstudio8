package net.sourceforge.nattable.summaryrow;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.PropertyUpdateEvent;
import net.sourceforge.nattable.layer.event.RowUpdateEvent;
import net.sourceforge.nattable.layer.event.RowVisualChangeEvent;
import net.sourceforge.nattable.resize.command.RowResizeCommand;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.util.ArrayUtil;

/**
 * Adds a summary row at the end. Uses {@link ISummaryProvider} to calculate the summaries for all columns.
 * The default summary provider is the {@link ISummaryProvider#SUM} which adds up all the cells in the
 * column, if they are numeric values. This layer also adds the following labels:
 * <ol>
 *  <li>{@link SummaryRowLayer#DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX} + column index</li>
 * 	<li>{@link SummaryRowLayer#DEFAULT_SUMMARY_ROW_CONFIG_LABEL} to all cells in the row</li>
 * </ol>
 *
 * Example: column with index 1 will have the DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 1 label applied.
 * Styling and {@link ISummaryProvider} can be hooked up to these labels.
 *
 * @see DefaultSummaryRowConfiguration
 */
public class SummaryRowLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	public static final String DEFAULT_SUMMARY_ROW_CONFIG_LABEL = "SummaryRow";
	public static final String DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX = "SummaryColumn_";

	private final IConfigRegistry configRegistry;
	private int summaryRowHeight = DataLayer.DEFAULT_ROW_HEIGHT;

	public SummaryRowLayer(IUniqueIndexLayer underlyingDataLayer, IConfigRegistry configRegistry) {
		this(underlyingDataLayer, configRegistry, true);
	}

	public SummaryRowLayer(IUniqueIndexLayer underlyingDataLayer, IConfigRegistry configRegistry, boolean autoConfigure) {
		super(underlyingDataLayer);
		this.configRegistry = configRegistry;
		if(autoConfigure){
			addConfiguration(new DefaultSummaryRowConfiguration());
		}
	}

	/**
	 * Calculates the summary for the column using the {@link ISummaryProvider} from the {@link IConfigRegistry}.<br/>
	 * In order to prevent the table from freezing (for large data sets), the summary is calculated in a separate Thread. While
	 * summary is being calculated {@link ISummaryProvider#DEFAULT_SUMMARY_VALUE} is returned.
	 * 
	 * NOTE: Since this is a {@link IUniqueIndexLayer} sitting close to the {@link DataLayer}, columnPosition == columnIndex
	 */
	@Override
	public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			if (getSummaryFromCache(columnPosition) != null) {
				return getSummaryFromCache(columnPosition);
			} else {
				
				// Get the summary provider from the configuration registry
				LabelStack labelStack = getConfigLabelsByPosition(columnPosition, rowPosition);
				String[] configLabels = labelStack.getLabels().toArray(ArrayUtil.STRING_TYPE_ARRAY);
				
				final ISummaryProvider summaryProvider = configRegistry.getConfigAttribute(
						SummaryRowConfigAttributes.SUMMARY_PROVIDER, DisplayMode.NORMAL, configLabels);
				
				// If there is no Summary provider - skip processing
				if(summaryProvider == ISummaryProvider.NONE){
					return ISummaryProvider.DEFAULT_SUMMARY_VALUE;
				}

				// Start thread to calculate summary
				new Thread() {
					@Override
					public void run() {
						Object summaryValue = calculateColumnSummary(columnPosition, summaryProvider);
						addToCache(columnPosition, summaryValue);
						fireLayerEvent(new RowUpdateEvent(SummaryRowLayer.this, rowPosition));
					}
				}.start();
			}
			return ISummaryProvider.DEFAULT_SUMMARY_VALUE;
		}
		return super.getDataValueByPosition(columnPosition, rowPosition);
	}

	private Object calculateColumnSummary(int columnIndex, ISummaryProvider summaryProvider) {
		Object summaryValue = null;
		if (summaryProvider != null) {
			summaryValue = summaryProvider.summarize(columnIndex);
		}
		return summaryValue;
	}

	/** Cache the calculated summary value, since its CPU intensive */
	protected Map<Integer, Object> summaryCache = new HashMap<Integer, Object>();
	
	public Object getSummaryFromCache(Integer columnIndex) {
		return summaryCache.get(columnIndex);
	}

	protected void addToCache(Integer columnIndex, Object summaryValue) {
		summaryCache.put(columnIndex, summaryValue);
	}

	protected void clearSummaryCache() {
		summaryCache.clear();
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		if (command instanceof RowResizeCommand) {
			RowResizeCommand rowResizeCommand = (RowResizeCommand) command;
			if (isSummaryRowPosition(rowResizeCommand.getRowPosition())) {
				summaryRowHeight = rowResizeCommand.getNewHeight();
				return true;
			}
		}
		return super.doCommand(command);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof RowVisualChangeEvent || event instanceof PropertyUpdateEvent) {
			clearSummaryCache();
		}
		super.handleLayerEvent(event);
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return new LabelStack(
					DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + columnPosition, 
					DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
		}
		return super.getConfigLabelsByPosition(columnPosition, rowPosition);
	}

	@Override
	public LayerCell getCellByPosition(int columnPosition, int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return new LayerCell(this, columnPosition, rowPosition);
		}
		return super.getCellByPosition(columnPosition, rowPosition);
	}

	@Override
	public int getHeight() {
		return super.getHeight() + getRowHeightByPosition(getRowCount() - 1);
	}

	@Override
	public int getRowCount() {
		return super.getRowCount() + 1;
	}

	@Override
	public int getRowIndexByPosition(int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return rowPosition;
		}
		return super.getRowIndexByPosition(rowPosition);
	}

	@Override
	public int getRowPositionByY(int y) {
		return LayerUtil.getRowPositionByY(this, y);
	}

	private boolean isSummaryRowPosition(int rowPosition) {
		return rowPosition == super.getRowCount();
	}

	@Override
	public int getRowHeightByPosition(int rowPosition) {
		if (isSummaryRowPosition(rowPosition)) {
			return summaryRowHeight;
		}
		return super.getRowHeightByPosition(rowPosition);
	}

	@Override
	public int getPreferredRowCount() {
		return getRowCount();
	}

	public int getRowPositionByIndex(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < getRowCount()) {
			return rowIndex;
		} else {
			return -1;
		}
	}

	public int getColumnPositionByIndex(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < getColumnCount()) {
			return columnIndex;
		} else {
			return -1;
		}
	}
}