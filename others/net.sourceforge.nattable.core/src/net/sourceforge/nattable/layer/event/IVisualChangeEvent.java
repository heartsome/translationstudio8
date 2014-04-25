package net.sourceforge.nattable.layer.event;

import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.Rectangle;

/**
 * An event which indicates a visible change to one or more cells in the layer.
 * A visible change simply indicates that one or more cells should be redrawn.
 * It does not imply a structural change to the layer. This means that cached
 * structure does not need to be invalidated due to visible change events.
 */
public interface IVisualChangeEvent extends ILayerEvent {

	/**
	 * Get the layer that the visible change event is originating from.
	 */
	public ILayer getLayer();
	
	/**
	 * Get the position rectangles that have changed and need to be redrawn.
	 * If no rectangles are returned, then the receiver should assume that the
	 * entire layer is changed and will need to be redrawn.
	 */
	public Collection<Rectangle> getChangedPositionRectangles();
	
}
