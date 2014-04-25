package net.sourceforge.nattable.conflation;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.nattable.layer.event.ILayerEvent;

public abstract class AbstractEventConflater implements IEventConflater {

	protected List<ILayerEvent> queue = new LinkedList<ILayerEvent>();

	public void addEvent(ILayerEvent event){
		queue.add(event);
	}
	
	public void clearQueue() {
		queue.clear();
	}
	
	public int getCount() {
		return queue.size();
	}

	public abstract Runnable getConflaterTask();
}
