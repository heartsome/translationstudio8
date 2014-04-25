package net.heartsome.cat.database.postgresql;

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
	
	/**
	 * PostgreSQL数据库名称,只支持小写
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.bean.MetaData#setDatabaseName(java.lang.String)
	 */
	@Override
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName.toLowerCase();
	}
}
