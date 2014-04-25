package net.sourceforge.nattable.layer.event;

import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractContextFreeEvent implements ILayerEvent {

	public boolean convertToLocal(ILayer localLayer) {
		return true;
	}

}
