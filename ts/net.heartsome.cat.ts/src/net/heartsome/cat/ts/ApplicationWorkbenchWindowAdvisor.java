package net.heartsome.cat.ts;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.ui.listener.PartAdapter2;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.p2update.autoupdate.AutomaticUpdate;
import net.heartsome.cat.ts.drop.EditorAreaDropAdapter;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * @author cheney,weachy
 * @version
 * @since JDK1.5
 */
@SuppressWarnings("restriction")
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private static final String AUTO_UPDATE_FLAG = "AUTO_UPDATE_FLAG";
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class);
	
	
	/**
	 * @param configurer
	 */
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
	 */
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
	 */
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

		// show the shortcut bar and progress indicator, which are hidden by default
		configurer.setShowCoolBar(true);

		// 检查是否显示装态栏 robert 2011-11-04 true为显示，false为隐藏，默认显示.
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		boolean visibleStatus = prefs.getBoolean(TsPreferencesConstant.TS_statusBar_status);
		configurer.setShowStatusLine(visibleStatus);
		configurer.setShowProgressIndicator(true);
		// 添加“编辑区域”的传递者（org.eclipse.swt.dnd.Transfer）
		configurer.addEditorAreaTransfer(LocalSelectionTransfer.getTransfer());
		configurer.addEditorAreaTransfer(FileTransfer.getInstance());
		// 添加“编辑区域”的释放拖拽监听
		configurer.configureEditorAreaDropListener(new EditorAreaDropAdapter(configurer.getWindow()));

		// 注释整理
		// TODO 根据注册的转换器插件，动态添加 XLIFF Editor 映射的文件后缀名。
		// abw,html,htm,inx,properties,js,mif,doc,xls,ppt,docx,xlsx,pptx,ppsx,sxw,sxc,sxi,sxd,odt,ods,odp,odg,txt,ttx,po,pot,rc,resx,rtf,svg,xml
		// String[] extensions = FileFormatUtils.getExtensions();
		// for (String extension : extensions) {
		// PlatformUI
		// .getWorkbench()
		// .getEditorRegistry()
		// .setDefaultEditor(extension,
		// "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor");
		// }

		// begin屏蔽掉向导中的General部分
		// AbstractExtensionWizardRegistry wizardRegistry = (AbstractExtensionWizardRegistry) WorkbenchPlugin
		// .getDefault().getNewWizardRegistry();
		// IWizardCategory[] categories = WorkbenchPlugin.getDefault()
		// .getNewWizardRegistry().getRootCategory().getCategories();
		// for (IWizardDescriptor wizard : getAllWizards(categories)) {
		// WorkbenchWizardElement wizardElement = (WorkbenchWizardElement) wizard;
		// if (!allowedWizard(wizardElement.getId())) {
		// wizardRegistry.removeExtension(wizardElement
		// .getConfigurationElement().getDeclaringExtension(),
		// new Object[] { wizardElement });
		// }
		// }
		// end
		
	}
	
	
	
	

	private IWizardDescriptor[] getAllWizards(IWizardCategory... categories) {
		List<IWizardDescriptor> results = new ArrayList<IWizardDescriptor>();
		for (IWizardCategory wizardCategory : categories) {
			results.addAll(Arrays.asList(wizardCategory.getWizards()));
			results.addAll(Arrays.asList(getAllWizards(wizardCategory.getCategories())));
		}
		return results.toArray(new IWizardDescriptor[0]);
	}

	protected static final List<String> FILE_NEW__ALLOWED_WIZARDS = Collections.unmodifiableList(Arrays
			.asList(new String[] {
					// "org.eclipse.ui.wizards.new.project",// Basic wizards
					// "org.eclipse.ui.wizards.new.folder",// Basic wizards
					// "org.eclipse.ui.wizards.new.file" // Basic wizards
					"net.heartsome.cat.ts.ui.wizards.new.folder", "net.heartsome.cat.database.ui.tb.wizard.createDb",
					"net.heartsome.cat.database.ui.tm.wizad.newTmDb",
					"net.heartsome.cat.ts.ui.wizards.NewProjectWizard" }));

	protected boolean allowedWizard(String wizardId) {
		return FILE_NEW__ALLOWED_WIZARDS.contains(wizardId);
	}

	@Override
	public void postWindowOpen() {
		IWorkbenchWindow workbenchWindow = getWindowConfigurer().getWindow();
//		workbenchWindow.getShell().setMaximized(true);
		
		restorEditorHistory();
		saveEditorHistory();
		
		final ICommandService commandService = (ICommandService) workbenchWindow.getService(ICommandService.class);
	
		// 在程序主体窗口打开之前，更新打开工具栏的菜单，让它初始化菜单图片 --robert 2012-03-19
		commandService.refreshElements("net.heartsome.cat.ts.openToolBarCommand", null);

		// 添加监听
		addViewPartVisibleListener(workbenchWindow);

		addWorkplaceListener();
		// 自动检查更新
		automaticCheckUpdate();
		
		// 设置将文件拖到导航视图时的模式为直接复制
		setDragModle();
		
		setListenerToPespective(commandService);
		
		setIdToHelpSystem();
		
		setProgressIndicatorVisible(false);
		
		setInitLinkEnable();
		
		// 处理 hunspell 内置词典内置文件
		try {
			CommonFunction.unZipHunspellDics();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void addWorkplaceListener(){
		   IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
		   workspace.addResourceChangeListener(new IResourceChangeListener() {
			
			public void resourceChanged(IResourceChangeEvent event) {
				//刷新项目导航视图
				Display.getDefault().syncExec(new Runnable() {		
					public void run() {
						IViewPart findView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.findView("net.heartsome.cat.common.ui.navigator.view");
						if(null == findView){
							return ;
						}
						IAction refreshActionHandler = findView.getViewSite().getActionBars()
								.getGlobalActionHandler(ActionFactory.REFRESH.getId());
						if(null == refreshActionHandler){
							return;
						}
						refreshActionHandler.run();
					}
				});
			}
		});
	}
	
	@Override
	public boolean preWindowShellClose() {
		try {
			ResourcesPlugin.getWorkspace().save(true, null);
		} catch (CoreException e) {
			LOGGER.error("Save workspace error.", e);
		}
		return super.preWindowShellClose();
	}

	private void addViewPartVisibleListener(final IWorkbenchWindow window) {
		final ICommandService commandService = (ICommandService) window.getService(ICommandService.class);

		window.getActivePage().addPartListener(new PartAdapter2() {
			private void setStates(String partId) {
				if ("net.heartsome.cat.common.ui.navigator.view".equals(partId)) {
					commandService.refreshElements("net.heartsome.cat.common.ui.navigator.command.OpenNavigatorView",
							null);
				} else if ("net.heartsome.cat.ts.ui.views.DocumentPropertiesViewPart".equals(partId)) {
					commandService.refreshElements(
							"net.heartsome.cat.ts.ui.handlers.OpenDocumentPropertiesViewCommand", null);
				} else if ("net.heartsome.cat.ts.ui.translation.view.matchview".equals(partId)) {
					commandService.refreshElements("net.heartsome.cat.ts.ui.translation.menu.command.openMatchView",
							null);
				} else if ("net.heartsome.cat.ts.ui.term.view.termView".equals(partId)) {
					commandService.refreshElements("net.heartsome.cat.ts.ui.term.command.openTermView", null);
				} else if ("net.heartsome.cat.ts.ui.qa.views.QAResultViewPart".equals(partId)) {
					commandService.refreshElements("net.heartsome.cat.ts.ui.qa.handlers.OpenQAResultViewCommand", null);
				}else if ("net.heartsome.cat.ts.websearch.ui.view.BrowserViewPart".equals(partId)) {
					commandService.refreshElements("net.heartsome.cat.ts.websearch.showOrHideView", null);
				}
			}

			/**
			 * 视图打开时
			 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
			 */
			public void partOpened(final IWorkbenchPartReference partRef) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setStates(partRef.getId());
					}
				});
			}

			/**
			 * 视图关闭时
			 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
			 */
			public void partClosed(IWorkbenchPartReference partRef) {
				if (!partRef.getPage().getWorkbenchWindow().getWorkbench().isClosing()) {
					setStates(partRef.getId());
				}

				// 关闭时，根据是否是合并打开的文件，如果是，标识其是否被存储--robert
				if ("net.heartsome.cat.ts.ui.xliffeditor.nattable.editor".equals(partRef.getId())) {
					if (partRef.getPage().getWorkbenchWindow().getWorkbench().isClosing()) {
						IXliffEditor xlfEditor = (IXliffEditor) partRef.getPart(true);
						xlfEditor.setStore(true);
					}
				}
			}
		});
	}

	/**
	 * 状态栏的显示与隐藏 robert 2011-11-03
	 */
	public void setStatusVisible(boolean visible) {
		getWindowConfigurer().setShowProgressIndicator(visible);
		getWindowConfigurer().setShowStatusLine(visible);
		getWindowConfigurer().getWindow().getShell().layout();
	}
	
	/**
	 * 控制状态栏右边的后台进度条的显示	robert	2012-11-07
	 */
	public void setProgressIndicatorVisible(boolean isVisible){
		boolean isProgessIndicatorVisble = getWindowConfigurer().getShowProgressIndicator();
		if ((isProgessIndicatorVisble && isVisible) || (!isProgessIndicatorVisble && !isVisible)) {
			return;
		}
		getWindowConfigurer().setShowStatusLine(false);
		getWindowConfigurer().setShowProgressIndicator(isVisible);
		getWindowConfigurer().setShowStatusLine(true);
		getWindowConfigurer().getWindow().getShell().layout();
	}
	
	/**
	 * 当程序第一次运行时(或是重新换了命名空间)，设置项目视图的 link with editor 按钮处于选中状态
	 * robert	2013-01-04
	 */
	private void setInitLinkEnable(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean isInitialRun = !store.getBoolean(IPreferenceConstants.INITIAL_RUN);
		if (isInitialRun) {
			IViewPart navigator = getWindowConfigurer().getWindow().getActivePage().findView("net.heartsome.cat.common.ui.navigator.view");
			CommonNavigator commonNavigator = (CommonNavigator) navigator;
			commonNavigator.setLinkingEnabled(true);
			store.setValue(IPreferenceConstants.INITIAL_RUN, true);
		}
	}

	/**
	 * 删除 RCP 自带的工具栏按钮
	 */
	public void postWindowCreate() {
		IActionBarConfigurer actionBarConfigurer = getWindowConfigurer().getActionBarConfigurer();
		IContributionItem[] coolItems = actionBarConfigurer.getCoolBarManager().getItems();
		for (int i = 0; i < coolItems.length; i++) {
			if (coolItems[i] instanceof ToolBarContributionItem) {
				ToolBarContributionItem toolbarItem = (ToolBarContributionItem) coolItems[i];
				if (toolbarItem.getId().equals("org.eclipse.ui.WorkingSetActionSet")
						|| toolbarItem.getId().equals("org.eclipse.ui.edit.text.actionSet.annotationNavigation")
						|| toolbarItem.getId().equals("org.eclipse.ui.edit.text.actionSet.navigation")) {
					toolbarItem.getToolBarManager().removeAll();
				}
			}
		}
		actionBarConfigurer.getCoolBarManager().update(true);

		addAutoPluginMenu();

	}
	

	private void automaticCheckUpdate() {
		// 自动检查更新
		final IPreferenceStore prefStore = net.heartsome.cat.ts.ui.Activator.getDefault().getPreferenceStore();
		int updatePolicy = prefStore.getInt(IPreferenceConstants.SYSTEM_AUTO_UPDATE);
		boolean flg = false;
		if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_NEVER) {
			return;
		} else if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_STARTUP) {
			// 启动时检查更新
			flg = true;
		} else if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY) {
			// 每月 xx 日检查更新
			int day = prefStore.getInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY_DATE);
			Calendar c = Calendar.getInstance();
			int cYear = c.get(Calendar.YEAR);
			int cMoth = c.get(Calendar.MONTH) + 1;
			int cDay = c.get(Calendar.DAY_OF_MONTH);
			String preUpdateDay = prefStore.getString("AUTO_UPDATE_FLAG");
			if (preUpdateDay.equals("")) {
				if (cDay == day) {
					flg = true;
					prefStore.setValue("AUTO_UPDATE_FLAG", cYear + "-" + cMoth + "-" + cDay);
				}
			} else {
				String[] ymdStr = preUpdateDay.split("-");
				Calendar uc = Calendar.getInstance();
				int uYeaer = Integer.parseInt(ymdStr[0]);
				int uMonth = Integer.parseInt(ymdStr[1]);
				int uDay = Integer.parseInt(ymdStr[2]);
				uc.set(uYeaer, uMonth - 1, uDay);
				if(cDay == day && c.getTime().compareTo(uc.getTime()) > 0){
					flg = true;
					prefStore.setValue("AUTO_UPDATE_FLAG", cYear + "-" + cMoth + "-" + cDay);
				}else if( cDay > day && (uYeaer < cYear || uMonth < cMoth )){
					flg = true;
					prefStore.setValue("AUTO_UPDATE_FLAG", cYear + "-" + cMoth + "-" + cDay);
				}
			}
		} else if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY) {
			// 每周 xx 日检查更新
			int weekday = prefStore.getInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE);
			Calendar c = Calendar.getInstance();
			int cWeekDay = c.get(Calendar.DAY_OF_WEEK);
			int cYear = c.get(Calendar.YEAR);
			int cMoth = c.get(Calendar.MONTH) + 1;
			int cDay = c.get(Calendar.DAY_OF_MONTH);
			String preUpdateDay = prefStore.getString(AUTO_UPDATE_FLAG);
			if (preUpdateDay.equals("")) {
				if (cWeekDay == weekday) {
					flg = true;
					prefStore.setValue(AUTO_UPDATE_FLAG, cYear + "-" + cMoth + "-" + cDay);
				}
			} else {
				String[] ymdStr = preUpdateDay.split("-");
				Calendar uc = Calendar.getInstance();
				int uYeaer = Integer.parseInt(ymdStr[0]);
				uc.set(uYeaer, Integer.parseInt(ymdStr[1]) - 1, Integer.parseInt(ymdStr[2]));

				if (cWeekDay == weekday && c.getTime().compareTo(uc.getTime()) > 0) {
					flg = true;
					prefStore.setValue(AUTO_UPDATE_FLAG, cYear + "-" + cMoth + "-" + cDay);
				}else if(cWeekDay > weekday && (uYeaer < cYear || uc.get(Calendar.WEEK_OF_YEAR) < c.get(Calendar.WEEK_OF_YEAR))){
					flg = true;
					prefStore.setValue(AUTO_UPDATE_FLAG, cYear + "-" + cMoth + "-" + cDay);
				}
			}
		}

		if (!flg) {
			return;
		}
		AutomaticUpdate checker = new AutomaticUpdate();
		checker.checkForUpdates();
	}

	/**
	 * 设置在拖动文件到导航视图时的模式为直接复制，见类 {@link CopyFilesAndFoldersOperation} 的方法 CopyFilesAndFoldersOperation
	 * robert	09-26
	 */
	private void setDragModle(){
		IPreferenceStore store= IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		store.setValue(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_MOVE_COPY);
		store.setValue(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE,
				IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_MOVE_COPY);
	}
	
	/**
	 * 给默认透视图添加监听，当透视图发生改变时，触发相关事件	robert	2012-10-18
	 * @param commandService
	 */
	private void setListenerToPespective(final ICommandService commandService){
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId) {
				
			}
			
			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				// 改变透视图时，发出监听去检查品质检查结果视图的状态，从而让 视图 菜单的状态与之保持一致。
				commandService.refreshElements("net.heartsome.cat.ts.ui.qa.handlers.OpenQAResultViewCommand", null);
				
				// 显示状态栏与工具栏
				IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);

				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				boolean statusVisible = preferenceStore.getBoolean(TsPreferencesConstant.TS_statusBar_status);
				if (!statusVisible) {
					preferenceStore.setValue(TsPreferencesConstant.TS_statusBar_status, false);
					try {
						handlerService.executeCommand("net.heartsome.cat.ts.command.openStatusBar", null);
					} catch (Exception ex) {
						throw new RuntimeException("CommandTest.exitcommand not found"); 
					}
				}
				
				WorkbenchWindow window_1 = (WorkbenchWindow)window;
				if (!window_1.getCoolBarVisible()) {
					try {
						window_1.toggleToolbarVisibility();
						commandService.refreshElements("net.heartsome.cat.ts.command.openToolBar", null);
					} catch (Exception ex) {
						throw new RuntimeException("CommandTest.exitcommand not found"); 
					}
				}
				
			}
		};
		
		window.addPerspectiveListener(perspectiveListener);
	}
	
	/**
	 * robert	2012-11-11
	 */
	private void setIdToHelpSystem(){
		WorkbenchHelpSystem helpSystem = (WorkbenchHelpSystem)PlatformUI.getWorkbench().getHelpSystem();	//setDesiredHelpSystemId
		// net.heartsome.cat.ts.ui.help.selfHelp  为 插件 net.heartsome.cat.ts.ui.help 与的扩展点 helpSupport 的 id
		helpSystem.setDesiredHelpSystemId("net.heartsome.cat.ts.ui.help.selfHelp");
	}
	
	/**
	 * 重新恢复产品上次退时出所保存的编辑器，此修复针对　mac 下的　bug 2998 启动：某客户安装的软件只能使用一次.		robert	2013-05-21
	 */
	private void restorEditorHistory(){
		// 只针对 mac 下的用户
		if (System.getProperty("os.name").indexOf("Mac") == -1) {
			return;
		}
		
		final WorkbenchPage page = (WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				monitor.beginTask("", 10);
				String tempEditorHistoryLC = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(Constant.TEMP_EDITOR_HISTORY).toOSString();
				File tempEditorHistoryFile = new File(tempEditorHistoryLC);
				if (!tempEditorHistoryFile.exists()) {
					return;
				}
				monitor.worked(1);
				
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				VTDGen vg = new VTDGen();
				try {
					boolean parseResult = vg.parseFile(tempEditorHistoryLC, true);
					if (!parseResult) {
						return;
					}
					VTDNav vn = vg.getNav();
					AutoPilot ap = new AutoPilot(vn);
					int storeFileSum = 0;
					ap.selectXPath("count(/editors/editor)");
					storeFileSum = (int)ap.evalXPathToNumber();
					
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 9, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
					subMonitor.beginTask("", storeFileSum);
					
					ap.selectXPath("/editors/editor");
					String editorId = null;
					String localFilePath = null;
					boolean activate = false;
					IEditorReference activateEditorRefe = null;
					IEditorReference firstEditor = null;
					IFile curIFile = null;
					int index = -1;
					while(ap.evalXPath() != -1){
						activate = false;
						editorId = null;
						localFilePath = null;
						if ((index = vn.getAttrVal("id")) != -1) {
							editorId = vn.toRawString(index);
						}
						if ((index = vn.getAttrVal("path")) != -1) {
							localFilePath = vn.toRawString(index);
						}
						if (editorId == null || editorId.trim().length() <= 0 || localFilePath == null || localFilePath.trim().length() <= 0) {
							continue;
						}
						if ((index = vn.getAttrVal("active")) != -1) {
							if ("true".equals(vn.toRawString(index))) {
								activate = true;
							}
						}
						
						curIFile = root.getFileForLocation(new Path(localFilePath));
						if (!curIFile.exists()) {
							subMonitor.worked(1);
							continue;
						}
						
						if (activate) {
							activateEditorRefe = page.getEditorManager().openEditor(editorId, new FileEditorInput(curIFile), false, null);
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().setActivePage(page);
						}else {
							if (firstEditor == null) {
								firstEditor = page.getEditorManager().openEditor(editorId, new FileEditorInput(curIFile), false, null);
							}else {
								page.getEditorManager().openEditor(editorId, new FileEditorInput(curIFile), false, null);
							}
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().setActivePage(page);
						}

						subMonitor.worked(1);
					}
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().setActivePage(page);
					if (activateEditorRefe != null) {
						if (firstEditor != null) {
							page.activate(firstEditor.getEditor(true));
						}
						page.activate(activateEditorRefe.getEditor(true));
					}
					subMonitor.done();
					monitor.done();
				} catch (Exception e) {
					LOGGER.error("restore editor file error", e);
				}
			}
		};
		
		try {
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, true, runnable);
		} catch (Exception e) {
			LOGGER.error("restore editor file error", e);
		}
		
	}
	
	
	/**
	 * 在退出程序时保存所有打开的编辑器，此修复针对　mac 下的　bug 2998 启动：某客户安装的软件只能使用一次.	robert	2013-05-21
	 */
	private void saveEditorHistory(){
		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				// 只针对 mac 下的用户
				if (System.getProperty("os.name").indexOf("Mac") == -1) {
					return true;
				}
				FileOutputStream stream = null;
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				try {
					String tempEditorHistoryLC = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(Constant.TEMP_EDITOR_HISTORY).toOSString();
					File tempEditorHistoryFile = new File(tempEditorHistoryLC);
					if (!tempEditorHistoryFile.exists()) {
						stream = new FileOutputStream(tempEditorHistoryLC);
						stream.write("<editors></editors>".getBytes("UTF-8"));
					}
					
					IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
					IEditorReference[] refreArray = page.getEditorReferences();
					StringBuffer sb = new StringBuffer();
					String editorId = "";
					String path = "";
					String active = "false";
					
					IEditorPart activeEditor = page.getActiveEditor();
					IEditorInput activeInput = null;
					if (activeEditor != null) {
						activeInput = activeEditor.getEditorInput();
					}
					
					IEditorInput curInput = null;
					for(IEditorReference refre : refreArray){
						active = "false";
						editorId = refre.getId();
						curInput = refre.getEditorInput();
						path = ((FileEditorInput)curInput).getFile().getFullPath().toOSString();
						path = root.getLocation().append(path).toOSString();
						if (activeInput != null && activeInput == curInput) {
							active = "true";
						}
						sb.append("<editor id='" + editorId + "' path='" + path + "' active='" + active + "'/>\n");
					}
					VTDGen vg = new VTDGen();
					if (!vg.parseFile(tempEditorHistoryLC, true)) {
						stream = new FileOutputStream(tempEditorHistoryLC);
						stream.write("<editors></editors>".getBytes("UTF-8"));
						vg.parseFile(tempEditorHistoryLC, true);
					}
					VTDNav vn = vg.getNav();
					AutoPilot ap = new AutoPilot(vn);
					XMLModifier xm = new XMLModifier(vn);
					// 先删除之前的记录
					ap.selectXPath("/editors/editor");
					while(ap.evalXPath() != -1){
						xm.remove();
					}
					
					ap.selectXPath("/editors");
					if (ap.evalXPath() != -1) {
						xm.insertBeforeTail(sb.toString());
					}
					xm.output(tempEditorHistoryLC);
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if (stream != null) {
						try {
							stream.close();
						} catch (Exception e2) {
						}
					}
				}
				return true;
			}
			
			public void postShutdown(IWorkbench workbench) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * 在插件一栏上，添加自定义插件的菜单 robert 2012-03-07 ;
	 */
	private void addAutoPluginMenu() {
		// IContributionItem[] mItems;
		// IMenuManager mm = getWindowConfigurer().getActionBarConfigurer().getMenuManager();
		// mItems = mm.getItems();
		// for (int i = 0; i < mItems.length; i++) {
		// if (mItems[i] instanceof MenuManager) {
		// System.out.println();
		// if (((MenuManager) mItems[i]).getId().equals("net.heartsome.cat.ts.ui.menu.plugin")) {
		// MenuManager manager = ((MenuManager) mItems[i]);
		//
		// final PluginConfigManage pluginManage = new PluginConfigManage();
		// List<PluginConfigBean> pluginList = pluginManage.getPluginCofigData();
		//
		// if (pluginList.size() > 0) {
		// manager.add(new Separator());
		// }
		// Action action;
		// for (int j = 0; j < pluginList.size(); j++) {
		// final PluginConfigBean bean = pluginList.get(j);
		// action = new Action() {
		// @Override
		// public void run() {
		// pluginManage.executePlugin(bean);
		// }
		// };
		// action.setId(bean.getId());
		// action.setText(bean.getName());
		// manager.add(action);
		// }
		// }
		// }
		// }
	}

}
