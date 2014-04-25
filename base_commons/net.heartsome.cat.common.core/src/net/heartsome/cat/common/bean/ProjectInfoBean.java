/**
 * ProjectConfigBean.java
 *
 * Version information :
 *
 * Date:Nov 25, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.heartsome.cat.common.locale.Language;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ProjectInfoBean implements PropertyChangeListener {
	private String projectName;
	private LinkedHashMap<String, String> mapField;
	private LinkedHashMap<String, Object[]> mapAttr;

	private Language sourceLang;
	private List<Language> targetLang;

	private List<DatabaseModelBean> tmDb;
	private List<DatabaseModelBean> tbDb;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public ProjectInfoBean() {
		this.mapField = new LinkedHashMap<String, String>();
		this.mapAttr = new LinkedHashMap<String, Object[]>();
		this.targetLang = new ArrayList<Language>();

		this.tmDb = new ArrayList<DatabaseModelBean>();
		this.tbDb = new ArrayList<DatabaseModelBean>();
	}

	/** @return the projectName */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName
	 *            the projectName to set
	 */
	public void setProjectName(String projectName) {
		propertyChangeSupport.firePropertyChange("projectName", this.projectName, this.projectName = projectName);
	}

	public LinkedHashMap<String, String> getMapField() {
		return mapField;
	}

	public void setMapField(LinkedHashMap<String, String> mapField) {
		this.mapField = mapField;
//		propertyChangeSupport.firePropertyChange("mapField", this.mapField, this.mapField = mapField);
	}

	public LinkedHashMap<String, Object[]> getMapAttr() {
		return mapAttr;
	}

	public void setMapAttr(LinkedHashMap<String, Object[]> mapAttr) {
		this.mapAttr = mapAttr;
//		propertyChangeSupport.firePropertyChange("mapAttr", this.mapAttr, this.mapAttr = mapAttr);
	}

	/** @return the sourceLang */
	public Language getSourceLang() {
		return sourceLang;
	}

	/**
	 * @param sourceLang
	 *            the sourceLang to set
	 */
	public void setSourceLang(Language sourceLang) {
		propertyChangeSupport.firePropertyChange("sourceLang", this.sourceLang, this.sourceLang = sourceLang);
	}

	/** @return 返回目标语言集 */
	public List<Language> getTargetLang() {
		return targetLang;
	}

	/**
	 * @param targetLang
	 *            the targetLang to set
	 */
	public void setTargetLang(List<Language> targetLang) {
		propertyChangeSupport.firePropertyChange("sourceLang", this.sourceLang, this.targetLang = targetLang);
	}

	/** @return the tmDb */
	public List<DatabaseModelBean> getTmDb() {
		return tmDb;
	}

	/**
	 * @param tmDb
	 *            the tmDb to set
	 */
	public void setTmDb(List<DatabaseModelBean> tmDb) {
		this.tmDb = tmDb;
	}

	/** @return the tbDb */
	public List<DatabaseModelBean> getTbDb() {
		return tbDb;
	}

	/**
	 * @param tbDb
	 *            the tbDb to set
	 */
	public void setTbDb(List<DatabaseModelBean> tbDb) {
		this.tbDb = tbDb;
	}

	public void propertyChange(PropertyChangeEvent arg0) {

	}

	/**
	 * 添加数据Banding监听
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * 注销数据Banding监听
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
