package net.heartsome.cat.database;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.heartsome.cat.common.bean.MetaData;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 操作数据库配置文件的数据
 * @author  terry
 * @version 
 * @since   JDK1.6
 */
public class DBConfig {

	private Logger logger = LoggerFactory.getLogger(DBConfig.class);
	private Element root;
	private Element defaultRoot;
	private MetaData metaData;
	
	public DBConfig(URL dbconfig, MetaData metaData){
		this.metaData = metaData;
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(dbconfig);
			root = doc.getRootElement();
			
			Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
			URL defaultUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
			SAXReader defaultReader = new SAXReader();
			Document defaultDoc = defaultReader.read(defaultUrl);
			defaultRoot = defaultDoc.getRootElement();
		} catch (DocumentException e) {
			logger.warn("", e);
		}
	}
	
	/**
	 * 构造函数，读取数据库配置文件信息并初始化
	 * @param dbconfig
	 */
	public DBConfig(URL dbconfig) {
		this(dbconfig, null);
	}

	/**
	 * 取得默认的数据库服务器
	 * @return
	 */
	public String getDefaultServer() {
		return getDefaultProperty("server");
	}

	/**
	 * 取得默认的数据库种类名称
	 * @return
	 */
	public String getDefaultType() {
		return getDefaultProperty("type");
	}

	/**
	 * 取得默认的数据库端口号
	 * @return
	 */
	public String getDefaultPort() {
		return getDefaultProperty("port");
	}

	/**
	 * 取得默认的配置属性
	 * @param name 配置的名称。
	 * @return ;
	 */
	private String getDefaultProperty(String name) {
		Element dfElement = getElementWithDefault("default");
		if (dfElement == null) {
			return "";
		}
		@SuppressWarnings("unchecked")
		List<Element> propertyElement = dfElement.elements("property");
		for (Element i : propertyElement) {
			if (name.equals(i.attribute("name").getText())) {
				return i.getText();
			}
		}
		return "";
	}

	/**
	 * 取得连接数据库 URL
	 * @return
	 */
	public String getDbURL() {
		Element urlElement = getElementWithDefault("url");
		if(urlElement == null){
			return null;
		}
		return Utils.replaceParams(urlElement.getTextTrim(), metaData);
	}

	/**
	 * 取得数据库连接配置属性
	 * @return
	 */
	public HashMap<String, String> getConfigProperty() {
		HashMap<String, String> result = new HashMap<String, String>();
		Element propertyElement = getElementWithDefault("config-property");
		if (propertyElement == null) {
			return result;
		}
		@SuppressWarnings("unchecked")
		List<Element> properties = propertyElement.elements("property");
		for (Element i : properties) {
			result.put(i.attributeValue("name"), Utils.replaceParams(i.getTextTrim(), metaData));
		}
		return result;
	}

	/**
	 * 取得创建数据库的语句
	 * @return
	 */
	public String getCreateDb() {
		Element createDbElement = getElementWithDefault("create-database");
		if(createDbElement == null){
			return null;
		}
		return Utils.replaceParams(createDbElement.getTextTrim(), metaData);
	}

	/**
	 * 取得删除数据库的语句
	 * @return
	 */
	public String getDropDb() {
		Element dropDbElement = getElementWithDefault("drop-database");
		if(dropDbElement == null){
			return null;
		}
		return Utils.replaceParams(dropDbElement.getTextTrim(), metaData);
	}
	
	/**
	 * 取得创建数据库表格的 SQL 语句
	 * @return
	 */
	public List<String> getCreateTables() {
		List<String> result = new ArrayList<String>();
		Element createTableElement = getElementWithDefault("create-tables");
		if (createTableElement == null) {
			return result;
		}
		@SuppressWarnings("unchecked")
		List<Element> createTables = createTableElement.elements("step");
		for (Element i : createTables) {
			result.add(Utils.replaceParams(i.getText(), metaData));
		}
		return result;
	}
	
	/**
	 * 获取创建索引的SQL语句
	 * @return ;
	 */
	public List<String> getCreateIndexs(){
		List<String> result = new ArrayList<String>();
		Element createTableElement = getElementWithDefault("create-index");
		if (createTableElement == null) {
			return result;
		}
		@SuppressWarnings("unchecked")
		List<Element> createTables = createTableElement.elements("step");
		for (Element i : createTables) {
			result.add(Utils.replaceParams(i.getText(), metaData));
		}
		return result;
	}

	/**
	 * 取得数据库连接驱动
	 * @return
	 */
	public String getDriver() {
		Element driverElement = getElementWithDefault("driver");
		if (driverElement == null) {
			return null;
		}
		return Utils.replaceParams(driverElement.getTextTrim(),metaData);
	}

	/**
	 * 取得系统库中存储的数据库列表
	 * @return
	 */
	public String getSysDbList() {
		Element e = getElementWithDefault("get-dblist");
		if(e == null){
			return null;
		}
		return Utils.replaceParams(e.getText(), metaData);
	}

	/**
	 * 取得插入系统库的 SQL 语句
	 * @return
	 */
	public String getInsertSysDb() {
		Element e = getElementWithDefault("insert-db");
		if(e == null){
			return null;
		}
		return Utils.replaceParams(e.getText(), metaData);
	}

	/**
	 * 取得创建系统库的 SQL 语句
	 * @return
	 */
	public String getCreateSysDb() {
		Element e = getElementWithDefault("create-database");
		if(e == null){
			return null;
		}
		return Utils.replaceParams(e.getText(), metaData);
	}

	/**
	 * 取得创建系统库表格的 SQL 语句
	 * @return
	 */
	public List<String> getCreateSysTables() {
		List<String> result = new ArrayList<String>();
		Element createTableElement = getElementWithDefault("create-hssystables");
		if (createTableElement == null) {
			return result;
		}
		@SuppressWarnings("unchecked")
		List<Element> createTables = createTableElement.elements("step");
		for (Element i : createTables) {
			result.add(Utils.replaceParams(i.getText(), metaData));
		}
		return result;
	}

	/**
	 * 取得删除系统库中记录的 SQL 语句
	 * @return
	 */
	public String getRemoveSysDb() {
		Element e = getElementWithDefault("remove-db");
		if(e == null){
			return null;
		}
		return Utils.replaceParams(e.getText(), metaData);
	}

	public List<String> getCreateMatrixTables(){
		return getCreateMatrix("create-table");
	}
	
	public List<String> getCreateMatrixIndexes(){
		return getCreateMatrix("create-index");
	}
	
	private List<String> getCreateMatrix(String elementName){
		List<String> result = new ArrayList<String>();
		Element matrixElement = getElementWithDefault("matrix-operation");
		if(matrixElement == null){
			return result;
		}
		@SuppressWarnings("unchecked")
		List<Element> elements = matrixElement.element(elementName).elements();
		if(elements == null){
			return result;
		}
		for(Element i : elements){
			result.add(Utils.replaceParams(i.getText(), metaData));
		}
		return result;
	}
	
	/**
	 * 取得操作 Matrix 表的 SQL 语句
	 * @param key
	 * @return ;
	 */
	public String getMatrixSQL(String key){
		Element e = getElementWithDefault("matrix-operation");
		if(e == null){
			return null;
		}
		return Utils.replaceParams(e.elementText(key),metaData);
	}
	
	/**
	 * 取得操作翻译库(记忆库，术语库)的 SQL 语句
	 * @param key
	 * @return
	 */
	public String getOperateDbSQL(String key) {
		if (root == null) {
			if(defaultRoot == null){
				return null;
			}
			else{
				return Utils.replaceParams(defaultRoot.element("operate-db").elementText(key), metaData);
			}
		}
		Element e = root.element("operate-db");
		if(e == null){
			if(defaultRoot == null){
				return null;
			}
			else{
				return Utils.replaceParams(defaultRoot.element("operate-db").elementText(key), metaData);
			}
		}
		else{
			Element e2 = e.element(key);
			if(e2 == null){
				if(defaultRoot == null){
					return null;
				}
				else{
					return Utils.replaceParams(defaultRoot.element("operate-db").elementText(key), metaData);
				}
			}
			else{
				return Utils.replaceParams(e2.getText(), metaData);
			}
		}
	}
	
	private Element getElementWithDefault(String name){
		if(root == null){
			if(defaultRoot == null){
				return null;
			}
			Element e = defaultRoot.element(name);
			if(e == null){
				return null;
			}
			else{
				return e;
			}
		}
		Element e = root.element(name);
		if(e == null){
			if(defaultRoot == null){
				return null;
			}
			else{
				e = defaultRoot.element(name);
			}
		}
		return e;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}
}
