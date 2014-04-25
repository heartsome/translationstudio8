package net.heartsome.cat.ts.ui.xliffeditor.nattable.menu;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.ui.NatEventData;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.ui.menu.PopupMenuAction;
import net.sourceforge.nattable.ui.menu.PopupMenuBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

/**
 * 基于 NatTable 的 XLIFF Editor 表头、列表的菜单设置
 * @author cheney
 * @since JDK1.6
 */
public class HeaderMenuConfiguration extends AbstractUiBindingConfiguration {

	private final Menu colHeaderMenu;



	public HeaderMenuConfiguration(NatTable natTable) {
		// colHeaderMenu = new PopupMenuBuilder(natTable)
		// .withHideColumnMenuItem()
		// .withShowAllColumnsMenuItem()
		// .withCreateColumnGroupsMenuItem()
		// .withUngroupColumnsMenuItem()
		// .withColumnChooserMenuItem()
		// .withAutoResizeSelectedColumnsMenuItem()
		// .withColumnStyleEditor("Edit styles")
		// .withColumnRenameDialog("Rename column")
		// .withCategoriesBasesColumnChooser("Choose columns")
		// .withClearAllFilters("Clear all filters")
		// .build();

		// 需要隐藏和显示特定列的功能
		colHeaderMenu = new PopupMenuBuilder(natTable)
		/*
		 * 第一项为“隐藏该列”的菜单项，如果以后菜单顺序做了修改，MenuListener中取出此菜单项的索引也要相应改变
		 */
		.withHideColumnMenuItem().withShowAllColumnsMenuItem().build();

		colHeaderMenu.addMenuListener(new MenuListener() {

			public void menuShown(MenuEvent event) {
				if (colHeaderMenu.getItemCount() > 0) {
					NatEventData data = (NatEventData) colHeaderMenu.getData();
					int columnPosition = data.getColumnPosition();
					int columnIndex = data.getNatTable().getColumnIndexByPosition(columnPosition);
					MenuItem item = colHeaderMenu.getItem(0 /* “隐藏该列”的菜单项的索引 */);

					XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					if (xliffEditor == null) {
						return;
					}
					if (xliffEditor.isHorizontalLayout()) {
						if (columnIndex == 1 || columnIndex == 3) {
							item.setEnabled(false);
						} else {
							item.setEnabled(true);
						}
					} else {
						if (columnIndex == VerticalNatTableConfig.SOURCE_COL_INDEX) {
							item.setEnabled(false);
						} else {
							item.setEnabled(true);
						}
					}
				}
			}

			public void menuHidden(MenuEvent e) {
			}
		});

		// rowHeaderMenu = new PopupMenuBuilder(natTable).withAutoResizeSelectedRowsMenuItem().build();
		// cornerHeaderMenu = new PopupMenuBuilder(natTable).withMenuItemProvider(new
		// AutoResizeALLRowMenuItemProvider())
		// .build();

		natTable.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				colHeaderMenu.dispose();
				// rowHeaderMenu.dispose();
			}

		});
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 3),
				new PopupMenuAction(colHeaderMenu));

		// uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, 3),
		// new PopupMenuAction(rowHeaderMenu));
		//
		// uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.CORNER, 3),
		// new PopupMenuAction(cornerHeaderMenu));
	}

	/**
	 * 重新计算所有行的菜单提供类
	 * @author cheney
	 * @since JDK1.6
	 */
	// private static final class AutoResizeALLRowMenuItemProvider implements IMenuItemProvider {
	//
	// @Override
	// public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
	// MenuItem autoResizeRows = new MenuItem(popupMenu, SWT.PUSH);
	// autoResizeRows.setText("Auto resize all rows");
	// autoResizeRows.setEnabled(true);
	//
	// autoResizeRows.addSelectionListener(new SelectionAdapter() {
	// @Override
	// public void widgetSelected(SelectionEvent event) {
	// int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
	// natTable.doCommand(new InitializeAutoResizeAllRowsCommand(natTable, rowPosition, natTable
	// .getConfigRegistry(), new GC(natTable)));
	// }
	// });
	// }
	// }

}
