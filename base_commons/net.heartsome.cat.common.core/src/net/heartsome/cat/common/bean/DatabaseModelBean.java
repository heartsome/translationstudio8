/**
 * DataBaseModelBean.java
 *
 * Version information :
 *
 * Date:Oct 24, 2011
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

/**
 * 数据库管理模块中对应的
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class DatabaseModelBean implements PropertyChangeListener {
	/** 在列表中的编号 */
	private String id;

	/** 数据库类型 */
	private String dbType;

	/** 数据库名称 */
	private String dbName;

	/** 数据库实例（Oracle） */
	private String instance;

	/** 服务器 */
	private String host;

	/** 端口号 */
	private String port;

	/** 用户名 */
	private String userName;

	/** 密码 */
	private String password;

	/** 数据存放路径（internalDB） */
	private String itlDBLocation;

	/** 库中的内容是否适用于项目 */
	private boolean hasMatch;

	private boolean isDefault;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * constructor
	 */
	public DatabaseModelBean() {
		this.dbName = "";
		this.dbType = "";
		this.host = "";
		this.port = "";
		this.instance = "";
		this.itlDBLocation = "";
		this.password = "";
		this.userName = "";
		this.hasMatch = false;
		this.isDefault = false;
	}

	/** @return the id */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	/** @return the dbType */
	public String getDbType() {
		return dbType;
	}

	/**
	 * @param dbType
	 *            the dbType to set
	 */
	public void setDbType(String dbType) {
		propertyChangeSupport.firePropertyChange("dbType", this.dbType, this.dbType = dbType);
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

	/** @return the instance */
	public String getInstance() {
		return instance;
	}

	/**
	 * @param instance
	 *            the instance to set
	 */
	public void setInstance(String instance) {
		propertyChangeSupport.firePropertyChange("instance", this.instance, this.instance = instance);
	}

	/** @return the host */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		propertyChangeSupport.firePropertyChange("host", this.host, this.host = host);
	}

	/** @return the port */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(String port) {
		propertyChangeSupport.firePropertyChange("port", this.port, this.port = port);
	}

	/** @return the userName */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		propertyChangeSupport.firePropertyChange("userName", this.userName, this.userName = userName);
	}

	/** @return the password */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		propertyChangeSupport.firePropertyChange("password", this.password, this.password = password);
	}

	/** @return the itlDBLocation */
	public String getItlDBLocation() {
		return itlDBLocation;
	}

	/**
	 * @param itlDBLocation
	 *            the itlDBLocation to set
	 */
	public void setItlDBLocation(String itlDBLocation) {
		propertyChangeSupport.firePropertyChange("itlDBLocation", this.itlDBLocation,
				this.itlDBLocation = itlDBLocation);
	}

	/** @return the hasMatch */
	public boolean isHasMatch() {
		return hasMatch;
	}

	/**
	 * @param hasMatch
	 *            the hasMatch to set
	 */
	public void setHasMatch(boolean hasMatch) {
		propertyChangeSupport.firePropertyChange("itlDBLocation", this.itlDBLocation, this.hasMatch = hasMatch);
	}

	/** @return isDefault */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * 设置当前数据库是不是默认数据库
	 * @param isDefault
	 *            the default to set
	 */
	public void setDefault(boolean isDefault) {
		propertyChangeSupport.firePropertyChange("isDefault", this.isDefault, this.isDefault = isDefault);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {

	}
	
	/**
	 * 将当前DatabaseModelBean转化为MetaData
	 * @return {@link MetaData} 通过该MetaData获取数据库连接;
	 */
	public MetaData toDbMetaData() {
		MetaData metaData = new MetaData();
		metaData.setDbType(this.getDbType());
		metaData.setDatabaseName(this.getDbName());
		metaData.setDataPath(this.getItlDBLocation());
		metaData.setInstance(this.getInstance());
		metaData.setServerName(this.getHost());
		metaData.setPort(this.getPort());
		metaData.setUserName(this.getUserName());
		metaData.setPassword(this.getPassword());
		return metaData;
	}
	
	/**
	 * 将数据库元数据转化为当前实例
	 * @param metaData
	 * @return ;
	 */
	public void metaDatatToBean(MetaData metaData){
		this.setDbType(metaData.getDbType());
		this.setDbName(metaData.getDatabaseName());
		this.setInstance(metaData.getInstance());
		this.setHost(metaData.getServerName());
		this.setPort(metaData.getPort());
		this.setUserName(metaData.getUserName());
		this.setPassword(metaData.getPassword());
		this.setItlDBLocation(metaData.getDataPath());
	}
	
	/**
	 * 将当前实例的内容拷贝到a中.a中的内容将被覆盖
	 * @return ;
	 */
	public DatabaseModelBean copyToOtherIntance(DatabaseModelBean a){
		a.setId(this.getId());
		a.setDbType(this.getDbType());
		a.setDbName(this.getDbName());
		a.setInstance(this.getInstance());
		a.setHost(this.getHost());
		a.setPort(this.getPort());
		a.setUserName(this.getUserName());
		a.setPassword(this.getPassword());
		a.setItlDBLocation(this.getItlDBLocation());
		a.setHasMatch(this.isHasMatch());
		a.setDefault(this.isDefault);
		return a;
	}
}
