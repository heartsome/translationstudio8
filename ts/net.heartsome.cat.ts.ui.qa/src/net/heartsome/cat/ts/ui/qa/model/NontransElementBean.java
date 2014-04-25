package net.heartsome.cat.ts.ui.qa.model;

import java.util.List;
import java.util.Map;

/**
 * 非译元素的pojo类。	
 * @author robert	2012-06-13
 */
public class NontransElementBean {
	private String id;
	private String name;
	private String content;
	private String regular;
	
	public NontransElementBean(){}
	
	public NontransElementBean(String id, String name, String content, String regular){
		this.id = id;
		this.name = name;
		this.content = content;
		this.regular = regular;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRegular() {
		return regular;
	}

	public void setRegular(String regular) {
		this.regular = regular;
	}


	/**
	 * <div style='color:red'>此方法是重写父类的方法，只用于 listViewer 的 refresh() 方法使用</div>
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NontransElementBean) {
			NontransElementBean objBean = (NontransElementBean) obj;
			if (id.equals(objBean.getId()) && name.equals(objBean.getName()) && content.equals(objBean.getContent()) && regular.equals(objBean.getRegular())) {
				return true;	// id.equals(objBean.getId())
			}
		}
		return false;
	}
	
	/**
	 * <div style='color:red'>id不一样，其他都一样时，标识为重复，若 都一样，则标记为同一个元素，此方法是用于编辑元素时所用</div>
	 */
	public boolean equalsOfEdit(Object obj) {
		if (obj instanceof NontransElementBean) {
			NontransElementBean objBean = (NontransElementBean) obj;
			if (!id.equals(objBean.getId()) && name.equals(objBean.getName()) && content.equals(objBean.getContent()) && regular.equals(objBean.getRegular())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <div style='color:red'>除id以外，其他都一样时，标识为重复，此方法是用于初始化界面检查非译元素重复时所用，见 NonTranslationQA 类中的 findAllRepeatElement() 方法</div>
	 * @param obj
	 * @return
	 */
	public boolean equalsOfRepeatCheck(Object obj) {
		if (obj instanceof NontransElementBean) {
			NontransElementBean objBean = (NontransElementBean) obj;
			if (name.equals(objBean.getName()) && content.equals(objBean.getContent()) && regular.equals(objBean.getRegular())) {
				return true;
			}
		}
		return false;
	}
}
