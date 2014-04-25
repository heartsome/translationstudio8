package net.heartsome.cat.ts.importproject.wizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.core.file.ProjectConfigerUtil;
import net.heartsome.cat.ts.importproject.Activator;
import net.heartsome.cat.ts.importproject.resource.Messages;
import net.heartsome.cat.ts.importproject.widgiet.ProjectResource;
import net.heartsome.cat.ts.importproject.widgiet.ResourceTree;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.TarEntry;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导入项目新的需求设计
 * @author robert 2013-02-27
 */
public class ImportProjectWizardPage2 extends WizardPage implements Listener {
	private static final String[] FILE_IMPORT_MASK = { "*" + Constant.PROJECT_PACK_EXTENSSION, "*" };
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	private static final String HTML_EDITOR_ID = "net.heartsome.cat.ts.ui.editor.HtmlBrowser";

	/** 项目的图标 */
	private Image projectImg;
	/** 文件夹的图标 */
	private Image folderImg;
	/** hsXliff 文件的图标 */
	private Image hsXLiffImg;
	/** html 的图标 */
	private Image htmlImg;
	private Image defaultImg;
	private Map<String, Image> imgMap;

	private String previouslyBrowsedPath = "";
	private Text filePathTxt;
	private Button browseBtn;
	/** 左边显示所有内容，包括项目，文件夹，文件的树 */
	private ResourceTree selectElementTree;
	/** 右边显示左边选中要导入资源的重复文件或重复的文件夹 */
	private ResourceTree repeatElementTree;
	private Button leftSelectAllBtn;
	private Button leftDisSelectAllBtn;
	private Button leftAllExpandBtn;
	private Button rightSelectAllBtn;
	private Button rightDisSelectAllBtn;
	private Button rightAllExpandBtn;
	private SelectProjectContentProvider selectContentProvider;
	private RepeatProjectContentProvider repeateContentProvider;

	/** 默认导入的资源，即这些资源是不会显示在资源树上的 */
	private List<IResource> defaultImportItemList = new ArrayList<IResource>();
	private ProjectRecord[] selectedProjects = new ProjectRecord[0];
	@SuppressWarnings("restriction")
	private ILeveledImportStructureProvider structureProvider;
	// The last selected path to minimize searches
	private String lastPath;
	// The last time that the file or folder at the selected path was modified to mimize searches
	private long lastModified;
	private boolean copyFiles = true;
	private boolean lastCopyFiles = false;
	/** 左边资源树选择文件后，重复的资源 */
	private Map<String, Set<ProjectResource>> checkedRepeatElementMap = new HashMap<String, Set<ProjectResource>>();
	private IWorkspaceRoot root;
	private IWorkbenchPage page;
	/** 初始化时，所有的根项目 */
	private ProjectResource[] rootProjectArray;
	private int totalWorkSum = 0;
	private boolean runContinue = false;
	private Map<IFile, IEditorPart> openedIfileMap;

	private final int INIT_TYPE = 0;
	private final int OTHER_TYPE = 1;

	private Logger LOGGER = LoggerFactory.getLogger(ImportProjectWizardPage2.class.getName());

	@SuppressWarnings("restriction")
	protected ImportProjectWizardPage2(String pageName, String initialPath, IStructuredSelection currentSelection) {
		super(pageName);
		setTitle(DataTransferMessages.DataTransfer_importTitle);

		initData();
	}

	private void initData() {
		root = ResourcesPlugin.getWorkspace().getRoot();
		page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		projectImg = Activator.getImageDescriptor("images/prj_open.png").createImage();
		folderImg = Activator.getImageDescriptor("images/folder.png").createImage();
		hsXLiffImg = Activator.getImageDescriptor("images/hsxliff.png").createImage();
		htmlImg = Activator.getImageDescriptor("images/html.png").createImage();
		defaultImg = Activator.getImageDescriptor("images/file_obj.png").createImage();
		imgMap = new HashMap<String, Image>();
	}

	public void createControl(Composite parent) {
		GridDataFactory.fillDefaults().hint(800, 450).grab(true, true).applyTo(parent);
		Composite tparent = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().hint(600, 600).grab(true, true).applyTo(tparent);
		tparent.setLayout(new GridLayout());
		initializeDialogUnits(parent);

		createBrowseBtn(tparent);
		createResourceTree(tparent);

		setControl(parent);
		setPageCompleteState(INIT_TYPE);
		setImageDescriptor(Activator.getImageDescriptor("images/importProject_logo.png"));
	}

	/**
	 * 创建 浏览 文件按钮
	 * @param tparent
	 */
	private void createBrowseBtn(Composite tparent) {
		Composite btnCmp = new Composite(tparent, SWT.NONE);
		btnCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCmp.setLayout(new GridLayout(3, false));

		Label lbl = new Label(btnCmp, SWT.NONE);
		lbl.setText(Messages.getString("importProjectWizardPage.selectFileLbl"));

		filePathTxt = new Text(btnCmp, SWT.BORDER);
		filePathTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		browseBtn = new Button(btnCmp, SWT.NONE);
		browseBtn.setText(Messages.getString("importProjectWizardPage.broswer"));
		browseBtn.addListener(SWT.Selection, this);
	}

	/**
	 * 创建两颗显示资源树
	 */
	private void createResourceTree(Composite tparent) {
		Composite resourceTreeCmp = new Composite(tparent, SWT.NONE);
		resourceTreeCmp.setLayoutData(new GridData(GridData.FILL_BOTH));
		resourceTreeCmp.setLayout(new GridLayout(2, true));

		Label leftLbl = new Label(resourceTreeCmp, SWT.NONE);
		leftLbl.setText(Messages.getString("importProjectWizardPage.projectOfImport"));

		Label rightLbl = new Label(resourceTreeCmp, SWT.NONE);
		rightLbl.setText(Messages.getString("importProjectWizardPage.repeatedContent"));

		// 定义两颗树
		selectContentProvider = new SelectProjectContentProvider();
		repeateContentProvider = new RepeatProjectContentProvider();
		selectElementTree = new ResourceTree(resourceTreeCmp, selectContentProvider, new ProjectLabelProvider());
		repeatElementTree = new ResourceTree(resourceTreeCmp, repeateContentProvider, new ProjectLabelProvider());

		// 左边的按钮
		Composite leftBtnCmp = new Composite(resourceTreeCmp, SWT.NONE);
		leftBtnCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(3).applyTo(leftBtnCmp);

		leftSelectAllBtn = new Button(leftBtnCmp, SWT.PUSH);
		leftSelectAllBtn.setText(Messages.getString("importProjectWizardPage.leftSelectAllBtn"));
		leftSelectAllBtn.addListener(SWT.Selection, this);
		setButtonLayoutData(leftSelectAllBtn);

		leftDisSelectAllBtn = new Button(leftBtnCmp, SWT.PUSH);
		leftDisSelectAllBtn.setText(Messages.getString("importProjectWizardPage.leftDisSelectAllBtn"));
		leftDisSelectAllBtn.addListener(SWT.Selection, this);
		setButtonLayoutData(leftDisSelectAllBtn);

		leftAllExpandBtn = new Button(leftBtnCmp, SWT.PUSH);
		leftAllExpandBtn.setText(Messages.getString("importProjectWizardPage.leftExpandAllBtn"));
		leftAllExpandBtn.addListener(SWT.Selection, this);
		setButtonLayoutData(leftAllExpandBtn);

		// 右边的按钮
		Composite rightBtnCmp = new Composite(resourceTreeCmp, SWT.NONE);
		rightBtnCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(3).applyTo(rightBtnCmp);

		rightSelectAllBtn = new Button(rightBtnCmp, SWT.PUSH);
		rightSelectAllBtn.setText(Messages.getString("importProjectWizardPage.rightSelectAllBtn"));
		rightSelectAllBtn.addListener(SWT.Selection, this);
		setButtonLayoutData(rightSelectAllBtn);

		rightDisSelectAllBtn = new Button(rightBtnCmp, SWT.PUSH);
		rightDisSelectAllBtn.setText(Messages.getString("importProjectWizardPage.rightDisSelectAllBtn"));
		rightDisSelectAllBtn.addListener(SWT.Selection, this);
		setButtonLayoutData(rightDisSelectAllBtn);

		rightAllExpandBtn = new Button(rightBtnCmp, SWT.PUSH);
		rightAllExpandBtn.setText(Messages.getString("importProjectWizardPage.rightExpandAllBtn"));
		rightAllExpandBtn.addListener(SWT.Selection, this);
		setButtonLayoutData(rightAllExpandBtn);

		// 初始化两颗树的事件
		initTreeListener();
		selectElementTree.setInput(this);
		repeatElementTree.setInput(this);
	}

	public void handleEvent(Event event) {
		if (event.widget == browseBtn) {
			handleBrowseBtnPressed();
		} else if (event.widget == leftSelectAllBtn) {
			// 执行全部选择(左边树)
			selectElementTreeBtnSelectFunction(true);
		} else if (event.widget == leftDisSelectAllBtn) {
			// 执行全部不选(右边树)
			selectElementTreeBtnSelectFunction(false);
		} else if (event.widget == leftAllExpandBtn) {
			selectElementTree.expandAll();
		} else if (event.widget == rightSelectAllBtn) {
			// 执行全部选择(左边树)
			checkRepeatTreeBtnSelectFunction(true);
		} else if (event.widget == rightDisSelectAllBtn) {
			// 执行全部不选(右边树)
			checkRepeatTreeBtnSelectFunction(false);
		} else if (event.widget == rightAllExpandBtn) {
			repeatElementTree.expandAll();
		}
		setPageCompleteState(OTHER_TYPE);
	}

	/**
	 * 重复树点击选择所有以及所有不选择按钮时执行的方法
	 */
	private void checkRepeatTreeBtnSelectFunction(final boolean checkState) {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				repeatElementTree.setCheckedAll(checkState);
				for (Object obj : repeatElementTree.getAllElement()) {
					if (obj instanceof ProjectResource) {
						ProjectResource proResourceElement = (ProjectResource) obj;
						proResourceElement.setNeedCover(repeatElementTree.getChecked(proResourceElement));
					}
				}
				repeatElementTree.refresh(true);
			}
		});
	}

	/**
	 * 点击选择所有以及所有不选择按钮时执行的方法
	 * @param event
	 */
	private void selectElementTreeBtnSelectFunction(final boolean checkState) {
		checkedRepeatElementMap.clear();
		selectElementTree.setCheckedAll(checkState);
		Object[] checkedElementArray = selectElementTree.getCheckedElements();
		for (Object checkedElement : checkedElementArray) {
			if (checkedElement instanceof ProjectResource) {
				ProjectResource proResourceElement = (ProjectResource) checkedElement;
				String projectName = proResourceElement.getProjectName();
				// 如果是重复的资源，那么添加到缓存中
				if (proResourceElement.isProjectRepeat()) {
					if (checkedRepeatElementMap.containsKey(projectName)) {
						Set<ProjectResource> checkedElementSet = checkedRepeatElementMap.get(projectName);
						checkedElementSet.add(proResourceElement);
						checkedRepeatElementMap.put(projectName, checkedElementSet);
					} else {
						Set<ProjectResource> checkedElementSet = new HashSet<ProjectResource>();
						checkedElementSet.add(proResourceElement);
						checkedRepeatElementMap.put(projectName, checkedElementSet);
					}
				}
			}
		}
		setNotCoverForNotcheckOfSelectTree();

		repeatElementTree.refresh();
		repeatElementTree.expandAll();
		checkRepeatTreeBtnSelectFunction(true);
	}

	/**
	 * 初始化两颗树的事件
	 */
	private void initTreeListener() {
		selectElementTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
					public void run() {
						selectElementTreeCheckFunction(event);
						setNotCoverForNotcheckOfSelectTree();
						setPageCompleteState(OTHER_TYPE);
					}
				});
			}
		});

		repeatElementTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
					public void run() {
						Object element = event.getElement();
						if (element instanceof ProjectResource) {
							ProjectResource proResourceElement = (ProjectResource) element;
							if (repeatElementTree.getChecked(proResourceElement)) {
								proResourceElement.setNeedCover(true);
							} else {
								proResourceElement.setNeedCover(false);
							}
							// 处理所有的子文件或文件夹，以保存证他们都处于要覆盖状态
							checkRepeatTreeChildCover(proResourceElement);
							// 处理当前节点的所有父节点
							ProjectResource parentProRes = proResourceElement.getParent();
							while (parentProRes != null) {
								parentProRes.setNeedCover(repeatElementTree.getChecked(parentProRes));
								parentProRes = parentProRes.getParent();
							}
							repeatElementTree.refresh(true);
						}
						setPageCompleteState(OTHER_TYPE);
					}
				});
			}
		});
	}

	/**
	 * 左边树点击事件触发的方法
	 * @param event
	 */
	private void selectElementTreeCheckFunction(CheckStateChangedEvent event) {
		checkedRepeatElementMap.clear();
		Object[] checkedElementArray = selectElementTree.getCheckedElements();
		for (Object checkedElement : checkedElementArray) {
			if (checkedElement instanceof ProjectResource) {
				ProjectResource proResourceElement = (ProjectResource) checkedElement;
				String projectName = proResourceElement.getProjectName();
				// 如果是重复的资源，那么添加到缓存中
				if (proResourceElement.isProjectRepeat()) {
					if (checkedRepeatElementMap.containsKey(projectName)) {
						Set<ProjectResource> checkedElementSet = checkedRepeatElementMap.get(projectName);
						checkedElementSet.add(proResourceElement);
						checkedRepeatElementMap.put(projectName, checkedElementSet);
					} else {
						Set<ProjectResource> checkedElementSet = new HashSet<ProjectResource>();
						checkedElementSet.add(proResourceElement);
						checkedRepeatElementMap.put(projectName, checkedElementSet);
					}
				}
			}
		}

		boolean noData = repeatElementTree.getTree().getItemCount() <= 0;
		repeatElementTree.refresh();
		if (noData) {
			checkRepeatTreeBtnSelectFunction(true);
			repeatElementTree.expandAll();
		}

	}

	/**
	 * 将左边树中未选择的节点的 needCover 属性恢复默认状态 备注：主要是修改 一个属性同步的BUG ： 全选左边树，再全选右边树，这时全不选左边树，这时所有属性
	 */
	private void setNotCoverForNotcheckOfSelectTree() {
		for (ProjectResource proSourceElement : selectElementTree.getAllElement()) {
			if (!selectElementTree.getChecked(proSourceElement)) {
				proSourceElement.restoreNeedCoverDefault();
			}
		}
	}

	/**
	 * 重复树点击事件触发的方法，该方法主要是实现用户针对重复文件的覆盖选择操作
	 * @param event
	 */
	private void checkRepeatTreeChildCover(ProjectResource parentElemnt) {
		Object[] childArray = repeateContentProvider.getChildren(parentElemnt);
		if (childArray == null || childArray.length <= 0) {
			return;
		}
		for (Object childObj : childArray) {
			if (childObj instanceof ProjectResource) {
				ProjectResource proResourceChild = (ProjectResource) childObj;
				proResourceChild.setNeedCover(repeatElementTree.getChecked(proResourceChild));
				checkRepeatTreeChildCover(proResourceChild);
			}
		}
	}

	protected void handleBrowseBtnPressed() {
		FileDialog dialog = new FileDialog(filePathTxt.getShell(), SWT.SHEET);
		dialog.setFilterExtensions(FILE_IMPORT_MASK);
		dialog.setText(Messages.getString("importProjectWizardPage.SelectArchiveDialogTitle"));

		String fileName = filePathTxt.getText().trim();
		if (fileName.length() == 0) {
			fileName = previouslyBrowsedPath;
		}

		if (fileName.length() == 0) {
			// IDEWorkbenchPlugin.getPluginWorkspace()
			dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
		} else {
			File path = new File(fileName).getParentFile();
			if (path != null && path.exists()) {
				dialog.setFilterPath(path.toString());
			}
		}

		String selectedArchive = dialog.open();
		if (selectedArchive != null) {
			previouslyBrowsedPath = selectedArchive;
			filePathTxt.setText(previouslyBrowsedPath);

			// 先择新的项目包时，清除重复树中的所有内容
			checkedRepeatElementMap.clear();
			repeatElementTree.refresh(true);

			updateProjectsList(selectedArchive);
			// 初始化时全部初始化
			selectElementTree.expandAll();
			selectElementTreeBtnSelectFunction(true);
		}
	}

	public void updateProjectsList(final String path) {
		if (path == null || path.length() == 0) {
			setMessage(Messages.getString("wizard.ImportProjectWizardPage.desc"));
			selectedProjects = new ProjectRecord[0];
			setPageComplete(selectElementTree.getCheckedElements().length > 0);
			lastPath = path;
			return;
		}

		final File directory = new File(path);
		long modified = directory.lastModified();
		if (path.equals(lastPath) && lastModified == modified && lastCopyFiles == copyFiles) {
			// since the file/folder was not modified and the path did not
			// change, no refreshing is required
			return;
		}

		lastPath = path;
		lastModified = modified;
		lastCopyFiles = copyFiles;

		// We can't access the radio button from the inner class so get the
		// status beforehand
		final boolean dirSelected = false;
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@SuppressWarnings({ "rawtypes", "restriction" })
				public void run(IProgressMonitor monitor) {

					monitor.beginTask(Messages.getString("importProjectWizardPage.searchingMessage"), 100);
					selectedProjects = new ProjectRecord[0];
					Collection files = new ArrayList();
					monitor.worked(10);
					if (!dirSelected && ArchiveFileManipulations.isTarFile(path)) {
						TarFile sourceTarFile = getSpecifiedTarSourceFile(path);
						if (sourceTarFile == null) {
							return;
						}

						structureProvider = new TarLeveledStructureProvider(sourceTarFile);
						Object child = structureProvider.getRoot();

						if (!collectProjectFilesFromProvider(files, child, 0, monitor)) {
							return;
						}
						Iterator filesIterator = files.iterator();
						selectedProjects = new ProjectRecord[files.size()];
						int index = 0;
						monitor.worked(50);
						monitor.subTask(Messages.getString("importProjectWizardPage.processingMessage"));
						while (filesIterator.hasNext()) {
							selectedProjects[index++] = (ProjectRecord) filesIterator.next();
						}
					} else if (!dirSelected && ArchiveFileManipulations.isZipFile(path)) {
						ZipFile sourceFile = getSpecifiedZipSourceFile(path);
						if (sourceFile == null) {
							return;
						}
						structureProvider = new ZipLeveledStructureProvider(sourceFile);
						Object child = structureProvider.getRoot();

						if (!collectProjectFilesFromProvider(files, child, 0, monitor)) {
							return;
						}
						Iterator filesIterator = files.iterator();
						selectedProjects = new ProjectRecord[files.size()];
						int index = 0;
						monitor.worked(50);
						monitor.subTask(Messages.getString("importProjectWizardPage.processingMessage"));
						while (filesIterator.hasNext()) {
							selectedProjects[index++] = (ProjectRecord) filesIterator.next();
						}
					} else {
						monitor.worked(60);
					}
					monitor.done();
				}

			});
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// 开始处理导入项目中的项目名称不合法的情况
		String projectName = "";
		StringBuffer errorProjectNameSB = new StringBuffer();
		StringBuffer errorCharSB = new StringBuffer();
		List<ProjectRecord> tempList = new ArrayList<ProjectRecord>();
		boolean isError = false;
		for (int i = 0; i < selectedProjects.length; i++) {
			projectName = selectedProjects[i].getProjectName();
			isError = false;
			for (int j = 0; j < Constant.RESOURCE_ERRORCHAR.length; j++) {
				if (projectName.indexOf(Constant.RESOURCE_ERRORCHAR[j]) != -1) {
					errorCharSB.append(Constant.RESOURCE_ERRORCHAR[j]);
					errorProjectNameSB.append(projectName + ", ");
					isError = true;
				}
			}
			if (!isError) {
				tempList.add(selectedProjects[i]);
			}
		}
		if (errorProjectNameSB.length() > 0) {
			final String errorTip = MessageFormat.format(
					Messages.getString("importProjectWizardPage.projectError"),
					new Object[] {
							errorProjectNameSB.toString().substring(0, errorProjectNameSB.toString().length() - 2),
							errorCharSB.toString() });
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(getShell(), Messages.getString("importProject.all.dialog.warning"),
							errorTip);
				}
			});
		}
		selectedProjects = tempList.toArray(new ProjectRecord[tempList.size()]);
		setPageComplete(selectElementTree.getCheckedElements().length > 0);

		selectElementTree.refresh(true);
	}

	public static void main(String[] args) {
		String[] a = new String[] { "1", "2", "3", "4" };
		String[] b = new String[a.length];
		for (int i = 0; i < 2; i++) {
			b[i] = a[i];
		}

		a = b;
		System.out.println(a.length);
		for (int i = 0; i < a.length; i++) {
			System.out.println(a[i]);
		}

	}

	@SuppressWarnings("restriction")
	private TarFile getSpecifiedTarSourceFile(String fileName) {
		if (fileName.length() == 0) {
			return null;
		}

		try {
			return new TarFile(fileName);
		} catch (TarException e) {
			displayErrorDialog(Messages.getString("importProjectWizardPage.dialog.badFormat"));
		} catch (IOException e) {
			displayErrorDialog(Messages.getString("importProjectWizardPage.dialog.couldNotRead"));
		}

		filePathTxt.setFocus();
		return null;
	}

	/**
	 * 获取左边树（选择项目资源树）的输入内容
	 * @return
	 */
	private ProjectResource[] getProjectResourceInput() {
		rootProjectArray = null;
		List<ProjectResource> proResourceList = new ArrayList<ProjectResource>();
		for (int i = 0; i < selectedProjects.length; i++) {
			proResourceList.add(new ProjectResource(selectedProjects[i].getParent(), selectedProjects[i],
					structureProvider));
		}
		rootProjectArray = (ProjectResource[]) proResourceList.toArray(new ProjectResource[proResourceList.size()]);
		selectElementTree.setRoot(rootProjectArray);
		return rootProjectArray;
	}

	/**
	 * 获取右边树（重复项目资源）的输入内容，即获取重复的项目资源
	 * @return
	 */
	private ProjectResource[] getRepeateProResourceInput() {
		List<ProjectResource> proResourceList = new ArrayList<ProjectResource>();
		for (ProjectResource proResource : rootProjectArray) {
			String projectName = proResource.getProjectName();
			if (checkedRepeatElementMap.containsKey(projectName)) {
				proResourceList.add(proResource);
			}
		}

		ProjectResource[] root = (ProjectResource[]) proResourceList
				.toArray(new ProjectResource[proResourceList.size()]);
		repeatElementTree.setRoot(root);
		return root;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean collectProjectFilesFromProvider(Collection files, Object entry, int level, IProgressMonitor monitor) {

		if (monitor.isCanceled()) {
			return false;
		}
		monitor.subTask(NLS.bind(Messages.getString("importProjectWizardPage.checkingMessage"),
				structureProvider.getLabel(entry)));
		List children = structureProvider.getChildren(entry);
		if (children == null) {
			children = new ArrayList(1);
		}
		Iterator childrenEnum = children.iterator();
		while (childrenEnum.hasNext()) {
			Object child = childrenEnum.next();
			if (structureProvider.isFolder(child)) {
				collectProjectFilesFromProvider(files, child, level + 1, monitor);
			}
			String elementLabel = structureProvider.getLabel(child);
			if (elementLabel.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
				files.add(new ProjectRecord(child, entry, level));
			}
		}
		return true;
	}

	private ZipFile getSpecifiedZipSourceFile(String fileName) {
		if (fileName.length() == 0) {
			return null;
		}

		try {
			return new ZipFile(fileName);
		} catch (ZipException e) {
			displayErrorDialog(Messages.getString("importProjectWizardPage.dialog.badFormat"));
		} catch (IOException e) {
			displayErrorDialog(Messages.getString("importProjectWizardPage.dialog.couldNotRead"));
		}

		filePathTxt.setFocus();
		return null;
	}

	protected void displayErrorDialog(String message) {
		MessageDialog.openError(getShell(), Messages.getString("importProjectWizardPage.dialog.error"), message);
	}

	public void performCancel() {
		disposeImg();
	}

	/**
	 * 创建项目
	 * @return
	 */
	public boolean createProjects(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (ProjectResource projRoot : rootProjectArray) {
					if (selectElementTree.getChecked(projRoot)) {
						totalWorkSum++;
					}
				}
			}
		});
		monitor.beginTask(Messages.getString("importProjectWizardPage.beginImportProj"), totalWorkSum);

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				openedIfileMap = getAllOpenedIFile();
			}
		});

		try {
			// 一个项目一个项目的进行相关处理
			for (final ProjectResource projRoot : rootProjectArray) {
				// 如果当前项目未被选择。跳过继续执行
				runContinue = false;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (!selectElementTree.getChecked(projRoot)) {
							runContinue = true;
						}
					}
				});
				if (runContinue) {
					continue;
				}

				monitor.setTaskName(MessageFormat.format(
						Messages.getString("importProjectWizardPage.beginImportProjName"),
						new Object[] { projRoot.getProjectName() }));

				final IProject newProject = root.getProject(projRoot.getProjectName());
				if (!projRoot.isProjectRepeat()) {
					// 创建项目
					newProject.create(null);
					if (!newProject.isOpen()) {
						newProject.open(null);
					}
				}

				IFile configIFile = newProject.getFile(Constant.FILE_CONFIG);
				ProjectConfigerUtil configUtil = null;
				if (configIFile.exists()) {
					configUtil = new ProjectConfigerUtil(newProject);
					configUtil.setDbMementos();
					configUtil.clearConfig();
				}
				final StringBuilder sb = new StringBuilder();
				try {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {							
								try {
									createProjectImpl(projRoot, newProject);
								} catch (Exception e) {
									sb.append(Messages.getString("importProjectWizardPage.importFail"));
									LOGGER.error("",e);
								}
							
						}
					});
				} finally {
					if (null != configUtil) {
						configUtil.restoreMementos();
					}
					if(!sb.toString().isEmpty()){
						monitor.done();
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openWarning(getShell(), Messages.getString("importProject.all.dialog.warning"),
										Messages.getString("importProjectWizardPage.importFail"));
							}
						});
						return false;
					}
					
				}

				createConfigFile(projRoot, newProject);

				createRequireFolder(newProject);
				resetConfigContent(newProject);
				monitor.worked(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			monitor.done();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(getShell(), Messages.getString("importProject.all.dialog.warning"),
							Messages.getString("importProjectWizardPage.importFail"));
				}
			});
			LOGGER.error(Messages.getString("importProjectWizardPage.LOGG.importEroor"), e);
		//	disposeImg();
			return false;
		}
		disposeImg();
		monitor.done();

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getShell(), Messages.getString("importProject.all.dialog.info"),
						Messages.getString("importProjectWizardPage.importSuccess"));
			}
		});

		return true;
	}

	/**
	 * 创建 .config 文件夹
	 * @param projRoot
	 * @param newProject
	 */
	private void createConfigFile(ProjectResource projRoot, IProject newProject) {
		try {
			IFile configIFile = newProject.getFile(Constant.FILE_CONFIG);

			if (!configIFile.exists()) {
				InputStream configStream = projRoot.getConfigFileContent();
				configIFile.create(configStream, true, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(Messages.getString("importProjectWizardPage.LOGG.importEroor"), e);
		}
	}

	public void resetConfigContent(IProject project) {
		ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(project);
		if (null == projectConfig) {
			return;
		}
		// FIX Bug#3709 导入项目时更新库--打开XLIFF文件时无法更新同名的记忆库/术语库
		projectConfig.reloadConfig();// 重新加载一次数据，如果ProjectConfigerFactory缓存中含有导入项目的名称时，有bug
		ProjectInfoBean currentProjectConfig = projectConfig.getCurrentProjectConfig();
		List<DatabaseModelBean> tmDb = currentProjectConfig.getTmDb();
		int index = 0;
		for (DatabaseModelBean bean : tmDb) {// update tm config
			if ("SQLite".equals(bean.getDbType())) {
				resetSqliteTMNameAndPath(project, bean, index);
				index++;
			}
		}

		List<DatabaseModelBean> tbDb = currentProjectConfig.getTbDb();
		index = 0;
		for (DatabaseModelBean bean : tbDb) {// update tb config
			if ("SQLite".equals(bean.getDbType())) {
				resetSqliteTBNameAndPath(project, bean, index);
				index++;
			}
		}
		projectConfig.updateProjectConfig(currentProjectConfig);
		try {
			project.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void resetSqliteTMNameAndPath(IProject project, DatabaseModelBean bean, int index) {
		IFolder folder = project.getFolder("TM");
		if (!folder.exists()) {
			return;
		}
		String dbName = bean.getDbName();
		IFile file = folder.getFile("Exported_" + index + "_" + dbName);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String rootLocation = root.getLocation().toOSString();
		String fileSeparator = System.getProperty("file.separator");
		if (file.exists()) {
			bean.setDbName("Exported_" + index + "_" + dbName);
		}
		if (file.exists() || folder.getFile(dbName).exists()) {
			bean.setItlDBLocation(rootLocation + fileSeparator + project.getName() + fileSeparator + "TM");

		}
	}

	private void resetSqliteTBNameAndPath(IProject project, DatabaseModelBean bean, int index) {
		IFolder folder = project.getFolder("TB");
		if (!folder.exists()) {
			return;
		}
		String dbName = bean.getDbName();
		IFile file = folder.getFile("Exported_" + index + "_" + dbName);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String rootLocation = root.getLocation().toOSString();
		String fileSeparator = System.getProperty("file.separator");
		if (file.exists()) {// FIXME
			bean.setDbName("Exported_" + index + "_" + dbName);
		}
		if (file.exists() || folder.getFile(dbName).exists()) {
			bean.setItlDBLocation(rootLocation + fileSeparator + project.getName() + fileSeparator + "TB");

		}
	}

	/**
	 * 如果这几个必须的文件夹在项目中不存在，则创建。创建几个必须的文件夹，如下： Intermediate[Report, SKL], Source, Target, XLIFF 。
	 */
	private void createRequireFolder(IProject newProject) {
		try {
			// 从上到下，先处理 Intermediate
			IFolder intermediateFolder = newProject.getFolder(Constant.FOLDER_INTERMEDDIATE);
			if (!intermediateFolder.exists()) {
				intermediateFolder.create(true, true, null);
			}

			// 再处理 Report
			IFolder reportFolder = intermediateFolder.getFolder(Constant.FOLDER_REPORT);
			if (!reportFolder.exists()) {
				reportFolder.create(true, true, null);
			}

			// 再处理 SKL
			IFolder sklFolder = intermediateFolder.getFolder(Constant.FOLDER_SKL);
			if (!sklFolder.exists()) {
				sklFolder.create(true, true, null);
			}

			// 再处理 source
			IFolder srcFolder = newProject.getFolder(Constant.FOLDER_SRC);
			if (!srcFolder.exists()) {
				srcFolder.create(true, true, null);
			}

			// 再处理 target
			IFolder tgtFolder = newProject.getFolder(Constant.FOLDER_TGT);
			if (!tgtFolder.exists()) {
				tgtFolder.create(true, true, null);
			}

			// 再处理 xliff
			IFolder xlfFolder = newProject.getFolder(Constant.FOLDER_XLIFF);
			if (!xlfFolder.exists()) {
				xlfFolder.create(true, true, null);
			}

		} catch (Exception e) {
			LOGGER.error(Messages.getString("importProjectWizardPage.LOGG.importEroor"), e);
		}
	}

	private void createProjectImpl(ProjectResource projRoot, IProject newProject) throws Exception {

		try {

			for (Object obj : selectContentProvider.getChildren(projRoot)) {
				if (obj instanceof ProjectResource) {
					ProjectResource proResource = (ProjectResource) obj;

					// 如果没有选中，那么不进行导入操作
					if (!selectElementTree.getChecked(proResource)) {
						continue;
					}

					if (proResource.isFolder()) {
						// 如果是文件夹，则创建
						IFolder childFolder = newProject.getFolder(proResource.getLabel());
						if (!childFolder.exists()) {
							childFolder.create(true, true, null);
						}
						createCurProjectResource(proResource, childFolder);
					} else {
						// 如果是文件，则判断是否需要覆盖，若是，则直接覆盖
						if (proResource.isNeedCover()) {
							IFile iFile = newProject.getFile(proResource.getLabel());
							InputStream inputStream = proResource.getInputStream();
							if (inputStream != null) {
								if (iFile.exists()) {
									closeOpenedFile(iFile);
									iFile.delete(true, null);
								}
								iFile.create(inputStream, true, null);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			//disposeImg();
			LOGGER.error(Messages.getString("importProjectWizardPage.LOGG.importEroor"), e);
			throw e;
		}

	}

	/**
	 * 获取指定节点被选择的子节点
	 * @param parentResource
	 *            树节点的父节点
	 * @param parentFolder
	 *            项目空间的父文件夹
	 * @return
	 */
	private void createCurProjectResource(ProjectResource parentResource, IFolder parentContainer) throws Exception {
		if (parentResource == null || parentContainer == null) {
			return;
		}
		for (Object obj : selectContentProvider.getChildren(parentResource)) {
			if (obj instanceof ProjectResource) {
				ProjectResource proResource = (ProjectResource) obj;
				if (!selectElementTree.getChecked(proResource)) {
					continue;
				}

				// 如果是文件夹，如果没有创建，直接创建
				if (proResource.isFolder()) {
					IFolder childFolder = parentContainer.getFolder(proResource.getLabel());
					if (!childFolder.exists()) {
						childFolder.create(true, true, null);
					}
					createCurProjectResource(proResource, childFolder);
				} else {
					// 如果是文件，则判断是否需要覆盖，若是，则直接覆盖
					if (proResource.isNeedCover()) {
						IFile iFile = parentContainer.getFile(proResource.getLabel());
						InputStream inputStream = proResource.getInputStream();
						if (inputStream != null) {
							if (iFile.exists()) {
								closeOpenedFile(iFile);
								iFile.delete(true, null);
							}
							iFile.create(inputStream, true, null);
						}
					}
				}
			}
		}
	}

	/**
	 * 设置向导界面状态，以及相关提示信息
	 */
	private void setPageCompleteState(int type) {
		if (INIT_TYPE == type) {
			setPageComplete(false);
			return;
		}
		String filepath = filePathTxt.getText().trim();
		if (filepath.length() <= 0) {
			setErrorMessage(Messages.getString("importProjectWizardPage.selectImportFilePath"));
			setPageComplete(false);
		} else if (rootProjectArray.length == 0) {
			if (filepath.endsWith(Constant.PROJECT_PACK_EXTENSSION)) {
				// setErrorMessage(Messages.getString("importProjectWizardPage.selectImportFilePath"));
				setMessage(Messages.getString("importProjectWizardPage.noProjectsToImport"), WARNING);
				setPageComplete(false);
			} else {
				setErrorMessage(Messages.getString("importProjectWizardPage.selectRightImportFilePath"));
				setPageComplete(false);
			}
		} else {
			if (selectElementTree.getCheckedElements().length <= 0) {
				setErrorMessage(Messages.getString("importProjectWizardPage.selectImportProject"));
				setPageComplete(false);
			} else {
				if (repeatElementTree.getTree().getItemCount() > 0) {
					if (repeatElementTree.getCheckedElements().length <= 0) {
						setErrorMessage(null);
						setMessage(Messages.getString("importProjectWizardPage.selectNeedCoverFile"));
						setPageComplete(true);
					} else {
						setErrorMessage(null);
						setMessage(null);
						setPageComplete(true);
					}
				} else {
					setErrorMessage(null);
					setMessage(null);
					setPageComplete(true);
				}
			}
		}
	}

	/**
	 * 当项目导入完成时，关闭已经打开的文件(针对通过编辑器打开，特别是　nattble　与　html 编辑器) 　备注：这里特别要注意合并打开的情况。
	 */
	private void closeOpenedFile(final IFile iFile) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IEditorPart editor = openedIfileMap.get(iFile);
				if (editor == null) {
					return;
				}

				page.closeEditor(editor, true);
			}
		});

	}

	/**
	 * 获取所有已经被打开的文件
	 * @return
	 */
	private Map<IFile, IEditorPart> getAllOpenedIFile() {
		Map<IFile, IEditorPart> openedIfileMap = new HashMap<IFile, IEditorPart>();

		IEditorReference[] referenceArray = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		for (IEditorReference reference : referenceArray) {
			IEditorPart editor = reference.getEditor(true);
			// 如果这是一个　nattable 编辑器
			if (XLIFF_EDITOR_ID.equals(editor.getSite().getId())) {
				IXliffEditor xlfEditor = (IXliffEditor) editor;
				if (xlfEditor.isMultiFile()) {
					for (File file : xlfEditor.getMultiFileList()) {
						openedIfileMap.put(ResourceUtils.fileToIFile(file.getAbsolutePath()), editor);
					}
				} else {
					openedIfileMap.put(((FileEditorInput) editor.getEditorInput()).getFile(), editor);
				}
			} else {
				// 其他情况，直接将文件丢进去就行了
				openedIfileMap.put(((FileEditorInput) editor.getEditorInput()).getFile(), editor);
			}
		}
		return openedIfileMap;
	}

	/**
	 * 销毁图片资源
	 */
	private void disposeImg() {
		if(null != projectImg && !projectImg.isDisposed()){			
			projectImg.dispose();
		}
		if(null != folderImg&& !folderImg.isDisposed()){			
			folderImg.dispose();
		}
		if(null !=hsXLiffImg && !hsXLiffImg.isDisposed() ){			
			hsXLiffImg.dispose();
		}
		if(null !=htmlImg && !htmlImg.isDisposed()){			
			htmlImg.dispose();
		}
		if(null !=defaultImg && !defaultImg.isDisposed()){			
			defaultImg.dispose();
		}
		for (Entry<String, Image> entry : imgMap.entrySet()) {
			Image value = entry.getValue();
			if(null != value && !value.isDisposed()){				
				value.dispose();
			}
		}
		imgMap.clear();
	}

	// ------------------------- 下面是几个 provider 以及项目的封装类 -------------------------//

	/**
	 * 标签提供者
	 * @author robert
	 */
	private final class ProjectLabelProvider extends LabelProvider implements IColorProvider {
		public String getText(Object element) {
			if (element instanceof ProjectResource) {
				ProjectResource proResource = (ProjectResource) element;
				return proResource.getLabel();
			}
			return null;
		}

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			// 　测试代码
			// ProjectResource proResource = (ProjectResource) element;
			// if (proResource.isNeedCover()) {
			// return getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
			// }
			return null;
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof ProjectResource) {
				ProjectResource proResource = (ProjectResource) element;
				String fileName = proResource.getLabel();
				if (proResource.isProject()) {
					return projectImg;
				} else if (proResource.isFolder()) {
					return folderImg;
				} else if (fileName.endsWith(".hsxliff")) {
					return hsXLiffImg;
				} else if (fileName.endsWith(".html")) {
					return htmlImg;
				} else {
					int index = fileName.lastIndexOf(".");
					if (index != -1) {
						String extension = fileName.substring(index, fileName.length());
						if (imgMap.containsKey(extension)) {
							return imgMap.get(extension);
						}
						Program program = Program.findProgram(extension);
						if (program != null) {
							ImageData imageData = program.getImageData();
							if (imageData != null) {
								Image img = new Image(getShell().getDisplay(), imageData);
								imgMap.put(extension, img);
								return img;
							}
						}
					}
				}
			}
			return defaultImg;
		}
	}

	/**
	 * 内容提供者
	 * @author robert
	 */
	private final class SelectProjectContentProvider implements ITreeContentProvider {
		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			return getProjectResourceInput();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement == null) {
				return new Object[0];
			}
			return ((ProjectResource) parentElement).getChildren().toArray();
		}

		public Object getParent(Object element) {
			if (element instanceof ProjectResource) {
				return ((ProjectResource) element).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return ((ProjectResource) element).hasChildren();
		}
	}

	/**
	 * 右边重复项目树的内容提供者
	 * @author robert
	 */
	private final class RepeatProjectContentProvider implements ITreeContentProvider {
		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		public Object[] getElements(Object inputElement) {
			return getRepeateProResourceInput();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement == null) {
				return new Object[0];
			}
			List<ProjectResource> childList = new ArrayList<ProjectResource>();
			Object[] allChildren = selectContentProvider.getChildren(parentElement);
			for (Object obj : allChildren) {
				if (obj instanceof ProjectResource) {
					// 先查看该子节点在左边树中是否被选中
					ProjectResource proResource = (ProjectResource) obj;
					String projectName = proResource.getProjectName();
					if (checkedRepeatElementMap.get(projectName).contains(proResource)) {
						// 如果这是一个重复的资源，那么就显示在右边的重复内容树上面
						if (proResource.isElementRepeat()) {
							childList.add(proResource);
						}
					}
				}
			}
			return childList.toArray(new ProjectResource[childList.size()]);
		}

		public Object getParent(Object element) {
			if (element instanceof ProjectResource) {
				return selectContentProvider.getParent(element);
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return selectContentProvider.hasChildren(element);
		}
	}

	/**
	 * 项目记录
	 * @author robert 2013-03-08
	 */
	public class ProjectRecord {
		File projectSystemFile;
		/** 项目的标识文件 .project */
		Object projectArchiveFile;
		/** 项目名称 */
		String projectName;
		/** 项目标识文件.project 的父文件夹 */
		Object parent;
		int level;
		boolean hasConflicts;
		IProjectDescription description;

		/**
		 * Create a record for a project based on the info in the file.
		 * @param file
		 */
		ProjectRecord(File file) {
			projectSystemFile = file;
			setProjectName();
		}

		/**
		 * @param file
		 *            The Object representing the .project file
		 * @param parent
		 *            The parent folder of the .project file
		 * @param level
		 *            The number of levels deep in the provider the file is
		 */
		ProjectRecord(Object file, Object parent, int level) {
			this.projectArchiveFile = file;
			this.parent = parent;
			this.level = level;
			setProjectName();
		}

		/**
		 * Set the name of the project based on the projectFile.
		 */
		@SuppressWarnings("restriction")
		private void setProjectName() {
			try {
				if (projectArchiveFile != null) {
					InputStream stream = structureProvider.getContents(projectArchiveFile);

					// If we can get a description pull the name from there
					if (stream == null) {
						if (projectArchiveFile instanceof ZipEntry) {
							IPath path = new Path(((ZipEntry) projectArchiveFile).getName());
							projectName = path.segment(path.segmentCount() - 2);
						} else if (projectArchiveFile instanceof TarEntry) {
							IPath path = new Path(((TarEntry) projectArchiveFile).getName());
							projectName = path.segment(path.segmentCount() - 2);
						}
					} else {
						description = ResourcesPlugin.getWorkspace().loadProjectDescription(stream);
						stream.close();
						projectName = description.getName();
					}
				}

				// If we don't have the project name try again
				if (projectName == null) {
					IPath path = new Path(projectSystemFile.getPath());
					// if the file is in the default location, use the directory
					// name as the project name
					if (isDefaultLocation(path)) {
						projectName = path.segment(path.segmentCount() - 2);
						description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
						// IProject project =
					} else {
						description = ResourcesPlugin.getWorkspace().loadProjectDescription(path);
						projectName = description.getName();
					}

				}
			} catch (CoreException e) {
				// no good couldn't get the name
			} catch (IOException e) {
				// no good couldn't get the name
			}
		}

		/**
		 * Returns whether the given project description file path is in the default location for a project
		 * @param path
		 *            The path to examine
		 * @return Whether the given path is the default location for a project
		 */
		private boolean isDefaultLocation(IPath path) {
			// The project description file must at least be within the project,
			// which is within the workspace location
			if (path.segmentCount() < 2)
				return false;
			return path.removeLastSegments(2).toFile().equals(Platform.getLocation().toFile());
		}

		/**
		 * Get the name of the project
		 * @return String
		 */
		public String getProjectName() {
			return projectName;
		}

		/**
		 * Gets the label to be used when rendering this project record in the UI.
		 * @return String the label
		 * @since 3.4
		 */
		public String getProjectLabel() {
			if (description == null)
				return projectName;

			String path = projectSystemFile == null ? structureProvider.getLabel(parent) : projectSystemFile
					.getParent();
			return NLS.bind(Messages.getString("importProjectWizardPage.projectLabel"), projectName, path);
		}

		/**
		 * @return Returns the hasConflicts.
		 */
		public boolean hasConflicts() {
			return hasConflicts;
		}

		public Object getParent() {
			return parent;
		}
	}

}
