package net.heartsome.cat.database.oracle;

import net.heartsome.cat.common.bean.MetaData;

public class MetaDataImpl extends MetaData{

	@Override
	public boolean dataPathSupported() {
		return false;
	}
	
}
