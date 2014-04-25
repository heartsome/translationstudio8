package net.sourceforge.nattable.data;

import java.io.Serializable;

public interface IRowIdAccessor<R> {

	public Serializable getRowId(R rowObject);

}
