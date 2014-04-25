package net.sourceforge.nattable.group.event;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.VisualRefreshEvent;

public class UngroupColumnsEvent extends VisualRefreshEvent {

	public UngroupColumnsEvent(ILayer layer) {
		super(layer);
	}

	protected UngroupColumnsEvent(UngroupColumnsEvent event) {
		super(event);
	}
	
	public UngroupColumnsEvent cloneEvent() {
		return new UngroupColumnsEvent(this);
	}

}
