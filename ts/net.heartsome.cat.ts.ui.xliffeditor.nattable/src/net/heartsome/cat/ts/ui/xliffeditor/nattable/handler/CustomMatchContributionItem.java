package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class CustomMatchContributionItem extends ContributionItem {

	public CustomMatchContributionItem() {
	}

	public CustomMatchContributionItem(String id) {
		super(id);
	}

	@Override
	public void fill(Menu menu, int index) {
		LinkedHashMap<String, String> map = XLFHandler.getCustomMatchFilterMap();
		if (map == null || map.size() == 0) {
			for (Item item : menu.getItems()) {
				if (!item.isDisposed()) {
					item.dispose();
				}
			}
		} else {
			for (Entry<String, String> entry : map.entrySet()) {
				final String xpath = entry.getValue();
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(entry.getKey());
				item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
						if (xliffEditor == null) {
							return;
						}
						xliffEditor.getXLFHandler().deleteAltTrans(xpath);
						ArrayList<Integer> rowList = new ArrayList<Integer>();
						int[] rows = xliffEditor.getSelectedRows();
						for (int i : rows) {
							rowList.add(i);
						}
						StructuredSelection selection = new StructuredSelection(rowList);
						xliffEditor.getSite().getSelectionProvider().setSelection(
								selection);
					}
				});
			}
		}
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
