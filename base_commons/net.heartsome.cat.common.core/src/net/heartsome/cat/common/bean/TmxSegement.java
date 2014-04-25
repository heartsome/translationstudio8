package net.heartsome.cat.common.bean;

/**
 * TUV
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmxSegement {

	private int dbPk;
	private String pureText;
	private String fullText;
	private String langCode;

	public TmxSegement() {
	};

	public TmxSegement(String pureText, String fullText, String langCode) {
		this.pureText = pureText;
		this.fullText = fullText;
		this.langCode = langCode;
	}

	/**
	 * Set the value of pureText
	 * @param newVar
	 *            the new value of pureText
	 */
	public void setPureText(String newVar) {
		pureText = newVar;
	}

	/**
	 * Get the value of pureText
	 * @return the value of pureText
	 */
	public String getPureText() {
		return pureText;
	}

	/**
	 * Set the value of fullText
	 * @param newVar
	 *            the new value of fullText
	 */
	public void setFullText(String newVar) {
		fullText = newVar;
	}

	/**
	 * Get the value of fullText
	 * @return the value of fullText
	 */
	public String getFullText() {
		return fullText;
	}

	/**
	 * Set the value of langCode
	 * @param newVar
	 *            the new value of langCode
	 */
	public void setLangCode(String newVar) {
		langCode = newVar;
	}

	/**
	 * Get the value of langCode
	 * @return the value of langCode
	 */
	public String getLangCode() {
		return langCode;
	}

	/** @return the dbPk */
	public int getDbPk() {
		return dbPk;
	}

	/**
	 * @param dbPk
	 *            the dbPk to set
	 */
	public void setDbPk(int dbPk) {
		this.dbPk = dbPk;
	}

}
