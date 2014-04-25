package net.sourceforge.nattable.resize.event;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.RowStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class RowResizeEvent extends RowStructuralChangeEvent {

	public RowResizeEvent(ILayer layer, int rowPosition) {
		super(layer, new Range(rowPosition, rowPosition + 1));
	}
	
	public RowResizeEvent(ILayer layer, Range rowPositionRange) {
		super(layer, rowPositionRange);
	}
	
	protected RowResizeEvent(RowResizeEvent event) {
		super(event);
	}
	
	public RowResizeEvent cloneEvent() {
		return new RowResizeEvent(this);
	}
	
	public Collection<StructuralDiff> getRowDiffs() {
		Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();
		
		for (Range range : getRowPositionRanges()) {
			new StructuralDiff(DiffTypeEnum.CHANGE, range, range);
		}
		
		return rowDiffs;
	}
	
}
