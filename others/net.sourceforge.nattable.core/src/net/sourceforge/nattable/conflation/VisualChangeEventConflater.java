package net.sourceforge.nattable.conflation;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IVisualChangeEvent;

/**
 * Gathers all the VisualChangeEvents. When its run, it refreshes/repaints the table. 
 *
 */
public class VisualChangeEventConflater extends AbstractEventConflater { 

	private final NatTable natTable;

	public VisualChangeEventConflater(NatTable ownerLayer) {
		natTable = ownerLayer;
	}

	@Override
	public void addEvent(ILayerEvent event) {
		if(event instanceof IVisualChangeEvent){
			super.addEvent(event);
		}
	}
	
	@Override
	public Runnable getConflaterTask() {
		return new Runnable() {

			public void run() {
				if (queue.size() > 0) {
					natTable.getDisplay().asyncExec(new Runnable() {
						public void run() {
							natTable.updateResize();
						}
					});

					clearQueue();
				}
			}
		};
	}

}