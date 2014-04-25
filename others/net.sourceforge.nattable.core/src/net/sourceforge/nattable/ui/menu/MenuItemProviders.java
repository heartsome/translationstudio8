package net.sourceforge.nattable.ui.menu;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.columnCategories.ChooseColumnsFromCategoriesCommand;
import net.sourceforge.nattable.columnChooser.command.DisplayColumnChooserCommand;
import net.sourceforge.nattable.columnRename.DisplayColumnRenameDialogCommand;
import net.sourceforge.nattable.filterrow.command.ClearAllFiltersCommand;
import net.sourceforge.nattable.filterrow.command.ToggleFilterRowCommand;
import net.sourceforge.nattable.group.command.OpenCreateColumnGroupDialog;
import net.sourceforge.nattable.group.command.UngroupColumnCommand;
import net.sourceforge.nattable.hideshow.command.ColumnHideCommand;
import net.sourceforge.nattable.hideshow.command.ShowAllColumnsCommand;
import net.sourceforge.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import net.sourceforge.nattable.resize.command.InitializeAutoResizeRowsCommand;
import net.sourceforge.nattable.style.editor.command.DisplayColumnStyleEditorCommand;
import net.sourceforge.nattable.ui.NatEventData;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Widget;

public class MenuItemProviders {

	/**
	 * Walk up the MenuItems (in case they are nested) and find the parent {@link Menu}
	 *
	 * @param selectionEvent
	 *            on the {@link MenuItem}
	 * @return data associated with the parent {@link Menu}
	 */
	public static NatEventData getNatEventData(SelectionEvent selectionEvent) {
		Widget widget = selectionEvent.widget;
		if (widget == null || !(widget instanceof MenuItem)) {
			return null;
		}

		MenuItem menuItem = (MenuItem) widget;
		Menu parentMenu = menuItem.getParent();
		Object data = null;
		while (parentMenu != null) {
			if (parentMenu.getData() == null) {
				parentMenu = parentMenu.getParentMenu();
			} else {
				data = parentMenu.getData();
				break;
			}
		}

		return data != null ? (NatEventData) data : null;
	}

	public static IMenuItemProvider hideColumnMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText("Hide column");
				menuItem.setImage(GUIHelper.getImage("hide_column"));
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new ColumnHideCommand(natTable, columnPosition));
					}
				});
			}
		};
	}

	public static IMenuItemProvider showAllColumnMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, Menu popupMenu) {
				MenuItem showAllColumns = new MenuItem(popupMenu, SWT.PUSH);
				showAllColumns.setText("Show all columns");
				showAllColumns.setImage(GUIHelper.getImage("show_column"));
				showAllColumns.setEnabled(true);

				showAllColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ShowAllColumnsCommand());
					}
				});
			}
		};
	}

	public static IMenuItemProvider autoResizeColumnMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem autoResizeColumns = new MenuItem(popupMenu, SWT.PUSH);
				autoResizeColumns.setText("Auto resize column");
				autoResizeColumns.setImage(GUIHelper.getImage("auto_resize"));
				autoResizeColumns.setEnabled(true);

				autoResizeColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new InitializeAutoResizeColumnsCommand(natTable, columnPosition, natTable.getConfigRegistry(), new GC(natTable)));
					}
				});
			}
		};
	}

	public static IMenuItemProvider autoResizeRowMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem autoResizeRows = new MenuItem(popupMenu, SWT.PUSH);
				autoResizeRows.setText("Auto resize row");
				autoResizeRows.setEnabled(true);

				autoResizeRows.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int rowPosition = getNatEventData(event).getRowPosition();
						natTable.doCommand(new InitializeAutoResizeRowsCommand(natTable, rowPosition, natTable.getConfigRegistry(), new GC(natTable)));
					}
				});
			}
		};
	}

	public static IMenuItemProvider autoResizeAllSelectedColumnMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem autoResizeColumns = new MenuItem(popupMenu, SWT.PUSH);
				autoResizeColumns.setText("Auto resize all selected columns");
				autoResizeColumns.setEnabled(true);

				autoResizeColumns.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new InitializeAutoResizeColumnsCommand(natTable, columnPosition, natTable.getConfigRegistry(), new GC(natTable)));
					}
				});
			}

		};
	}

	public static IMenuItemProvider columnChooserMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnChooser = new MenuItem(popupMenu, SWT.PUSH);
				columnChooser.setText(menuLabel);
				columnChooser.setImage(GUIHelper.getImage("column_chooser"));
				columnChooser.setEnabled(true);

				columnChooser.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new DisplayColumnChooserCommand(natTable));
					}
				});
			}
		};
	}

	public static IMenuItemProvider columnChooserMenuItemProvider() {
		return columnChooserMenuItemProvider("Choose columns");
	}

	public static IMenuItemProvider columnStyleEditorMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
				columnStyleEditor.setText(menuLabel);
				columnStyleEditor.setImage(GUIHelper.getImage("preferences"));
				columnStyleEditor.setEnabled(true);

				columnStyleEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						int rowPosition = getNatEventData(event).getRowPosition();
						int columnPosition = getNatEventData(event).getColumnPosition();
						natTable.doCommand(new DisplayColumnStyleEditorCommand(natTable, natTable.getConfigRegistry(), columnPosition, rowPosition));
					}
				});
			}

		};
	}

	public static IMenuItemProvider renameColumnMenuItemProvider(final String label) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(label);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						natTable.doCommand(new DisplayColumnRenameDialogCommand(natTable, getNatEventData(event).getColumnPosition()));
					}
				});
			}
		};
	}

	public static IMenuItemProvider createColumnGroupMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
				columnStyleEditor.setText("Create column group");
				columnStyleEditor.setEnabled(true);

				columnStyleEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new OpenCreateColumnGroupDialog(natTable.getShell()));
					}
				});
			}
		};
	}

	public static IMenuItemProvider ungroupColumnsMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
				columnStyleEditor.setText("Ungroup columns");
				columnStyleEditor.setEnabled(true);

				columnStyleEditor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new UngroupColumnCommand());
					}
				});
			}
		};
	}

	public static IMenuItemProvider inspectLabelsMenuItemProvider() {
		return new IMenuItemProvider() {

			public void addMenuItem(NatTable natTable, Menu popupMenu) {
				MenuItem inspectLabelsMenuItem = new MenuItem(popupMenu, SWT.PUSH);
				inspectLabelsMenuItem.setText("Debug info");
				inspectLabelsMenuItem.setEnabled(true);

				inspectLabelsMenuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						NatEventData natEventData = getNatEventData(e);
						NatTable natTable = natEventData.getNatTable();
						int columnPosition = natEventData.getColumnPosition();
						int rowPosition = natEventData.getRowPosition();

						String msg = "Display mode: " + natTable.getDisplayModeByPosition(columnPosition, rowPosition) + "\nConfig labels: "
								+ natTable.getConfigLabelsByPosition(columnPosition, rowPosition) + "\nData value: "
								+ natTable.getDataValueByPosition(columnPosition, rowPosition) + "\n\nColumn position: " + columnPosition + "\nColumn index: "
								+ natTable.getColumnIndexByPosition(columnPosition) + "\n\nRow position: " + rowPosition + "\nRow index: "
								+ natTable.getRowIndexByPosition(rowPosition);

						MessageBox messageBox = new MessageBox(natTable.getShell(), SWT.ICON_INFORMATION | SWT.OK);
						messageBox.setText("Debug Information");
						messageBox.setMessage(msg);
						messageBox.open();
					}
				});
			}
		};
	}

	public static IMenuItemProvider categoriesBasedColumnChooserMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem columnChooser = new MenuItem(popupMenu, SWT.PUSH);
				columnChooser.setText(menuLabel);
				columnChooser.setImage(GUIHelper.getImage("column_categories_chooser"));
				columnChooser.setEnabled(true);

				columnChooser.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ChooseColumnsFromCategoriesCommand(natTable));
					}
				});
			}
		};
	}

	public static IMenuItemProvider clearAllFiltersMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setImage(GUIHelper.getImage("remove_filter"));
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ClearAllFiltersCommand());
					}
				});
			}
		};
	}

	public static IMenuItemProvider clearToggleFilterRowMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setImage(GUIHelper.getImage("toggle_filter"));
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						natTable.doCommand(new ToggleFilterRowCommand());
					}
				});
			}
		};
	}

	public static IMenuItemProvider separatorMenuItemProvider() {
		return new IMenuItemProvider() {
			public void addMenuItem(NatTable natTable, Menu popupMenu) {
				 new MenuItem(popupMenu, SWT.SEPARATOR);
			}
		};
	}

}