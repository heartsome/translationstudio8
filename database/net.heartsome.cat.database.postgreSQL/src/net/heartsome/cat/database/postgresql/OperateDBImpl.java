package net.heartsome.cat.database.postgresql;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.postgresql.resource.Messages;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperateDBImpl extends SystemDBOperator {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public OperateDBImpl() {
		metaData = new MetaDataImpl();
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
		metaData.setServerName(dbConfig.getDefaultServer());
		metaData.setPort(dbConfig.getDefaultPort());
		metaData.setDbType(dbConfig.getDefaultType());
	}

	@Override
	public boolean checkDbConnection() {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);

		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		// 创建数据库
		try {
			conn = getConnection(driver, url.substring(0, url.lastIndexOf("/")) + "/template1", prop); //$NON-NLS-1$			
		} catch (ClassNotFoundException e) {
			logger.error(Messages.getString("postgresql.OperateDBImpl.logger1"), e);
			return false;
		} catch (SQLException e) {
			logger.error(Messages.getString("postgresql.OperateDBImpl.logger2"), e);
			return false;
		} finally {
			freeConnection(stmt, conn);
		}
		return true;
	}

	@Override
	protected int createDB(String createDb, List<String> createTables, MetaData metaData) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);

		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		// 创建数据库
		try {
			conn = getConnection(driver, url.substring(0, url.lastIndexOf("/")) + "/template1", prop); //$NON-NLS-1$
			stmt = conn.createStatement();
			stmt.execute(createDb);
		} catch (ClassNotFoundException e) {
			logger.error(Messages.getString("postgresql.OperateDBImpl.logger1"), e);
			return Constants.FAILURE;
		} finally {
			freeConnection(stmt, conn);
		}

		// 创建数据库表格
		try {
			conn = getConnection(driver, url, prop);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			if (createTables != null) {
				for (String i : createTables) {
					String step = Utils.replaceParams(i, metaData);
					stmt.execute(step);
				}
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.rollback();
				}
			} catch (SQLException e1) {
				logger.warn("", e1);
			}
			logger.warn("", e);
			return Constants.FAILURE;
		} catch (ClassNotFoundException e) {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.rollback();
				}
			} catch (SQLException e1) {
				logger.warn("", e1);
			}
			logger.warn("", e);
			return Constants.FAILURE;
		} finally {
			freeConnection(stmt, conn);
		}
		return Constants.SUCCESS;
	}
	
	@Override
	public void dropDb(String dbName) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		metaData.setDatabaseName(dbName);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);

		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		// 删除数据库
		try {
			conn = getConnection(driver, url.substring(0, url.lastIndexOf("/")) + "/template1", prop); //$NON-NLS-1$
			stmt = conn.createStatement();
			String dropDb = Utils.replaceParams(dbConfig.getDropDb(), metaData);
			stmt.execute(dropDb);
		} catch (ClassNotFoundException e) {
			logger.warn("", e);
		} finally {
			freeConnection(stmt, conn);
		}
	}
	
	@Override
	protected Connection getConnection(String driver, String url, Properties prop) throws ClassNotFoundException,
			SQLException {
		Class.forName(driver);
		return DriverManager.getConnection(url, prop);
	}
}
