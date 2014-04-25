package net.heartsome.cat.database.mysql;

import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

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

	@Override
	public Vector<Hashtable<String, String>> findMatch(String puretext, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash,
			String nextHash) throws SQLException {
		Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();

		int[] ngrams = generateNgrams(srcLang, puretext);

		int size = ngrams.length;

		if (size == 0) {
			return result;
		}
		int min = size * minSimilarity / 100;
		int max = size * 100 / minSimilarity;
		Map<String, Integer> tpkids = getCandidatesTextDataPks(Utils.langToCode(srcLang), min, max, ngrams);

		// 构建SQL
		String textDataSql = dbConfig.getOperateDbSQL("getTMMatch");
		PreparedStatement stmt = conn.prepareStatement(textDataSql);
		Iterator<Entry<String, Integer>> it = tpkids.entrySet().iterator();
		StringBuffer bf = new StringBuffer();
		// long l = System.currentTimeMillis();
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			String tpkid = entry.getKey();
			float c = entry.getValue();
			if (c >= min && c <= max) {
				stmt.setString(1, tpkid);
				stmt.setString(2, srcLang);
				stmt.setString(3, tgtLang);
				// System.out.println(stmt);
				ResultSet rs = stmt.executeQuery();
				Hashtable<String, String> tu = new Hashtable<String, String>();
				bf.append(tpkid + ",");
				while (rs.next()) {
					String tuid = rs.getString("TUID");
					String lang = rs.getString("LANG");
					String fullText = rs.getString("CONTENT");
					String pureText = rs.getString("PURE");
					String creationId = rs.getString("CREATIONID");
					if (creationId == null) {
						creationId = System.getProperty("user.name");
					}
					String creationDate = rs.getString("CREATIONDATE");
					if (creationDate == null) {
						creationDate = "";
					}
					String changeid = rs.getString("CHANGEID");
					if (changeid == null) {
						changeid = System.getProperty("user.name");
					}

					String changeDate = rs.getString("CHANGEDATE");
					if (changeDate == null) {
						changeDate = "";
					}

					String preContext = rs.getString("PRECONTEXT");
					if (preContext == null) {
						preContext = "";
					}

					String nextContext = rs.getString("NEXTCONTEXT");
					if (nextContext == null) {
						nextContext = "";
					}

					String projectRef = rs.getString("PROJECTREF");
					if (projectRef == null) {
						projectRef = "";
					}

					String jobRef = rs.getString("JOBREF");
					if (jobRef == null) {
						jobRef = "";
					}

					String client = rs.getString("CLIENT");
					if (client == null) {
						client = "";
					}
					tu.put("tuId", tuid);//
					if (lang.equalsIgnoreCase(srcLang)) {
						tu.put("srcLang", lang);//
						tu.put("srcText", pureText);//
						tu.put("srcContent", fullText);//
						tu.put("srcCreationId", creationId);
						tu.put("srcCreationDate", creationDate);
						tu.put("srcChangeId", changeid);
						tu.put("srcChangeDate", changeDate);
						tu.put("srcProjectRef", projectRef);
						tu.put("srcJobRef", jobRef);
						tu.put("srcClient", client);
						tu.put("preContext", preContext);//
						tu.put("nextContext", nextContext);//
					} else if (lang.equalsIgnoreCase(tgtLang)) {
						tu.put("tgtLang", lang);//
						tu.put("tgtText", pureText);//
						tu.put("tgtContent", fullText);//
						tu.put("tgtCreationId", creationId);
						tu.put("tgtCreationDate", creationDate);
						tu.put("tgtChangeId", changeid);
						tu.put("tgtChangeDate", changeDate);
						tu.put("tgtProjectRef", projectRef);
						tu.put("tgtJobRef", jobRef);
						tu.put("tgtClient", client);
					}
				}
				rs.close();

				// 当没有目标语言时继续查找下一个TU
				String targetLang = tu.get("tgtLang");
				if (targetLang == null) {
					continue;
				}

				String pure = tu.get("srcText");

				int distance;
				if (caseSensitive) {
					distance = similarity(puretext, pure);
				} else {
					distance = similarity(puretext.toLowerCase(), pure.toLowerCase());
				}
				// TODO 罚分实现

				if (distance == 100) {
					String preContext = tu.get("preContext");
					String nextContext = tu.get("nextContext");
					String[] preContexts = preContext.split(",");
					String[] nextContexts = nextContext.split(",");
					if (preContexts.length > contextSize) {
						preContext = ""; //$NON-NLS-1$
						for (int i = 0; i < contextSize; i++) {
							preContext += "," + preContexts[i]; //$NON-NLS-1$
						}
						if (!"".equals(preContext)) { //$NON-NLS-1$
							preContext = preContext.substring(1);
						}
					}

					if (nextContexts.length > contextSize) {
						nextContext = ""; //$NON-NLS-1$
						for (int i = 0; i < contextSize; i++) {
							nextContext += "," + nextContexts[i]; //$NON-NLS-1$
						}
						if (!"".equals(nextContext)) { //$NON-NLS-1$
							nextContext = nextContext.substring(1);
						}
					}

					if (preHash.equals(preContext) && nextHash.equals(nextContext)) {
						distance = 101;
					}

				}

				if (distance >= minSimilarity) {
					tu.put("similarity", "" + distance);
					if (!isDuplicated(result, tu)) {
						tu.put("dbName", getMetaData().getDatabaseName());
						result.add(tu);
					}
				}
			}
		}
		// System.out.println(System.currentTimeMillis() - l);
		// System.out.println(bf.toString());
		stmt.close();

		// 按匹配率高低排序后，如果结果中的记录超过了最大限制数，则取匹配率最高的相应的结果
		Collections.sort(result, new FindMatchComparator());
		int resultSize = result.size();
		while (resultSize > matchUpperLimit) {
			resultSize--;
			result.remove(resultSize);
		}
		return result;
	}

	@Override
	public Vector<Hashtable<String, String>> findAllTermsByText(String srcPureText, String srcLang, String tarLang)
			throws SQLException {
		commit();
		Vector<Hashtable<String, String>> terms = new Vector<Hashtable<String, String>>();
		// 构建SQL
		String getTermSql = dbConfig.getOperateDbSQL("getTerm");
		PreparedStatement stmt = conn.prepareStatement(getTermSql);

		stmt.setString(1, tarLang);
		stmt.setString(2, srcLang + "," + tarLang);
		stmt.setString(3, tarLang + "," + srcLang);
		stmt.setString(4, srcLang);
		stmt.setString(5, srcPureText);

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {

			String tuid = rs.getString(1);
			String srcWord = rs.getString(2);
			String tgtWord = rs.getString(3);
			String property = rs.getString(4);
			Hashtable<String, String> tu = new Hashtable<String, String>();
			tu.put("tuid", tuid);
			tu.put("srcLang", srcLang);
			tu.put("srcWord", srcWord);
			tu.put("tgtLang", tarLang);
			tu.put("tgtWord", tgtWord);
			tu.put("property", property == null ? "" : property);
			terms.add(tu);
		}
		rs.close();
		stmt.close();

		return terms;
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
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " REGEXP "
					+ (isCaseSensitive ? "BINARY " : "") + "'" + TextUtil.replaceRegextSqlWithMOP(strSearch) + "'");
		} else if (isCaseSensitive) {
			strCondition.append(" AND BINARY " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " LIKE '%"
					+ TextUtil.cleanStringByLikeWithMysql(strSearch) + "%'");
		} else {
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " LIKE '%"
					+ TextUtil.cleanStringByLikeWithMysql(strSearch) + "%'");
		}

		if (arrFilter != null) {
			StringBuffer strFilter = new StringBuffer(arrFilter[1] + " '%"
					+ TextUtil.cleanStringByLikeWithMysql(arrFilter[2]) + "%'");
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
