package net.sourceforge.nattable.edit.config;

import net.sourceforge.nattable.config.AbstractLayerConfiguration;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.data.validate.DefaultDataValidator;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.command.EditCellCommandHandler;
import net.sourceforge.nattable.edit.editor.TextCellEditor;
import net.sourceforge.nattable.edit.event.InlineCellEditEventHandler;
import net.sourceforge.nattable.grid.layer.GridLayer;

public class DefaultEditConfiguration extends AbstractLayerConfiguration<GridLayer> {

	@Override
	public void configureTypedLayer(GridLayer gridLayer) {
		gridLayer.registerCommandHandler(new EditCellCommandHandler());
		gridLayer.registerEventHandler(new InlineCellEditEventHandler(gridLayer));
	}
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new TextCellEditor());
		configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, new DefaultDataValidator());
	}
	
}
