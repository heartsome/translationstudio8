package net.sourceforge.nattable.layer;

import net.sourceforge.nattable.layer.event.ILayerEvent;


/**
 * Object interested in receiving events related to a {@link ILayer}.
 */
public interface ILayerListener {

    /**
     * Handle an event notification from an {@link ILayer}
     */
    public void handleLayerEvent(ILayerEvent event);
    
}
