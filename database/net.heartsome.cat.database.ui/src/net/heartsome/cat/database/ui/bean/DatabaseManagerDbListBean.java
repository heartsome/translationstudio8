/**
 * DatabaseManagerDbListBean.java
 *
 * Version information :
 *
 * Date:Dec 2, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class DatabaseManagerDbListBean implements PropertyChangeListener {
	private String index;
	private String dbName;
	private String langs;	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	/** @return the index */
	public String getIndex() {
		return index;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(String index) {
		propertyChangeSupport.firePropertyChange("index", this.index, this.index = index);
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
		propertyChangeSupport.firePropertyChange("dbName", this.dbName, this.dbName = dbName);
	}

	/** @return the langs ,所有code以逗号连接，可能在语言的前后包含有空格*/
	public String getLangs() {
		return langs;
	}

	/**
	 * @param langs
	 *            the langs to set
	 */
	public void setLangs(String langs) {
		propertyChangeSupport.firePropertyChange("langs", this.langs, this.langs = langs);
	}


	public void propertyChange(PropertyChangeEvent arg0) {
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

}
