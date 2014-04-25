/**
 * ExportFilterBean.java
 *
 * Version information :
 *
 * Date:Feb 16, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.bean;

import java.util.List;

/**
 * 导出过滤条件
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportFilterBean {

	private String filterName;
	private List<ExportFilterComponentBean> filterOption;
	private String filterConnector;
	private String filterType;

	
	/** @return the filterName */
	public String getFilterName() {
		return filterName;
	}

	/**
	 * @param filterName
	 *            the filterName to set
	 */
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	/** @return the filterOption */
	public List<ExportFilterComponentBean> getFilterOption() {
		return filterOption;
	}

	/**
	 * @param filterOption
	 *            the filterOption to set
	 */
	public void setFilterOption(List<ExportFilterComponentBean> filterOption) {
		this.filterOption = filterOption;
	}

	/** @return the filterConnector "AND" or "OR"*/
	public String getFilterConnector() {
		return filterConnector;
	}

	/** @param filterConnector the filterConnector to set */
	public void setFilterConnector(String filterConnector) {
		this.filterConnector = filterConnector;
	}

	/** @return the filterType "TMX" or "TBX"*/
	public String getFilterType() {
		return filterType;
	}

	/** @param filterType the filterType to set */
	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}
	
	
}
