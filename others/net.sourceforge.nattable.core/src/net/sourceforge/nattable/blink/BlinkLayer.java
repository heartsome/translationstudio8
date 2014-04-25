package net.sourceforge.nattable.blink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.nattable.blink.command.BlinkTimerEnableCommandHandler;
import net.sourceforge.nattable.blink.event.BlinkEvent;
import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.IndexCoordinate;
import net.sourceforge.nattable.data.IColumnPropertyResolver;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.data.IRowIdAccessor;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.PropertyUpdateEvent;
import net.sourceforge.nattable.style.DisplayMode;

import org.eclipse.swt.widgets.Display;

/**
 * Blinks cells when they are updated.
 * Returns blinking cell styles for the cells which have been updated.
 *
 * Every time its asked for config labels:
 * 	 Checks the UpdateEventsCache for changes to the cell
 * 	 If a cell is updated
 * 		The cell is tracked as 'blinking' and blinking config labels are returned
 *		A TimerTask is started which will stop the blinking after the blink period is over
 *
 * @param <T> Type of the Bean in the backing {@linkplain IDataProvider}
 */
public class BlinkLayer<T> extends AbstractLayerTransform implements IUniqueIndexLayer {

	private final IUniqueIndexLayer dataLayer;
	private final IRowDataProvider<T> rowDataProvider;
	private final IConfigRegistry configRegistry;
	private final IRowIdAccessor<T> rowIdAccessor;
	private Timer stopBlinkTimer;

	protected boolean blinkingEnabled = true;

	/** Cache all the update events allowing the layer to track what got updated */
	private final UpdateEventsCache<T> updateEventsCache;

	/** Duration of a single blink */
	private int blinkDurationInMilis = 1000;

	/** Track the updates which are currently blinking */
	Map<String, PropertyUpdateEvent<T>> blinkingUpdates = new HashMap<String, PropertyUpdateEvent<T>>();

	/** Track the blinking TimerTasks which are currently running*/
	Map<String, TimerTask> blinkingTasks = new HashMap<String, TimerTask>();

	public BlinkLayer(IUniqueIndexLayer dataLayer,
			IRowDataProvider<T> listDataProvider,
			IRowIdAccessor<T> rowIdAccessor,
			IColumnPropertyResolver columnPropertyResolver,
			IConfigRegistry configRegistry) {
		super(dataLayer);
		this.dataLayer = dataLayer;
		this.rowDataProvider = listDataProvider;
		this.rowIdAccessor = rowIdAccessor;
		this.configRegistry = configRegistry;
		this.updateEventsCache = new UpdateEventsCache<T>(rowIdAccessor, columnPropertyResolver);

		registerCommandHandler(new BlinkTimerEnableCommandHandler(this));
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		if (!blinkingEnabled) {
			return getUnderlyingLayer().getConfigLabelsByPosition(columnPosition, rowPosition);
		}

		int rowIndex = getUnderlyingLayer().getRowIndexByPosition(rowPosition);
		int columnIndex = getUnderlyingLayer().getColumnIndexByPosition(columnPosition);

		String rowId = rowIdAccessor.getRowId(rowDataProvider.getRowObject(rowIndex)).toString();
		String key = updateEventsCache.getKey(columnIndex, rowId);

		LabelStack underlyingLabelStack = getUnderlyingLayer().getConfigLabelsByPosition(columnPosition, rowPosition);
		IndexCoordinate coordinate = new IndexCoordinate(columnIndex, rowIndex);

		// Cell has been updated
		if (updateEventsCache.isUpdated(key)) {
			PropertyUpdateEvent<T> event = updateEventsCache.getEvent(key);

			// Old update in middle of a blink - cancel it
			if (blinkingUpdates.containsKey(key)) {
				blinkingTasks.get(key).cancel();
				getStopBlinkTimer().purge();
				blinkingTasks.remove(key);
				blinkingUpdates.remove(key);
			}

			LabelStack blinkingConfigTypes = resolveConfigTypes(event.getOldValue(), event.getNewValue(), coordinate, getUnderlyingLayer());

			// start blinking cell
			if (blinkingConfigTypes != null) {
				TimerTask stopBlinkTask = getStopBlinkTask(key, this);
				blinkingUpdates.put(key, event);
				blinkingTasks.put(key, stopBlinkTask);
				updateEventsCache.remove(key);
				getStopBlinkTimer().schedule(stopBlinkTask, blinkDurationInMilis);
				return blinkingConfigTypes;
			} else {
				return new LabelStack();
			}
		}
		// Previous blink timer is still running
		if (blinkingUpdates.containsKey(key)) {
			PropertyUpdateEvent<T> event = blinkingUpdates.get(key);
			return resolveConfigTypes(event.getOldValue(), event.getNewValue(), coordinate, getUnderlyingLayer());

		}
		return underlyingLabelStack;
	}

	private IBlinkingCellResolver getBlinkingCellResolver(List<String> configTypes) {
		return configRegistry.getConfigAttribute(BlinkConfigAttributes.BLINK_RESOLVER, DisplayMode.NORMAL, configTypes);
	}

	/**
	 * Find the {@link IBlinkingCellResolver} from the {@link ConfigRegistry}.
	 * Use the above to find the config types associated with a blinking cell.
	 */
	public LabelStack resolveConfigTypes(Object oldValue, Object newValue, final IndexCoordinate coordinate, ILayer underlyingLayer) {
		// Acquire default config types for the coordinate. Use these to search for the associated resolver.
		LabelStack underlyingLabelStack = underlyingLayer.getConfigLabelsByPosition(coordinate.columnIndex, coordinate.rowIndex);

		IBlinkingCellResolver resolver = getBlinkingCellResolver(underlyingLabelStack.getLabels());
		String[] blinkConfigTypes = null;
		if (resolver != null) {
			blinkConfigTypes = resolver.resolve(oldValue, newValue);
		}
		if(blinkConfigTypes != null && blinkConfigTypes.length > 0){
			return new LabelStack(blinkConfigTypes);
		}
		return null;
	}

	/**
	 * Stops the cell from blinking at the end of the blinking period.
	 */
	private TimerTask getStopBlinkTask(final String key, final ILayer layer) {

		return new TimerTask() {
			@Override
			public void run() {
				blinkingUpdates.remove(key);
				blinkingTasks.remove(key);
				if (blinkingTasks.isEmpty()) {
					stopBlinkTimer.cancel();
					stopBlinkTimer = null;
				}

				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						fireLayerEvent(new BlinkEvent(layer));
					}

				});
			}
		};

	}

	public Timer getStopBlinkTimer() {
		if (stopBlinkTimer == null) {
			stopBlinkTimer = new Timer("Stop Blink Task Timer");
		}
		return stopBlinkTimer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof PropertyUpdateEvent) {
			updateEventsCache.put((PropertyUpdateEvent<T>) event);
		}
		super.handleLayerEvent(event);
	}

	public void setBlinkingEnabled(boolean enabled) {
		this.blinkingEnabled = enabled;
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return dataLayer.getColumnPositionByIndex(columnIndex);
	}

	public int getRowPositionByIndex(int rowIndex) {
		return dataLayer.getRowPositionByIndex(rowIndex);
	}

	public void setBlinkDurationInMilis(int blinkDurationInMilis) {
		this.blinkDurationInMilis = blinkDurationInMilis;
	}

}