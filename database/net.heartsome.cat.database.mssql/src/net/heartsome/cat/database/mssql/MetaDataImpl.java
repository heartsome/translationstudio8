package net.heartsome.cat.database.mssql;

import net.heartsome.cat.common.bean.MetaData;

public class MetaDataImpl extends MetaData{
	
	@Override
	public boolean dataPathSupported() {
		return false;
	}

	@Override
	public boolean instanceSupported() {
		return false;
	}	
	
}
