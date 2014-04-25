package net.heartsome.cat.database.mysql;

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

public class OperateSystemDBImpl extends SystemDBOperator{

	public OperateSystemDBImpl(){
		metaData = new MetaDataImpl();
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
		metaData.setServerName(dbConfig.getDefaultServer());
		metaData.setPort(dbConfig.getDefaultPort());
		metaData.setDbType(dbConfig.getDefaultType());
	}
	
	@Override
	protected Connection getConnection(String driver, String url, Properties prop) throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		DriverManager.setLoginTimeout(1);
		return DriverManager.getConnection(url, prop);
	}
}
