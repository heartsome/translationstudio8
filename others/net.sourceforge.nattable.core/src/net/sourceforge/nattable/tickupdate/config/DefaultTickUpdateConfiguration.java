package net.sourceforge.nattable.tickupdate.config;

import org.eclipse.swt.SWT;

import net.sourceforge.nattable.config.AbstractLayerConfiguration;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.tickupdate.ITickUpdateHandler;
import net.sourceforge.nattable.tickupdate.TickUpdateConfigAttributes;
import net.sourceforge.nattable.tickupdate.action.TickUpdateAction;
import net.sourceforge.nattable.tickupdate.command.TickUpdateCommandHandler;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;

public class DefaultTickUpdateConfiguration extends AbstractLayerConfiguration<SelectionLayer> {
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(TickUpdateConfigAttributes.UPDATE_HANDLER, ITickUpdateHandler.UPDATE_VALUE_BY_ONE);
	}

	@Override
	public void configureTypedLayer(SelectionLayer selectionLayer) {
		selectionLayer.registerCommandHandler(new TickUpdateCommandHandler(selectionLayer));
	}
	
	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerKeyBinding(
				new KeyEventMatcher(SWT.NONE, SWT.KEYPAD_ADD), 
				new TickUpdateAction(true));

		uiBindingRegistry.registerKeyBinding(
				new KeyEventMatcher(SWT.NONE, SWT.KEYPAD_SUBTRACT), 
				new TickUpdateAction(false));
	}
	
}
