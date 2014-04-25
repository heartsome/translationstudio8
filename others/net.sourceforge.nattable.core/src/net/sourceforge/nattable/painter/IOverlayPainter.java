package net.sourceforge.nattable.painter;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.GC;

/**
 * An overlay painter is given a chance to paint the canvas once<br/>
 * the layers have finished rendering.
 * 
 *  @see NatTable#addOverlayPainter(IOverlayPainter)
 */
public interface IOverlayPainter {

	public void paintOverlay(GC gc, ILayer layer);
	
}
