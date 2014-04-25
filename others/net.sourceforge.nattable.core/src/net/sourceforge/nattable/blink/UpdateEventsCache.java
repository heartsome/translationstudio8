package net.sourceforge.nattable.blink;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sourceforge.nattable.data.IColumnPropertyResolver;
import net.sourceforge.nattable.data.IRowIdAccessor;
import net.sourceforge.nattable.layer.event.PropertyUpdateEvent;

/**
 * Cache for the update events coming in.
 *
 * This cache is used by the {@link BlinkLayer} to check if updates are
 * available for a cell (hence, does it need to blink).
 *
 * @param <T> Type of the Bean in the backing list.
 */
public class UpdateEventsCache<T> {

	/** Initial startup delay for the expired event removal task */
	public static final int INITIAL_DELAY = 100;

	/** TTL for an event in the cache. The event is deleted when this expires */
	public static final int TIME_TO_LIVE = 500;

	private final IRowIdAccessor<T> rowIdAccessor;
	private final IColumnPropertyResolver columnPropertyAccessor;

	private Map<String, TimeStampedEvent> updateEvents;

	private ScheduledExecutorService cleanupScheduler;

	public UpdateEventsCache(IRowIdAccessor<T> rowIdAccessor, IColumnPropertyResolver columnPropertyAccessor) {
		this.rowIdAccessor = rowIdAccessor;
		this.columnPropertyAccessor = columnPropertyAccessor;
		this.updateEvents = new HashMap<String, TimeStampedEvent>();
	}

	/**
	 * We are not interested in update events which are too old and need not be blinked.
	 * This task cleans them up, by looking at the received time stamp.
	 */
	private Runnable getStaleUpdatesCleanupTask() {
		return new Runnable(){

			public void run() {
				Map<String, TimeStampedEvent> recentEvents = new HashMap<String, TimeStampedEvent>();
				Date recent = new Date(System.currentTimeMillis() - TIME_TO_LIVE);

				for (Map.Entry<String, TimeStampedEvent> entry : updateEvents.entrySet()) {
					if (entry.getValue().timeRecieved.after(recent)) {
						recentEvents.put(entry.getKey(), entry.getValue());
					}
				}
				synchronized (updateEvents) {
					updateEvents = recentEvents;
					checkUpdateEvents();
				}
			}

		};
	}

	private void checkUpdateEvents() {
		if (updateEvents.isEmpty()) {
			cleanupScheduler.shutdownNow();
			cleanupScheduler = null;
		} else {
			if (cleanupScheduler == null) {
				cleanupScheduler = Executors.newScheduledThreadPool(1);
				cleanupScheduler.scheduleAtFixedRate(getStaleUpdatesCleanupTask(), INITIAL_DELAY, TIME_TO_LIVE, TimeUnit.MILLISECONDS);
			}
		}
	}

	public void put(PropertyUpdateEvent<T> event) {
		String key = getKey(event);
		updateEvents.put(key, new TimeStampedEvent(event));
		checkUpdateEvents();
	}

	protected String getKey(PropertyUpdateEvent<T> event) {
		int columnIndex = columnPropertyAccessor.getColumnIndex(event.getPropertyName());
		String rowId = rowIdAccessor.getRowId(event.getSourceBean()).toString();
		return getKey(columnIndex, rowId);
	}

	public String getKey(int columnIndex, String rowId) {
		return columnIndex + "-" + rowId;
	}

	public PropertyUpdateEvent<T> getEvent(String key){
		return updateEvents.get(key).event;
	}

	public int getCount() {
		return updateEvents.size();
	}

	public boolean contains(int columnIndex, String rowId) {
		return updateEvents.containsKey(getKey(columnIndex, rowId));
	}

	public boolean isUpdated(String key) {
		return updateEvents.containsKey(key);
	}

	public void clear() {
		updateEvents.clear();
		checkUpdateEvents();
	}

	public void remove(String key) {
		updateEvents.remove(key);
		checkUpdateEvents();
	}

	/**
	 * Class to keep track of the time when an event was received
	 */
	private class TimeStampedEvent {
		Date timeRecieved;
		PropertyUpdateEvent<T> event;

		public TimeStampedEvent(PropertyUpdateEvent<T> event) {
			this.event = event;
			this.timeRecieved = new Date();
		}
	}

}