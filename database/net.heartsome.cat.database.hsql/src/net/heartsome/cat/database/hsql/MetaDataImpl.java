package net.heartsome.cat.database.hsql;

import net.heartsome.cat.common.bean.MetaData;

public class MetaDataImpl extends MetaData{

	
	@Override
	public boolean instanceSupported() {
		return false;
	}

	@Override
	public boolean passwordSupported() {
		return false;
	}

	@Override
	public boolean portSupported() {
		return false;
	}

	@Override
	public boolean serverNameSupported() {
		return false;
	}

	@Override
	public boolean userNameSupported() {
		return false;
	}
	
	
}
