package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.CellRegion;
import net.sourceforge.nattable.layer.ILayer;

public interface ISearchStrategy {

	CellRegion executeSearch(Object valueToMatch);

	void setContextLayer(ILayer contextLayer);

	ILayer getContextLayer();

	ICellSearchStrategy getCellSearchStrategy();

	void setCellSearchStrategy(ICellSearchStrategy cellSearchStrategy);
}
