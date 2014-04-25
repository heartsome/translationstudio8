package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.command;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.ICellSearchStrategy;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.ISearchStrategy;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.ILayerListener;

public class FindReplaceCommand implements ILayerCommand {

	private ILayer context;
	private final ISearchStrategy searchStrategy;
	private final String searchText;
	private final ICellSearchStrategy cellSearchStrategy;
	private ILayerListener searchEventListener;

	public FindReplaceCommand(ILayer layer, ISearchStrategy searchStrategy, ICellSearchStrategy cellSearchStrategy) {
		this(null, layer, searchStrategy, cellSearchStrategy);
	}

	public FindReplaceCommand(String searchText, ILayer layer, ISearchStrategy searchStrategy,
			ICellSearchStrategy cellSearchStrategy) {
		this.context = layer;
		this.searchStrategy = searchStrategy;
		this.searchText = searchText;
		this.cellSearchStrategy = cellSearchStrategy;
	}

	protected FindReplaceCommand(FindReplaceCommand command) {
		this.context = command.context;
		this.searchStrategy = command.searchStrategy;
		this.searchText = command.searchText;
		this.cellSearchStrategy = command.cellSearchStrategy;
		this.searchEventListener = command.searchEventListener;
	}

	public ILayer getContext() {
		return context;
	}

	public ISearchStrategy getSearchStrategy() {
		return searchStrategy;
	}

	public String getSearchText() {
		return searchText;
	}

	public ILayerListener getSearchEventListener() {
		return searchEventListener;
	}

	public void setSearchEventListener(ILayerListener listener) {
		this.searchEventListener = listener;
	}

	public ICellSearchStrategy getCellSearchStrategy() {
		return cellSearchStrategy;
	}

	public boolean convertToTargetLayer(ILayer targetLayer) {
		context = targetLayer;
		return true;
	}

	public FindReplaceCommand cloneCommand() {
		return new FindReplaceCommand(this);
	}
}
