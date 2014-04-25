package net.sourceforge.nattable.hideshow.event;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.PositionUtil;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.event.ColumnStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class ShowColumnPositionsEvent extends ColumnStructuralChangeEvent {

	public ShowColumnPositionsEvent(IUniqueIndexLayer layer, Collection<Integer> columnPositions) {
		super(layer, PositionUtil.getRanges(columnPositions));
	}
	
	// Copy constructor
	public ShowColumnPositionsEvent(ShowColumnPositionsEvent event) {
		super(event);
	}
	
	public Collection<StructuralDiff> getColumnDiffs() {
		Collection<StructuralDiff> columnDiffs = new ArrayList<StructuralDiff>();

		int offset = 0;
		for (Range range : getColumnPositionRanges()) {
			columnDiffs.add(new StructuralDiff(DiffTypeEnum.ADD, new Range(range.start - offset, range.start - offset), range));
			offset += range.size();
		}
		
		return columnDiffs;
	}
	
	public ShowColumnPositionsEvent cloneEvent() {
		return new ShowColumnPositionsEvent(this);
	}
	
	@Override
	public boolean convertToLocal(ILayer localLayer) {
		super.convertToLocal(localLayer);
		return true;
	}
	
}