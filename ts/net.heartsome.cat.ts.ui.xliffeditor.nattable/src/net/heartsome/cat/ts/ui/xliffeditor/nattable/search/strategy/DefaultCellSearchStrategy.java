package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy;

import net.heartsome.cat.common.ui.utils.InnerTagUtil;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

/**
 * The comparator will base its comparison on the display value of a cell. The display value is assumed to be a string.
 */
public class DefaultCellSearchStrategy implements ICellSearchStrategy {

	public DefaultCellSearchStrategy() {
	}

	private boolean searchForward = true;

	private int startOffset = 0;

	private boolean caseSensitive;

	private boolean wholeWord;

	private boolean regExSearch;
	/** burke 修改find/replace界面修改  不需要fuzzySearch*/
	//private boolean fuzzySearch;

	public IRegion executeSearch(String findValue, String dataValue) {
		// 如果查找的字符和单元格中的数据长度为 0，，则直接返回没有找到。
		if (findValue == null || findValue.length() == 0 || dataValue == null || dataValue.length() == 0) {
			return null;
		}
		Document doc = new Document(dataValue);
		FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(doc);
		IRegion region;
		try {
			if (startOffset == -1) {
				if (searchForward) {
					startOffset = 0;
				} else {
					startOffset = adapter.length() - 1;
				}
			}
			region = adapter.find(startOffset, findValue, searchForward, caseSensitive, wholeWord, regExSearch);
			while (region != null) {
				boolean inTag = false;
				for (int i = region.getOffset(); i < region.getOffset() + region.getLength(); i++) {
					Position tagRange = InnerTagUtil.getStyledTagRange(dataValue, i);
					if (tagRange != null) {
						if (searchForward) {
							if (tagRange.getOffset() + tagRange.getLength() == dataValue.length()) {
								return null; // 如果句首是一个标记，则直接返回 null，会继续查找上一个文本段。
							}
							startOffset = tagRange.getOffset() + tagRange.getLength();
						} else {
							if (tagRange.offset == 0) {
								return null; // 如果句首是一个标记，则直接返回 null，会继续查找上一个文本段。
							}
							startOffset = tagRange.getOffset() - 1;
						}
						inTag = true;
						break;
					}
				}
				if (inTag) {
					region = adapter.find(startOffset, findValue, searchForward, caseSensitive, wholeWord, regExSearch);
				} else {
					break;
				}
			}
			return region;
		} catch (BadLocationException e) {
			return null;
		}
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public boolean isWholeWord() {
		return wholeWord;
	}

	public boolean isRegExSearch() {
		return regExSearch;
	}
	/** burke 修改find/replace界面修改  不需要fuzzySearch*/
	/*public boolean isFuzzySearch() {
		return fuzzySearch;
	}*/

	public boolean isSearchForward() {
		return searchForward;
	}

	public int getStartOffset() {
		return startOffset;
	}
	
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
	/** burke 修改find/replace界面修改  不需要fuzzySearch*/
	/*public void init(boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch,
			boolean fuzzySearch, int startOffset) {*/
	public void init(boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch,
			int startOffset) {
		this.searchForward = searchForward;
		this.caseSensitive = caseSensitive;
		this.wholeWord = wholeWord;
		this.regExSearch = regExSearch;
		/** burke 修改find/replace界面修改  不需要fuzzySearch*/
		//this.fuzzySearch = fuzzySearch;
		this.startOffset = startOffset;
	}
}
