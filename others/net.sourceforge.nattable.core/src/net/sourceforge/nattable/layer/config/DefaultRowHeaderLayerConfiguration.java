package net.sourceforge.nattable.layer.config;

import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.layer.RowHeaderLayer;
import net.sourceforge.nattable.resize.config.DefaultRowResizeBindings;

/**
 * Default setup for the Row header area. Added by the {@link RowHeaderLayer}
 * Override the methods in this class to customize style / UI bindings.
 *
 * @see GridRegion
 */
public class DefaultRowHeaderLayerConfiguration extends AggregateConfiguration {

	public DefaultRowHeaderLayerConfiguration() {
		addRowHeaderStyleConfig();
		addRowHeaderUIBindings();
	}

	protected void addRowHeaderStyleConfig() {
		addConfiguration(new DefaultRowHeaderStyleConfiguration());
	}

	protected void addRowHeaderUIBindings() {
		addConfiguration(new DefaultRowResizeBindings());
	}

}
