package net.sourceforge.nattable.ui.menu;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;

public class BodyMenuConfiguration extends AbstractUiBindingConfiguration {

	private final Menu colHeaderMenu;
	
	public BodyMenuConfiguration(NatTable natTable, ILayer bodyLayer) {
		colHeaderMenu = new PopupMenuBuilder(natTable)
								.withColumnStyleEditor("Customize blotter")
								.build();
		
		natTable.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				colHeaderMenu.dispose();
			}
			
		});
	}
	
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 3), 
				new PopupMenuAction(colHeaderMenu));
	}

}
