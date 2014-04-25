package net.sourceforge.nattable.ui.menu;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;

public class HeaderMenuConfiguration extends AbstractUiBindingConfiguration {

	private final Menu colHeaderMenu;
	private final Menu rowHeaderMenu;
	public enum MenuOptions {HideColumn, ShowColumn};

	public HeaderMenuConfiguration(NatTable natTable) {
		colHeaderMenu = new PopupMenuBuilder(natTable)
								.withHideColumnMenuItem()
								.withShowAllColumnsMenuItem()
								.withCreateColumnGroupsMenuItem()
								.withUngroupColumnsMenuItem()
								.withColumnChooserMenuItem()
								.withAutoResizeSelectedColumnsMenuItem()
								.withColumnStyleEditor("Edit styles")
								.withColumnRenameDialog("Rename column")
								.withCategoriesBasesColumnChooser("Choose columns")
								.withClearAllFilters("Clear all filters")
								.build();

		rowHeaderMenu = new PopupMenuBuilder(natTable)
								.withAutoResizeSelectedRowsMenuItem()
								.build();

		natTable.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				colHeaderMenu.dispose();
				rowHeaderMenu.dispose();
			}

		});
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 3),
				new PopupMenuAction(colHeaderMenu));

		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, 3),
				new PopupMenuAction(rowHeaderMenu));
	}

}
