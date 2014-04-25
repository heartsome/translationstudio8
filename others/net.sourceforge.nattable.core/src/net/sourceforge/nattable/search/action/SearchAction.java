package net.sourceforge.nattable.search.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.search.CellValueAsStringComparator;
import net.sourceforge.nattable.search.gui.SearchDialog;
import net.sourceforge.nattable.search.strategy.GridSearchStrategy;
import net.sourceforge.nattable.ui.action.IKeyAction;

import org.eclipse.swt.events.KeyEvent;

public class SearchAction implements IKeyAction {

	private SearchDialog searchDialog;
	
	public void run(NatTable natTable, KeyEvent event) {
		if (searchDialog == null) {
			searchDialog =  SearchDialog.createDialog(natTable.getShell(), natTable);
		}
		GridSearchStrategy searchStrategy = new GridSearchStrategy(natTable.getConfigRegistry(), true);
		searchDialog.setSearchStrategy(searchStrategy, new CellValueAsStringComparator<String>());
		searchDialog.open();
	}
}
