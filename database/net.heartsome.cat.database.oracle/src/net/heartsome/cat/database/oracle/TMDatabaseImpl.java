package net.heartsome.cat.database.oracle;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import net.heartsome.cat.common.bean.TmxContexts;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.TranslationMemoryTools;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.bean.ExportFilterComponentBean;
import net.heartsome.cat.database.bean.FuzzySearchResults;
import net.heartsome.cat.database.bean.TranslationUnitAnalysisResults;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OraclePreparedStatement;

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
	public void setMetaData(MetaData metaData) {
		super.setMetaData(metaData);
		dbConfig.setMetaData(metaData);
	}

	// TODO =========================TMX部分(开始)=========================
	/**
	 * 将TMX的header接点的主要属性写入到mheader表中
	 * @throws SQLException
	 */
	public String insertHeader(Hashtable<String, String> params) throws SQLException {
		CallableStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-mheader");
			stmt = conn.prepareCall(sql);
			int i = 1;
			stmt.setString(i++, params.get("CREATIONTOOL"));
			stmt.setString(i++, params.get("CTVERSION"));
			stmt.setString(i++, params.get("TMF"));
			stmt.setString(i++, params.get("SRCLANG"));
			stmt.setString(i++, params.get("ADMINLANG"));
			stmt.setString(i++, params.get("DATATYPE"));
			stmt.setString(i++, params.get("SEGTYPE"));
			stmt.setString(i++, params.get("CREATIONID"));
			stmt.setString(i++, params.get("CREATIONDATE"));
			stmt.setString(i++, params.get("CHANGEID"));
			stmt.setString(i++, params.get("CHANGEDATE"));
			stmt.setString(i++, params.get("ENCODING"));
			stmt.registerOutParameter(i++, Types.INTEGER);
			stmt.execute();
			return stmt.getString(i - 1);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 将数据插入HEADERNODE表
	 * @param params
	 *            ;
	 * @throws SQLException
	 */
	public void insertHeaderNode(Hashtable<String, String> params) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-mheadernode");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setInt(i++, Integer.parseInt(params.get("HEADERID")));
			stmt.setString(i++, params.get("NODENAME"));
			stmt.setString(i++, params.get("NODETYPE"));
			stmt.setString(i++, params.get("CONTENT"));
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

	}

	/**
	 * 将数据写入到MTU表中
	 * @param params
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public String insertTU(Hashtable<String, String> params) throws SQLException {
		CallableStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tu");
			stmt = conn.prepareCall(sql);
			int i = 1;
			stmt.setInt(i++, Integer.parseInt(params.get("HEADERID")));
			stmt.setString(i++, params.get("TUID"));
			stmt.setString(i++, params.get("CREATIONID"));
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(params.get("CREATIONDATE")));
			stmt.setString(i++, params.get("CHANGEID"));
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(params.get("CHANGEDATE")));
			stmt.setString(i++, params.get("CREATIONTOOL"));
			stmt.setString(i++, params.get("CREATIONTOOLVERSION"));
			stmt.setString(i++, params.get("CLIENT"));
			stmt.setString(i++, params.get("PROJECTREF"));
			stmt.setString(i++, params.get("JOBREF"));
			stmt.registerOutParameter(i++, Types.INTEGER);
			stmt.execute();
			return stmt.getString(i - 1);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 插入数据到TEXTDATA表，更新LANGS表和MATRIX_LANG表
	 * @param params
	 * @return 插入TEXTDATA表记录的ID
	 * @throws SQLException
	 */
	public String insertTextData(String type, String groupId, String hash, String pureText, String content,
			String lang, String preContext, String nextContext) throws SQLException {
		/*
		 * 步骤 1.添加记录到TEXTDATA表 2.查看LANGS表是否有刚才添加的语言，没有则需要增加一条记录，然后需要创建相对应的matrix表 3.添加记录到Matrix表
		 */
		String textDataId = null;
		PreparedStatement stmt = null;
		try {
			String langCode = Utils.langToCode(lang).toUpperCase();
			String sql = dbConfig.getOperateDbSQL("get-lang-bycode");
			if (!langCaches.contains(lang)) {

				Map<Integer, Map<String, String>> langRs = query(sql, new Object[] { lang });
				if (langRs.size() == 0) { // 说明对应的Matrix表还没有创建
					sql = dbConfig.getOperateDbSQL("insert-lang");
					stmt = conn.prepareStatement(sql);
					stmt.setString(1, lang);
					stmt.executeUpdate();
					stmt.close();

					// 创建表
					List<String> createMatrixTables = dbConfig.getCreateMatrixTables();
					for (String i : createMatrixTables) {
						i = i.replaceAll("__LANG__", langCode);
						stmt = conn.prepareStatement(i);
						stmt.execute();
						stmt.close();
					}

					// 创建索引
					List<String> createMatrixIndex = dbConfig.getCreateMatrixIndexes();
					for (String i : createMatrixIndex) {
						i = i.replaceAll("__LANG__", langCode);
						stmt = conn.prepareStatement(i);
						stmt.execute();
						stmt.close();
					}
					langCaches.add(lang);
				}
			}
			if (pureText != null && lang != null && content != null) {
				sql = dbConfig.getOperateDbSQL("insert-textdata");
				OracleCallableStatement callStmt = (OracleCallableStatement) conn.prepareCall(sql);
				int i = 1;
				callStmt.setString(i++, type);
				callStmt.setInt(i++, Integer.parseInt(groupId));
				callStmt.setInt(i++, Integer.parseInt(hash));
				callStmt.setStringForClob(i++, pureText);
				callStmt.setStringForClob(i++, content);
				callStmt.setString(i++, lang);
				callStmt.setString(i++, preContext);
				callStmt.setString(i++, nextContext);
				callStmt.registerOutParameter(i++, Types.INTEGER);
				callStmt.execute();
				textDataId = callStmt.getString(i - 1);
				callStmt.close();

				int[] ngrams = generateNgrams(lang, pureText);
				if (ngrams.length > 0) {
					String insertMatrix = dbConfig.getMatrixSQL("insert");
					insertMatrix = insertMatrix.replaceAll("__LANG__", langCode);
					stmt = conn.prepareStatement(insertMatrix);
					for (int j = 0; j < ngrams.length; j++) {
						stmt.setInt(1, Integer.parseInt(textDataId));
						stmt.setInt(2, ngrams[j]);
						stmt.setShort(3, (short) ngrams.length);
						stmt.addBatch();
					}
					stmt.executeBatch();
					stmt.close();
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		return textDataId;
	}

	@Override
	public int insertTU(int headerId, String tuId, String creationId, String creationDate, String changeId,
			String changeDate, String creationTool, String creationToolVersion, String client, String projectRef,
			String jobRef) throws SQLException {
		CallableStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tu");
			if (null == conn) {
				return -1;
			}
			stmt = conn.prepareCall(sql);
			int i = 1;
			stmt.setInt(i++, headerId);
			stmt.setString(i++, tuId);
			stmt.setString(i++, creationId);
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(creationDate));
			stmt.setString(i++, changeId);
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(changeDate));
			stmt.setString(i++, creationTool);
			stmt.setString(i++, creationToolVersion);
			stmt.setString(i++, client);
			stmt.setString(i++, projectRef);
			stmt.setString(i++, jobRef);
			stmt.registerOutParameter(i, Types.INTEGER);
			stmt.executeUpdate();
			return  stmt.getInt(i);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	@Override
	public String insertTextData(String type, int groupId, String hash, String pureText, String content, String lang,
			String preContext, String nextContext) throws SQLException {
		/*
		 * 步骤 1.添加记录到TEXTDATA表 2.查看LANGS表是否有刚才添加的语言，没有则需要增加一条记录，然后需要创建相对应的matrix表 3.添加记录到Matrix表
		 */
		String textDataId = null;
		CallableStatement stmt = null;
		try {
			String langCode = Utils.langToCode(lang).toUpperCase();
			String sql = dbConfig.getOperateDbSQL("get-lang-bycode");
			if (!langCaches.contains(lang)) {
				Map<Integer, Map<String, String>> langRs = query(sql, new Object[] { lang });
				if (langRs.size() == 0) { // 说明对应的Matrix表还没有创建
					sql = dbConfig.getOperateDbSQL("insert-lang");
					stmt = conn.prepareCall(sql);
					stmt.setString(1, lang);
					stmt.executeUpdate();
					stmt.close();

					// 创建表
					List<String> createMatrixTables = dbConfig.getCreateMatrixTables();
					for (String i : createMatrixTables) {
						i = i.replaceAll("__LANG__", langCode);
						stmt = conn.prepareCall(i);
						stmt.execute();
						stmt.close();
					}

					// 创建索引
					List<String> createMatrixIndex = dbConfig.getCreateMatrixIndexes();
					for (String i : createMatrixIndex) {
						i = i.replaceAll("__LANG__", langCode);
						stmt = conn.prepareCall(i);
						stmt.execute();
						stmt.close();
					}
					langCaches.add(lang);
				}
			}
			if (pureText != null && lang != null && content != null) {
				sql = dbConfig.getOperateDbSQL("insert-textdata");
				stmt = conn.prepareCall(sql);
				int i = 1;
				stmt.setString(i++, type);
				stmt.setInt(i++, groupId);
				stmt.setInt(i++, Integer.parseInt(hash));
				stmt.setString(i++, pureText);
				stmt.setString(i++, content);
				stmt.setString(i++, lang);
				stmt.setString(i++, preContext);
				stmt.setString(i++, nextContext);
				stmt.registerOutParameter(i, Types.VARCHAR);
				stmt.execute();
				textDataId = stmt.getString(i);
				stmt.close();

				int[] ngrams = generateNgrams(lang, pureText);
				if (ngrams.length > 0) {
					String insertMatrix = dbConfig.getMatrixSQL("insert");
					insertMatrix = insertMatrix.replaceAll("__LANG__", langCode);
					stmt = conn.prepareCall(insertMatrix);
					for (int j = 0; j < ngrams.length; j++) {
						stmt.setInt(1, Integer.parseInt(textDataId));
						stmt.setInt(2, ngrams[j]);
						stmt.setShort(3, (short) ngrams.length);
						stmt.addBatch();
					}
					stmt.executeBatch();
					stmt.close();
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		return textDataId;
	}
	/**
	 * 将数据插入TMXPROPS表
	 * @param params
	 *            ;
	 * @throws SQLException
	 */
	public void insertTMXProp(Map<String, String> params) throws SQLException {
		OraclePreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tmxprops");
			stmt = (OraclePreparedStatement) conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, params.get("PARENTNAME"));
			stmt.setInt(i++, Integer.parseInt(params.get("PARENTID")));
			stmt.setString(i++, params.get("TYPE"));
			stmt.setString(i++, params.get("LANG"));
			stmt.setString(i++, params.get("ENCODING"));			
			stmt.setStringForClob(i++, params.get("CONTENT"));
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 将数据插入TMXNOTES表
	 * @param params
	 *            ;
	 * @throws SQLException
	 */
	public void insertTMXNote(Hashtable<String, String> params) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tmxnotes");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, params.get("PARENTNAME"));
			stmt.setInt(i++, Integer.parseInt(params.get("PARENTID")));
			stmt.setString(i++, params.get("CONTENT"));
			stmt.setString(i++, params.get("CREATIONID"));
			stmt.setString(i++, params.get("CREATIONDATE"));
			stmt.setString(i++, params.get("CHANGEID"));
			stmt.setString(i++, params.get("CHANGEDATE"));
			stmt.setString(i++, params.get("ENCODING"));
			stmt.setString(i++, params.get("LANG"));
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 将数据写入到Extra表中
	 * @param type
	 * @param eleName
	 * @param eleContent
	 * @param pName
	 * @param pId
	 * @throws SQLException
	 *             ;
	 */
	public void insertTMXExtra(String type, String eleName, String eleContent, String pName, String pId)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tmxextra");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, pName);
			stmt.setInt(i++, Integer.parseInt(pId));
			stmt.setString(i++, type);
			stmt.setString(i++, eleName);
			stmt.setString(i++, eleContent);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	// TODO =========================TMX部分(结束)=========================

	// TODO ================tbx(开始)==================
	/**
	 * 写MartifHeader节点内容
	 * @param hContent
	 *            整个节点的内容
	 * @param hIdAttr
	 *            MartifHeader节点的ID属性;
	 * @return
	 * @throws SQLException
	 */
	public int insertBMartifHeader(String hContent, String hIdAttr) throws SQLException {
		CallableStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-bmartifheader");
			stmt = conn.prepareCall(sql);
			stmt.setString(1, hIdAttr);
			stmt.setString(2, hContent);
			stmt.registerOutParameter(3, Types.INTEGER);
			stmt.execute();
			return stmt.getInt(3);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 写BAttribute的内容
	 * @param attrs
	 * @param parentName
	 * @param parentId
	 * @throws SQLException
	 *             ;
	 */
	public void insertBAttribute(Map<String, String> attrs, String parentName, int parentId) throws SQLException {
		if (attrs != null) {
			PreparedStatement stmt = null;
			String sql = dbConfig.getOperateDbSQL("insert-battribute");
			Iterator<Entry<String, String>> iter = attrs.entrySet().iterator();
			try {
				while (iter.hasNext()) {
					Entry<String, String> entry = iter.next();
					String attrName = entry.getKey();
					String attrValue = entry.getValue();
					stmt = conn.prepareStatement(sql);
					stmt.setInt(1, parentId);
					stmt.setString(2, attrName);
					stmt.setString(3, attrValue);
					stmt.setString(4, parentName);
					stmt.addBatch();
				}
				stmt.executeBatch();
				stmt.clearBatch();
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		}
	}

	/**
	 * 写BRefObjectList内容
	 * @param roblContent
	 * @param roblIdAttr
	 * @param headerId
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public int insertBRefobjectlist(String roblContent, String roblIdAttr, int headerId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-brefobjectlist");
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, headerId);
			stmt.setString(2, roblIdAttr);
			stmt.setString(3, roblContent);
			stmt.execute();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return 0;
	}

	/**
	 * 写TermEntry内容
	 * @param teContent
	 * @param teIdAttr
	 * @param headerId
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public int insertBTermentry(String teContent, String teIdAttr, int headerId) throws SQLException {
		CallableStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-btermentry");
			stmt = conn.prepareCall(sql);
			stmt.setInt(1, headerId);
			stmt.setString(2, teIdAttr);
			stmt.setString(3, teContent);
			stmt.registerOutParameter(4, Types.INTEGER);
			stmt.execute();
			return stmt.getInt(4);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Override
	public void start() throws SQLException, ClassNotFoundException {
		String driver = dbConfig.getDriver();
		Class.forName(driver);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		conn = DriverManager.getConnection(url, prop);
		conn.setAutoCommit(false);
	}

	/**
	 * 实现匹配算法,查询matrix_lang表
	 * @param srcLang
	 * @param similarity
	 * @param ngrams
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	private List<String> getCandidatesTextDataPks4Oracle(String srcLang, int similarity, int[] ngrams)
			throws SQLException {
		List<String> result = new ArrayList<String>();
		if (!this.langCaches.contains(srcLang)) {
			if (!hasLangInDB(srcLang)) {
				return result;
			}
			langCaches.add(srcLang);
		}
		int size = ngrams.length;
		int min = size * similarity / 100;
		int max = size * 100 / similarity;

		String set = "" + ngrams[0]; //$NON-NLS-1$
		for (int i = 1; i < size; i++) {
			set = set + "," + ngrams[i]; //$NON-NLS-1$
		}
		String select = dbConfig.getMatrixSQL("search");
		select = select.replaceAll("__SET__", set); //$NON-NLS-1$
		select = select.replaceAll("__LANG__", srcLang.replace("-", "").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(select);
			stmt.setInt(1, min);
			stmt.setInt(2, max);
			stmt.setInt(3, min);
			stmt.setInt(4, max);
			rs = stmt.executeQuery();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return result;
	}

	@Override
	public Vector<Hashtable<String, String>> findMatch_1(String puretext, String fullText, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash,
			String nextHash, boolean isIngoreTarget) throws SQLException {
		Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();

		int[] ngrams = generateNgrams(srcLang, puretext);
		int size = ngrams.length;
		if (size == 0) {
			return result;
		}
		// long l1 = System.currentTimeMillis();
		List<String> tpkids = getCandidatesTextDataPks4Oracle(srcLang, minSimilarity, ngrams);
		// System.out.println("查MATEX_LANG表:" + (System.currentTimeMillis() - l1));
		// long l = System.currentTimeMillis();

		// 过虑拆分条件,解决Oracle中where xx in (参数) 参数不越过1000个
		StringBuffer bf = new StringBuffer();
		List<String> tmpTpkids = new ArrayList<String>();
		for (int i = 0; i < tpkids.size(); i++) {
			String tpkid = tpkids.get(i);
			bf.append(",");
			bf.append(tpkid);
			if ((i + 1) % 100 == 0) {
				tmpTpkids.add(bf.toString().substring(1));
				bf = new StringBuffer();
			}
		}
		if (bf.toString().equals("")) {
			return result;
		}
		tmpTpkids.add(bf.toString().substring(1));
		bf = new StringBuffer();
		bf.append("TPKID IN (" + tmpTpkids.get(0) + ")");
		for (int i = 1; i < tmpTpkids.size(); i++) {
			bf.append(" OR TPKID IN (" + tmpTpkids.get(i) + ")");
		}
		String textDataSql = dbConfig.getOperateDbSQL("getTMMatch1");
		textDataSql = textDataSql.replace("__WHERE__", bf.toString());
		PreparedStatement stmt = conn.prepareStatement(textDataSql);
		stmt.setString(1, srcLang);
		stmt.setString(2, tgtLang);
		ResultSet rs = stmt.executeQuery();
		Map<Integer, Map<String, String>> tuSrc = new HashMap<Integer, Map<String, String>>();
		Map<Integer, Map<String, String>> tuTgt = new HashMap<Integer, Map<String, String>>();
		while (rs.next()) {
			Integer groupId = rs.getInt("GROUPID");
			String lang = rs.getString("LANG");
			String pureText = rs.getString("PURE");
			String content = rs.getString("CONTENT");

			String creationId = rs.getString("CREATIONID");
			creationId = creationId == null ? "" : creationId;

			String creationDate = "";
			Timestamp tempCdate = rs.getTimestamp("CREATIONDATE");
			if (tempCdate != null) {
				creationDate = DateUtils.formatToUTC(tempCdate.getTime());
			}

			String changeDate = "";
			Timestamp tempChangeDate = rs.getTimestamp("CHANGEDATE");
			if (tempChangeDate != null) {
				changeDate = DateUtils.formatToUTC(tempChangeDate.getTime());
			}

			String changeid = rs.getString("CHANGEID");
			changeid = changeid == null ? "" : changeid;

			String projectRef = rs.getString("PROJECTREF");
			projectRef = projectRef == null ? "" : projectRef;

			String jobRef = rs.getString("JOBREF");
			jobRef = jobRef == null ? "" : jobRef;

			String client = rs.getString("CLIENT");
			client = client == null ? "" : client;

			if (lang.equalsIgnoreCase(srcLang)) {
				int distance;
				if (caseSensitive) {
					if(isIngoreTarget){
						distance = similarity(puretext, pureText);
					}else {
						distance = similarity(fullText, content);
					}
				} else {
					if(isIngoreTarget){
						distance = similarity(puretext.toLowerCase(), pureText.toLowerCase());
					}else {
						distance = similarity(fullText.toLowerCase(), content.toLowerCase());
					}
				}

				if (distance == 100 && CommonFunction.checkEdition("U")) {
					String preContext = rs.getString("PRECONTEXT");
					preContext =  preContext == null ? "" : preContext;
					String nextContext = rs.getString("NEXTCONTEXT");
					nextContext = nextContext == null ? "" : nextContext;					
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
							distance = 101;
						}
					}
				}

				if (distance >= minSimilarity) {
					Map<String, String> srcMap = new HashMap<String, String>();
					srcMap.put("srcLang", lang);
					srcMap.put("srcText", pureText);
					srcMap.put("srcContent", content);
					srcMap.put("srcCreationId", creationId);
					srcMap.put("srcCreationDate", creationDate);
					srcMap.put("srcChangeId", changeid);
					srcMap.put("srcChangeDate", changeDate);
					srcMap.put("srcProjectRef", projectRef);
					srcMap.put("srcJobRef", jobRef);
					srcMap.put("srcClient", client);
					srcMap.put("similarity", distance + "");
					tuSrc.put(groupId, srcMap);
				}
			}
			if (lang.equalsIgnoreCase(tgtLang)) {
				Map<String, String> tgtMap = new HashMap<String, String>();
				tgtMap.put("tgtLang", lang);
				tgtMap.put("tgtText", pureText);
				tgtMap.put("tgtContent", content);
				tgtMap.put("tgtCreationId", creationId);
				tgtMap.put("tgtCreationDate", creationDate);
				tgtMap.put("tgtChangeId", changeid);
				tgtMap.put("tgtChangeDate", changeDate);
				tgtMap.put("tgtProjectRef", projectRef);
				tgtMap.put("tgtJobRef", jobRef);
				tgtMap.put("tgtClient", client);
				tuTgt.put(groupId, tgtMap);
			}
		}
		stmt.close();
		String dbName = getMetaData().getDatabaseName();
		if (tuSrc.size() > 0) {
			Iterator<Entry<Integer, Map<String, String>>> itr = tuSrc.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Integer, Map<String, String>> entry = itr.next();
				Integer key = entry.getKey();
				Map<String, String> srcMap = entry.getValue();
				Map<String, String> tgtMap = tuTgt.get(key);
				if (tgtMap == null) {
					continue;
				}
				Hashtable<String, String> tu = new Hashtable<String, String>();
				tu.putAll(srcMap);
				tu.putAll(tgtMap);
				tu.put("dbName", dbName); // 应用于origin属性
				result.add(tu);
			}
		}

		int resultSize = result.size();
		if (resultSize > 1) {
			Collections.sort(result, new FindMatchComparator());
		}

		while (resultSize > matchUpperLimit) {
			resultSize--;
			result.remove(resultSize);
		}
		// System.out.println(bf.toString());
		return result;
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
		List<String> tpkids = getCandidatesTextDataPks4Oracle(srcLang, minSimilarity, ngrams);
	
		// 过虑拆分条件,解决Oracle中where xx in (参数) 参数不越过1000个
		StringBuffer bf = new StringBuffer();
		List<String> tmpTpkids = new ArrayList<String>();
		for (int i = 0; i < tpkids.size(); i++) {
			String tpkid = tpkids.get(i);
			bf.append(",");
			bf.append(tpkid);
			if ((i + 1) % 100 == 0) {
				tmpTpkids.add(bf.toString().substring(1));
				bf = new StringBuffer();
			}
		}
		if (bf.toString().equals("")) {
			return;
		}
		tmpTpkids.add(bf.toString().substring(1));
		bf = new StringBuffer();
		bf.append("TPKID IN (" + tmpTpkids.get(0) + ")");
		for (int i = 1; i < tmpTpkids.size(); i++) {
			bf.append(" OR TPKID IN (" + tmpTpkids.get(i) + ")");
		}
		
		String tag = TranslationMemoryTools.getInnerTagContent(fullText);
		String textDataSql = dbConfig.getOperateDbSQL("fuzzySearch");
		textDataSql = textDataSql.replace("__WHERE__", bf.toString());
		Statement stm = null;
		ResultSet rs = null;
		Statement tmpStm = null;
		try {
			stm = conn.createStatement();
			tmpStm = conn.createStatement();
			rs = stm.executeQuery(textDataSql);
			// SELECT TPKID ,GROUPID, PURE, CONTENT, PRECONTEXT, NEXTCONTEXT FROM TEXTDATA WHERE TPKID IN (__SET__)
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
				String targetSqlTemp = targetSql.replace("__GROUPID__", tuId + "");
				// PURE, CONTENT, CREATIONID, CREATIONDATE, CHANGEID, CHANGEDATE ,PROJECTREF
				ResultSet rs1 = null;
				try {
					rs1 = tmpStm.executeQuery(targetSqlTemp);
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
							preContext =  preContext == null ? "" : preContext;						
							nextContext = nextContext == null ? "" : nextContext;
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
			if (tmpStm != null){
				tmpStm.close();
			}
		}
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
		List<String> tpkids = getCandidatesTextDataPks4Oracle(srcLang, minSimilarity, ngrams);
	
		// 过虑拆分条件,解决Oracle中where xx in (参数) 参数不越过1000个
		StringBuffer bf = new StringBuffer();
		List<String> tmpTpkids = new ArrayList<String>();
		for (int i = 0; i < tpkids.size(); i++) {
			String tpkid = tpkids.get(i);
			bf.append(",");
			bf.append(tpkid);
			if ((i + 1) % 100 == 0) {
				tmpTpkids.add(bf.toString().substring(1));
				bf = new StringBuffer();
			}
		}
		if (bf.toString().equals("")) {
			return;
		}
		tmpTpkids.add(bf.toString().substring(1));
		bf = new StringBuffer();
		bf.append("A.TPKID IN (" + tmpTpkids.get(0) + ")");
		for (int i = 1; i < tmpTpkids.size(); i++) {
			bf.append(" OR A.TPKID IN (" + tmpTpkids.get(i) + ")");
		}
		
		String tag = TranslationMemoryTools.getInnerTagContent(fullText);
		String textDataSql = dbConfig.getOperateDbSQL("fuzzySearch-wordsFA");
		textDataSql = textDataSql.replace("__WHERE__", bf.toString());
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
					preContext =  preContext == null ? "" : preContext;						
					nextContext = nextContext == null ? "" : nextContext;
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
			if (tmpStm != null){
				tmpStm.close();
			}
		}
	
	}
	
	@Override
	public String generationExportTMXFilter(String tableName, ExportFilterBean filterBean) {
		String connector = filterBean.getFilterConnector();
		List<ExportFilterComponentBean> filterOption = filterBean.getFilterOption();
		Map<String, Character> tuMatch = Utils.getFilterMatchMTU("MTU");
		Map<String, Character> mNoteDateMatch = Utils.getFilterMatchMTU("MNOTE");
		Map<String, Character> textDateMatch = Utils.getFilterMatchMTU("TEXTDATA");

		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < filterOption.size(); i++) {
			ExportFilterComponentBean bean = filterOption.get(i);

			Character fieldType = null;
			String field = bean.getMatchDbField();
			String op = bean.getExpressionMatchDb();
			String value = bean.getFilterVlaue();
			if ("MTU".equals(tableName)) { // 添加 "A.","B.","C."请参考查询SQL
				fieldType = tuMatch.get(field);
				field = "A." + field;
			} else if ("MNOTE".equals(tableName)) {
				fieldType = mNoteDateMatch.get(field);
				field = "B." + field;
			} else if ("TEXTDATA".equals(tableName)) {
				fieldType = textDateMatch.get(field);
				field = "C." + field;
			}
			if (fieldType == null) {
				continue;
			}
			bf.append(field);
			bf.append(" " + op + " ");

			switch (fieldType) {
			case '1': // 文本内容,包含/不包含内容
				if (op.equals("like") || op.equals("not like")) {
					bf.append(" '%" + value + "%' ");
				} else {
					bf.append("' " + value + "' ");
				}
				bf.append(connector + " ");
				break;
			case '2': // 日期类型
				bf.append(" to_date('" + value + "','yyyy-mm-dd hh24:mi:ss') ");
				bf.append(connector + " ");
				break;
			default:
				return "";
			}
		}
		String result = bf.toString();
		if (result.equals("")) {
			return result;
		}
		return result.substring(0, result.lastIndexOf(connector));
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
			// ‘c’ 说明在进行匹配时区分大小写（缺省值）；
			// 'i' 说明在进行匹配时不区分大小写；
			strCondition.append(" AND REGEXP_LIKE(" + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + ",'"
					+ TextUtil.replaceRegextSqlWithMOP(strSearch) + "','" + (isCaseSensitive ? "c" : "i") + "')");
		} else if (isCaseSensitive) {
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " LIKE '%"
					+ TextUtil.cleanStringByLikeWithOracle(strSearch) + "%' escape '\\\\'");
		} else {
			// Oracle 默认区分大小写，使用 upper 函数将其转换成大写查询
			strCondition.append(" AND upper(" + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + ") LIKE '%"
					+ TextUtil.cleanStringByLikeWithOracle(strSearch).toUpperCase() + "%' escape '\\\\'");
		}
		if (arrFilter != null) {
			StringBuffer strFilter = new StringBuffer(arrFilter[1] + " '%"
					+ TextUtil.cleanStringByLikeWithOracle(arrFilter[2]).toUpperCase() + "%' escape '\\\\'");
			// 过滤条件要加在源语言中
			if (arrFilter[0].equalsIgnoreCase(srcLang)) {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", "");
				strFilter.insert(0, " AND upper(A.PURE) ");
				strCondition.append(strFilter.toString());
			} else {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", " ," + getMetaData().getDatabaseName()
						+ "_TEXTDATA B ");
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
