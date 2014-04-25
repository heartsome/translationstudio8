package net.sourceforge.nattable.reorder.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.nattable.coordinate.PositionUtil;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ColumnStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class ColumnReorderEvent extends ColumnStructuralChangeEvent {

	private Collection<Range> beforeFromColumnPositionRanges;

	private int beforeToColumnPosition;

	public ColumnReorderEvent(ILayer layer, int beforeFromColumnPosition, int beforeToColumnPosition) {
		this(layer, Arrays.asList(new Integer[] { Integer.valueOf(beforeFromColumnPosition) }), beforeToColumnPosition);
	}

	public ColumnReorderEvent(ILayer layer, List<Integer> beforeFromColumnPositions, int beforeToColumnPosition) {
		super(layer);
		this.beforeFromColumnPositionRanges = PositionUtil.getRanges(beforeFromColumnPositions);
		this.beforeToColumnPosition = beforeToColumnPosition;

		List<Integer> allColumnPositions = new ArrayList<Integer>(beforeFromColumnPositions);
		allColumnPositions.add(beforeToColumnPosition);
		setColumnPositionRanges(PositionUtil.getRanges(allColumnPositions));
	}

	public ColumnReorderEvent(ColumnReorderEvent event) {
		super(event);
		this.beforeFromColumnPositionRanges = event.beforeFromColumnPositionRanges;
		this.beforeToColumnPosition = event.beforeToColumnPosition;
	}

	public Collection<Range> getBeforeFromColumnPositionRanges() {
		return beforeFromColumnPositionRanges;
	}

	public int getBeforeToColumnPosition() {
		return beforeToColumnPosition;
	}

	public Collection<StructuralDiff> getColumnDiffs() {
		Collection<StructuralDiff> columnDiffs = new ArrayList<StructuralDiff>();

		Collection<Range> beforeFromColumnPositionRanges = getBeforeFromColumnPositionRanges();

		int afterAddColumnPosition = beforeToColumnPosition;
		for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
			if (beforeFromColumnPositionRange.start < beforeToColumnPosition) {
				afterAddColumnPosition -= Math.min(beforeFromColumnPositionRange.end, beforeToColumnPosition) - beforeFromColumnPositionRange.start;
			} else {
				break;
			}
		}
		int cumulativeAddSize = 0;
		for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
			cumulativeAddSize += beforeFromColumnPositionRange.size();
		}

		int offset = 0;
		for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
			int afterDeleteColumnPosition = beforeFromColumnPositionRange.start - offset;
			if (afterAddColumnPosition < afterDeleteColumnPosition) {
				afterDeleteColumnPosition += cumulativeAddSize;
			}
			columnDiffs.add(new StructuralDiff(DiffTypeEnum.DELETE, beforeFromColumnPositionRange, new Range(afterDeleteColumnPosition, afterDeleteColumnPosition)));
			offset += beforeFromColumnPositionRange.size();
		}
		Range beforeAddRange = new Range(beforeToColumnPosition, beforeToColumnPosition);
		offset = 0;
		for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
			int size = beforeFromColumnPositionRange.size();
			columnDiffs.add(new StructuralDiff(DiffTypeEnum.ADD, beforeAddRange, new Range(afterAddColumnPosition + offset, afterAddColumnPosition + offset + size)));
			offset += size;
		}

		return columnDiffs;
	}

	@Override
	public boolean convertToLocal(ILayer targetLayer) {
		beforeFromColumnPositionRanges = targetLayer.underlyingToLocalColumnPositions(getLayer(), beforeFromColumnPositionRanges);
		beforeToColumnPosition = targetLayer.underlyingToLocalColumnPosition(getLayer(), beforeToColumnPosition);

		if (beforeToColumnPosition >= 0) {
			return super.convertToLocal(targetLayer);
		} else {
			return false;
		}
	}

	public ColumnReorderEvent cloneEvent() {
		return new ColumnReorderEvent(this);
	}

}
