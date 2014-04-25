package net.heartsome.cat.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.database.resource.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统数据库管理类,管理当前数据库类型中的所有记忆库/术语库
 * @author terry
 */
public abstract class SystemDBOperator {

	private Logger logger = LoggerFactory.getLogger(SystemDBOperator.class);

	/** 数据库元数据 */
	protected MetaData metaData;

	/** 操作数据库配置 */
	protected DBConfig dbConfig;

	/** 不能连接数据库 */
	public static final int FAILURE_1 = -1;

	/**
	 * 创建数据库操作
	 * @return
	 * @throws SQLException
	 */
	public int createDB() throws SQLException {
		String createDb = Utils.replaceParams(dbConfig.getCreateDb(), metaData);
		List<String> createTables = dbConfig.getCreateTables();
		createTables.addAll(dbConfig.getCreateIndexs());
		return createDB(createDb, createTables, metaData);
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
		String createDb = Utils.replaceParams(dbConfig.getCreateSysDb(), data);
		List<String> createTables = dbConfig.getCreateSysTables();
		return createDB(createDb, createTables, data);
	}

	protected int createDB(String createDb, List<String> createTables, MetaData metaData) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);

		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		// 创建数据库
		try {
			conn = getConnection(driver, url.substring(0, url.lastIndexOf("/") + 1), prop); //$NON-NLS-1$
			stmt = conn.createStatement();
			stmt.execute(createDb);
		} catch (ClassNotFoundException e) {
			logger.error("", e);
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
			logger.warn("SQL Exception", e);
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

	/**
	 * 测试数据库服务器连接，不适用于Internal DB
	 * @return false 连接失败, true 连接成功;
	 */
	public boolean checkDbConnection() {
		Connection conn = null;
		MetaData data = null;
		try {
			data = (MetaData) metaData.clone();
			data.setDatabaseName("");
		} catch (CloneNotSupportedException e) {
			logger.warn("", e);
			return false;
		}
		
		try {
			String url = Utils.replaceParams(dbConfig.getDbURL(), data);
			String driver = dbConfig.getDriver();
			Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
			conn = getConnection(driver, url, prop);
		} catch (ClassNotFoundException e) {
			logger.error(Messages.getString("database.SystemDBOperator.logger1"), e);
			return false;
		} catch (SQLException e) {
			logger.error(Messages.getString("database.SystemDBOperator.logger2"), e);
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.warn("", e);
				}
			}
		}
		return true;
	}

	/**
	 * 更新系统库，如果系统库没有创建，则先创建系统库再插入数据
	 * @throws SQLException
	 */
	public void updataSysDb(int type) throws SQLException {
		boolean result = checkSysDb();
		if (result) {
			addSysDb(metaData.getDatabaseName(), (Constants.OPTIMIZE_SPEED.equals(metaData.getOptimize())) ? "y" : "n",type);
		} else {
			createSysDb();
		}
	}

	/**
	 * 检查系统库是否已经创建
	 * @return true 已创建, false 没有创建
	 */
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
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), data);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
		try {
			conn = getConnection(driver, url, prop);
		} catch (ClassNotFoundException e) {
			logger.warn(Messages.getString("database.SystemDBOperator.logger3"), e);
			return false;
		} catch (SQLException e) {
			logger.warn("", e);
			return false;
		} finally {
			freeConnection(stmt, conn);
		}
		return true;
	}

	/**
	 * 插入系统库
	 * @param dbName
	 *            数据库名称
	 * @param quality
	 *            是否优化
	 */
	public void addSysDb(String dbName, String quality,int type) {
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
			freeConnection(stmt, conn);
		}
	}

	/**
	 * 删除系统库中保存的指定的数据库
	 * @param dbName
	 * @throws SQLException
	 */
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

	/**
	 * 删除指定数据库
	 * @param dbName
	 * @throws SQLException
	 */
	public void dropDb(String dbName) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		metaData.setDatabaseName(dbName);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);

		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		// 删除数据库
		try {
			conn = getConnection(driver, url.substring(0, url.lastIndexOf("/") + 1), prop);
			stmt = conn.createStatement();
			String dropDb = Utils.replaceParams(dbConfig.getDropDb(), metaData);
			stmt.execute(dropDb);
		} catch (ClassNotFoundException e) {
			logger.warn("", e);
		} finally {
			freeConnection(stmt, conn);
		}
	}

	protected abstract Connection getConnection(String driver, String url, Properties p) throws ClassNotFoundException,
			SQLException;

	protected void freeConnection(Statement stmt, Connection conn) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				logger.warn("", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.warn("", e);
			}
		}
	}

	/**
	 * 取得数据库元数据
	 * @return
	 */
	public MetaData getMetaData() {
		return metaData;
	}

	/**
	 * 设置数据库元数据
	 */
	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	/**
	 * 取得数据库配置数据
	 * @return
	 */
	public DBConfig getDBConfig() {
		return dbConfig;
	}

	/**
	 * 检查数据库在当前服务器上是否已经存在，而非在R8系统中已经存在<br>
	 * @return true已经存在.false不存在
	 */
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
			conn = getConnection(driver, url, prop);
		} catch (ClassNotFoundException e) {
			logger.error(Messages.getString("database.SystemDBOperator.logger1"), e);
			return false;
		} catch (SQLException e) {
			// logger.error("", e);
			// Just check ,so don't need record the exception
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.warn("", e);
				}
			}
		}
		return true;
	}
}
