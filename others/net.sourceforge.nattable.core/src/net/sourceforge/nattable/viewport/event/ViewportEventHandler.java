package net.sourceforge.nattable.viewport.event;

import java.util.Collection;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.event.ILayerEventHandler;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.StructuralDiff;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class ViewportEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

	private final ViewportLayer viewportLayer;

	public ViewportEventHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<IStructuralChangeEvent> getLayerEventClass() {
		return IStructuralChangeEvent.class;
	}

	public void handleLayerEvent(IStructuralChangeEvent event) {
		if (event.isHorizontalStructureChanged()) {
			viewportLayer.invalidateHorizontalStructure();
		}
		if (event.isVerticalStructureChanged()) {
			viewportLayer.invalidateVerticalStructure();
		}
		
		Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
		if (columnDiffs != null) {
			int columnOffset = 0;
			
			int minimumOriginColumnPosition = viewportLayer.getMinimumOriginColumnPosition();
			for (StructuralDiff columnDiff : columnDiffs) {
				switch (columnDiff.getDiffType()) {
				case ADD:
					Range afterPositionRange = columnDiff.getAfterPositionRange();
					if (afterPositionRange.start < minimumOriginColumnPosition) {
						columnOffset += afterPositionRange.size();
					}
					break;
				case DELETE:
					Range beforePositionRange = columnDiff.getBeforePositionRange();
					if (beforePositionRange.start < minimumOriginColumnPosition) {
						columnOffset -= Math.min(beforePositionRange.end, minimumOriginColumnPosition + 1) - beforePositionRange.start;
					}
					break;
				}
			}
			
			viewportLayer.setMinimumOriginColumnPosition(minimumOriginColumnPosition + columnOffset);
		}
		
		Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
		if (rowDiffs != null) {
			int rowOffset = 0;
			
			int minimumOriginRowPosition = viewportLayer.getMinimumOriginRowPosition();
			for (StructuralDiff rowDiff : rowDiffs) {
				switch (rowDiff.getDiffType()) {
				case ADD:
					Range afterPositionRange = rowDiff.getAfterPositionRange();
					if (afterPositionRange.start < minimumOriginRowPosition) {
						rowOffset += afterPositionRange.size();
					}
					break;
				case DELETE:
					Range beforePositionRange = rowDiff.getBeforePositionRange();
					if (beforePositionRange.start < minimumOriginRowPosition) {
						rowOffset -= Math.min(beforePositionRange.end, minimumOriginRowPosition + 1) - beforePositionRange.start;
					}
					break;
				}
			}
			
			viewportLayer.setMinimumOriginRowPosition(minimumOriginRowPosition + rowOffset);
		}
	}

}
