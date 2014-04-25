package net.heartsome.cat.database.tmx;

import java.util.List;

import net.heartsome.cat.common.bean.TmxProp;

/**
 * 执行相关搜索时需要用到的 Bean
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ConcordanceBean {

	/** 对应 MTU 表的 MTUPKID */
	private Integer id;
	
	/** 对应 MTU 表的 CREATIONID(创建者) */
	private String creationId;
	
	/** 对应 MTU 表的 CREATIONDATE(创建时间) */
	private String creationDate;
	
	/** 对应 MTU 表的 CHANGEID(更新者) */
	private String changeId;
	
	/** 对应 MTU 表的 CHANGEDATE(更新时间) */
	private String changeDate;
	
	/** 是否带标记（指&lt;prop type='x-flag'&gt;HS-Flag&lt;/prop&gt;），对应 MPROP 表 */
	private boolean blnIsFlag;
	
	private List<LanguageTMX> languageList;
	
	/** 自定义属性集合 */
	private List<TmxProp	> attributeList;
	
	/**
	 * 构造方法
	 */
	public ConcordanceBean() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCreationId() {
		return creationId;
	}

	public void setCreationId(String creationId) {
		this.creationId = creationId;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getChangeId() {
		return changeId;
	}

	public void setChangeId(String changeId) {
		this.changeId = changeId;
	}

	public String getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(String changeDate) {
		this.changeDate = changeDate;
	}
	
	public List<LanguageTMX> getLanguageList() {
		return languageList;
	}

	public void setLanguageList(List<LanguageTMX> languageList) {
		this.languageList = languageList;
	}

	public boolean isBlnIsFlag() {
		return blnIsFlag;
	}

	public void setBlnIsFlag(boolean blnIsFlag) {
		this.blnIsFlag = blnIsFlag;
	}

	public List<TmxProp> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<TmxProp> attributeList) {
		this.attributeList = attributeList;
	}
	
}
