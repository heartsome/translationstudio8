package net.heartsome.cat.common.bean;


/**
 * 存放数据库配置的元数据
 * @author  terry
 * @version 
 * @since   JDK1.6
 */
public class MetaData implements Cloneable {

	/**  数据库类型 */
	protected String dbType;

	/**  数据库名称 */
	protected String databaseName = "";

	/**  数据库实例（Oracle） */
	protected String instance = "";

	/**  服务器名 */
	protected String serverName = "";

	/**  端口号 */
	protected String port = "";

	/**  用户名 */
	protected String userName = "";

	/**  密码 */
	protected String password = "";

	/**  优化策略 */
	protected String optimize = "speed";

	/**  数据存放路径（internalDB） */
	protected String dataPath = "";

	/**  是否是术语库 */
	protected boolean isTB = true;

	/** 是否是记忆库 */
	protected boolean isTM = true;

	/**
	 * 获取数据库名称
	 * @return ;
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * 设置数据库名称
	 * @param databaseName ;
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * 是否支持数据库名称
	 * @return ;
	 */
	public boolean databaseNameSupported() {
		return true;
	}

	/**
	 * 取得数据库实例名称
	 * @return ;
	 */
	public String getInstance() {
		return instance;
	}

	/**
	 * 设置数据库实例名称
	 * @param instance ;
	 */
	public void setInstance(String instance) {
		this.instance = instance;
	}

	/**
	 * 是否支持使用数据库实例名称
	 * @return ;
	 */
	public boolean instanceSupported() {
		return true;
	}

	/**
	 * 取得服务器名称
	 * @return ;
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * 设置服务器名称
	 * @param serverName ;
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * 是否支持使用服务器名称
	 * @return ;
	 */
	public boolean serverNameSupported() {
		return true;
	}

	/**
	 * 取得端口号
	 * @return ;
	 */
	public String getPort() {
		return port;
	}

	/**
	 * 设置端口号
	 * @param port ;
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * 是否支持使用端口号
	 * @return ;
	 */
	public boolean portSupported() {
		return true;
	}

	/**
	 * 取得用户名
	 * @return ;
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * 设置用户名
	 * @param userName ;
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * 是否支持使用用户名
	 * @return ;
	 */
	public boolean userNameSupported() {
		return true;
	}

	/**
	 * 取得密码
	 * @return ;
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 设置密码
	 * @param password ;
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 是否支持使用密码
	 * @return ;
	 */
	public boolean passwordSupported() {
		return true;
	}

	/**
	 * 取得优化策略
	 * @return ;
	 */
	public String getOptimize() {
		return optimize;
	}

/*	*//**
	 * 设置优化策略
	 * @param optimize ;
	 *//*
	public void setOptimize(String optimize) {
		this.optimize = optimize;
	}*/

	/**
	 * 取得数据库文件路径
	 * @return ;
	 */
	public String getDataPath() {
		return dataPath;
	}

	/**
	 * 设置数据库文件路径
	 * @param dataPath ;
	 */
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	/**
	 * 是否支持使用数据库文件路径
	 * @return ;
	 */
	public boolean dataPathSupported() {
		return true;
	}

	/**
	 * 取得数据库类型
	 * @return ;
	 */
	public String getDbType() {
		return dbType;
	}

	/**
	 * 设置数据库类型
	 * @param dbType ;
	 */
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	/**
	 * 取得是否是术语库
	 * @return ;
	 */
	public boolean isTB() {
		return isTB;
	}

	/**
	 * 设置是否是术语库
	 * @param isTB ;
	 */
	public void setTB(boolean isTB) {
		this.isTB = isTB;
	}

	/**
	 * 取得是否是记忆库
	 * @return ;
	 */
	public boolean isTM() {
		return isTM;
	}

	/**
	 * 设置是否是记忆库
	 * @param isTM ;
	 */
	public void setTM(boolean isTM) {
		this.isTM = isTM;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
