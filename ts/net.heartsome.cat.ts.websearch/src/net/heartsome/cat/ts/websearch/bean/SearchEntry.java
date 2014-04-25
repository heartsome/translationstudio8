/**
 * SearchEntry.java
 *
 * Version information :
 *
 * Date:2013-9-22
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.websearch.bean;


/**
 * 搜索设置的配置信息
 * @author  yule
 * @version 
 * @since   JDK1.6
 */
public class SearchEntry {


	/**
	 * 关键字替换字符
	 */
	public  final static String KEY_WORD = "\\[\\{\\}\\]";
	/**
	 * 搜索名字
	 */
	private  String searchName;
	/**
	 * 搜索url配置
	 */
	private String searchUrl;
	/**
	 * 搜索是否选中
	 */
	private boolean isChecked;
     /**
      * 搜索图标	,没有使用.图片改为自动从网站获取
      */
	private String imagePath;
	
	/** @return the searchName */
	
	private boolean isDefault;
	
	private String id ;

	/** @return the id */
	public String getId() {
		if(null == id){
			id =""+System.currentTimeMillis();
		}
		return id;
	}

	/** @param id the id to set */
	public void setId(String id) {
		this.id = id;
	}

	/** @return the isDefault */
	public boolean isDefault() {
		return isDefault;
	}
	
	/** @param isDefault the isDefault to set */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public String getSearchName() {
		return searchName;
	}
	/** @param searchName the searchName to set */
	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}
	/** @return the searchUrl */
	public String getSearchUrl() {
		return searchUrl;
	}
	/** @param searchUrl the searchUrl to set */
	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}
	/** @return the isChecked */
	public boolean isChecked() {
		return isChecked;
	}
	/** @param isChecked the isChecked to set */
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	/** @return the imagePath */
	public String getImagePath() {
		return imagePath;
	}
	/** @param imagePath the imagePath to set */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
}
