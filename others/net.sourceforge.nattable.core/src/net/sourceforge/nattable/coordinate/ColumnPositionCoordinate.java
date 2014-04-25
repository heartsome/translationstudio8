package net.sourceforge.nattable.coordinate;

import net.sourceforge.nattable.layer.ILayer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class ColumnPositionCoordinate {
	
	private ILayer layer;
	
	public int columnPosition;

	public ColumnPositionCoordinate(ILayer layer, int columnPosition) {
		this.layer = layer;
		this.columnPosition = columnPosition;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
    public int getColumnPosition() {
        return columnPosition;
    }
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + layer + ":" + columnPosition + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof ColumnPositionCoordinate == false) {
			return false;
		}
		
		ColumnPositionCoordinate that = (ColumnPositionCoordinate) obj;
		
		return new EqualsBuilder()
			.append(this.layer, that.layer)
			.append(this.columnPosition, that.columnPosition)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(133, 95)
			.append(layer)
			.append(columnPosition)
			.toHashCode();
	}
	
}
