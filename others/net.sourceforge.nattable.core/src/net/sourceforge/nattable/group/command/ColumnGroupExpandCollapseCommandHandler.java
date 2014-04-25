package net.sourceforge.nattable.group.command;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.group.ColumnGroupExpandCollapseLayer;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.ColumnGroupModel.ColumnGroup;
import net.sourceforge.nattable.hideshow.event.HideColumnPositionsEvent;
import net.sourceforge.nattable.hideshow.event.ShowColumnPositionsEvent;
import net.sourceforge.nattable.layer.event.ILayerEvent;

public class ColumnGroupExpandCollapseCommandHandler extends AbstractLayerCommandHandler<ColumnGroupExpandCollapseCommand> {

	private final ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;

	public ColumnGroupExpandCollapseCommandHandler(ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer) {
		this.columnGroupExpandCollapseLayer = columnGroupExpandCollapseLayer;
	}
	
	public Class<ColumnGroupExpandCollapseCommand> getCommandClass() {
		return ColumnGroupExpandCollapseCommand.class;
	}

	@Override
	protected boolean doCommand(ColumnGroupExpandCollapseCommand command) {
		int columnIndex = columnGroupExpandCollapseLayer.getColumnIndexByPosition(command.getColumnPosition());
		ColumnGroupModel model = columnGroupExpandCollapseLayer.getModel();
		boolean wasCollapsed = model.isCollapsed(columnIndex);
		ColumnGroup columnGroup = model.toggleColumnGroupExpandCollapse(columnIndex);
		
		List<Integer> columnPositions = new ArrayList<Integer>(columnGroup.getMembers());
		columnPositions.remove(0);
		
		ILayerEvent event;
		if (wasCollapsed) {
			event = new ShowColumnPositionsEvent(columnGroupExpandCollapseLayer, columnPositions);
		} else {
			event = new HideColumnPositionsEvent(columnGroupExpandCollapseLayer, columnPositions);
		}
		
		columnGroupExpandCollapseLayer.fireLayerEvent(event);
		
		return true;
	}

}
