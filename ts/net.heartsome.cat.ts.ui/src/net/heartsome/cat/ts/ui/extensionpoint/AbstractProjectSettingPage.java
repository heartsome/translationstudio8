package net.heartsome.cat.ts.ui.extensionpoint;

import net.heartsome.cat.common.bean.ProjectInfoBean;

import org.eclipse.jface.preference.PreferencePage;

/**
 * 项目设置扩展点抽象
 * @author jason
 * @version
 * @since JDK1.6
 */
public abstract class AbstractProjectSettingPage extends PreferencePage {
	protected ProjectInfoBean projectInfoBean;
	private String pageType;
	/**
	 * 构造器,初始化项目信息为空
	 */
	protected AbstractProjectSettingPage(String pageType) {
		this.pageType = pageType;
		projectInfoBean = new ProjectInfoBean();
	}

	/**
	 * 设置当前项目信息
	 * @param projInfoBean
	 *            ;
	 */
	public abstract void setProjectInfoBean(ProjectInfoBean projInfoBean);

	/**
	 * 获取当前项目信息
	 * @return ;
	 */
	public ProjectInfoBean getProjectInfoBean() {
		return projectInfoBean;
	}
	
	public String getPageType(){
		return this.pageType;
	}
}
