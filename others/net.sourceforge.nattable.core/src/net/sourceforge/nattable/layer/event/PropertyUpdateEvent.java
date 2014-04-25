package net.sourceforge.nattable.layer.event;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.Rectangle;

public class PropertyUpdateEvent<T> implements IVisualChangeEvent {

	private final PropertyChangeEvent propertyChangeEvent = null;
	private final T sourceBean;
	private final String propertyName;
	private final Object newValue;
	private final Object oldValue;

	private ILayer layer;

	public PropertyUpdateEvent(ILayer layer, T sourceBean, String propertyName, Object oldValue, Object newValue) {
		this.layer = layer;
		this.sourceBean = sourceBean;
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	// Interface methods

	public ILayerEvent cloneEvent() {
		return new PropertyUpdateEvent<T>(this.layer, this.sourceBean, this.propertyName, this.oldValue, this.newValue);
	}

	public boolean convertToLocal(ILayer localLayer) {
		this.layer = localLayer;
		return true;
	}

	public Collection<Rectangle> getChangedPositionRectangles() {
		return Arrays.asList(new Rectangle(0, 0, layer.getWidth(), layer.getHeight()));
	}

	public ILayer getLayer() {
		return layer;
	}

	// Accessors

	public PropertyChangeEvent getPropertyChangeEvent() {
		return propertyChangeEvent;
	}

	public T getSourceBean() {
		return sourceBean;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getNewValue() {
		return newValue;
	}

	public Object getOldValue() {
		return oldValue;
	}
}
