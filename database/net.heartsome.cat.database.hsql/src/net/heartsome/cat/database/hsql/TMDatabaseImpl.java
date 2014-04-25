package net.heartsome.cat.database.hsql;

import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.Utils;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @author terry
 * @version
 * @since JDK1.6
 */
public class TMDatabaseImpl extends DBOperator {

	/**
	 * 构造函数
	 */
	public TMDatabaseImpl() {
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
	}

	@Override
	public void start() throws SQLException, ClassNotFoundException {
		String driver = dbConfig.getDriver();
		Class.forName(driver);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		prop.setProperty("ifexists", "true");
		conn = DriverManager.getConnection(url, prop);
//		conn.setAutoCommit(false);
	}

//	@Override
//	public void end() throws SQLException {
//		if (conn != null && !conn.isClosed()) {
//			Statement statement = null;
//			try {
//				statement = conn.createStatement();
//				statement.executeUpdate("SHUTDOWN;");
//			} catch (SQLNonTransientConnectionException e) {
//				e.printStackTrace();
//				super.end();
//			}catch (SQLException ex) {
//				LOGGER.error("", ex);
//			} finally {
//				if (statement != null) {
//					statement.close();
//				}
//			}
//		}
//		super.end();
//	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.DBOperator#replaceTMOrTBConditionSql(java.lang.String, java.lang.String, boolean,
	 *      boolean, boolean, java.lang.String, java.lang.String[])
	 */
	public String replaceTMOrTBConditionSql(String sql, String strSearch, boolean isCaseSensitive,
			boolean isApplyRegular, boolean isIgnoreMark, String srcLang, String[] arrFilter) {
		strSearch = strSearch == null ? "" : strSearch;
		StringBuffer strCondition = new StringBuffer();
		if (srcLang != null) {
			strCondition.append(" AND LANG='" + srcLang + "'");
		} else {
			return null;
		}
		if (isApplyRegular) {
			strCondition.append(" AND REGEXP_SUBSTRING(" + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + ", '"
					+ (isCaseSensitive ? "" : "(?i)") + TextUtil.replaceRegextSqlWithHSQL(strSearch) + "')!=''");
		} else if (isCaseSensitive) {
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " LIKE '%"
					+ TextUtil.cleanStringByLikeWithHSQL(strSearch) + "%' ESCAPE '\\\\'");
		} else {
			strCondition.append(" AND upper(" + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + ") LIKE '%"
					+ TextUtil.cleanStringByLikeWithHSQL(strSearch).toUpperCase() + "%' ESCAPE '\\\\'");
		}
		if (arrFilter != null) {
			StringBuffer strFilter = new StringBuffer(arrFilter[1] + " '%"
					+ TextUtil.cleanStringByLikeWithHSQL(arrFilter[2]).toUpperCase() + "%' ESCAPE '\\\\'");
			// 过滤条件要加在源语言中
			if (arrFilter[0].equalsIgnoreCase(srcLang)) {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", "");
				strFilter.insert(0, " AND upper(A.PURE) ");
				strCondition.append(strFilter.toString());
			} else {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", " ,TEXTDATA B ");
				strCondition.append(" AND A.GROUPID=B.GROUPID AND B.TYPE='M' AND B.LANG='" + arrFilter[0] + "'");
				strFilter.insert(0, " AND upper(B.PURE) ");
				strCondition.append(strFilter.toString());
			}
		} else {
			sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", "");
		}
		sql = Utils.replaceString(sql, "__CONDITION__", strCondition.toString());
		return sql;
	}	
}
