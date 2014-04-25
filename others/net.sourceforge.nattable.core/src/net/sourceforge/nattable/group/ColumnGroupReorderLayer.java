package net.sourceforge.nattable.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.nattable.group.command.GroupColumnReorderCommandHandler;
import net.sourceforge.nattable.group.command.GroupMultiColumnReorderCommandHandler;
import net.sourceforge.nattable.group.command.ReorderColumnGroupCommandHandler;
import net.sourceforge.nattable.group.command.ReorderColumnsAndGroupsCommandHandler;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommand;

/**
 * Adds functionality allowing the reordering of the the Column groups. 
 */
public class ColumnGroupReorderLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	private IUniqueIndexLayer underlyingLayer;
	
	private final ColumnGroupModel model;
	
	public ColumnGroupReorderLayer(IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
		setUnderlyingLayer(underlyingLayer);
		this.underlyingLayer = underlyingLayer;
		this.model = model;
		
		registerCommandHandler(new ReorderColumnGroupCommandHandler(this));
		registerCommandHandler(new ReorderColumnsAndGroupsCommandHandler(this));
		registerCommandHandler(new GroupColumnReorderCommandHandler(this));
		registerCommandHandler(new GroupMultiColumnReorderCommandHandler(this));
	}
	
	public boolean reorderColumnGroup(int fromColumnPosition, int toColumnPosition) {
		int fromColumnIndex = underlyingLayer.getColumnIndexByPosition(fromColumnPosition);
		
		List<Integer> fromColumnPositions = getColumnGroupPositions(fromColumnIndex);
		return underlyingLayer.doCommand(new MultiColumnReorderCommand(this, fromColumnPositions, toColumnPosition));
	}
	
	public ColumnGroupModel getModel() {
		return model;
	}
	
	@Override
	public ILayer getUnderlyingLayer() {
		return super.getUnderlyingLayer();
	}
	
	// Horizontal features
	
	// Columns
	
	public int getColumnPositionByIndex(int columnIndex) {
		return underlyingLayer.getColumnPositionByIndex(columnIndex);
	}
	
	// Vertical features
	
	// Rows
	
	public int getRowPositionByIndex(int rowIndex) {
		return underlyingLayer.getRowPositionByIndex(rowIndex);
	}
	
	// Column Groups
	
	/**
	 * @return the column positions for all the columns in this group
	 */
	public List<Integer> getColumnGroupPositions(int fromColumnIndex) {
		List<Integer> fromColumnIndexes = model.getColumnIndexesInGroup(fromColumnIndex);
		List<Integer> fromColumnPositions = new ArrayList<Integer>();
		
		for (Integer columnIndex : fromColumnIndexes) {
			fromColumnPositions.add(
					Integer.valueOf(underlyingLayer.getColumnPositionByIndex(columnIndex.intValue())));
		}
		//These positions are actually consecutive but the Column Group does not know about the order 
		Collections.sort(fromColumnPositions);
		return fromColumnPositions;
	}
	
}
