/**
 * ProjcetConfigerUtil.java
 *
 * Version information :
 *
 * Date:2013-11-12
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.core.file;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.ProjectInfoBean;

/**
 * 处理数据库的连接
 * @author yule
 * @version
 * @since JDK1.6
 */
public class ProjectConfigerUtil {

	private ProjectConfiger config;

	private List<DatabaseModelBean> mementoTmDbs;

	private List<DatabaseModelBean> mementoTbDbs;

	private IProject projcet;
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectConfigerUtil.class);

	
	public ProjectConfigerUtil(IProject projcet) {
		this.projcet = projcet;
		this.config = ProjectConfigerFactory.getProjectConfiger(projcet);
	}

	public void setDbMementos() {
		if (null == config) {
			return;
		}
		ProjectInfoBean currentProjectConfig = config.getCurrentProjectConfig();
		if (null == currentProjectConfig) {
			return;
		}
		this.mementoTbDbs = currentProjectConfig.getTbDb();
		this.mementoTmDbs = currentProjectConfig.getTmDb();
	}

	public void clearConfig() {
		
		if (null == config) {
			return;
		}
		ProjectInfoBean currentProjectBean = config.getCurrentProjectConfig();
		if (null == currentProjectBean) {
			return;
		}
		currentProjectBean.setTbDb(new ArrayList<DatabaseModelBean>());
		currentProjectBean.setTmDb(new ArrayList<DatabaseModelBean>());
		config.updateProjectConfig(currentProjectBean);
		try {
			projcet.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			logger.error("", e);
		}
	}

	public void restoreMementos() {
		if (null == config) {
			return;
		}
		ProjectInfoBean currentProjectBean = config.getCurrentProjectConfig();
		if (null == currentProjectBean) {
			return;
		}
		if (null != mementoTmDbs) {
			currentProjectBean.setTmDb(mementoTmDbs);
		}
		if (null != mementoTbDbs) {
			currentProjectBean.setTbDb(mementoTbDbs);
		}
		config.updateProjectConfig(currentProjectBean);
		try {
			projcet.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			logger.error("", e);
		}
	}

}
