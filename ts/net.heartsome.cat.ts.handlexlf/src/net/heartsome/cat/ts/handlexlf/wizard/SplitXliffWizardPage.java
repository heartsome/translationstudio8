package net.heartsome.cat.ts.handlexlf.wizard;

import net.heartsome.cat.ts.handlexlf.Activator;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.handlexlf.split.SplitOrMergeXlfModel;
import net.heartsome.cat.ts.handlexlf.split.SplitXliff;
import net.heartsome.cat.ts.handlexlf.split.TableViewerLabelProvider;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class SplitXliffWizardPage extends WizardPage {
	private IFile splitFile;
	// /** 切割Xliff所需要的切割点, 保存的是序列号 */
	// private List<Integer> splitPoints;
	// private XLIFFEditorImplWithNatTable xliffEditor;
	private SplitOrMergeXlfModel model;
	private SplitXliff splitXliff;
	private IWorkspaceRoot root;

	private Text xliffNameTxt;
	private Text targetXlfPathTxt;
	private TableViewer tableViewer;
	private IContainer targetFolder;
	private String separator = "";

	protected SplitXliffWizardPage(String pageName, SplitOrMergeXlfModel model, SplitXliff splitXliff) {
		super(pageName);
		this.model = model;
		this.splitFile = model.getSplitFile();
		setTitle(Messages.getString("wizard.SplitXliffWizardPage.title"));
		this.splitXliff = splitXliff;
		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	public void createControl(Composite parent) {
		Composite tparent = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		tparent.setLayout(layout);

		GridDataFactory.fillDefaults().hint(600, 600).grab(true, true).applyTo(tparent);

		createSplitXlfNameGroup(tparent);
		createSplitInfo(tparent);
		setImageDescriptor(Activator.getImageDescriptor("images/file/file-split-logo.png"));
		setControl(parent);
	}

	/**
	 * 创建要分割文件的显示区
	 * @param tparent
	 */
	public void createSplitXlfNameGroup(Composite tparent) {
		final Group xliffDataGroup = new Group(tparent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(xliffDataGroup);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(xliffDataGroup);
		xliffDataGroup.setText(Messages.getString("wizard.SplitXliffWizardPage.xliffDataGroup"));

		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textData.widthHint = 200;

		Label xlfNameLbl = new Label(xliffDataGroup, SWT.RIGHT);
		xlfNameLbl.setText(Messages.getString("wizard.SplitXliffWizardPage.xlfNameLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(xlfNameLbl);

		xliffNameTxt = new Text(xliffDataGroup, SWT.BORDER);
		xliffNameTxt.setText(splitFile.getFullPath().toOSString());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(xliffNameTxt);
		xliffNameTxt.setEditable(false);

		Label targetFilsPathLbl = new Label(xliffDataGroup, SWT.RIGHT);
		targetFilsPathLbl.setText(Messages.getString("wizard.SplitXliffWizardPage.targetFilsPathLbl"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(targetFilsPathLbl);

		targetXlfPathTxt = new Text(xliffDataGroup, SWT.BORDER);
		targetXlfPathTxt.setLayoutData(textData);
		targetXlfPathTxt.setText(splitFile.getParent().getFullPath().append(splitFile.getName() + "_split")
				.toOSString());
		targetXlfPathTxt.setEditable(false);

		if ("\\".equals(System.getProperty("file.separator"))) {
			separator = "\\";
		} else {
			separator = "/";
		}

		validXliff();
	}

	/**
	 * 创建分割文件段的相关信息
	 * @param tparent
	 */
	public void createSplitInfo(Composite tparent) {
		tableViewer = new TableViewer(tparent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		final Table table = tableViewer.getTable();
		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		tableData.heightHint = 50;

		table.setLayoutData(tableData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] columnNames = new String[] { Messages.getString("wizard.SplitXliffWizardPage.columnNames1"),
				Messages.getString("wizard.SplitXliffWizardPage.columnNames2"),
				Messages.getString("wizard.SplitXliffWizardPage.columnNames3") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(splitXliff.getSplitTableInfos());
		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.2, 0.5, 0.29 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

	}

	public String getTargetXlfPathStr() {
		return targetXlfPathTxt.getText();
	}

	/**
	 * 验证xliff文件
	 */
	public void validXliff() {
		if (!splitXliff.validXLiff()) {
			setErrorMessage(Messages.getString("wizard.SplitXliffWizardPage.msg"));
			setPageComplete(false);
		}
	}
}
