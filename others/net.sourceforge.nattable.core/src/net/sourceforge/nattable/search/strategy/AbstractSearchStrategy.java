package net.sourceforge.nattable.search.strategy;

import java.util.Comparator;

import net.sourceforge.nattable.layer.ILayer;

public abstract class AbstractSearchStrategy implements ISearchStrategy {
	private ILayer contextLayer;
	protected String searchDirection;
	protected boolean wrapSearch = false;
	protected boolean caseSensitive = false;
	protected Comparator<?> comparator;
	
	public void setContextLayer(ILayer contextLayer) {
		this.contextLayer = contextLayer;
	}
	
	public ILayer getContextLayer() {
		return contextLayer;
	}
	
	public void setSearchDirection(String searchDirection) {
		this.searchDirection = searchDirection;
	}
	
	public String getSearchDirection() {
		return searchDirection;
	}
	
	public void setWrapSearch(boolean wrapSearch) {
		this.wrapSearch = wrapSearch;
	}
	
	public boolean isWrapSearch() {
		return wrapSearch;
	}
	
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
	
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	public Comparator<?> getComparator() {
		return comparator;
	}
	
	public void setComparator(Comparator<?> comparator) {
		this.comparator = comparator;
	}
}
