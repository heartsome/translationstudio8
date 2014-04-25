package net.sourceforge.nattable.conflation;

import net.sourceforge.nattable.layer.event.ILayerEvent;

/**
 * A Conflater queues events and periodically runs a task to 
 * handle those Events. This prevents the table from
 * being overwhelmed by ultra fast updates.
 */
public interface IEventConflater {

	public abstract void addEvent(ILayerEvent event);

	public abstract void clearQueue();

	/**
	 * @return Number of events currently waiting to be handled
	 */
	public abstract int getCount();
	
	public Runnable getConflaterTask();

}