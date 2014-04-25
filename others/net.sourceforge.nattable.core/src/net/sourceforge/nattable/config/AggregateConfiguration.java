package net.sourceforge.nattable.config;

import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;

/**
 * Aggregates {@link IConfiguration} objects and invokes configure methods on all its members.
 */
public class AggregateConfiguration implements IConfiguration {

	private final Collection<IConfiguration> configurations = new LinkedList<IConfiguration>();

	public void addConfiguration(IConfiguration configuration) {
		configurations.add(configuration);
	}

	public void configureLayer(ILayer layer) {
		for (IConfiguration configuration : configurations) {
			configuration.configureLayer(layer);
		}
	}

	public void configureRegistry(IConfigRegistry configRegistry) {
		for (IConfiguration configuration : configurations) {
			configuration.configureRegistry(configRegistry);
		}
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		for (IConfiguration configuration : configurations) {
			configuration.configureUiBindings(uiBindingRegistry);
		}
	}

}
