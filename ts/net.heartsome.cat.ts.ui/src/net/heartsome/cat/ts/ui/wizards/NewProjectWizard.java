/**
 * NewProjectWizard.java
 *
 * Version information :
 *
 * Date:Oct 20, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.extensionpoint.AbstractNewProjectWizardPage;
import net.heartsome.cat.ts.ui.extensionpoint.IConverterCaller;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.propertyTester.RTFEnabledPropertyTester;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class NewProjectWizard extends Wizard implements INewWizard {

	public final static Logger logger = LoggerFactory.getLogger(NewProjectWizard.class);

	public final String PAGE_EXTENSION_ID = "net.heartsome.cat.ts.ui.extensionpoint.newproject";
	private NewProjectWizardProjInfoPage firstPage;
	private NewProjectWizardLanguagePage secondPage;
	private NewProjectWizardSourceFilePage srcFilesPage;

	private List<AbstractNewProjectWizardPage> extensionPages;

	private IConverterCaller convertImpl;
	private List<IFile> sourcefiles = new ArrayList<IFile>();

	/**
	 * 
	 */
	public NewProjectWizard() {
		setWindowTitle(Messages.getString("wizard.NewProjectWizard.title"));
		setNeedsProgressMonitor(true);
		extensionPages = new ArrayList<AbstractNewProjectWizardPage>();
		runWizardPageExtension();
	}

	/**
	 * 加载扩展向导页 ;
	 */
	private void runWizardPageExtension() {
		// 加载向导扩展
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(PAGE_EXTENSION_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof AbstractNewProjectWizardPage) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("wizard.NewProjectWizard.logger1"), exception);
						}

						public void run() throws Exception {
							extensionPages.add((AbstractNewProjectWizardPage) o);
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("wizard.NewProjectWizard.logger1"), ex);
		}

		// 加载转换器扩展
		IConfigurationElement[] config2 = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"net.heartsome.cat.ts.ui.extension.converter");
		try {
			for (IConfigurationElement e : config2) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof IConverterCaller) {
					ISafeRunnable runnable = new ISafeRunnable() {

						public void handleException(Throwable exception) {
							logger.error(Messages.getString("wizard.NewProjectWizard.logger2"), exception);
						}

						public void run() throws Exception {
							convertImpl = (IConverterCaller) o;
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			logger.error(Messages.getString("wizard.NewProjectWizard.logger2"), ex);
		}
	}

	private AbstractNewProjectWizardPage getPageByType(String type) {
		for (AbstractNewProjectWizardPage extensionPage : extensionPages) {
			if (extensionPage.getPageType().equals(type)) {
				return extensionPage;
			}
		}
		return null;
	}

	@Override
	public void addPages() {
		firstPage = new NewProjectWizardProjInfoPage();
		addPage(firstPage);
		secondPage = new NewProjectWizardLanguagePage();
		addPage(secondPage);
		AbstractNewProjectWizardPage pageTm = getPageByType("TM");
		if (pageTm != null) {
			addPage(pageTm);
		}
		AbstractNewProjectWizardPage pageTb = getPageByType("TB");
		if (pageTb != null) {
			addPage(pageTb);
		}
		srcFilesPage = new NewProjectWizardSourceFilePage();
		srcFilesPage.setConvertInfo(convertImpl);
		addPage(srcFilesPage);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		final IProject project = firstPage.getProject();
		final List<String> srcFiles = srcFilesPage.getSrcFiles();

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.getString("wizard.NewProjectWizard.task1"), 3);
				try {
					createProject(project, new SubProgressMonitor(monitor, 1));
					// // 获取源文件并复制到项目中
					if (srcFiles != null) {
						copySourceFile(project, Constant.FOLDER_SRC, srcFiles, new SubProgressMonitor(monitor, 1));
					}
					initProjectConfig(project, new SubProgressMonitor(monitor, 1));
					
				} catch (CoreException e) {
					try {
						project.delete(true, monitor);
					} catch (CoreException e1) {
						logger.error(Messages.getString("wizard.NewProjectWizard.logger3"), e1);
					}
					logger.error(Messages.getString("wizard.NewProjectWizard.logger4"), e);
				}
				monitor.done();
			}
		};

		try {
			getContainer().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			logger.error("", e);
			return false;
		} catch (InterruptedException e) {
			logger.error("", e);
			return false;
		}
		// 创建项目成功后刷新导入/导出 RTF 功能的可用状态
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
		if (evaluationService != null) {
			evaluationService.requestEvaluation(RTFEnabledPropertyTester.PROPERTY_NAMESPACE + "."
					+ RTFEnabledPropertyTester.PROPERTY_ENABLED);
		}

		if (srcFilesPage.isOpenConverter()) {
			convertImpl.openConverter(sourcefiles);
		}
		
		// 创建项目后刷新资源视图
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			try {
				project.delete(true, null);
			} catch (CoreException e1) {
				logger.error("", e1);
			}
			logger.error("", e);
		}
		return true;
	}

	private void createProject(IProject project, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 3);
		monitor.setTaskName(Messages.getString("wizard.NewProjectWizard.task1"));
		try {
			project.create(null);
			monitor.worked(1);
			if (!project.isOpen()) {
				project.open(null);
			}
			monitor.worked(1);

			// 创建项目所需要的文件目录
			String[] folderNames = { Constant.FOLDER_INTERMEDDIATE, Constant.FOLDER_SRC, Constant.FOLDER_TGT,
					Constant.FOLDER_XLIFF };
			createFolders(project, folderNames);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				project.delete(true, monitor);
				throw new OperationCanceledException();
			}
		} catch (CoreException e) {
			logger.error(Messages.getString("wizard.NewProjectWizard.logger5"), e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * 创建项目中文件夹
	 * @param project
	 *            文件夹所属的项目
	 * @param folderNames
	 *            多个文件夹的名字;
	 */
	private void createFolders(IProject project, String[] folderNames) {
		for (String folderName : folderNames) {
			IFolder folder = project.getFolder(folderName);
			if (!folder.exists()) {
				try {
					folder.create(true, true, null);
					if (folderName.equals(Constant.FOLDER_INTERMEDDIATE)) {
						createChildrenFoders(folder, new String[] { Constant.FOLDER_REPORT, Constant.FOLDER_SKL });
					}
				} catch (CoreException e) {
					logger.error(Messages.getString("wizard.NewProjectWizard.logger6"), e);
				}
			}
		}
	}

	/**
	 * 创建子目录
	 * @param parentFolder
	 *            父目录
	 * @param childrenFolderNames
	 *            子目录名称
	 * @throws CoreException
	 *             ;
	 */
	private void createChildrenFoders(IFolder parentFolder, String[] childrenFolderNames) throws CoreException {
		if (parentFolder.exists()) {
			for (String childName : childrenFolderNames) {
				IFolder folder = parentFolder.getFolder(childName);
				if (!folder.exists()) {
					folder.create(true, true, null);
				}
			}
		}
	}

	/**
	 * 构建项目配置信息
	 * @param project
	 *            ;
	 * @throws CoreException
	 */
	private void initProjectConfig(final IProject project, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		final IProgressMonitor sMonitor = monitor;
		sMonitor.beginTask(Messages.getString("wizard.NewProjectWizard.task2"), 3);
		sMonitor.worked(1);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ProjectInfoBean bean = new ProjectInfoBean();
				bean.setProjectName(project.getName());
				bean.setMapField(firstPage.getFieldMap());
				bean.setMapAttr(firstPage.getAttributeMap());
				bean.setSourceLang(secondPage.getSrcLanguage());
				bean.setTargetLang(secondPage.getTargetlanguage());
				for (AbstractNewProjectWizardPage extensionPage : extensionPages) {
					if (extensionPage.getPageType().equals("TM")) {
						bean.setTmDb(extensionPage.getSelectedDatabase());
					} else if (extensionPage.getPageType().equals("TB")) {
						bean.setTbDb(extensionPage.getSelectedDatabase());
					}
				}
				sMonitor.worked(1);

				ProjectConfiger projCfg = ProjectConfigerFactory.getProjectConfiger(project);
				sMonitor.worked(1);
				projCfg.updateProjectConfig(bean);

				// 记住当前语言信息
				IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
				ps.setValue(IPreferenceConstants.NEW_PROJECT_SRC_LANG, bean.getSourceLang().getCode());
				List<Language> targetLangList = bean.getTargetLang();
				StringBuffer bf = new StringBuffer();
				for (Language lang : targetLangList) {
					bf.append(lang.getCode());
					bf.append(",");
				}
				ps.setValue(IPreferenceConstants.NEW_PROJECT_TGT_LANG, bf.substring(0, bf.lastIndexOf(",")));

				if (sMonitor.isCanceled()) {
					try {
						project.delete(true, sMonitor);
					} catch (CoreException e) {
						logger.error("", e);
					}
					throw new OperationCanceledException();
				}
			}
		});
		sMonitor.done();
	}

	/**
	 * 将源文件复制到项目中
	 * @param srcFiles
	 *            源文件路径列表 ;
	 * @param project
	 *            项目
	 * @param folderName
	 *            目录名称
	 * @throws CoreException
	 */
	private void copySourceFile(final IProject project, String folderName, final List<String> srcFiles,
			IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.getString("wizard.NewProjectWizard.task3"), srcFiles.size());
		monitor.setTaskName(Messages.getString("wizard.NewProjectWizard.task3"));
		final IProgressMonitor sMonitor = monitor;
		final IFolder folder = project.getFolder(folderName);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				for (int i = 0; i < srcFiles.size(); i++) {
					if (sMonitor.isCanceled()) {
						try {
							project.delete(true, sMonitor);
						} catch (CoreException e) {
							e.printStackTrace();
						}
						throw new OperationCanceledException();
					}
					File file = new File(srcFiles.get(i));
					String fileName = file.getName();
					InputStream inputStream;
					try {
						inputStream = new FileInputStream(file);
						IFile iFile = folder.getFile(fileName);
						if (iFile.exists()) {
							if (!MessageDialog.openConfirm(getShell(),
									Messages.getString("wizard.NewProjectWizard.msgTitle"),
									Messages.getString("wizard.NewProjectWizard.msg1"))) {
								continue;
							}
						}
						iFile.create(inputStream, false, null);
						sourcefiles.add(iFile);
						sMonitor.worked(1);
					} catch (Exception e) {
						logger.error("", e);
						MessageDialog.openInformation(getShell(),
								Messages.getString("wizard.NewProjectWizard.msgTitle"), MessageFormat.format(Messages.getString("wizard.NewProjectWizard.msg2"), fileName));
						continue;
					} finally {
						sMonitor.done();
					}
				}
			}
		});
	}
}
