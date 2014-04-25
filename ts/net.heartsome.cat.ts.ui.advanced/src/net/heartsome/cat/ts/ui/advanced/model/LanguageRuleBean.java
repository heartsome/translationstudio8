package net.heartsome.cat.ts.ui.advanced.model;

public class LanguageRuleBean {
	private String isBreak;
	private String preBreak;
	private String afterBreak;
	
	public LanguageRuleBean(){}

	public LanguageRuleBean(String isBreak, String preBreak, String afterBreak){
		this.isBreak = isBreak;
		this.preBreak = preBreak;
		this.afterBreak = afterBreak;
	}

	public String getIsBreak() {
		return isBreak;
	}

	public void setIsBreak(String isBreak) {
		this.isBreak = isBreak;
	}

	public String getPreBreak() {
		return preBreak;
	}

	public void setPreBreak(String preBreak) {
		this.preBreak = preBreak;
	}

	public String getAfterBreak() {
		return afterBreak;
	}

	public void setAfterBreak(String afterBreak) {
		this.afterBreak = afterBreak;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LanguageRuleBean) {
			LanguageRuleBean targetBean = (LanguageRuleBean) obj;
			if (targetBean.getIsBreak().equals(this.isBreak) && targetBean.getPreBreak().equals(this.preBreak)
					&& targetBean.getAfterBreak().equals(this.afterBreak)) {
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
}
