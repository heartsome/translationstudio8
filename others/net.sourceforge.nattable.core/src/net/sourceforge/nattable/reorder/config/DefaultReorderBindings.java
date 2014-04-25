package net.sourceforge.nattable.reorder.config;

import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.reorder.action.ColumnReorderDragMode;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;

/**
 * Column reorder bindings. Added by {@link DefaultColumnReorderLayerConfiguration}
 */
public class DefaultReorderBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.columnHeaderLeftClick(SWT.NONE), new ColumnReorderDragMode());
	}

}
