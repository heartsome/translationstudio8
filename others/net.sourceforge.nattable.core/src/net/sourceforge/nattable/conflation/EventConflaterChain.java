package net.sourceforge.nattable.conflation;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sourceforge.nattable.layer.event.ILayerEvent;

/**
 * A Chain of Conflaters. Every conflater in the chain is given the chance to
 * queue an event. When the chain runs every conflater in the chain can run its
 * own task to handle the events as it sees fit.
 */
public class EventConflaterChain implements IEventConflater {

	public static final int DEFAULT_INITIAL_DELAY = 100;
	public static final int DEFAULT_REFRESH_INTERVAL = 100;

	private final List<IEventConflater> chain = new LinkedList<IEventConflater>();
	private boolean started;
	private final long refreshInterval;
	private final long initialDelay;
	private ScheduledExecutorService scheduler;

	public EventConflaterChain() {
		this(DEFAULT_REFRESH_INTERVAL, DEFAULT_INITIAL_DELAY);
	}

	public EventConflaterChain(int refreshInterval, int initialDelay) {
		this.refreshInterval = refreshInterval;
		this.initialDelay = initialDelay;
	}

	public void add(IEventConflater conflater) {
		chain.add(conflater);
	}

	public void start() {
		scheduler = Executors.newScheduledThreadPool(1);
		if (!started) {
			scheduler.scheduleWithFixedDelay(getConflaterTask(), initialDelay, refreshInterval, TimeUnit.MILLISECONDS);
			started = true;
		}
	}

	public void stop() {
		if (started) {
			scheduler.shutdownNow();
			started = false;
		}
	}

	public void addEvent(ILayerEvent event) {
		for (IEventConflater eventConflater : chain) {
			eventConflater.addEvent(event);
		}
	}

	public void clearQueue() {
		for (IEventConflater eventConflater : chain) {
			eventConflater.clearQueue();
		}
	}

	public int getCount() {
		int count = 0;
		for (IEventConflater eventConflater : chain) {
			count = count + eventConflater.getCount();
		}
		return count;
	}

	public Runnable getConflaterTask() {
		return new Runnable() {
			public void run() {
				for (IEventConflater conflater : chain) {
					conflater.getConflaterTask().run();
				}
			}
		};
	}
}
