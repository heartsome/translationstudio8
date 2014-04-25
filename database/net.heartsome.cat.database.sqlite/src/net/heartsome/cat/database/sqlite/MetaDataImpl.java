package net.heartsome.cat.database.sqlite;

import net.heartsome.cat.common.bean.MetaData;

public class MetaDataImpl extends MetaData{

	public boolean instanceSupported() {
		return false;
	}

	public boolean portSupported() {
		return false;
	}

	public boolean serverNameSupported() {
		return false;
	}
	
	public boolean passwordSupported() {
		return false;
	}
	
	public boolean userNameSupported() {
		return false;
	}

}
