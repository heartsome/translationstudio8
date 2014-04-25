package net.heartsome.cat.database.mysql;

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
