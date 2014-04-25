/**
 * ExportDatabaseBean.java
 *
 * Version information :
 *
 * Date:Feb 14, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.bean;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.MetaData;

/**
 * 导出TMX/TBX时对数据库及导出参数的封装
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportDatabaseBean {

	private String index;

	/** 数据库参数 */
	private MetaData dbBean;

	/** 数据库已有语言 */
	private String existLangs;

	/** 　需要导出的语言　 */
	private List<String> hasSelectedLangs;

	/** 源语言 */
	private String srcLang;

	private String exportFilePath;

	/** 可供选择的源语言，根据选择的需要导出的语言定下来的 */
	private List<String> canSelSrcLangs;

	public ExportDatabaseBean(MetaData db, String existLangs) {
		this.dbBean = db;
		this.existLangs = existLangs;
		this.hasSelectedLangs = new ArrayList<String>();
		this.canSelSrcLangs = new ArrayList<String>();
		String[] arrayLang = existLangs.split(",");
		for (int i = 0; i < arrayLang.length; i++) {
			hasSelectedLangs.add(arrayLang[i]);
			canSelSrcLangs.add(arrayLang[i]);
		}
		this.srcLang = "";
	}

	/** @return the index */
	public String getIndex() {
		return index;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(String index) {
		this.index = index;
	}

	/** @return the dbBean */
	public MetaData getDbBean() {
		return dbBean;
	}

	/**
	 * @param dbBean
	 *            the dbBean to set
	 */
	public void setDbBean(MetaData dbBean) {
		this.dbBean = dbBean;
	}

	/** @return the existLangs */
	public String getExistLangs() {
		return existLangs;
	}

	/**
	 * @param existLangs
	 *            the existLangs to set
	 */
	public void setExistLangs(String existLangs) {
		this.existLangs = existLangs;
	}

	/** @return the sourceLangs */
	public List<String> getCanSelSrcLangs() {
		return canSelSrcLangs;
	}

	/**
	 * @param sourceLangs
	 *            the sourceLangs to set
	 */
	public void setCanSelSrcLangs(List<String> sourceLangs) {
		this.canSelSrcLangs = sourceLangs;
	}

	/** @return the hasSelectedLangs */
	public List<String> getHasSelectedLangs() {
		return hasSelectedLangs;
	}

	/**
	 * @param hasSelectedLangs
	 *            the hasSelectedLangs to set
	 */
	public void setHasSelectedLangs(List<String> hasSelectedLangs) {
		this.hasSelectedLangs = hasSelectedLangs;
	}

	/** @return the srcLangs */
	public String getSrcLang() {
		return srcLang;
	}

	/**
	 * @param srcLangs
	 *            the srcLangs to set
	 */
	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}

	/** @return the exportFilePath */
	public String getExportFilePath() {
		return exportFilePath;
	}

	/**
	 * @param exportFilePath
	 *            the exportFilePath to set
	 */
	public void setExportFilePath(String exportFilePath) {
		this.exportFilePath = exportFilePath;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ExportDatabaseBean) {
			ExportDatabaseBean bean = (ExportDatabaseBean) o;
			MetaData b = bean.getDbBean();
			MetaData a = this.getDbBean();
			String dbname = a.getDatabaseName();
			String host = b.getServerName();
			String port = a.getPort();
			String instance = a.getInstance();
			String localPath = a.getDataPath();
			if (b.getDatabaseName().equals(dbname) && b.getServerName().equals(host)
					&& b.getDataPath().equals(localPath) && b.getPort().equals(port)
					&& b.getInstance().equals(instance)) {
				return true;
			}
		}
		return super.equals(o);
	}

}
