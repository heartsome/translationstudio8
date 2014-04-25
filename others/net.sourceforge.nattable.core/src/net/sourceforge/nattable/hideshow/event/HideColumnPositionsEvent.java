package net.sourceforge.nattable.hideshow.event;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.PositionUtil;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ColumnStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class HideColumnPositionsEvent extends ColumnStructuralChangeEvent {

	public HideColumnPositionsEvent(ILayer layer, Collection<Integer> columnPositions) {
		super(layer, PositionUtil.getRanges(columnPositions));
	}

	// Copy constructor
	protected HideColumnPositionsEvent(HideColumnPositionsEvent event) {
		super(event);
	}

	public HideColumnPositionsEvent cloneEvent() {
		return new HideColumnPositionsEvent(this);
	}

	public Collection<StructuralDiff> getColumnDiffs() {
		Collection<StructuralDiff> columnDiffs = new ArrayList<StructuralDiff>();

		for (Range range : getColumnPositionRanges()) {
			StructuralDiff diff = new StructuralDiff(DiffTypeEnum.DELETE, range, new Range(range.start, range.start));
			columnDiffs.add(diff);
		}

		return columnDiffs;
	}

	@Override
	public boolean convertToLocal(ILayer localLayer) {
		super.convertToLocal(localLayer);
		return true;
	}

}