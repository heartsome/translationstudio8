package net.sourceforge.nattable.reorder.config;

import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.reorder.ColumnReorderLayer;

/**
 * Added by the {@link ColumnReorderLayer}
 */
public class DefaultColumnReorderLayerConfiguration extends AggregateConfiguration {

	public DefaultColumnReorderLayerConfiguration() {
		addColumnReorderUIBindings();
	}

	protected void addColumnReorderUIBindings() {
		addConfiguration(new DefaultReorderBindings());
	}

}
