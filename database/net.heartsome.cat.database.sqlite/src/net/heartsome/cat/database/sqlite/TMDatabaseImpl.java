package net.heartsome.cat.database.sqlite;

import java.io.File;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.TmxContexts;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.TranslationMemoryTools;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.bean.FuzzySearchResults;
import net.heartsome.cat.database.bean.TranslationUnitAnalysisResults;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.sqlite.Function;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.SynchronousMode;

/**
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TMDatabaseImpl extends DBOperator {

	public TMDatabaseImpl() {
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
	}

	@Override
	public void start() throws SQLException, ClassNotFoundException {
		File file = new File(metaData.getDataPath()+File.separator+metaData.getDatabaseName());
		if(!file.exists()){
			throw new SQLException("File not found");
		}
		
		SQLiteConfig conf = new SQLiteConfig();
		conf.setSynchronous(SynchronousMode.OFF);

		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
	
		url = url.replace("__FILE_SEPARATOR__", File.separator);
		String driver = dbConfig.getDriver();
		Class.forName(driver);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		SQLiteConfig config = new SQLiteConfig(prop);
		config.setSynchronous(SynchronousMode.OFF);
		conn = DriverManager.getConnection(url, config.toProperties());
		// conn.setAutoCommit(false);
	}

	@Override
	public void rollBack() throws SQLException {
		Statement stmt = null;
		if (null == conn) {
			return;
		}
		try {
			stmt = conn.createStatement();
			stmt.execute("rollback;");
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Override
	public void beginTransaction() throws SQLException {
		Statement stmt = null;
		try {
			if (null == conn) {
				return;
			}
			stmt = conn.createStatement();
			if (null == stmt) {
				return;
			}
			stmt.setQueryTimeout(30);
			stmt.execute("begin immediate;");
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Override
	public void commit() throws SQLException {
		Statement stmt = null;
		try {
			if (null == conn) {
				return;
			}
			stmt = conn.createStatement();
			stmt.execute("commit;");
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Override
	public void fuzzySearch(String pureText, String fullText, String srcLang, String tgtLang, int minSimilarity,
			boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash, String nextHash,
			boolean isIngoreTarget, FuzzySearchResults searchResults, int tagPelanty) throws SQLException {
		int[] ngrams = generateNgrams(srcLang, pureText);
		int size = ngrams.length;
		if (size == 0) {
			return;
		}
		// long l1 = System.currentTimeMillis();
		int min = size * minSimilarity / 100;
		int max = size * 100 / minSimilarity;
		Map<String, Integer> tpkids = getCandidatesTextDataPks(srcLang, min, max, ngrams);
		// System.out.println("查MATEX_LANG表:"+(System.currentTimeMillis() - l1));
		// 构建SQL
		Iterator<Entry<String, Integer>> it = tpkids.entrySet().iterator();
		StringBuffer bf = new StringBuffer();
		// long l = System.currentTimeMillis();
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			String tpkid = entry.getKey();
			float c = entry.getValue();
			if (c >= min && c <= max) {
				bf.append(",");
				bf.append(tpkid);
			}
		}
		if (bf.toString().equals("")) {
			return;
		}
		String tag = TranslationMemoryTools.getInnerTagContent(fullText);
		String textDataSql = dbConfig.getOperateDbSQL("fuzzySearch");
		textDataSql = textDataSql.replace("__SET__", bf.toString().substring(1));
		Statement stm = null;
		ResultSet rs = null;
		Statement tmpStm = null;
		try {
			stm = conn.createStatement();
			tmpStm = conn.createStatement();
			rs = stm.executeQuery(textDataSql);
			// SELECT TPKID, GROUPID, PURE, CONTENT, PRECONTEXT, NEXTCONTEXT FROM TEXTDATA WHERE TPKID IN (__SET__)
			String targetSql = dbConfig.getOperateDbSQL("fuzzySearch-target").replace("__LANG__", tgtLang);
			String dbName = getMetaData().getDatabaseName();
			while (rs.next()) {
				String _pureText = rs.getString(3);
				String _fullText = rs.getString(4);
				int similarity = 0;
				if (caseSensitive) {
					similarity = similarity(pureText, _pureText);
				} else {
					similarity = similarity(pureText.toLowerCase(), _pureText.toLowerCase());
				}

				String _tag = TranslationMemoryTools.getInnerTagContent(_fullText);
				if (!isIngoreTarget && !tag.equals(_tag)) {
					// 标记内容不相等，则执行罚分
					similarity -= tagPelanty;
				}

				if (similarity < minSimilarity) {
					continue;
				}
				int tuId = rs.getInt(2);
				String temptargetSql = targetSql.replace("__GROUPID__", tuId + "");
				// PURE, CONTENT, CREATIONID, CREATIONDATE, CHANGEID, CHANGEDATE ,PROJECTREF
				ResultSet rs1 = null;
				try {
					rs1 = tmpStm.executeQuery(temptargetSql);
					if (rs1.next()) {
						TmxSegement source = new TmxSegement(_pureText, _fullText, srcLang);
						source.setDbPk(rs.getInt(1));
						_pureText = rs1.getString(2);
						_fullText = rs1.getString(3);
						if (_pureText == null || _pureText.equals("") || _fullText == null || _fullText.equals("")) {
							continue;
						}
						TmxSegement target = new TmxSegement(_pureText, _fullText, tgtLang);
						target.setDbPk(rs1.getInt(1));
						TmxTU tu = new TmxTU(source, target);
						FuzzySearchResult searchRs = new FuzzySearchResult(tu);
						if (searchResults.contains(searchRs)) {
							continue;
						}

						String creationId = rs1.getString(4);
						creationId = creationId == null ? "" : creationId;
						String creationDate = "";
						Timestamp tempCdate = rs1.getTimestamp(5);
						if (tempCdate != null) {
							creationDate = DateUtils.formatToUTC(tempCdate.getTime());
						}
						String changeid = rs1.getString(6);
						changeid = changeid == null ? "" : changeid;
						String changeDate = "";
						Timestamp tempChangeDate = rs1.getTimestamp(7);
						if (tempChangeDate != null) {
							changeDate = DateUtils.formatToUTC(tempChangeDate.getTime());
						}
						String projectRef = rs1.getString(8);
						projectRef = projectRef == null ? "" : projectRef;
						tu.setCreationDate(creationDate);
						tu.setCreationUser(creationId);
						tu.setChangeDate(changeDate);
						tu.setChangeUser(changeid);
						List<TmxProp> attrs = getTuMprops(tuId, "TU");
						tu.setProps(attrs);

						String preContext = rs.getString(5);
						String nextContext = rs.getString(6);
						tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, preContext);
						tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, nextContext);
						if (similarity == 100 && CommonFunction.checkEdition("U")) {
							if (preContext != null && nextContext != null) {
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
									similarity = 101;
								}
							}
						}
						searchRs.setDbName(dbName);
						searchRs.setSimilarity(similarity);
						searchRs.setDbOp(this);
						searchRs.getTu().setTmId(tuId);
						searchResults.add(searchRs);
					}
				} finally {
					if (rs1 != null) {
						rs1.close();
					}
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
			if (tmpStm != null) {
				tmpStm.close();
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.DBOperator#findAllTermsByText(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Vector<Hashtable<String, String>> findAllTermsByText(String srcPureText, String srcLang, String tarLang)
			throws SQLException {
		Vector<Hashtable<String, String>> terms = new Vector<Hashtable<String, String>>();
		if (srcPureText == null) {
			return terms;
		}
		// 此处理需要特殊处理衍生的拉丁字母，由于SQLite 的upper方法这些衍生拉丁字母不起作用。
		srcPureText = srcPureText.toUpperCase().trim();
		String _srcPureText = SQLiteUtils.lowerNonAscii(srcPureText, Locale.ENGLISH);
		if (_srcPureText.equals(srcPureText)) {
			// 没有衍生拉丁字母 如é
			findTrems(srcPureText, srcLang, tarLang, terms);
		} else {
			findTrems(_srcPureText, srcLang, tarLang, terms);
			findTrems(srcPureText, srcLang, tarLang, terms);
		}

		return terms;
	}

	private void findTrems(String srcPureText, String srcLang, String tarLang, Vector<Hashtable<String, String>> result)
			throws SQLException {
		Vector<Hashtable<String, String>> terms = new Vector<Hashtable<String, String>>();
		// 构建SQL
		String getTermSql = dbConfig.getOperateDbSQL("getTerm");
		PreparedStatement stmt = conn.prepareStatement(getTermSql);

		stmt.setString(1, tarLang);
		stmt.setString(2, srcLang + "," + tarLang);
		stmt.setString(3, tarLang + "," + srcLang);
		stmt.setString(4, srcLang);
		stmt.setString(5, srcPureText);
		/*
		 * SELECT A.TPKID, A.PURE, B.PURE FROM TEXTDATA A LEFT JOIN TEXTDATA B ON A.GROUPID=B.GROUPID AND B.LANG=? AND
		 * B.TYPE='B' WHERE A.TYPE='B' AND A.LANG=? AND LOCATE(A.PURE, ?) AND B.PURE IS NOT NULL;
		 */
		ResultSet rs = stmt.executeQuery();
		TO: while (rs.next()) {

			String tuid = rs.getString(1);
			String srcWord = rs.getString(2);
			String tgtWord = rs.getString(3);
			String property = rs.getString(4);
			for (Hashtable<String, String> temp : result) {
				String _srcWord = temp.get("srcWord");
				String _tgtWord = temp.get("tgtWord");
				if (srcWord.equals(_srcWord) && tgtWord.equals(_tgtWord)) {
					continue TO;
				}
			}
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
		result.addAll(terms);
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
		try {
			Function.create(conn, "sqliteRegexp", new Function() {
				protected void xFunc() {
					try {
						if (args() == 2) {
							String input = value_text(0);
							String regex = value_text(1);
							if (Pattern.matches(regex, input)) {
								result(1);
							} else {
								result(0);
							}
						} else {
							error("");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (isApplyRegular) {
			strCondition.append(" AND sqliteRegexp('" + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + "','" + strSearch
					+ "')==1");
		} else {
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " LIKE '%"
					+ /*TextUtil.cleanStringByLikeWithMsSql(*/strSearch/*)*/ + "%'");
		}

		if (arrFilter != null) {
			StringBuffer strFilter = new StringBuffer(arrFilter[1] + " '%"
					+ /*TextUtil.cleanStringByLikeWithMsSql(*/arrFilter[2]/*)*/ + "%'");
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

	@Override
	public void translationUnitAnalysis(String pureText, String fullText, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash,
			String nextHash, boolean isIngoreTarget, TranslationUnitAnalysisResults analysisResults, int tagPelanty)
			throws SQLException {

		int[] ngrams = generateNgrams(srcLang, pureText);
		int size = ngrams.length;
		if (size == 0) {
			return;
		}
		// long l1 = System.currentTimeMillis();
		int min = size * minSimilarity / 100;
		int max = size * 100 / minSimilarity;
		Map<String, Integer> tpkids = getCandidatesTextDataPks(srcLang, min, max, ngrams);
		// System.out.println("查MATEX_LANG表:"+(System.currentTimeMillis() - l1));
		// 构建SQL
		Iterator<Entry<String, Integer>> it = tpkids.entrySet().iterator();
		StringBuffer bf = new StringBuffer();
		// long l = System.currentTimeMillis();
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			String tpkid = entry.getKey();
			float c = entry.getValue();
			if (c >= min && c <= max) {
				bf.append(",");
				bf.append(tpkid);
			}
		}
		if (bf.toString().equals("")) {
			return;
		}
		String tag = TranslationMemoryTools.getInnerTagContent(fullText);
		String textDataSql = dbConfig.getOperateDbSQL("fuzzySearch-wordsFA");
		textDataSql = textDataSql.replace("__SET__", bf.toString().substring(1));
		textDataSql = textDataSql.replace("__TARGETLANG__", tgtLang);
		Statement stm = null;
		ResultSet rs = null;
		Statement tmpStm = null;
		try {
			stm = conn.createStatement();
			tmpStm = conn.createStatement();
			rs = stm.executeQuery(textDataSql);
			// SELECT GROUPID, PURE, CONTENT, PRECONTEXT, NEXTCONTEXT FROM TEXTDATA WHERE TPKID IN (__SET__)
			String dbName = getMetaData().getDatabaseName();
			while (rs.next()) {
				String _pureText = rs.getString(3);
				String _fullText = rs.getString(4);
				int similarity = 0;
				if (caseSensitive) {
					similarity = similarity(pureText, _pureText);
				} else {
					similarity = similarity(pureText.toLowerCase(), _pureText.toLowerCase());
				}

				String _tag = TranslationMemoryTools.getInnerTagContent(_fullText);
				if (!isIngoreTarget && !tag.equals(_tag)) {
					// 标记内容不相等，则执行罚分
					similarity -= tagPelanty;
				}

				if (similarity < minSimilarity) {
					continue;
				}
				if (similarity == 100 && CommonFunction.checkEdition("U")) {
					String preContext = rs.getString(5);
					String nextContext = rs.getString(6);
					if (preContext != null && nextContext != null) {
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
							similarity = 101;
						}
					}
				}
				TranslationUnitAnalysisResult r = new TranslationUnitAnalysisResult(similarity, dbName);
				analysisResults.add(r);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
			if (tmpStm != null) {
				tmpStm.close();
			}
		}

	}

	@Override
	public HashMap<String, IdentityHashMap<String, String>> getTermBaseResult(String strSearch,
			boolean isCaseSensitive, boolean isApplyRegular, boolean isIgnoreMark, String strLang,
			List<String> lstLangs, int intMatchQuality) {
		if (!isCaseSensitive) {
			HashMap<String, IdentityHashMap<String, String>> mapTermBase = new HashMap<String, IdentityHashMap<String, String>>();
			String lo = strSearch.toLowerCase();
			mapTermBase.putAll(super.getTermBaseResult(lo, isCaseSensitive, isApplyRegular, isIgnoreMark, strLang,
					lstLangs, intMatchQuality));
			String up = strSearch.toUpperCase();
			mapTermBase.putAll(super.getTermBaseResult(up, isCaseSensitive, isApplyRegular, isIgnoreMark, strLang,
					lstLangs, intMatchQuality));
			return mapTermBase;
		} else {
			return super.getTermBaseResult(strSearch, isCaseSensitive, isApplyRegular, isIgnoreMark, strLang, lstLangs,
					intMatchQuality);
		}
	}
}
