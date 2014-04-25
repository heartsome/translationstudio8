package net.sourceforge.nattable.layer.event;

import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.Rectangle;

/**
 * General event indicating that structures cached by the layers need refreshing. <br/>
 * TIP: Consider throwing a more focused event (subclass) if you need to do this.
 */
public class StructuralRefreshEvent implements IStructuralChangeEvent {

	private ILayer layer;

	public StructuralRefreshEvent(ILayer layer) {
		this.layer = layer;
	}
	
	protected StructuralRefreshEvent(StructuralRefreshEvent event) {
		this.layer = event.layer;
	}
	
	public ILayer getLayer() {
		return layer;
	}

	public boolean convertToLocal(ILayer localLayer) {
		layer = localLayer;
		
		return true;
	}
	
	public Collection<Rectangle> getChangedPositionRectangles() {
		return Arrays.asList(new Rectangle[] { new Rectangle(0, 0, layer.getColumnCount(), layer.getRowCount()) });
	}
	
	public boolean isHorizontalStructureChanged() {
		return true;
	}
	
	public boolean isVerticalStructureChanged() {
		return true;
	}

	public Collection<StructuralDiff> getColumnDiffs() {
		return null;
	}

	public Collection<StructuralDiff> getRowDiffs() {
		return null;
	}

	public ILayerEvent cloneEvent() {
		return this;
	}

}
