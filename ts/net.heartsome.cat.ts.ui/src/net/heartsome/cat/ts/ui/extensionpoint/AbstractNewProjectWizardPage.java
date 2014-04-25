package net.heartsome.cat.ts.ui.extensionpoint;

import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.ts.ui.wizards.NewProjectWizardLanguagePage;

import org.eclipse.jface.wizard.WizardPage;

/**
 * 新建项目向导页面抽象,用于扩展点实现
 * @author  jason
 * @version 
 * @since   JDK1.6
 */
public abstract class AbstractNewProjectWizardPage extends WizardPage {

	/**
	 * 当前创建项目的源语言
	 */
	protected Language projSourceLang;
	/**
	 * 页面类型,记忆库设置或术语库设置
	 */
	private String pageType;

	protected AbstractNewProjectWizardPage(String pageName,String pageType) {
		super(pageName);
		this.pageType = pageType;
	}

	/**
	 * 获取添加的库
	 * @return ;
	 */
	public abstract List<DatabaseModelBean> getSelectedDatabase();
	
	/**
	 * 获取页面类型 {@link #pageType}
	 * @return “TM”时，表示该页面用于设置记忆库,“TB”时，表示该页面用于设置术语库;
	 */
	public String getPageType(){
		return pageType;
	}
	
	
	
	/**
	 * 提供项目的源语言
	 * @see NewProjectWizardLanguagePage#getNextPage()
	 * @param projSourceLang
	 *            the projSourceLang to set
	 */
	public void setProjSourceLang(Language projSourceLang) {
		this.projSourceLang = projSourceLang;
	}

}
