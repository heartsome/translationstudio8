package net.sourceforge.nattable.config;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;

public abstract class AbstractRegistryConfiguration implements IConfiguration {

	public void configureLayer(ILayer layer) {}
	
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}

}
