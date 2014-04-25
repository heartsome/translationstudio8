package net.sourceforge.nattable.print.config;

import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.print.action.PrintAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;

import org.eclipse.swt.SWT;

public class DefaultPrintBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, 'p'), new PrintAction());
	}

}
