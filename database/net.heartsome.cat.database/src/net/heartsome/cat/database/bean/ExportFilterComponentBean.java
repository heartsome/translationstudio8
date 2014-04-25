/**
 * ExportFilterBean.java
 *
 * Version information :
 *
 * Date:Feb 15, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.bean;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.heartsome.cat.database.resource.Messages;

/**
 * 导出过滤器基础数据，对应设置过滤条件组件
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportFilterComponentBean {

	private String optionName; // 过滤条件名称

	private String expression; // 过滤条件可用表达式

	/** key-过滤条件名称;value-过滤条件表达式名称 */
	private LinkedHashMap<String, String> filterContent; // 过滤条件基础数据,初始化组件

	private String filterVlaue; // 过滤的内容

	private Map<String, String> matchDb;

	private Map<String, String> matchDbOp;

	/**
	 * 构造器
	 * @param ruleType
	 *            用于指定是TMX还是TBX
	 */
	public ExportFilterComponentBean(String ruleType) {
		filterContent = new LinkedHashMap<String, String>();
		if (ruleType.equals("TMX")) {
			filterContent.put(
					Messages.getString("bean.ExportFilterComponentBean.segment"),
					Messages.getString("bean.ExportFilterComponentBean.contain") + ","
							+ Messages.getString("bean.ExportFilterComponentBean.notcontain"));
			filterContent.put(
					Messages.getString("bean.ExportFilterComponentBean.creationDate"),
					Messages.getString("bean.ExportFilterComponentBean.gt") + ","
							+ Messages.getString("bean.ExportFilterComponentBean.lt"));
			filterContent.put(
					Messages.getString("bean.ExportFilterComponentBean.changeDate"),
					Messages.getString("bean.ExportFilterComponentBean.gt") + ","
							+ Messages.getString("bean.ExportFilterComponentBean.lt"));
			filterContent.put(Messages.getString("bean.ExportFilterComponentBean.creationId"),
					Messages.getString("bean.ExportFilterComponentBean.eq"));
			filterContent.put(Messages.getString("bean.ExportFilterComponentBean.changeId"),
					Messages.getString("bean.ExportFilterComponentBean.eq"));
//			filterContent.put(Messages.getString("bean.ExportFilterComponentBean.projectRef"),
//					Messages.getString("bean.ExportFilterComponentBean.contain"));
//			filterContent.put(Messages.getString("bean.ExportFilterComponentBean.jobRef"),
//					Messages.getString("bean.ExportFilterComponentBean.contain"));
			filterContent.put(Messages.getString("bean.ExportFilterComponentBean.noteRef"),
					Messages.getString("bean.ExportFilterComponentBean.contain"));

			this.optionName = Messages.getString("bean.ExportFilterComponentBean.segment");
			this.expression = Messages.getString("bean.ExportFilterComponentBean.contain");

			matchDb = new HashMap<String, String>();
			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.creationDate"), "CREATIONDATE");
			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.changeDate"), "CHANGEDATE");
			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.creationId"), "CREATIONID");
			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.changeId"), "CHANGEID");
//			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.projectRef"), "PROJECTREF");
//			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.jobRef"), "JOBREF");
			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.segment"), "PURE");
			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.noteRef"), "CONTENT");
		} else {
			filterContent.put(Messages.getString("bean.ExportFilterComponentBean.tbContent"),
					Messages.getString("bean.ExportFilterComponentBean.contain"));

			this.optionName = Messages.getString("bean.ExportFilterComponentBean.tbContent");
			this.expression = Messages.getString("bean.ExportFilterComponentBean.contain");

			matchDb = new HashMap<String, String>();
			matchDb.put(Messages.getString("bean.ExportFilterComponentBean.tbContent"), "PURE");
		}

		matchDbOp = new HashMap<String, String>();
		matchDbOp.put(Messages.getString("bean.ExportFilterComponentBean.eq"), "=");
		matchDbOp.put(Messages.getString("bean.ExportFilterComponentBean.gt"), ">");
		matchDbOp.put(Messages.getString("bean.ExportFilterComponentBean.lt"), "<");
		matchDbOp.put(Messages.getString("bean.ExportFilterComponentBean.contain"), "like");
		matchDbOp.put(Messages.getString("bean.ExportFilterComponentBean.notcontain"), "not like");
	}

	/**
	 * 获取过滤条件的表达式
	 * @param filterName
	 *            过滤条件名称
	 * @return 过滤条件的表达式名称;
	 */
	public String[] getExpressionsByName(String filterName) {
		return filterContent.get(filterName).split(",");
	}

	/**
	 * 获取当前过滤条件的所有表达式
	 * @return ;
	 */
	public String[] getCurrentFilterExpressions() {
		return filterContent.get(this.optionName).split(",");
	}

	/**
	 * 获取所有过滤条件的名称
	 * @return ;
	 */
	public String[] getFilterNames() {
		Set<String> set = filterContent.keySet();
		return set.toArray(new String[set.size()]);
	}

	/** @return the optionName */
	public String getOptionName() {
		return optionName;
	}

	public String getMatchDbField() {
		return matchDb.get(optionName);
	}

	/**
	 * @param optionName
	 *            the optionName to set
	 */
	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}

	/** @return 当前过滤条件表达式 */
	public String getCurrentExpression() {
		return expression;
	}

	public String getExpressionMatchDb() {
		return matchDbOp.get(expression);
	}

	/**
	 * @param 当前过滤条件表达式名称
	 *            the expression to set
	 */
	public void setCurrentExpression(String expression) {
		this.expression = expression;
	}

	/** @return the filterContent */
	public LinkedHashMap<String, String> getFilterContent() {
		return filterContent;
	}

	/**
	 * @param filterContent
	 *            the filterContent to set
	 */
	public void setFilterContent(LinkedHashMap<String, String> filterContent) {
		this.filterContent = filterContent;
	}

	/** @return the filterVlaue */
	public String getFilterVlaue() {
		return filterVlaue;
	}

	/**
	 * @param filterVlaue
	 *            the filterVlaue to set
	 */
	public void setFilterVlaue(String filterVlaue) {
		this.filterVlaue = filterVlaue;
	}

}
