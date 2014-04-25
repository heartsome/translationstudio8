package net.sourceforge.nattable.selection.event;

import java.util.Collection;
import java.util.Set;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.event.ILayerEventHandler;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.selection.ISelectionModel;
import net.sourceforge.nattable.selection.SelectionLayer;

import org.eclipse.swt.graphics.Rectangle;

public class SelectionLayerStructuralChangeEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

	private ISelectionModel selectionModel;
	private final SelectionLayer selectionLayer;
	
	public SelectionLayerStructuralChangeEventHandler(SelectionLayer selectionLayer, ISelectionModel selectionModel) {
		this.selectionLayer = selectionLayer;
		this.selectionModel = selectionModel;
	}

	public Class<IStructuralChangeEvent> getLayerEventClass() {
		return IStructuralChangeEvent.class;
	}

	public void handleLayerEvent(IStructuralChangeEvent event) {
		if (event.isHorizontalStructureChanged()) {
			// TODO handle column deletion
		}
		
		if (event.isVerticalStructureChanged()) {
			Collection<Rectangle> rectangles = event.getChangedPositionRectangles();
			for (Rectangle rectangle : rectangles) {
				if(selectedRowModified(rectangle.y)) {
					selectionLayer.clear();
				}
			}
		}
	}
	
	private boolean selectedRowModified(int rowPosition){
		Set<Range> selectedRows = selectionModel.getSelectedRows();
		for (Range rowRange : selectedRows) {
			if (rowRange.contains(rowPosition)){
				return true;
			}
		}
		return false;
	}

}
