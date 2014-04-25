package net.heartsome.cat.database.mssql;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.SystemDBOperator;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class OperateDBImpl extends SystemDBOperator{

	public OperateDBImpl(){
		metaData = new MetaDataImpl();
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
		metaData.setDbType(dbConfig.getDefaultType());
		metaData.setServerName(dbConfig.getDefaultServer());
		metaData.setPort(dbConfig.getDefaultPort());
	}

	@Override
	protected Connection getConnection(String driver, String url, Properties prop) throws ClassNotFoundException,
			SQLException {
		Class.forName(driver);
		return DriverManager.getConnection(url, prop);
	}
	
}
