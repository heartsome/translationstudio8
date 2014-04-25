package net.sourceforge.nattable.ui;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.LabelStack;

import org.eclipse.swt.events.MouseEvent;

public class NatEventData {
	
	private Object originalEventData;
	private final NatTable natTable;
	private final LabelStack regionLabels;
	int columnPosition;
	int rowPosition;

	public static NatEventData createInstanceFromEvent(MouseEvent event) {
		NatTable natTable = (NatTable) event.widget;
		
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);
        
        return new NatEventData(
				natTable,
				natTable.getRegionLabelsByXY(event.x, event.y),
				columnPosition,
				rowPosition,
				event.data
		);
	}
	
	public NatEventData(NatTable natTable, LabelStack regionLabels, int columnPosition, int rowPosition, Object originalEventData) {
		this.natTable = natTable;
		this.regionLabels = regionLabels;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
		this.originalEventData = originalEventData;
	}

	public NatTable getNatTable() {
		return natTable;
	}

	public LabelStack getRegionLabels() {
		return regionLabels;
	}

	public int getColumnPosition() {
		return columnPosition;
	}

	public int getRowPosition() {
		return rowPosition;
	}
	
	public Object getOriginalEventData() {
		return originalEventData;
	}

}