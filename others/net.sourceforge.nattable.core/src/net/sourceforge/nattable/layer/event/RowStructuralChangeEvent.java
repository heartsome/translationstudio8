package net.sourceforge.nattable.layer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.Rectangle;

/**
 * @see ColumnStructuralChangeEvent
 */
public abstract class RowStructuralChangeEvent extends RowVisualChangeEvent implements IStructuralChangeEvent {

	public RowStructuralChangeEvent(ILayer layer, Range...rowPositionRanges) {
		this(layer, Arrays.asList(rowPositionRanges));
	}
	
	public RowStructuralChangeEvent(ILayer layer, Collection<Range> rowPositionRanges) {
		super(layer, rowPositionRanges);
	}
	
	protected RowStructuralChangeEvent(RowStructuralChangeEvent event) {
		super(event);
	}
	
	@Override
	public Collection<Rectangle> getChangedPositionRectangles() {
		Collection<Rectangle> changedPositionRectangles = new ArrayList<Rectangle>();
		
		int columnCount = getLayer().getColumnCount();
		int rowCount = getLayer().getRowCount();
		for (Range range : getRowPositionRanges()) {
			changedPositionRectangles.add(new Rectangle(0, range.start, columnCount, rowCount - range.start));
		}
		
		return changedPositionRectangles;
	}
	
	public boolean isHorizontalStructureChanged() {
		return false;
	}
	
	public Collection<StructuralDiff> getColumnDiffs() {
		return null;
	}
	
	public boolean isVerticalStructureChanged() {
		return true;
	}
	
}
