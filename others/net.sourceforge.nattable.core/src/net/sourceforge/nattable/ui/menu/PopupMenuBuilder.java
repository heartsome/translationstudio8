package net.sourceforge.nattable.ui.menu;

import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.widgets.Menu;

public class PopupMenuBuilder {

	NatTable natTable;
	Menu popupMenu;

	public PopupMenuBuilder(NatTable parent) {
		this.natTable = parent;
		popupMenu = new Menu(parent.getShell());
	}

	/**
	 * Use this to add your own item to the popup menu.
	 */
	public PopupMenuBuilder withMenuItemProvider(IMenuItemProvider meuItemProvider){
		meuItemProvider.addMenuItem(natTable, popupMenu);
		return this;
	}

	public PopupMenuBuilder withHideColumnMenuItem() {
		return withMenuItemProvider(MenuItemProviders.hideColumnMenuItemProvider());
	}

	public PopupMenuBuilder withShowAllColumnsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.showAllColumnMenuItemProvider());
	}

	public PopupMenuBuilder withAutoResizeSelectedColumnsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.autoResizeColumnMenuItemProvider());
	}

	public PopupMenuBuilder withAutoResizeSelectedRowsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.autoResizeRowMenuItemProvider());
	}

	public PopupMenuBuilder withColumnChooserMenuItem() {
		return withMenuItemProvider(MenuItemProviders.columnChooserMenuItemProvider());
	}

	public PopupMenuBuilder withColumnChooserMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.columnChooserMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withColumnStyleEditor(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.columnStyleEditorMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withColumnRenameDialog(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.renameColumnMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withCreateColumnGroupsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.createColumnGroupMenuItemProvider());
	}

	public PopupMenuBuilder withUngroupColumnsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.ungroupColumnsMenuItemProvider());
	}

	public PopupMenuBuilder withInspectLabelsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.inspectLabelsMenuItemProvider());
	}

	public PopupMenuBuilder withCategoriesBasesColumnChooser(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.categoriesBasedColumnChooserMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withClearAllFilters(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.clearAllFiltersMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withToggleFilterRow(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.clearToggleFilterRowMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withSeparator(){
		return withMenuItemProvider(MenuItemProviders.separatorMenuItemProvider());	}

	public Menu build(){
		return popupMenu;
	}

}

