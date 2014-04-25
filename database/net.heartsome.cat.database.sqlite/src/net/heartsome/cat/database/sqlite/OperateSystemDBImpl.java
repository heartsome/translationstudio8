package net.heartsome.cat.database.sqlite;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
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
import net.heartsome.cat.database.sqlite.resource.Messages;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

public class OperateSystemDBImpl extends SystemDBOperator {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperateSystemDBImpl.class);

	private Connection conn = null;

	public OperateSystemDBImpl() {
		metaData = new MetaDataImpl();
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
		metaData.setDbType(dbConfig.getDefaultType());
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#createDB()
	 */
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
		createDirs();
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		url = url.replace("__FILE_SEPARATOR__", File.separator);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		try {
			conn = getConnection(driver, url, prop);
			stmt = conn.createStatement();
			if (createTables != null) {
				stmt.execute("begin immediate;");
				for (String i : createTables) {
					String step = Utils.replaceParams(i, metaData);
					stmt.executeUpdate(step);
				}
				stmt.execute("commit;");
			}
		} catch (ClassNotFoundException e) {
			LOGGER.error("", e);
			e.printStackTrace();
			return Constants.FAILURE;
		} catch (SQLException e) {
			LOGGER.error("", e);
			try {
				if (conn != null && !conn.isClosed()) {
					stmt.execute("rollback;");
				}
			} catch (SQLException e1) {
				throw e1;
			}
			throw e;
		} finally {
			freeConnection(stmt, conn);
		}
		return Constants.SUCCESS;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#createSysDb()
	 */
	public int createSysDb() throws SQLException {
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
		} catch (CloneNotSupportedException e) {
			LOGGER.warn("", e);
			return Constants.FAILURE;
		}
		List<String> createTables = dbConfig.getCreateSysTables();
		return createDB(createTables, data);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#checkDbConnection()
	 */
	public boolean checkDbConnection() {
		return true;
	}

	/**
	 * 检查 dbName 是否存在
	 * @param dbName
	 * @return ;
	 */
	private boolean checkDBIsExist(String dbName) {
		String path = metaData.getDataPath() + File.separator + dbName;
		File file = new File(path);
		return file.exists() && file.isFile();
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#checkSysDb()
	 */
	public boolean checkSysDb() {
		return true;
		// return checkDBIsExist(Constants.HSSYSDB);
	}

	public void addSysDb(String dbName, String quality, int type) {
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
		} catch (CloneNotSupportedException e) {
			LOGGER.warn("", e);
			return;
		}
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), data);
		url = url.replace("__FILE_SEPARATOR__", File.separator);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
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
			LOGGER.warn("", e);
		} catch (SQLException e) {
			LOGGER.warn("", e);
		} finally {
			freeConnection(stmt, conn);
		}
	}

	/**
	 * 删除系统库中保存的指定的数据库
	 * @param dbName
	 * @throws SQLException
	 */
	public void removeSysDb(String dbName) throws SQLException {
		PreparedStatement pstmt = null;
		String driver = dbConfig.getDriver();
		metaData.setDatabaseName(Constants.HSSYSDB);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		url = url.replace("__FILE_SEPARATOR__", File.separator);
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
			LOGGER.warn("", e);
		} finally {
			freeConnection(pstmt, conn);
		}
	}

	/**
	 * 得到系统库中的存储的数据库名称
	 * @return
	 * @throws SQLException
	 */
	public List<String> getSysDbNames(int type) {
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
		} catch (CloneNotSupportedException e) {
			LOGGER.warn("", e);
			return null;
		}
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), data);
		url = url.replace("__FILE_SEPARATOR__", File.separator);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
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
			LOGGER.warn("", e);
		} catch (SQLException e) {
			LOGGER.warn("", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.warn("", e);
				}
			}
			freeConnection(stmt, conn);
		}
		return result;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#dropDb(java.lang.String)
	 */
	public void dropDb(String dbName) throws SQLException {
		if (checkDBIsExist(dbName)) {
			boolean isDelete = new File(metaData.getDataPath() + File.separator + dbName).delete();
			if (!isDelete) {
				throw new SQLException(Messages.getString("sqlite.OperateSystemDBImpl.errorMsg1"));
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#checkDbExistOnServer()
	 */
	public boolean checkDbExistOnServer() {
		return checkDBIsExist(metaData.getDatabaseName());
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.SystemDBOperator#getConnection(java.lang.String, java.lang.String,
	 *      java.util.Properties)
	 */
	protected synchronized Connection getConnection(String driver, String url, Properties p)
			throws ClassNotFoundException, SQLException {
		if (conn != null && !conn.isClosed()) {
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
			conn.close();
		}
		Class.forName(driver);
		SQLiteConfig config = new SQLiteConfig(p);
		return config.createConnection(url);
	}

	protected void freeConnection(Statement stmt, Connection conn) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.warn("", e);
			}
		}
		try {
			if (conn != null && !conn.isClosed()) {
				if (!conn.getAutoCommit()) {
					conn.commit();
				}
				conn.close();
			}
		} catch (SQLException e) {
			LOGGER.warn("", e);
		}
	}
	/**
	 * 创建库时，创建目录如果目录不存在
	 *  ;
	 */
	private void createDirs(){
		String filePath = metaData.getDataPath();
		if(null == filePath || filePath.isEmpty()){
			return ;
		}
		File file = new File(filePath);
		if(!file.exists()){
			file.mkdirs();
		}
		
	}
}
