package net.heartsome.cat.database.postgresql;

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

	public void start() throws SQLException, ClassNotFoundException {
		String driver = dbConfig.getDriver();
		Class.forName(driver);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		conn = DriverManager.getConnection(url, prop);
		// 在 PostgreSQL 中如果使用事务，那么在事务中创建表格会抛出异常。
		conn.setAutoCommit(false);
	}

	/**
	 * 构造函数
	 */
	public TMDatabaseImpl() {
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
	}

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
			strCondition.append(" AND A.LANG='" + srcLang + "'");
		} else {
			return null;
		}
		if (isApplyRegular) {
			// ~ 匹配正则表达式，大小写相关。例: 'thomas' ~ '.*thomas.*'
			// ~* 匹配正则表达式，大小写无关。例: 'thomas' ~* '.*Thomas.*'
			// !~ 不匹配正则表达式，大小写相关。例: 'thomas' !~ '.*Thomas.*'
			// !~* 不匹配正则表达式，大小写无关。例: 'thomas' !~* '.*vadim.*'
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " "
					+ (isCaseSensitive ? "~" : "~*") + " '" + TextUtil.replaceRegextSqlWithMOP(strSearch) + "'");
		} else if (isCaseSensitive) {
			// postgreSql 中区分大小写用 LIKE，不区分用 ILIKE
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " LIKE '%"
					+ TextUtil.cleanStringByLikeWithPostgreSql(strSearch) + "%' ESCAPE '\\\\'");
		} else {
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " ILIKE '%"
					+ TextUtil.cleanStringByLikeWithPostgreSql(strSearch) + "%' ESCAPE '\\\\'");
		}
		if (arrFilter != null) {
			StringBuffer strFilter = new StringBuffer(arrFilter[1].replaceAll("LIKE", "ILIKE").replaceAll("like",
					"ILIKE")
					+ " '%" + TextUtil.cleanStringByLikeWithPostgreSql(arrFilter[2]) + "%' ESCAPE '\\\\'");
			// 过滤条件要加在源语言中
			if (arrFilter[0].equalsIgnoreCase(srcLang)) {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", "");
				strFilter.insert(0, " AND A.PURE ");
				strCondition.append(strFilter.toString());
			} else {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", " ,TEXTDATA B ");
				strCondition.append(" AND A.GROUPID=B.GROUPID AND B.TYPE='M' AND B.LANG='" + arrFilter[0] + "'");
				strFilter.insert(0, " AND B.PURE ");
				strCondition.append(strFilter.toString());
			}
		} else {
			sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", "");
		}
		sql = Utils.replaceString(sql, "__CONDITION__", strCondition.toString());
		return sql;
	}
}
