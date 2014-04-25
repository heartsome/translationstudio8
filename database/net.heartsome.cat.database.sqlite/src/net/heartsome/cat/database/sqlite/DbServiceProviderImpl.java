package net.heartsome.cat.database.sqlite;

import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.DBServiceProvider;
import net.heartsome.cat.database.SystemDBOperator;

/**
 * 
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class DbServiceProviderImpl implements DBServiceProvider {

	public DBOperator getTmDatabaseInstance() {
		return new TMDatabaseImpl();
	}

	public SystemDBOperator getOperateDBInstance() {
		return new OperateSystemDBImpl();
	}

}
