package net.sourceforge.nattable.layer.event;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class RowDeleteEvent extends RowStructuralChangeEvent {
	
	public RowDeleteEvent(ILayer layer, int rowPosition) {
		this(layer, new Range(rowPosition, rowPosition + 1));
	}
	
	public RowDeleteEvent(ILayer layer, Range rowPositionRange) {
		super(layer, rowPositionRange);
	}
	
	protected RowDeleteEvent(RowDeleteEvent event) {
		super(event);
	}
	
	public RowDeleteEvent cloneEvent() {
		return new RowDeleteEvent(this);
	}
	
	public Collection<StructuralDiff> getRowDiffs() {
		Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();
		
		for (Range range : getRowPositionRanges()) {
			new StructuralDiff(DiffTypeEnum.DELETE, range, new Range(range.start, range.start));
		}
		
		return rowDiffs;
	}
	
}