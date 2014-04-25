package net.sourceforge.nattable.coordinate;

import net.sourceforge.nattable.layer.ILayer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class PositionCoordinate {
	
	private ILayer layer;
	
	public int columnPosition;

	public int rowPosition;

	public PositionCoordinate(ILayer layer, int columnPosition, int rowPosition) {
		this.layer = layer;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
    public int getColumnPosition() {
        return columnPosition;
    }
	
	public int getRowPosition() {
	    return rowPosition;
	}
	
	public void set(int rowPosition, int columnPosition) {
	    this.rowPosition = rowPosition;
	    this.columnPosition = columnPosition;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + layer + ":" + columnPosition + "," + rowPosition + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof PositionCoordinate == false) {
			return false;
		}
		
		PositionCoordinate that = (PositionCoordinate) obj;
		
		return new EqualsBuilder()
			.append(this.layer, that.layer)
			.append(this.columnPosition, that.columnPosition)
			.append(this.rowPosition, that.rowPosition)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(31, 59)
			.append(layer)
			.append(columnPosition)
			.append(rowPosition)
			.toHashCode();
	}
	
}
