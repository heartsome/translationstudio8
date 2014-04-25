package net.sourceforge.nattable.layer.event;

public interface ILayerEventHandler <T extends ILayerEvent> {

	public void handleLayerEvent(T event);
	
	public Class<T> getLayerEventClass();
	
}
