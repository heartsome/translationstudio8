/**
 * TmTranslation.java
 *
 * Version information :
 *
 * Date:2012-4-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.tm.match;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.TmxContexts;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.bean.FuzzySearchResults;
import net.heartsome.cat.database.bean.TMPreferenceConstants;
import net.heartsome.cat.database.bean.TranslationUnitAnalysisResults;
import net.heartsome.cat.database.tm.TmDbOperatorManager;
import net.heartsome.cat.database.tm.TmTransParamsBean;
import net.heartsome.cat.database.tm.resource.Messages;
import net.heartsome.cat.ts.tm.match.extension.AbstractTmMatch;
import net.heartsome.cat.ts.tm.match.extension.ITmMatch;

import org.eclipse.core.resources.IProject;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmMatcher extends AbstractTmMatch implements ITmMatch {

	private TmDbOperatorManager tmDbOperatorManager;
	private TmTransParamsBean transParameters;

	public TmMatcher() {
		transParameters = new TmTransParamsBean();
		tmDbOperatorManager = new TmDbOperatorManager();
	}

	public boolean checkTmMatcher(IProject project) {
		this.setProject(project);
		if (tmDbOperatorManager.getDbOperatorList().size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.translation.ITmMatch.ITmTranslation#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		tmDbOperatorManager.setProject(project);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.translation.ITmMatch.ITmTranslation#getMaxMatchSize()
	 */
	public int getMaxMatchSize() {
		return transParameters.getMaxMatchSize();
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.ts.tm.match.extension.ITmMatch#getTagPenalty()
	 */
	public int getTagPenalty() {
		return transParameters.getTagPenalty();
	}
	
	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.translation.ITmMatch.ITmTranslation#findMatch()
	 */
	public Vector<Hashtable<String, String>> findMatch() {
		Vector<Hashtable<String, String>> matchsVector = new Vector<Hashtable<String, String>>();
		List<DBOperator> dbList = tmDbOperatorManager.getDbOperatorList();
		if (dbList.size() == 0) {
			return matchsVector;
		}

		String pureText = tuInfoBean.getSrcPureText();
		String fullText = tuInfoBean.getSrcFullText();
		String srcLang = Utils.convertLangCode(tuInfoBean.getSrcLanguage());
		String tgtLang = Utils.convertLangCode(tuInfoBean.getTgtLangugage());
		String preContext = tuInfoBean.getPreContext();
		String nextContext = tuInfoBean.getNextContext();

		int maxMatchSize = transParameters.getMaxMatchSize();
		int contextSize = transParameters.getContextSize();
		int minSimilarity = transParameters.getMinSimilarity();
		boolean isCaseSensitive = transParameters.isCaseSensitive();
		boolean isIgnoreTag = transParameters.isIgnoreTag();

		if (pureText == null || pureText.equals("") || srcLang == null || srcLang.equals("") || tgtLang == null
				|| tgtLang.equals("")) {
			return matchsVector;
		}

		for (int i = 0; i < dbList.size(); i++) {
			try {
				matchsVector.addAll(dbList.get(i).findMatch_1(pureText, fullText, srcLang, tgtLang, minSimilarity,
						isCaseSensitive, maxMatchSize, contextSize, preContext, nextContext, isIgnoreTag));
			} catch (SQLException e) {
				logger.error(Messages.getString("match.TmMatcher.logger1"), e);
				continue;
			}
		}
		if (matchsVector.size() > 1) {
			Collections.sort(matchsVector, new FindMatchComparator());
			checkMaxMatchSize(matchsVector);
		}
		return matchsVector;
	}

	public List<FuzzySearchResult> fuzzySearch() {
		int maxMatchSize = transParameters.getMaxMatchSize();
		int contextSize = transParameters.getContextSize();
		int minSimilarity = transParameters.getMinSimilarity();
		int tagPelanty = transParameters.getTagPenalty();
		boolean isCaseSensitive = transParameters.isCaseSensitive();
		boolean isIgnoreTag = transParameters.isIgnoreTag();

		FuzzySearchResults results = new FuzzySearchResults(tmDbOperatorManager.getDefaultDbName(),
				transParameters.getMatchSortStrategry(), maxMatchSize);
		List<DBOperator> dbList = tmDbOperatorManager.getDbOperatorList();
		if (dbList.size() == 0) {
			return results.getSearchResult();
		}

		String pureText = tuInfoBean.getSrcPureText();
		String fullText = tuInfoBean.getSrcFullText();
		String srcLang = Utils.convertLangCode(tuInfoBean.getSrcLanguage());
		String tgtLang = Utils.convertLangCode(tuInfoBean.getTgtLangugage());
		String preContext = tuInfoBean.getPreContext();
		String nextContext = tuInfoBean.getNextContext();

		if (pureText == null || pureText.equals("") || srcLang == null || srcLang.equals("") || tgtLang == null
				|| tgtLang.equals("")) {
			return results.getSearchResult();
		}
		for (int i = 0; i < dbList.size(); i++) {
			try {
				dbList.get(i).fuzzySearch(pureText, fullText, srcLang, tgtLang, minSimilarity, isCaseSensitive,
						maxMatchSize, contextSize, preContext, nextContext, isIgnoreTag, results, tagPelanty);

				results.sort();
				results.clearResults();

			} catch (SQLException e) {
				logger.error(Messages.getString("match.TmMatcher.logger1"), e);
				continue;
			}
		}
		results.sort();
		results.clearResults();
		return results.getSearchResult();
	}

	public List<TranslationUnitAnalysisResult> translationUnitAnalysis() {
		int maxMatchSize = transParameters.getMaxMatchSize();
		int contextSize = transParameters.getContextSize();
		int minSimilarity = transParameters.getMinSimilarity();
		int tagPelanty = transParameters.getTagPenalty();
		boolean isCaseSensitive = transParameters.isCaseSensitive();
		boolean isIgnoreTag = transParameters.isIgnoreTag();

		TranslationUnitAnalysisResults results = new TranslationUnitAnalysisResults(
				tmDbOperatorManager.getDefaultDbName(), transParameters.getMatchSortStrategry(), maxMatchSize);
		List<DBOperator> dbList = tmDbOperatorManager.getDbOperatorList();
		if (dbList.size() == 0) {
			return results.getAnaylysisResults();
		}

		String pureText = tuInfoBean.getSrcPureText();
		String fullText = tuInfoBean.getSrcFullText();
		String srcLang = Utils.convertLangCode(tuInfoBean.getSrcLanguage());
		String tgtLang = Utils.convertLangCode(tuInfoBean.getTgtLangugage());
		String preContext = tuInfoBean.getPreContext();
		String nextContext = tuInfoBean.getNextContext();

		if (pureText == null || pureText.equals("") || srcLang == null || srcLang.equals("") || tgtLang == null
				|| tgtLang.equals("")) {
			return results.getAnaylysisResults();
		}
		for (int i = 0; i < dbList.size(); i++) {
			try {
				dbList.get(i).translationUnitAnalysis(pureText, fullText, srcLang, tgtLang, minSimilarity,
						isCaseSensitive, maxMatchSize, contextSize, preContext, nextContext, isIgnoreTag, results,
						tagPelanty);

				results.sort();
				results.clearResults();

			} catch (SQLException e) {
				logger.error(Messages.getString("match.TmMatcher.logger1"), e);
				continue;
			}
		}
		results.sort();
		results.clearResults();
		return results.getAnaylysisResults();
	}

	/**
	 * 查找匹配结果排序器, 按匹配率由高到低排序
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	private final class FindMatchComparator implements Comparator<Hashtable<String, String>> {
		private int matchSortStrategry = 0;

		public FindMatchComparator() {
			matchSortStrategry = transParameters.getMatchSortStrategry();
		}

		public int compare(Hashtable<String, String> a, Hashtable<String, String> b) {
			Integer a1 = Integer.parseInt(a.get("similarity"));
			Integer b1 = Integer.parseInt(b.get("similarity"));

			if (a1 < b1) {
				return 1;
			} else if (a1 == b1) {
				if (matchSortStrategry == TMPreferenceConstants.DEFAULT_DB_PRECEDENCE) {
					// sort by default database
					String dbName = b.get("dbName");
					if (dbName.equals(tmDbOperatorManager.getDefaultDbName())) {
						return -1;
					}
				} else if (matchSortStrategry == TMPreferenceConstants.DATE_REVERSE_PRECEDENCE) {
					// sort by update date
					String t1Str = a.get("tgtChangeDate");
					String t2Str = b.get("tgtChangeDate");
					if (t1Str.length() != 0 && t2Str.length() != 0) {
						Timestamp t1 = DateUtils.getTimestampFromUTC(t1Str);
						Timestamp t2 = DateUtils.getTimestampFromUTC(t2Str);
						return t2.compareTo(t1);

					} else if (t1Str.length() == 0 && t2Str.length() != 0) {
						return -1;
					}
				}
				return 0;
			} else {
				return -1;
			}
		}
	}

	/**
	 * 保留最大上限匹配数量
	 * @param tmpVector
	 *            ;
	 */
	private void checkMaxMatchSize(List<?> tmpVector) {
		int size = tmpVector.size();
		while (size > transParameters.getMaxMatchSize()) {
			size--;
			tmpVector.remove(size);
		}
	}

	public int getContextSize() {
		return transParameters.getContextSize();
	}

	public void clearResource() {
		tmDbOperatorManager.clearResources();
		transParameters.clearResources();
		super.tuInfoBean.resetTuInfo();

	}

	public void clearDbResource() {
		tmDbOperatorManager.clearResources();
	}

	public void setCustomeMatchParameters(int maxMatchSize, boolean isIgnoreTag, int minSimilarity,
			boolean isCaseSensitive, int contextSize, int tagPenalty) {
		this.transParameters.clearResources();
		this.transParameters = new TmTransParamsBean(maxMatchSize, isIgnoreTag, minSimilarity, isCaseSensitive,
				contextSize, tagPenalty);
	}

	public void setCustomeMatchParameters(int maxMatchSize, int minSimilarity) {
		boolean pIsIgnoreTag = this.transParameters.isIgnoreTag();
		int pContextSize = this.transParameters.getContextSize();
		boolean pIsCaseSensitive = this.transParameters.isCaseSensitive();
		int tagPenalty = this.transParameters.getTagPenalty();
		this.transParameters.clearResources();
		this.transParameters = new TmTransParamsBean(maxMatchSize, pIsIgnoreTag, minSimilarity, pIsCaseSensitive,
				pContextSize, tagPenalty);
	}

	public boolean isIgnoreTag() {
		return this.transParameters.isIgnoreTag();
	}

	public int getMinMatchQuality() {
		return this.transParameters.getMinSimilarity();
	}

	public void deleteFuzzyResult(FuzzySearchResult fr) throws Exception {
		Object obj = fr.getDbOp();
		if (obj == null) {
			throw new Exception(Messages.getString("match.TmMatcher.deleteFuzzyResult.msg1"));
		}
		DBOperator dbOp = (DBOperator) obj;
		boolean needEnd = false;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if (dbOp.isClosed()) {
				needEnd = true;
				dbOp.start();
			}
			int tuDbPk = fr.getTu().getTmId();
			int srcDbPk = fr.getTu().getSource().getDbPk();
			int tgtDbPk = fr.getTu().getTarget().getDbPk();

			String sql = "SELECT COUNT(*) FROM TEXTDATA WHERE GROUPID = " + tuDbPk + " AND TYPE = 'M'";
			Connection conn = dbOp.getConnection();
			if (conn == null || conn.isClosed()) {
				throw new Exception(Messages.getString("match.TmMatcher.deleteFuzzyResult.msg2"));
			}
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			stmt.close();
			if (count == 0) {
				throw new Exception(Messages.getString("match.TmMatcher.deleteFuzzyResult.msg3"));
			} else if (count > 2) {
				// 删除对应的语言
				dbOp.beginTransaction();
				dbOp.deleteAllTuvRelations(Arrays.asList(tgtDbPk), fr.getTu().getTarget().getLangCode());

				sql = "UPDATE MTU SET CHANGEID = '" + fr.getTu().getChangeUser() + "', CHANGEDATE = '"
						+ DateUtils.getTimestampFromUTC(fr.getTu().getChangeDate()) + "' WHERE MTUPKID = " + tuDbPk;
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				dbOp.commit();
			} else {
				// 删除整个 TU
				dbOp.beginTransaction();
				stmt = conn.createStatement();

				sql = "DELETE FROM MTU WHERE MTUPKID = " + tuDbPk;
				stmt.addBatch(sql);
				sql = "DELETE FROM MPROP WHERE PARENTNAME = 'TU' AND PARENTID = " + tuDbPk;
				stmt.addBatch(sql);
				sql = "DELETE FROM MNOTE WHERE PARENTNAME = 'TU' AND PARENTID = " + tuDbPk;
				stmt.addBatch(sql);
				sql = "DELETE FROM MEXTRA WHERE PARENTNAME = 'TU' AND PARENTID = " + +tuDbPk;
				stmt.executeBatch();

				dbOp.deleteAllTuvRelations(Arrays.asList(srcDbPk), fr.getTu().getSource().getLangCode());
				dbOp.deleteAllTuvRelations(Arrays.asList(tgtDbPk), fr.getTu().getTarget().getLangCode());

				dbOp.commit();
			}
		} catch (Exception e) {
			logger.error("delete TM in db error", e);
			try {
				dbOp.rollBack();
			} catch (SQLException e1) {
				logger.error("delete tm DB rollback error", e);
			}
			throw e;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error("delete TM stmt close error", e);
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.error("delete TM ResultSet close error", e);
				}
			}
			if (needEnd) {
				try {
					dbOp.end();
				} catch (SQLException e) {
					logger.error("delete TM  DB close conneciton error", e);
				}
			}
			if (needEnd) {
				try {
					if (dbOp != null) {
						dbOp.end();
					}
				} catch (SQLException e) {
					logger.error("delete TM  DB close conneciton error", e);
				}
			}			
		}
	}

	public void updateFuzzyResult(FuzzySearchResult fr) throws Exception {
		Object obj = fr.getDbOp();
		if (obj == null) {
			throw new Exception(Messages.getString("match.TmMatcher.updateFuzzyResult.msg1"));
		}
		DBOperator dbOp = (DBOperator) obj;
		boolean needEnd = false;
		PreparedStatement stmt = null;
		try {
			if (dbOp.isClosed()) {
				needEnd = true;
				dbOp.start();
			}
			int tuDbPk = fr.getTu().getTmId();
			int srcDbPk = fr.getTu().getSource().getDbPk();
			int tgtDbPk = fr.getTu().getTarget().getDbPk();
			Connection conn = dbOp.getConnection();
			if (conn == null || conn.isClosed()) {
				throw new Exception(Messages.getString("match.TmMatcher.updateFuzzyResult.msg2"));
			}
			dbOp.beginTransaction();
			dbOp.deleteAllTuvRelations(Arrays.asList(srcDbPk), fr.getTu().getSource().getLangCode());
			dbOp.deleteAllTuvRelations(Arrays.asList(tgtDbPk), fr.getTu().getTarget().getLangCode());
			TmxContexts contexts = fr.getTu().getContexts();
			saveTuv(dbOp, tuDbPk, fr.getTu().getSource(), contexts == null ? null : contexts.getPreContext(),
					contexts == null ? null : contexts.getNextContext());
			saveTuv(dbOp, tuDbPk, fr.getTu().getTarget(), null, null);

			String sql = "UPDATE MTU SET CHANGEID = ?, CHANGEDATE = ? WHERE MTUPKID = ? ";
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, fr.getTu().getChangeUser());
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(fr.getTu().getChangeDate()));
			stmt.setInt(i++, tuDbPk);
			stmt.executeUpdate();
			dbOp.commit();
		} catch (Exception e) {
			logger.error("delete TM in db error", e);
			try {
				dbOp.rollBack();
			} catch (SQLException e1) {
				logger.error("delete tm DB rollback error", e);
			}
			throw e;
		} finally {
			// 修复 Bug #3064 编辑匹配--更换记忆库后再编辑原记忆库匹配，出现异常.连接释放问题
			// sqlite 必须先要释放 Statement 然后再关闭连接

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error("delete TM stmt close error", e);
				}
			}
			if (needEnd) {
				try {
					if (dbOp != null) {
						dbOp.end();
					}
				} catch (SQLException e) {
					logger.error("delete TB  DB close conneciton error", e);
				}
			}
		}
	}

	private void saveTuv(DBOperator dbOp, int tuPk, TmxSegement tuv, String preContext, String nextContext)
			throws Exception {
		String pureText = tuv.getPureText();
		String hash = pureText == null ? null : pureText.hashCode() + "";
		try {
			dbOp.insertTextData("M", tuPk, hash, pureText, tuv.getFullText(), Utils.convertLangCode(tuv.getLangCode()),
					preContext == null ? "" : preContext, nextContext == null ? "" : nextContext);
		} catch (Exception e) {
			throw e;
		}
	}

	// public static void main(String[] arg) throws SQLException, ClassNotFoundException{
	// String driver = "com.mysql.jdbc.Driver";
	// String url = "jdbc:mysql://127.0.0.1:3306/test5";
	// String name = "root";
	// String pw = "root";
	//
	// Class.forName(driver);
	// Connection conn = DriverManager.getConnection(url,name,pw);
	//
	// TmxTU tu = new TmxTU();
	// tu.setTmId(1);
	// TmxSegement source = new TmxSegement();
	// source.setDbPk(1);
	// source.setLangCode("en-US");
	// TmxSegement target = new TmxSegement();
	// target.setDbPk(2);
	// target.setLangCode("zh-CN");
	// tu.setSource(source);
	// tu.setTarget(target);
	// FuzzySearchResult fr = new FuzzySearchResult(tu);
	//
	// TmMatcher tm = new TmMatcher();
	// tm.deleteFuzzyResult(fr, conn);
	// }

}
