package net.sourceforge.nattable.group.event;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.VisualRefreshEvent;

public class GroupColumnsEvent extends VisualRefreshEvent {

	public GroupColumnsEvent(ILayer layer) {
		super(layer);
	}

	protected GroupColumnsEvent(GroupColumnsEvent event) {
		super(event);
	}
	
	public GroupColumnsEvent cloneEvent() {
		return new GroupColumnsEvent(this);
	}

}
