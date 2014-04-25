package net.heartsome.cat.common.bean;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class FuzzySearchResult {

	private TmxTU tu;
	private String dbName;
	private int similarity;
	
	private Object dbOp;
	
	public FuzzySearchResult(TmxTU tu) {
		this.tu = tu;
	}
	
	public void setDbOp(Object dbOp){
		this.dbOp = dbOp;
	}
	
	public Object getDbOp(){
		return this.dbOp;
	}

	/**
	 * Set the value of tu
	 * @param newVar
	 *            the new value of tu
	 */
	public void setTu(TmxTU newVar) {
		tu = newVar;
	}

	/**
	 * Get the value of tu
	 * @return the value of tu
	 */
	public TmxTU getTu() {
		return tu;
	}

	/**
	 * Set the value of dbName
	 * @param newVar
	 *            the new value of dbName
	 */
	public void setDbName(String newVar) {
		dbName = newVar;
	}

	/**
	 * Get the value of dbName
	 * @return the value of dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * Set the value of similarity
	 * @param newVar
	 *            the new value of similarity
	 */
	public void setSimilarity(int newVar) {
		similarity = newVar;
	}

	/**
	 * Get the value of similarity
	 * @return the value of similarity
	 */
	public int getSimilarity() {
		return similarity;
	}

}
