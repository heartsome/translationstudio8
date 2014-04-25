package net.sourceforge.nattable.group;

import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.nattable.group.command.ColumnGroupExpandCollapseCommandHandler;
import net.sourceforge.nattable.hideshow.AbstractColumnHideShowLayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;

/**
 * Tracks the Expand/Collapse of a Column Group header
 *    NOTE: Only relevant when Column Grouping is enabled.
 */
public class ColumnGroupExpandCollapseLayer extends AbstractColumnHideShowLayer implements IColumnGroupModelListener {

	private final ColumnGroupModel model;

	public ColumnGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
		super(underlyingLayer);
		this.model = model;

		model.registerColumnGroupModelListner(this);

		registerCommandHandler(new ColumnGroupExpandCollapseCommandHandler(this));
	}

	public ColumnGroupModel getModel() {
		return model;
	}

	// Expand/collapse

	@Override
	public boolean isColumnIndexHidden(int columnIndex) {
		IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
		return ColumnGroupUtils.isColumnIndexHiddenInUnderLyingLayer(columnIndex, this, underlyingLayer)
			|| (model.isCollapsed(columnIndex) && !ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(columnIndex, this, underlyingLayer, model));
	}

	@Override
	public Collection<Integer> getHiddenColumnIndexes() {
		Collection<Integer> hiddenColumnIndexes = new HashSet<Integer>();

		IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
		int underlyingColumnCount = underlyingLayer.getColumnCount();
		for (int i = 0; i < underlyingColumnCount; i++) {
			int colIndex = underlyingLayer.getColumnIndexByPosition(i);

			if (model.isCollapsed(colIndex)) {
				if (!ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(colIndex, this, underlyingLayer, model)) {
					hiddenColumnIndexes.add(Integer.valueOf(colIndex));
				}
			}
		}

		return hiddenColumnIndexes;
	}

	// IColumnGroupModelListener

	public void columnGroupModelChanged() {
		invalidateCache();
	}

}