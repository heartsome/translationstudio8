package net.sourceforge.nattable.freeze.event;

import java.util.Collection;

import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.freeze.FreezeLayer;
import net.sourceforge.nattable.layer.event.ILayerEventHandler;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;

public class FreezeEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

	private final FreezeLayer freezeLayer;
	
	public FreezeEventHandler(FreezeLayer freezeLayer) {
		this.freezeLayer = freezeLayer;
	}

	public Class<IStructuralChangeEvent> getLayerEventClass() {
		return IStructuralChangeEvent.class;
	}

	public void handleLayerEvent(IStructuralChangeEvent event) {
		PositionCoordinate topLeftPosition = freezeLayer.getTopLeftPosition();
		PositionCoordinate bottomRightPosition = freezeLayer.getBottomRightPosition();
		
		Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
		if (columnDiffs != null) {
			int leftOffset = 0;
			int rightOffset = 0;
			
			for (StructuralDiff columnDiff : columnDiffs) {
				switch (columnDiff.getDiffType()) {
				case ADD:
					Range afterPositionRange = columnDiff.getAfterPositionRange();
					if (afterPositionRange.start < topLeftPosition.columnPosition) {
						leftOffset += afterPositionRange.size();
					}
					if (afterPositionRange.start <= bottomRightPosition.columnPosition) {
						rightOffset += afterPositionRange.size();
					}
					break;
				case DELETE:
					Range beforePositionRange = columnDiff.getBeforePositionRange();
					if (beforePositionRange.start < topLeftPosition.columnPosition) {
						leftOffset -= Math.min(beforePositionRange.end, topLeftPosition.columnPosition + 1) - beforePositionRange.start;
					}
					if (beforePositionRange.start <= bottomRightPosition.columnPosition) {
						rightOffset -= Math.min(beforePositionRange.end, bottomRightPosition.columnPosition + 1) - beforePositionRange.start;
					}
					break;
				}
			}
			
			topLeftPosition.columnPosition += leftOffset;
			bottomRightPosition.columnPosition += rightOffset;
		}
		
		Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
		if (rowDiffs != null) {
			int leftOffset = 0;
			int rightOffset = 0;
			
			for (StructuralDiff rowDiff : rowDiffs) {
				switch (rowDiff.getDiffType()) {
				case ADD:
					Range afterPositionRange = rowDiff.getAfterPositionRange();
					if (afterPositionRange.start < topLeftPosition.rowPosition) {
						leftOffset += afterPositionRange.size();
					}
					if (afterPositionRange.start <= bottomRightPosition.rowPosition) {
						rightOffset += afterPositionRange.size();
					}
					break;
				case DELETE:
					Range beforePositionRange = rowDiff.getBeforePositionRange();
					if (beforePositionRange.start < topLeftPosition.rowPosition) {
						leftOffset -= Math.min(beforePositionRange.end, topLeftPosition.rowPosition + 1) - beforePositionRange.start;
					}
					if (beforePositionRange.start <= bottomRightPosition.rowPosition) {
						rightOffset -= Math.min(beforePositionRange.end, bottomRightPosition.rowPosition + 1) - beforePositionRange.start;
					}
					break;
				}
			}
			
			topLeftPosition.rowPosition += leftOffset;
			bottomRightPosition.rowPosition += rightOffset;
		}
	}
	
}
