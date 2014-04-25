package net.sourceforge.nattable.coordinate;

import net.sourceforge.nattable.layer.ILayer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class RowPositionCoordinate {
	
	private ILayer layer;
	
	public int rowPosition;

	public RowPositionCoordinate(ILayer layer, int rowPosition) {
		this.layer = layer;
		this.rowPosition = rowPosition;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
	public int getRowPosition() {
	    return rowPosition;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + layer + ":" + rowPosition + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof RowPositionCoordinate == false) {
			return false;
		}
		
		RowPositionCoordinate that = (RowPositionCoordinate) obj;
		
		return new EqualsBuilder()
			.append(this.layer, that.layer)
			.append(this.rowPosition, that.rowPosition)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(647, 579)
			.append(layer)
			.append(rowPosition)
			.toHashCode();
	}
	
}
