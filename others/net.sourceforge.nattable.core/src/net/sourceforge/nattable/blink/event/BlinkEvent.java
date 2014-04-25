package net.sourceforge.nattable.blink.event;

import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IVisualChangeEvent;

import org.eclipse.swt.graphics.Rectangle;

public class BlinkEvent implements IVisualChangeEvent {

	private ILayer layer;

	public BlinkEvent(ILayer layer) {
		this.layer = layer;
	}

	public ILayerEvent cloneEvent() {
		return new BlinkEvent(this.layer);
	}

	public Collection<Rectangle> getChangedPositionRectangles() {
		return Arrays.asList(new Rectangle(0, 0, layer.getHeight(), layer.getWidth()));
	}

	public ILayer getLayer() {
		return layer;
	}

	public boolean convertToLocal(ILayer localLayer) {
		return true;
	}

}
