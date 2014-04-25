package net.sourceforge.nattable.freeze.config;

import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.freeze.action.FreezeGridAction;
import net.sourceforge.nattable.freeze.action.UnFreezeGridAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;

import org.eclipse.swt.SWT;

public class DefaultFreezeGridBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL | SWT.SHIFT, 'f'), new FreezeGridAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL | SWT.SHIFT, 'u'), new UnFreezeGridAction());
	}
}
