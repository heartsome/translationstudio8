package net.sourceforge.nattable.resize.event;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ColumnStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class ColumnResizeEvent extends ColumnStructuralChangeEvent {

	public ColumnResizeEvent(ILayer layer, int columnPosition) {
		super(layer, new Range(columnPosition, columnPosition + 1));
	}
	
	protected ColumnResizeEvent(ColumnResizeEvent event) {
		super(event);
	}
	
	public ColumnResizeEvent cloneEvent() {
		return new ColumnResizeEvent(this);
	}
	
	public Collection<StructuralDiff> getColumnDiffs() {
		Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();
		
		for (Range range : getColumnPositionRanges()) {
			rowDiffs.add(new StructuralDiff(DiffTypeEnum.CHANGE, range, range));
		}
		
		return rowDiffs;
	}
	
}
