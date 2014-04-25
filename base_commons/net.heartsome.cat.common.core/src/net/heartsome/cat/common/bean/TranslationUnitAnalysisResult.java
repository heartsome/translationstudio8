/**
 * TranslationUnitAnalysResult.java
 *
 * Version information :
 *
 * Date:2012-12-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.bean;

/**
 * Translation Unit Translation memory analys result
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TranslationUnitAnalysisResult {
	private int similarity;
	private String dbName;

	public TranslationUnitAnalysisResult() {
		this(0, null);
	}

	public TranslationUnitAnalysisResult(int similarity){
		this(similarity, null);
	}
	
	public TranslationUnitAnalysisResult(int similarity, String dbName) {
		this.similarity = similarity;
		this.dbName = dbName;
	}

	/** @return the similarity */
	public int getSimilarity() {
		return similarity;
	}

	/**
	 * @param similarity
	 *            the similarity to set
	 */
	public void setSimilarity(int similarity) {
		this.similarity = similarity;
	}

	/** @return the dbName */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName
	 *            the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

}
