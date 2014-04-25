package net.heartsome.cat.database;

public interface DBServiceProvider {

	public DBOperator getTmDatabaseInstance();
	
	public SystemDBOperator getOperateDBInstance();
	
}
