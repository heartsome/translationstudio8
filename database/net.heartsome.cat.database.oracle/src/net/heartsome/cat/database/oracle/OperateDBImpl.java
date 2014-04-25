package net.heartsome.cat.database.oracle;

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
import net.heartsome.cat.database.oracle.resource.Messages;

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
	
	/**
	 * 检查系统库是否已经创建
	 * @return true 已创建, false 没有创建
	 */
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
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(), data);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
		try {
			conn = getConnection(driver, url, prop);
			stmt = conn.createStatement();
			stmt.execute("SELECT DBNAME, QUALITY, USERS FROM HSCATSYSDB");
		} catch (ClassNotFoundException e) {
			logger.warn(Messages.getString("oracle.OperateDBImpl.logger1"), e);
			return false;
		} catch (SQLException e) {
			return false;
		} finally {
			freeConnection(stmt, conn);
		}
		return true;
	}
	
	@Override
	public int createDB() throws SQLException {
		dbConfig.setMetaData(metaData);
		List<String> createTables = dbConfig.getCreateTables();
		createTables.addAll(dbConfig.getCreateIndexs());
		return benchOperate(createTables);
	}

	/**
	 * 创建系统库
	 * @return
	 * @throws SQLException
	 */
	public int createSysDb() throws SQLException {		
		List<String> createTables = dbConfig.getCreateSysTables();
		return benchOperate(createTables);
	}

	private int benchOperate(List<String> createTables) {
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(),metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		try {
			conn = getConnection(driver, url, prop);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			if (createTables != null) {
				for (String i : createTables) {
					stmt.execute(i);
				}
			}
			conn.commit();
		} catch (ClassNotFoundException e) {
			return Constants.FAILURE;
		} catch (SQLException e) {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.rollback();
				}
			} catch (SQLException e1) {
				logger.warn("", e1);
			}
			e.printStackTrace();
		} finally {
			freeConnection(stmt, conn);
		}
		return Constants.SUCCESS;
	}

	@Override
	public void dropDb(String dbName) throws SQLException {		
		ArrayList<String> dropSql = new ArrayList<String>();
		dropSql.add(dbConfig.getOperateDbSQL("drop-1").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-2").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-3").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-4").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-5").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-6").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-7").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-8").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-9").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-10").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-11").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-12").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-13").replace("__DATABASE_NAME__", dbName));
		dropSql.add(dbConfig.getOperateDbSQL("drop-14").replace("__DATABASE_NAME__", dbName));
		dropSql.addAll(dropMatrixDb(dbName)); //获取删除matrix_lang表的SQL
		benchOperate(dropSql);
	}
	
	/**
	 * 删除matrix_lang表
	 *  ;
	 */
	private List<String> dropMatrixDb(String dbName){
		List<String> dropSql = new ArrayList<String>();
		String sql = dbConfig.getOperateDbSQL("drop-matrix").replace("__DATABASE_NAME__", dbName);
		String getLangSql = dbConfig.getOperateDbSQL("get-langs").replace("__DATABASE_NAME__", dbName);
		Connection conn = null;
		Statement stmt = null;
		String driver = dbConfig.getDriver();
		String url = Utils.replaceParams(dbConfig.getDbURL(),metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		try {
			conn = getConnection(driver, url, prop);		
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(getLangSql);
			while(rs.next()){
				dropSql.add(sql.replace("__LANG__", Utils.langToCode(rs.getString(1)).toUpperCase()));
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally{
			freeConnection(stmt, conn);
		}
		return dropSql;
	}
	
	@Override
	protected Connection getConnection(String driver, String url, Properties prop) throws ClassNotFoundException,
			SQLException {
		Class.forName(driver);
		return DriverManager.getConnection(url, prop);
	}
	
	@Override
	public boolean checkDbExistOnServer() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			MetaData data = (MetaData) metaData.clone();
			data.setDatabaseName(Constants.HSSYSDB);
			String url = Utils.replaceParams(dbConfig.getDbURL(), data);
			String driver = dbConfig.getDriver();
			Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), data);
			conn = getConnection(driver, url, prop);
			String getDbList = "SELECT DBNAME, QUALITY, USERS FROM HSCATSYSDB WHERE DBNAME=?";
			stmt = conn.prepareStatement(getDbList);
			stmt.setString(1,metaData.getDatabaseName());
			rs = stmt.executeQuery();
			if(rs.next()){
				return true;
			}else {
				return false;
			}
		} catch (CloneNotSupportedException e) {
			logger.error("", e);
		} catch (ClassNotFoundException e) {
			logger.error("", e);
		} catch (SQLException e) {
			logger.error("", e);
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
		return true;
	}
}
