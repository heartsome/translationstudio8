package net.sourceforge.nattable.edit.event;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.event.ILayerEvent;

import org.eclipse.swt.widgets.Composite;

public class InlineCellEditEvent implements ILayerEvent {

	private final PositionCoordinate cellCoordinate;
	private ILayer layer;
	private final Composite parent;
	private final IConfigRegistry configRegistry;
	private final Character initialValue;

	public InlineCellEditEvent(ILayer layer, PositionCoordinate cellCoordinate, Composite parent, IConfigRegistry configRegistry, Character initialValue) {
		this.layer = layer;
		this.cellCoordinate = cellCoordinate;
		this.parent = parent;
		this.configRegistry = configRegistry;
		this.initialValue = initialValue;
	}
	
	public boolean convertToLocal(ILayer localLayer) {
		cellCoordinate.columnPosition = localLayer.underlyingToLocalColumnPosition(layer, cellCoordinate.columnPosition);
		if (cellCoordinate.columnPosition < 0 || cellCoordinate.columnPosition >= localLayer.getColumnCount()) {
			return false;
		}
		
		cellCoordinate.rowPosition = localLayer.underlyingToLocalRowPosition(layer, cellCoordinate.rowPosition);
		if (cellCoordinate.rowPosition < 0 || cellCoordinate.rowPosition >= localLayer.getRowCount()) {
			return false;
		}
		
		this.layer = localLayer;
		return true;
	}

	public int getColumnPosition() {
		return cellCoordinate.columnPosition;
	}
	
	public int getRowPosition() {
		return cellCoordinate.rowPosition;
	}
	
	public Composite getParent() {
		return parent;
	}
	
	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public Character getInitialValue() {
		return initialValue;
	}
	
	public InlineCellEditEvent cloneEvent() {
		return new InlineCellEditEvent(layer, cellCoordinate, parent, configRegistry, initialValue);
	}
	
}