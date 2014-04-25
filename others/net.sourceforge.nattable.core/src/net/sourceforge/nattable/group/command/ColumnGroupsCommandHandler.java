package net.sourceforge.nattable.group.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.group.ColumnGroupHeaderLayer;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupUtils;
import net.sourceforge.nattable.group.event.GroupColumnsEvent;
import net.sourceforge.nattable.group.event.UngroupColumnsEvent;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommand;
import net.sourceforge.nattable.selection.SelectionLayer;

public class ColumnGroupsCommandHandler extends AbstractLayerCommandHandler<IColumnGroupCommand>  {
	
	private final ColumnGroupModel model;
	private final SelectionLayer selectionLayer;
	private final ColumnGroupHeaderLayer contextLayer;
	private Map<Integer, Integer> columnIndexesToPositionsMap;

	public ColumnGroupsCommandHandler(ColumnGroupModel model, SelectionLayer selectionLayer, ColumnGroupHeaderLayer contextLayer) {
		this.model = model;
		this.selectionLayer = selectionLayer;
		this.contextLayer = contextLayer;
	}

	public boolean doCommand(IColumnGroupCommand command) {
		if (command instanceof CreateColumnGroupCommand) {
			if (columnIndexesToPositionsMap.size() > 0) {
				handleGroupColumnsCommand(((CreateColumnGroupCommand)command).getColumnGroupName());
				columnIndexesToPositionsMap.clear();
				return true;
			}
		} else if (command instanceof OpenCreateColumnGroupDialog) {
			OpenCreateColumnGroupDialog openDialogCommand = (OpenCreateColumnGroupDialog)command;
			loadSelectedColumnsIndexesWithPositions();
			if (selectionLayer.getFullySelectedColumnPositions().length > 0 && columnIndexesToPositionsMap.size() > 0) {
				openDialogCommand.openDialog(contextLayer);
			} else {				
				openDialogCommand.openErrorBox("Please select non-grouped columns prior to creating a group.");				
			}
			return true;
		} else if (command instanceof UngroupColumnCommand) {
			handleUngroupCommand();
			return true;
		}
		return false;
	}
	
	public Class<IColumnGroupCommand> getCommandClass() {
		return IColumnGroupCommand.class;
	}
	
	protected void loadSelectedColumnsIndexesWithPositions() {
		columnIndexesToPositionsMap = new LinkedHashMap<Integer, Integer>();
		int[] fullySelectedColumns = selectionLayer.getFullySelectedColumnPositions();
		
		if (fullySelectedColumns.length > 0) {
			for (int index = 0; index < fullySelectedColumns.length; index++) {
				final int columnPosition = fullySelectedColumns[index];
				int columnIndex = selectionLayer.getColumnIndexByPosition(columnPosition);
				if (model.isPartOfAGroup(columnIndex)){
					columnIndexesToPositionsMap.clear();
					break;
				}
				columnIndexesToPositionsMap.put(Integer.valueOf(columnIndex), Integer.valueOf(columnPosition));
			}
			
		}
	}

	public void handleGroupColumnsCommand(String columnGroupName) {
			
		try {
			List<Integer> selectedPositions = new ArrayList<Integer>();
			int[] fullySelectedColumns = new int[columnIndexesToPositionsMap.size()];
			int count = 0;
			for (Integer columnIndex : columnIndexesToPositionsMap.keySet()) {
				fullySelectedColumns[count++] = columnIndex.intValue();
				selectedPositions.add(columnIndexesToPositionsMap.get(columnIndex));
			}
			model.addColumnsIndexesToGroup(columnGroupName, fullySelectedColumns);
			selectionLayer.doCommand(new MultiColumnReorderCommand(selectionLayer, selectedPositions, selectedPositions.get(0).intValue()));
			selectionLayer.clear();
		} catch (Throwable t) {
		}
		contextLayer.fireLayerEvent(new GroupColumnsEvent(contextLayer));
	}

	public void handleUngroupCommand() {
		// Grab fully selected column positions
		int[] fullySelectedColumns = selectionLayer.getFullySelectedColumnPositions();
		Map<String, Integer> toColumnPositions = new HashMap<String, Integer>();
		if (fullySelectedColumns.length > 0) {
		
		// Pick the ones which belong to a group and remove them from the group
			for (int index = 0; index < fullySelectedColumns.length; index++) {
				final int columnPosition = fullySelectedColumns[index];
				int columnIndex = selectionLayer.getColumnIndexByPosition(columnPosition);
				if (model.isPartOfAGroup(columnIndex) && !model.isPartOfAnUnbreakableGroup(columnIndex)){
					handleRemovalFromGroup(toColumnPositions, columnIndex);
				}
			}
		// The groups which were affected should be reordered to the start position, this should group all columns together
			Collection<Integer> values = toColumnPositions.values();
			final Iterator<Integer> toColumnPositionsIterator = values.iterator();
			while(toColumnPositionsIterator.hasNext()) {
				Integer toColumnPosition = toColumnPositionsIterator.next();
				selectionLayer.doCommand(new ReorderColumnGroupCommand(selectionLayer, toColumnPosition.intValue(), toColumnPosition.intValue()));
			}
			selectionLayer.clear();
		} 
		
		contextLayer.fireLayerEvent(new UngroupColumnsEvent(contextLayer));
	}

	private void handleRemovalFromGroup(Map<String, Integer> toColumnPositions, int columnIndex) {
		final String columnGroupName = model.getColumnGroupNameForIndex(columnIndex);
		final List<Integer> columnIndexesInGroup = model.getColumnIndexesInGroup(columnIndex);
		final int columnGroupSize = columnIndexesInGroup.size();
		if (!toColumnPositions.containsKey(columnGroupName)) {
			for (int colGroupIndex : columnIndexesInGroup) {
				if (ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(colGroupIndex, contextLayer, selectionLayer, model)) {
					int toPosition = selectionLayer.getColumnPositionByIndex(colGroupIndex);
					if (colGroupIndex == columnIndex) {
						if (columnGroupSize == 1) {
							break;
						} else {
							toPosition++;
						}
					}
					toColumnPositions.put(columnGroupName, Integer.valueOf(toPosition));
					break;
				}
			}
		} else {
			if (columnGroupSize - 1 <= 0) {
				toColumnPositions.remove(columnGroupName);
			}
		}
		model.removeColumnFromGroup(columnIndex);
	}	
}
