package net.heartsome.cat.ts.ui.advanced.model;

/**
 * 高级菜单下分段规则印射规则的pojo类
 * @author robert 2012-03-02
 * @version
 * @since JDK1.6
 */
public class MapRuleBean {
	private String languageModel;
	private String langRuleName;
	
	public MapRuleBean (){}
	
	public MapRuleBean(String languageModel, String langRuleName){
		this.languageModel = languageModel;
		this.langRuleName = langRuleName;
	}

	public String getLanguageModel() {
		return languageModel;
	}

	public void setLanguageModel(String languageModel) {
		this.languageModel = languageModel;
	}

	public String getLangRuleName() {
		return langRuleName;
	}

	public void setLangRuleName(String langRuleName) {
		this.langRuleName = langRuleName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof MapRuleBean) {
			MapRuleBean targetBean = (MapRuleBean) obj;
			if (targetBean.getLanguageModel().equals(this.languageModel) && targetBean.getLangRuleName().equals(this.langRuleName)) {
				return true;
			}
		}
		return false;
	}
}
