package net.sourceforge.nattable.layer;

public final class LayoutCoordinate {
	
	public final int x;

	public final int y;

	public LayoutCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
    public int getColumnPosition() {
        return x;
    }
	
	public int getRowPosition() {
	    return y;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + x + "," + y + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != getClass()) return true;
		LayoutCoordinate pc = (LayoutCoordinate) obj;
		return pc.getRowPosition() == getRowPosition() && pc.getColumnPosition() == getColumnPosition();
	}
	
	@Override
	public int hashCode() {
		int hash = 77;
		hash = 11 * hash + getRowPosition() + 99;
		hash = 11 * hash + getColumnPosition();
		return hash;
	}
	
}
