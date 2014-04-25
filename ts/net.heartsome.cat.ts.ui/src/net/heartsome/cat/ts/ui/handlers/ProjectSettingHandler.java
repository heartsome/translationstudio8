/**
 * ProjectSettingHandler.java
 *
 * Version information :
 *
 * Date:Nov 28, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.ui.extensionpoint.AbstractProjectSettingPage;
import net.heartsome.cat.ts.ui.projectsetting.ProjectSettingBaseInfoPage;
import net.heartsome.cat.ts.ui.projectsetting.ProjectSettingDialog;
import net.heartsome.cat.ts.ui.projectsetting.ProjectSettingLanguagePage;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目设置Handler
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ProjectSettingHandler extends AbstractHandler {

	public static final Logger logger = LoggerFactory.getLogger(ProjectSettingHandler.class);
	private List<AbstractProjectSettingPage> extensionPages = new ArrayList<AbstractProjectSettingPage>();

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		runWizardPageExtension();

		ISelection curSelection = HandlerUtil.getCurrentSelection(event);
		if (curSelection != null && !curSelection.isEmpty() && curSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) curSelection;
			Object obj = selection.getFirstElement();
			if (obj instanceof IProject) {
				IProject project = (IProject) obj;
				ProjectConfiger cfg = ProjectConfigerFactory.getProjectConfiger(project);
				final ProjectInfoBean cfgBean = cfg.getCurrentProjectConfig();
				
				cfgBean.setProjectName(project.getName());
				ProjectSettingBaseInfoPage infoPage = new ProjectSettingBaseInfoPage(cfgBean);
				ProjectSettingLanguagePage langPag = new ProjectSettingLanguagePage(cfgBean);

				final PreferenceManager mgr = new PreferenceManager();
				IPreferenceNode infoNode = new PreferenceNode("infoPage", infoPage);
				IPreferenceNode langNode = new PreferenceNode("langPag", langPag);

				mgr.addToRoot(infoNode);
				mgr.addTo("infoPage", langNode);

				AbstractProjectSettingPage pageTm = getPageByType("TM");
				if (pageTm != null) {
					pageTm.setProjectInfoBean(cfgBean);
					IPreferenceNode extensionNode = new PreferenceNode("tmInfo", pageTm);
					mgr.addTo("infoPage", extensionNode);
				}

				AbstractProjectSettingPage pageTb = getPageByType("TB");
				if (pageTb != null) {
					pageTb.setProjectInfoBean(cfgBean);
					IPreferenceNode extensionNode = new PreferenceNode("tbInfo", pageTb);
					mgr.addTo("infoPage", extensionNode);
				}

				for (AbstractProjectSettingPage page : extensionPages) {
					page.setProjectInfoBean(cfgBean);
					IPreferenceNode extensionNode = new PreferenceNode("tbInfo", page);
					mgr.addTo("infoPage", extensionNode);
				}

				ProjectSettingDialog dialog = new ProjectSettingDialog(window.getShell(), mgr);
				dialog.create();
				dialog.setMessage(infoPage.getTitle());
				if (dialog.open() == 0) {
					cfg.updateProjectConfig(cfgBean);
					try {
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e) {
						logger.error("", e);
					}
				}

			} else {
				MessageDialog.openError(window.getShell(),
						Messages.getString("handlers.ProjectSettingHandler.msgTitle"),
						Messages.getString("handlers.ProjectSettingHandler.msg1"));
				return null;
			}
		} else {
			MessageDialog.openError(window.getShell(), Messages.getString("handlers.ProjectSettingHandler.msgTitle"),
					Messages.getString("handlers.ProjectSettingHandler.msg1"));
			return null;
		}
		return null;
	}

	/**
	 * 加载扩展向导页 ;
	 */
	private void runWizardPageExtension() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"net.heartsome.cat.ts.ui.extensionpoint.projectsetting");
		try {
			// 修改术语库重复
			extensionPages.clear();
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof AbstractProjectSettingPage) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("handlers.ProjectSettingHandler.logger1"), exception);
						}

						public void run() throws Exception {
							extensionPages.add((AbstractProjectSettingPage) o);
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("handlers.ProjectSettingHandler.logger1"), ex);
		}
	}

	private AbstractProjectSettingPage getPageByType(String type) {
		for (AbstractProjectSettingPage page : extensionPages) {
			if (page.getPageType().equals(type)) {
				extensionPages.remove(page);
				return page;
			}
		}
		return null;
	}
}
