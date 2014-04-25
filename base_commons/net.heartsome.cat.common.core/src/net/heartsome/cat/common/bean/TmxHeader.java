/**
 * TmxHeader.java
 *
 * Version information :
 *
 * Date:2013-1-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.bean;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxHeader {
	private String adminlang;
	private String changedate;
	private String changeid;
	private String creationdate;
	private String creationid;
	private String creationtool;
	private String creationtoolversion;
	private String datatype;
	/** container of Prop,Note,Ude (XML Element) */
	private Object[] items;
	private String oencoding;
	private String otmf;
	private String segtype;
	private String srclang;

	/** @return the adminlang */
	public String getAdminlang() {
		return adminlang;
	}

	/**
	 * @param adminlang
	 *            the adminlang to set
	 */
	public void setAdminlang(String adminlang) {
		this.adminlang = adminlang;
	}

	/** @return the changedate */
	public String getChangedate() {
		return changedate;
	}

	/**
	 * @param changedate
	 *            the changedate to set
	 */
	public void setChangedate(String changedate) {
		this.changedate = changedate;
	}

	/** @return the changeid */
	public String getChangeid() {
		return changeid;
	}

	/**
	 * @param changeid
	 *            the changeid to set
	 */
	public void setChangeid(String changeid) {
		this.changeid = changeid;
	}

	/** @return the creationdate */
	public String getCreationdate() {
		return creationdate;
	}

	/**
	 * @param creationdate
	 *            the creationdate to set
	 */
	public void setCreationdate(String creationdate) {
		this.creationdate = creationdate;
	}

	/** @return the creationid */
	public String getCreationid() {
		return creationid;
	}

	/**
	 * @param creationid
	 *            the creationid to set
	 */
	public void setCreationid(String creationid) {
		this.creationid = creationid;
	}

	/** @return the creationtool */
	public String getCreationtool() {
		return creationtool;
	}

	/**
	 * @param creationtool
	 *            the creationtool to set
	 */
	public void setCreationtool(String creationtool) {
		this.creationtool = creationtool;
	}

	/** @return the creationtoolversion */
	public String getCreationtoolversion() {
		return creationtoolversion;
	}

	/**
	 * @param creationtoolversion
	 *            the creationtoolversion to set
	 */
	public void setCreationtoolversion(String creationtoolversion) {
		this.creationtoolversion = creationtoolversion;
	}

	/** @return the datatype */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * @param datatype
	 *            the datatype to set
	 */
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	/** @return the items */
	public Object[] getItems() {
		return items;
	}

	/**
	 * @param items
	 *            the items to set
	 */
	public void setItems(Object[] items) {
		this.items = items;
	}

	/** @return the oencoding */
	public String getOencoding() {
		return oencoding;
	}

	/**
	 * @param oencoding
	 *            the oencoding to set
	 */
	public void setOencoding(String oencoding) {
		this.oencoding = oencoding;
	}

	/** @return the otmf */
	public String getOtmf() {
		return otmf;
	}

	/**
	 * @param otmf
	 *            the otmf to set
	 */
	public void setOtmf(String otmf) {
		this.otmf = otmf;
	}

	/** @return the segtype */
	public String getSegtype() {
		return segtype;
	}

	/**
	 * @param segtype
	 *            the segtype to set
	 */
	public void setSegtype(String segtype) {
		this.segtype = segtype;
	}

	/** @return the srclang */
	public String getSrclang() {
		return srclang;
	}

	/**
	 * @param srclang
	 *            the srclang to set
	 */
	public void setSrclang(String srclang) {
		this.srclang = srclang;
	}
}
