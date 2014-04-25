package net.sourceforge.nattable.layer.config;

import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.resize.config.DefaultColumnResizeBindings;

/**
 * Sets up Column header styling and resize bindings.
 * Added by the {@link ColumnHeaderLayer}
 */
public class DefaultColumnHeaderLayerConfiguration extends AggregateConfiguration {

	public DefaultColumnHeaderLayerConfiguration() {
		addColumnHeaderStyleConfig();
		addColumnHeaderUIBindings();
	}

	protected void addColumnHeaderUIBindings() {
		addConfiguration(new DefaultColumnResizeBindings());
	}

	protected void addColumnHeaderStyleConfig() {
		addConfiguration(new DefaultColumnHeaderStyleConfiguration());
	}

}
