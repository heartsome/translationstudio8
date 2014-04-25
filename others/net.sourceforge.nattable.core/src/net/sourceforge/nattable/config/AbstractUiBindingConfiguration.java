package net.sourceforge.nattable.config;

import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractUiBindingConfiguration implements IConfiguration {

	public void configureLayer(ILayer layer) {}
	
	public void configureRegistry(IConfigRegistry configRegistry) {}
	
}
