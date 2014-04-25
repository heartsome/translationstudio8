package net.sourceforge.nattable.config;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;

/**
 * Casts the layer to be to the type parameter for convenience. 
 * @param <L> type of the layer being configured 
 */
public abstract class AbstractLayerConfiguration<L extends ILayer> implements IConfiguration {

	@SuppressWarnings("unchecked")
	public void configureLayer(ILayer layer) {
		configureTypedLayer((L) layer);
	}
	
	public abstract void configureTypedLayer(L layer);
	
	public void configureRegistry(IConfigRegistry configRegistry) {}
	
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}

}
