package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy;

import org.eclipse.jface.text.IRegion;

public interface ICellSearchStrategy {

	IRegion executeSearch(String firstValue, String secondValue);
	/** burke 修改find/replace界面框  修改  不需要fuzzySearch*/
	//void init(boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch, boolean fuzzySearch, int startOffset);
	void init(boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch, int startOffset);

	boolean isSearchForward();
}
