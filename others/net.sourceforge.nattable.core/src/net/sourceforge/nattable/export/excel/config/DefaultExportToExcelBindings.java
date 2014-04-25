package net.sourceforge.nattable.export.excel.config;

import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.export.excel.action.ExportToExcelAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;

import org.eclipse.swt.SWT;

public class DefaultExportToExcelBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, 'e'), new ExportToExcelAction());
	}

}
