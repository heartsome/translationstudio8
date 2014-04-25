package net.heartsome.cat.ts.handlexlf.wizard;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.ui.dialog.FileFolderSelectionDialog;
import net.heartsome.cat.ts.handlexlf.Activator;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.handlexlf.split.SplitOrMergeXlfModel;
import net.heartsome.cat.ts.handlexlf.split.TableViewerLabelProvider;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 合并xliff文件的向导页面
 * @author robert
 */
@SuppressWarnings("restriction")
public class MergeXliffWizardPage extends WizardPage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MergeXliffWizardPage.class);
	
	/** 显示要合并的xliff文件 */
	private TableViewer tableViewer;
	private SplitOrMergeXlfModel model;
	private IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	private List<IFile> exsistFileList = new LinkedList<IFile>();
	/** 项目路径 */
	private String projectPath = null;

	protected MergeXliffWizardPage(String pageName, SplitOrMergeXlfModel model) {
		super(pageName);
		this.model = model;
	}

	public void createControl(Composite parent) {
		setTitle(Messages.getString("wizard.MergeXliffWizardPage.title"));
		setMessage(Messages.getString("wizard.MergeXliffWizardPage.desc"));
		Composite tparent = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		tparent.setLayout(layout);

		GridDataFactory.fillDefaults().hint(600, 500).grab(true, true).applyTo(tparent);

		createMergeXlfGroup(tparent);
		setImageDescriptor(Activator.getImageDescriptor("images/file/file-merge-logo.png"));

		setControl(parent);

	}

	public void createMergeXlfGroup(Composite tparent) {
		final Group xliffDataGroup = new Group(tparent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(xliffDataGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(xliffDataGroup);
		xliffDataGroup.setText(Messages.getString("wizard.MergeXliffWizardPage.xliffDataGroup"));

		tableViewer = new TableViewer(xliffDataGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		final Table table = tableViewer.getTable();
		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		tableData.heightHint = 50;

		table.setLayoutData(tableData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] columnNames = new String[] { Messages.getString("wizard.MergeXliffWizardPage.columnNames1"),
				Messages.getString("wizard.MergeXliffWizardPage.columnNames2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(getSplitTableInfos());
		validXlf();
		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.15, 0.75 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		Composite buttonComp = new Composite(xliffDataGroup, SWT.None);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(8, 8).applyTo(buttonComp);
		GridDataFactory.fillDefaults().grab(false, true).hint(100, SWT.DEFAULT).applyTo(buttonComp);

		Button addbutton = new Button(buttonComp, SWT.NONE);
		addbutton.setText(Messages.getString("wizard.MergeXliffWizardPage.addbutton"));
		addbutton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addbutton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileFolderSelectionDialog dialog = new FileFolderSelectionDialog(xliffDataGroup.getShell(), true,
						IResource.FILE);
				dialog.setMessage(Messages.getString("wizard.MergeXliffWizardPage.dialogMsg"));
				dialog.setTitle(Messages.getString("wizard.MergeXliffWizardPage.dialogTitle"));
				dialog.setDoubleClickSelects(true);
				try {
					dialog.setInput(EFS.getStore(root.getLocationURI()));
				} catch (CoreException e1) {
					LOGGER.error("", e1);
					e1.printStackTrace();
				}

				dialog.addFilter(new ViewerFilter() {
					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						if (element instanceof LocalFile) {
							LocalFile folder = (LocalFile) element;
							if (folder.getName().equalsIgnoreCase(".hsConfig")
									|| folder.getName().equalsIgnoreCase(".metadata")
									|| folder.getName().equalsIgnoreCase(".config")
									|| folder.getName().equalsIgnoreCase(".nonTransElement")) {
								return false;
							}
							if (projectPath.equals(folder.toString())) {
								return true;
							}
							String xliffFolderPath = folder.toString();
							String tempPath = projectPath + System.getProperty("file.separator") + ".TEMP";
							String configPath = projectPath + System.getProperty("file.separator") + ".config";
							String projectFilePath = projectPath + System.getProperty("file.separator") + ".project";
							if (xliffFolderPath.startsWith(tempPath) || xliffFolderPath.startsWith(configPath)
									|| xliffFolderPath.startsWith(projectFilePath)) {
								return false;
							} else if (xliffFolderPath.startsWith(projectPath)) {
								return xliffFolderPath.substring(projectPath.length()).startsWith(
										System.getProperty("file.separator"));
							}
						}
						return false;
					}
				});
				dialog.create();
				dialog.open();
				if (dialog.getResult() != null) {
					Object[] selectFiles = dialog.getResult();
					
					XLFValidator.resetFlag();
					for (int i = 0; i < selectFiles.length; i++) {
						IFile iFile = root.getFileForLocation(Path.fromOSString(selectFiles[i].toString()));
						if (XLFValidator.validateXliffFile(iFile)) {
							// 如果该文件已经存在于列表中，就向添加到重复集合中
							if (model.getMergeXliffFile().indexOf(iFile) >= 0) {
								exsistFileList.add(iFile);
							}
							model.getMergeXliffFile().add(iFile);
						}
					}
					XLFValidator.resetFlag();
					tableViewer.setInput(getSplitTableInfos());
					if (!validIsRepeate()) {
						validXlf();
					}
					
//					for (int i = 0; i < selectFiles.length; i++) {
//						IFile file = root.getFileForLocation(Path.fromOSString(selectFiles[i].toString()));
//						// 如果该文件已经存在于列表中，就向添加到重复集合中
//						if (model.getMergeXliffFile().indexOf(file) >= 0) {
//							exsistFileList.add(file);
//						}
//						model.getMergeXliffFile().add(file);
//					}
//					tableViewer.setInput(getSplitTableInfos());
//					if (!validIsRepeate()) {
//						validXlf();
//					}
				}
			}
		});

		Button deleteButton = new Button(buttonComp, SWT.NONE);
		deleteButton.setText(Messages.getString("wizard.MergeXliffWizardPage.deleteButton"));
		deleteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				Table table = tableViewer.getTable();
				if (selection != null && !selection.isEmpty()) {
					int[] indices = table.getSelectionIndices();
					for (int index : indices) {
						String fileFullPath = table.getItem(index).getText(1);
						for (int i = 0; i < model.getMergeXliffFile().size(); i++) {
							if (model.getMergeXliffFile().get(i).getFullPath().toOSString().equals(fileFullPath)) {
								model.getMergeXliffFile().remove(i);
								break;
							}
						}

						// 如果该文件存在于重复集合中，则从该集合中删除
						for (int j = 0; j < exsistFileList.size(); j++) {
							if (exsistFileList.get(j).getFullPath().toOSString().equals(fileFullPath)) {
								exsistFileList.remove(j);
								break;
							}
						}
					}
					tableViewer.setInput(getSplitTableInfos());
				}
				if (!validIsRepeate()) {
					validXlf();
				}
			}
		});
	}

	/**
	 * 验证要合并的文件是否重复添加
	 * @return
	 */
	public boolean validIsRepeate() {
		String copyFilesTip = "";
		if (exsistFileList.size() > 0) {
			for (int index = 0; index < exsistFileList.size(); index++) {
				copyFilesTip += exsistFileList.get(index).getFullPath().toOSString() + "，";
			}
			copyFilesTip = copyFilesTip.substring(0, copyFilesTip.length() - 1);
			setErrorMessage(MessageFormat.format(Messages.getString("wizard.MergeXliffWizardPage.msg1"), copyFilesTip));
			setPageComplete(false);
			
		} else {
			return false;
		}
		return true;
	}

	/**
	 * 初步验证所选的xliff文件是否能合并。
	 */
	private void validXlf() {
		if (model.getMergeXliffFile().size() < 2) {
			setErrorMessage(Messages.getString("wizard.MergeXliffWizardPage.msg2"));
			setPageComplete(false);
			return;
		} else {
			// 验证文件是否是同一类型
			String fileExtension = "";
			for (IFile iFIle : model.getMergeXliffFile()) {
				if (iFIle.getFileExtension() == null || "".equals(iFIle.getFileExtension())) {
					setErrorMessage(MessageFormat.format(Messages.getString("wizard.MergeXliffWizardPage.msg3"), model
							.getMergeXliffFile().indexOf(iFIle) + 1) + 1);
					setPageComplete(false);
					return;
				}
				String curExtenstion = iFIle.getFileExtension();
				if ("".equals(fileExtension)) {
					fileExtension = curExtenstion;
				} else if (!curExtenstion.equals(fileExtension)) {
					setErrorMessage(MessageFormat.format(Messages.getString("wizard.MergeXliffWizardPage.msg4"), model
							.getMergeXliffFile().indexOf(iFIle) + 1));
					setPageComplete(false);
					return;
				}
			}
		}
		setPageComplete(true);
		setErrorMessage(null);
	}

	public String[][] getSplitTableInfos() {
		Vector<IFile> mergeFiles = model.getMergeXliffFile();
		if (mergeFiles != null && mergeFiles.size() > 0) {
			projectPath = mergeFiles.get(0).getProject().getLocation().toOSString();
		}
		ArrayList<String[]> mergeTableInfos = new ArrayList<String[]>();
		for (int i = 0; i < mergeFiles.size(); i++) {
			String[] tableInfo = new String[] { "" + (i + 1), mergeFiles.get(i).getFullPath().toOSString() };
			mergeTableInfos.add(tableInfo);
		}
		return mergeTableInfos.toArray(new String[][] {});
	}
}
