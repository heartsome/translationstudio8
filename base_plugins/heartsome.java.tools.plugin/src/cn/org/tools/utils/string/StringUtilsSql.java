/**
 * StringUtilsSql.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.string;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import cn.org.tools.utils.constant.Constants;

/**
 * 涉及拼凑SQL语句的字符串操作.
 * @author  simon
 * @version 
 * @since   JDK1.6
 */
public class StringUtilsSql extends StringUtilsBasic {

	/** The sqllist. */
	private List<String> sqllist;

	/** The add sql map. */
	private HashMap<String, String> addSqlMap;

	/** The subname. */
	private StringBuffer subname;

	/** The subvalue. */
	private StringBuffer subvalue;

	/** The temp. */
	private StringBuffer temp;

	/** The Constant OPERATOR_LIKE. */
	public static final String OPERATOR_LIKE = "like";

	/** The Constant OPERATOR_EQUALS. */
	public static final String OPERATOR_EQUALS = "=";

	/** The Constant OPERATOR_GREATER. */
	public static final String OPERATOR_GREATER = ">";

	/** The Constant OPERATOR_LESS. */
	public static final String OPERATOR_LESS = "<";

	/** The Constant OPERATOR_GREATER_EQUALS. */
	public static final String OPERATOR_GREATER_EQUALS = ">=";

	/** The Constant OPERATOR_LESS_EQUALS. */
	public static final String OPERATOR_LESS_EQUALS = "<=";

	/**
	 * 获得 sql 条件语句.
	 * @param colName
	 *            数据库列名
	 * @param operator
	 *            操作符(使用本类中定义的操作符常量)
	 * @param value
	 *            列值
	 * @return String
	 * 			  sql 条件语句
	 */
	public static String getCondition(String colName, String operator, String value) {
		StringBuffer temp = new StringBuffer();
		temp.append(colName);
		if (OPERATOR_LIKE.equals(operator)) {
			temp.append(" " + OPERATOR_LIKE + " '%" + value + "%'");
		} else if (OPERATOR_EQUALS.equals(operator)) {
			temp.append(" " + OPERATOR_EQUALS + " '" + value + "'");
		} else if (OPERATOR_GREATER.equals(operator)) {
			temp.append(" " + OPERATOR_GREATER + " '" + value + "'");
		} else if (OPERATOR_LESS.equals(operator)) {
			temp.append(" " + OPERATOR_LESS + " '" + value + "'");
		} else if (OPERATOR_GREATER_EQUALS.equals(operator)) {
			temp.append(" " + OPERATOR_GREATER_EQUALS + " '" + value + "'");
		} else if (OPERATOR_LESS_EQUALS.equals(operator)) {
			temp.append(" " + OPERATOR_LESS_EQUALS + " '" + value + "'");
		}
		return temp.toString();
	}

	/**
	 * 构造方法.
	 */
	public StringUtilsSql() {
		sqllist = new ArrayList<String>();

		addSqlMap = new LinkedHashMap<String, String>();

		subname = new StringBuffer();

		subvalue = new StringBuffer();

		temp = new StringBuffer();

	}

	/**
	 * 清空数据。
	 */
	public void clear() {
		StringUtilsBasic.clearStringBuffer(subname);
		StringUtilsBasic.clearStringBuffer(subvalue);
		StringUtilsBasic.clearStringBuffer(temp);
		sqllist.clear();
		addSqlMap.clear();
	}

	/**
	 * 获得 sql 语句.
	 * @param sql
	 *            进行查询的SQL语句，如果为空字符串，则根据传入的其它参数组成SQL语句
	 * @param orderby
	 *            进行排序的字段名
	 * @param tablename
	 *            如果参数 sql 为空字符串，则表示需要查询的表名，否则这个参数将忽略,如果 sql 和 tablename 同时为 null,返回空串""
	 * @param where
	 *            查询数据库时的 where 条件
	 * @param order
	 *            查询数据库时的排序方式，"ASC"为升序(默认)，"DESC"为降序
	 * @param groupby
	 *            查询数据库时的分组字段。
	 * @return String
	 * 			  由传入参数组成的 sql 语句
	 */
	public static String getSql(String sql, String orderby, String tablename, String where, String order, String groupby) {
		if (orderby == null) {
			orderby = "";
		}
		if (sql == null && tablename == null) {
			return "";
		}
		if (sql == null) {
			sql = "";
		}
		if (where == null) {
			where = "";
		}
		if (order == null) {
			order = "";
		}
		if (groupby == null) {
			groupby = "";
		}
		if ("".equals(sql)) {
			sql = "select * from " + tablename + " where 1=1 " + where;
			if (!"".equals(groupby)) {
				sql += " group by " + groupby;
			}
			if (!"".equals(orderby)) {
				sql += " order by " + orderby + " " + order;
			}
		} else {
			if (!"".equals(groupby) && sql.indexOf("group by") < 0) {
				sql += " group by " + groupby;
			}
			if ("".equals(orderby)) {
				sql += where;
			} else {
				if (sql.indexOf("order") != -1) {
					sql = sql.substring(0, sql.indexOf("order"));
				}
				sql += where + " order by " + orderby + " " + order;
			}
		}
		return sql;
	}

	/**
	 * 获得查询记录数的 sql 语句
	 * @param sql
	 *            进行查询的SQL语句
	 * @return String
	 * 			  查询记录数的 sql 语句
	 */
	public String getCountSql(String sql) {
		String countsql = "";
		if (!"".equals(sql)) {
			countsql = "select count(*) from (" + sql + ") a";
		}
		return countsql;
	}

	/**
	 * 将 sql 语句添加 limit 子句，查询从 startpos 开始(第一条记录为0)的 pagesize 条记录
	 * @param sql
	 *            进行查询的SQL语句
	 * @param startpos
	 *            开始记录
	 * @param pagesize
	 *            记录数
	 * @return String
	 * 				添加了 limit 子句的 sql 语句
	 */
	public String getLimitSql(String sql, int startpos, int pagesize) {
		String limitsql = "";
		if (!"".equals(sql)) {
			switch (Constants.DATABASE_TYPE) {
			case Constants.DATABASE_TYPE_MYSQL:
				limitsql = sql + " limit " + startpos + "," + pagesize;
				break;
			case Constants.DATABASE_TYPE_MSSQL:
				break;
			case Constants.DATABASE_TYPE_OROCALE:
				break;
			default:
				break;
			}
		}
		return limitsql;
	}
}
