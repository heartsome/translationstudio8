package net.sourceforge.nattable.selection.config;

import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.search.config.DefaultSearchBindings;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.tickupdate.config.DefaultTickUpdateConfiguration;

/**
 * Sets up default styling and UI bindings. Override the methods in here to
 * customize behavior. Added by the {@link SelectionLayer}
 */
public class DefaultSelectionLayerConfiguration extends AggregateConfiguration {

	public DefaultSelectionLayerConfiguration() {
		addSelectionStyleConfig();
		addSelectionUIBindings();
		addSearchUIBindings();
		addTickUpdateConfig();
		addMoveSelectionConfig();
	}

	protected void addSelectionStyleConfig() {
		addConfiguration(new DefaultSelectionStyleConfiguration());
	}

	protected void addSelectionUIBindings() {
		addConfiguration(new DefaultSelectionBindings());
	}

	protected void addSearchUIBindings() {
		addConfiguration(new DefaultSearchBindings());
	}

	protected void addTickUpdateConfig() {
		addConfiguration(new DefaultTickUpdateConfiguration());
	}

	protected void addMoveSelectionConfig() {
		addConfiguration(new DefaultMoveSelectionConfiguration());
	}
}
