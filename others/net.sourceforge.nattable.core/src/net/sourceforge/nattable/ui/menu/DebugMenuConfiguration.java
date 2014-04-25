package net.sourceforge.nattable.ui.menu;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;

public class DebugMenuConfiguration extends AbstractUiBindingConfiguration {

	private final Menu debugMenu;

	public DebugMenuConfiguration(NatTable natTable) {
		debugMenu = new PopupMenuBuilder(natTable)
								.withInspectLabelsMenuItem()
								.build();

		natTable.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				debugMenu.dispose();
			}
		});
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, null, 3),
				new PopupMenuAction(debugMenu));
	}

}
