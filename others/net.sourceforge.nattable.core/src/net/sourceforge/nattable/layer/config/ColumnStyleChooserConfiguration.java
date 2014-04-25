package net.sourceforge.nattable.layer.config;

import net.sourceforge.nattable.config.AbstractRegistryConfiguration;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.AbstractLayer;
import net.sourceforge.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import net.sourceforge.nattable.style.editor.command.DisplayColumnStyleEditorCommandHandler;

/**
 * Registers the {@link DisplayColumnStyleEditorCommandHandler}
 * 
 */
public class ColumnStyleChooserConfiguration extends AbstractRegistryConfiguration {

	private AbstractLayer bodyLayer;
	private ColumnOverrideLabelAccumulator labelAccumulator;

	public ColumnStyleChooserConfiguration(AbstractLayer bodyLayer) {
		this.bodyLayer = bodyLayer;
		labelAccumulator = new ColumnOverrideLabelAccumulator(bodyLayer);
		bodyLayer.setConfigLabelAccumulator(labelAccumulator);
	}

	public void configureRegistry(IConfigRegistry configRegistry) {
		DisplayColumnStyleEditorCommandHandler columnChooserCommandHandler =
			new DisplayColumnStyleEditorCommandHandler(labelAccumulator, configRegistry);

		bodyLayer.registerCommandHandler(columnChooserCommandHandler);
	}
}
