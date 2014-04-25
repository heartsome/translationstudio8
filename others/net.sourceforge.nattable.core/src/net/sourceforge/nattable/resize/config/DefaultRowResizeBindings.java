package net.sourceforge.nattable.resize.config;

import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.resize.action.AutoResizeRowAction;
import net.sourceforge.nattable.resize.action.RowResizeCursorAction;
import net.sourceforge.nattable.resize.event.RowResizeEventMatcher;
import net.sourceforge.nattable.resize.mode.RowResizeDragMode;
import net.sourceforge.nattable.ui.action.ClearCursorAction;
import net.sourceforge.nattable.ui.action.NoOpMouseAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;

public class DefaultRowResizeBindings extends AbstractUiBindingConfiguration {

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		// Mouse move - Show resize cursor
		uiBindingRegistry.registerFirstMouseMoveBinding(new RowResizeEventMatcher(SWT.NONE, 0), new RowResizeCursorAction());
		uiBindingRegistry.registerMouseMoveBinding(new MouseEventMatcher(), new ClearCursorAction());
		
		// Row resize
		uiBindingRegistry.registerFirstMouseDragMode(new RowResizeEventMatcher(SWT.NONE, 1), new RowResizeDragMode());

		uiBindingRegistry.registerDoubleClickBinding(new RowResizeEventMatcher(SWT.NONE, 1), new AutoResizeRowAction());
		uiBindingRegistry.registerSingleClickBinding(new RowResizeEventMatcher(SWT.NONE, 1), new NoOpMouseAction());
	}
	
}
