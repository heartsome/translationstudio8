package net.sourceforge.nattable.selection.config;

import net.sourceforge.nattable.selection.action.RowSelectionDragMode;
import net.sourceforge.nattable.selection.action.SelectRowAction;
import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;

public class RowOnlySelectionBindings extends DefaultSelectionBindings {

	@Override
	protected void configureBodyMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
		IMouseAction action = new SelectRowAction();
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), action);
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.CTRL), action);
		uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.CONTROL),	action);
	}

	@Override
	protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
		IDragMode dragMode = new RowSelectionDragMode();
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.NONE), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.CONTROL), dragMode);
		uiBindingRegistry.registerFirstMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.CONTROL), dragMode);
	}
}
