package net.sourceforge.nattable.layer.event;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class RowInsertEvent extends RowStructuralChangeEvent {
	
	public RowInsertEvent(ILayer layer, int rowPosition) {
		this(layer, new Range(rowPosition, rowPosition + 1));
	}
	
	public RowInsertEvent(ILayer layer, Range rowPositionRange) {
		super(layer, rowPositionRange);
	}
	
	public RowInsertEvent(RowInsertEvent event) {
		super(event);
	}
	
	public RowInsertEvent cloneEvent() {
		return new RowInsertEvent(this);
	}
	
	public Collection<StructuralDiff> getRowDiffs() {
		Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();
		
		for (Range range : getRowPositionRanges()) {
			new StructuralDiff(DiffTypeEnum.ADD, new Range(range.start, range.start), range);
		}
		
		return rowDiffs;
	}

}
