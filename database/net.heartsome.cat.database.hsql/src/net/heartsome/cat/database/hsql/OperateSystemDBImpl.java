package net.heartsome.cat.database.hsql;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.hsql.resource.Messages;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperateSystemDBImpl extends SystemDBOperator {

	private Logger logger = LoggerFactory.getLogger(OperateSystemDBImpl.class);

	public OperateSystemDBImpl() {
		metaData = new MetaDataImpl();
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
		metaData.setDbType(dbConfig.getDefaultType());
	}

	@Override
	public int createDB() throws SQLException {
		List<String> createTables = dbConfig.getCreateTables();
		createTables.addAll(dbConfig.getCreateIndexs());
		return createDB(createTables, metaData);
	}

	/**
	 * 创建数据表
	 * @param createTables
	 * @param metaData
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	private int createDB(List<String> createTables, MetaData metaData) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
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
		} catch (ClassNotFoundException e) {
			logger.error("", e);
			e.printStackTrace();
			return Constants.FAILURE;
		} catch (SQLException e) {
			logger.error("", e);
			e.printStackTrace();
			try {
				if (conn != null && !conn.isClosed()) {
					conn.rollback();
				}
			} catch (SQLException e1) {
				throw e1;
			}
		} finally {
			try {
				stmt.executeUpdate("SHUTDOWN;");
			} catch (SQLException e) {
				logger.error("", e);
			}
			freeConnection(stmt, conn);
		}
		return Constants.SUCCESS;
	}
	
	@Override
	public void removeSysDb(String dbName) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String driver = dbConfig.getDriver();
		metaData.setDatabaseName(Constants.HSSYSDB);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);

		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		// 删除系统表中的数据
		prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		try {
			conn = getConnection(driver, url, prop);
			String removeDb = dbConfig.getRemoveSysDb();
			pstmt = conn.prepareStatement(removeDb);
			pstmt.setString(1, dbName);
			pstmt.execute();
		} catch (ClassNotFoundException e) {
			logger.warn("", e);
		} finally {
			Statement _stmt = null;
			try {
				_stmt = conn.createStatement();
				_stmt.executeUpdate("SHUTDOWN;");
			} catch (SQLException e) {
				logger.error("", e);
			} finally{
				if(_stmt != null){
					_stmt.close();
				}
			}
			freeConnection(pstmt, conn);
		}
	}
	
	/**
	 * 插入系统库
	 * @param dbName
	 *            数据库名称
	 * @param quality
	 *            是否优化
	 */
	public void addSysDb(String dbName, String quality, int type) {
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
		} catch (CloneNotSupportedException e) {
			logger.warn("", e);
			return;
		}
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), data);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = getConnection(driver, url, prop);
			String insertDb = Utils.replaceParams(dbConfig.getInsertSysDb(), data);
			stmt = conn.prepareStatement(insertDb);
			stmt.setString(1, dbName);
			stmt.setString(2, quality);
			stmt.setInt(3, type);
			stmt.execute();
		} catch (ClassNotFoundException e) {
			logger.warn("", e);
		} catch (SQLException e) {
			logger.warn("", e);
		} finally {
			Statement _stmt = null;
			try {
				_stmt = conn.createStatement();
				_stmt.executeUpdate("SHUTDOWN;");
			} catch (SQLException e) {
				logger.error("", e);
			} finally{
				if(_stmt != null){
					try {
						_stmt.close();
					} catch (SQLException e) {
						logger.error("",e);
					}
				}
			}
			freeConnection(stmt, conn);
		}
	}

	/**
	 * 创建系统库
	 * @return
	 * @throws SQLException
	 */
	public int createSysDb() throws SQLException {
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
		} catch (CloneNotSupportedException e) {
			logger.warn("", e);
			return Constants.FAILURE;
		}
		List<String> createTables = dbConfig.getCreateSysTables();
		return createDB(createTables, data);
	}

	@Override
	public boolean checkSysDb() {
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
		} catch (CloneNotSupportedException e) {
			logger.warn("", e);
			return false;
		}
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), data);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
		try {
			conn = getConnection(driver, url, prop);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT DBNAME, QUALITY, USERS FROM HSCATSYSDB");
		} catch (ClassNotFoundException e) {
			logger.warn(Messages.getString("hsql.OperateSystemDBImpl.logger1"), e);
			return false;
		} catch (SQLException e) {
			// 系统表不存在则创建系统表
			List<String> createTables = dbConfig.getCreateSysTables();
			try {
				createDB(createTables, data);
				return true;
			} catch (SQLException e1) {
				logger.error(Messages.getString("hsql.OperateSystemDBImpl.logger2"), e);
				return false;
			}
		} finally {
			try {
				if(stmt != null){
					stmt.executeUpdate("SHUTDOWN;");
				}
			} catch (SQLException e) {
//				logger.error("", e);
			}
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error("",e);
				}
			}
			freeConnection(stmt, conn);
		}
		return true;
	}

	@Override
	public List<String> getSysDbNames(int type) {
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
		} catch (CloneNotSupportedException e) {
			logger.warn("", e);
			return null;
		}
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), data);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> result = new ArrayList<String>();
		try {
			conn = getConnection(driver, url, prop);
			String sql = dbConfig.getSysDbList();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, type);
			rs = stmt.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("DBNAME"));
			}
		} catch (ClassNotFoundException e) {
			logger.warn("", e);
		} catch (SQLException e) {
			logger.warn("", e);
		} finally {
			Statement _stmt  = null;
			try {
				_stmt = conn.createStatement();
				_stmt.executeUpdate("SHUTDOWN;");
			} catch (SQLException e) {
				logger.error("", e);
			} finally{
				if(stmt != null){
					try {
						_stmt.close();
					} catch (SQLException e) {
						logger.warn("", e);
					}
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.warn("", e);
				}
			}
			freeConnection(stmt, conn);
		}
		return result;
	}

	@Override
	public boolean checkDbConnection() {
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#checkDbExistOnServer()
	 */
	@Override
	public boolean checkDbExistOnServer() {
		Connection conn = null;
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
		} catch (CloneNotSupportedException e) {
			logger.warn("", e);
			return false;
		}
		try {
			String url = Utils.replaceParams(dbConfig.getDbURL(), data);
			String driver = dbConfig.getDriver();
			Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
			prop.setProperty("ifexists", "true");
			conn = getConnection(driver, url, prop);
		} catch (ClassNotFoundException e) {
			logger.error(Messages.getString("hsql.OperateSystemDBImpl.logger3"), e);
			return false;
		} catch (SQLException e) {
			//logger.error("", e);
			// Just check ,so don't need record the exception
			return false;
		} finally {
			if (conn != null) {
				try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate("SHUTDOWN;");
					freeConnection(stmt, conn);
				} catch (SQLException e) {
					logger.error("", e);
				}
			}
		}
		return true;
	}

	@Override
	public void dropDb(String dbName) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		metaData.setDatabaseName(dbName);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		try {
			conn = getConnection(driver, url, prop);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			String path = metaData.getDataPath();
			stmt.executeUpdate("SHUTDOWN;");
			stmt.close();
			conn.close();
			File f = new File(path + "/" + dbName + ".script");
			if (f.exists()) {
				f.delete();
			}
			f = new File(path + "/" + dbName + ".properties");
			if (f.exists()) {
				f.delete();
			}
			f = new File(path + "/" + dbName + ".data");
			if (f.exists()) {
				f.delete();
			}
			f = new File(path + "/" + dbName + ".backup");
			if (f.exists()) {
				f.delete();
			}
			f = new File(path + "/" + dbName + ".log");
			if (f.exists()) {
				f.delete();
			}
			f = new File(path + "/" + dbName + ".lck");
			if (f.exists()) {
				f.delete();
			}
			f = new File(path + "/" + dbName + ".tmp");
			if (f.exists()) {
				f.delete();
			}
			f = null;
		} catch (ClassNotFoundException e) {
			logger.warn("", e);
		}
	}

	@Override
	protected Connection getConnection(String driver, String url, Properties prop) throws ClassNotFoundException,
			SQLException {
		Class.forName(driver);
		return DriverManager.getConnection(url, prop);
	}

}
