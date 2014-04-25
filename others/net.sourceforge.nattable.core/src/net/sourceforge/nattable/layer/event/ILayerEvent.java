package net.sourceforge.nattable.layer.event;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.layer.ILayer;

/**
 * Event fired by the {@link ILayerCommandHandler} classes (usually to signal to handling of a {@link ILayerCommand}).<br/>
 * Every layer in the grid is given a chance to respond to an event via {@link ILayer#handleLayerEvent(ILayerEvent)}.
 * 
 *  @see ILayerEventHandler
 */
public interface ILayerEvent {

	/**
	 * Convert the column/row positions carried by the event to the layer about to
	 * handle the event.
	 * @param localLayer layer about to receive the event
	 * @return TRUE if successfully converted, FALSE otherwise
	 */
	public boolean convertToLocal(ILayer localLayer);

	/**
	 * @return A cloned copy of the event. This cloned copy is provided to each listener.
	 */
	public ILayerEvent cloneEvent();
	
}
