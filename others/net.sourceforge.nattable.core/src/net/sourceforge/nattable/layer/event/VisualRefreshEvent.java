package net.sourceforge.nattable.layer.event;

import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.Rectangle;

public abstract class VisualRefreshEvent implements IVisualChangeEvent {

	private ILayer layer;

	public VisualRefreshEvent(ILayer layer) {
		this.layer = layer;
	}
	
	protected VisualRefreshEvent(VisualRefreshEvent event) {
		this.layer = event.layer;
	}
	
	public ILayer getLayer() {
		return layer;
	}

	public boolean convertToLocal(ILayer localLayer) {
		layer = localLayer;
		
		return true;
	}
	
	public Collection<Rectangle> getChangedPositionRectangles() {
		return Arrays.asList(new Rectangle[] { new Rectangle(0, 0, layer.getColumnCount(), layer.getRowCount()) });
	}
	
}
