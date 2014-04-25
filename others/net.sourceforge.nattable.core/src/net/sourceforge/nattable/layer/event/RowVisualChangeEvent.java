package net.sourceforge.nattable.layer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.Rectangle;

public abstract class RowVisualChangeEvent implements IVisualChangeEvent {

	private ILayer layer;
	
	private Collection<Range> rowPositionRanges = new ArrayList<Range>();
	
	public RowVisualChangeEvent(ILayer layer, Range...rowPositionRanges) {
		this(layer, Arrays.asList(rowPositionRanges));
	}
	
	public RowVisualChangeEvent(ILayer layer, Collection<Range> rowPositionRanges) {
		this.layer = layer;
		this.rowPositionRanges = rowPositionRanges;
	}
	
	// Copy constructor
	protected RowVisualChangeEvent(RowVisualChangeEvent event) {
		this.layer = event.layer;
		this.rowPositionRanges = event.rowPositionRanges;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
	public Collection<Range> getRowPositionRanges() {
		return rowPositionRanges;
	}
	
	public boolean convertToLocal(ILayer localLayer) {
		rowPositionRanges = localLayer.underlyingToLocalRowPositions(layer, rowPositionRanges);
		layer = localLayer;
		
		return rowPositionRanges != null && rowPositionRanges.size() > 0;
	}
	
	public Collection<Rectangle> getChangedPositionRectangles() {
		Collection<Rectangle> changedPositionRectangles = new ArrayList<Rectangle>();
		
		int columnCount = layer.getColumnCount();
		for (Range range : rowPositionRanges) {
			changedPositionRectangles.add(new Rectangle(0, range.start, columnCount, range.end - range.start));
		}
		
		return changedPositionRectangles;
	}
	
	public String toString() {
		return getClass().getSimpleName();
	}
	
}
