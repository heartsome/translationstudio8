package net.heartsome.cat.database.ui.tm.newproject;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.ui.HSDropDownButton;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tm.Activator;
import net.heartsome.cat.database.ui.tm.ImageConstants;
import net.heartsome.cat.database.ui.tm.Utils;
import net.heartsome.cat.database.ui.tm.dialog.TmDbManagerDialog;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.database.ui.tm.wizard.ImportTmxWizard;
import net.heartsome.cat.database.ui.tm.wizard.ImportTmxWizardDialog;
import net.heartsome.cat.database.ui.tm.wizard.NewTmDbWizard;
import net.heartsome.cat.ts.ui.extensionpoint.AbstractNewProjectWizardPage;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewProjectTmPage extends AbstractNewProjectWizardPage {

	private static final Logger LOGGER = LoggerFactory.getLogger(NewProjectTmPage.class);

	private TableViewer tableViewer;
	private List<Object> curDbList;
	private Image checkedImage = Activator.getImageDescriptor(ImageConstants.CHECKED).createImage();
	private Image uncheckedImage = Activator.getImageDescriptor(ImageConstants.UNCHECKED).createImage();

	/**
	 * Create the wizard.
	 */
	public NewProjectTmPage() {
		super("wizardPage", "TM");
		setTitle(Messages.getString("newproject.NewProjectTmPage.title"));
		setDescription(Messages.getString("newproject.NewProjectTmPage.desc"));

		curDbList = new ArrayList<Object>();
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));

		tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		tableViewer.setContentProvider(new ArrayContentProvider());
		createColumn(tableViewer);
		tableViewer.setInput(curDbList);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				DatabaseModelBean dbModel = (DatabaseModelBean) selection.getFirstElement();
				if (dbModel != null && !dbModel.isHasMatch()) {
					setMessage(Messages.getString("newproject.NewProjectTmPage.msg1"));
				} else {
					setMessage(null);
				}
			}
		});

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new GridLayout(5, false));
		new Label(composite, SWT.NONE);

		HSDropDownButton addBtn = new HSDropDownButton(composite, SWT.NONE);
		addBtn.setText(Messages.getString("projectsetting.ProjectSettingTMPage.addBtn"));
		Menu addMenu = addBtn.getMenu();
		MenuItem item = new MenuItem(addMenu, SWT.PUSH);
		item.setText(Messages.getString("tm.dialog.addTm.DropDownButton.AddFileTm"));
		item.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialg = new FileDialog(getShell());
				fileDialg.setFilterExtensions(new String[] { "*.hstm", "*.*" });
				String result = fileDialg.open();
				if (result == null) {
					return;
				}
				File f = new File(result);
				if (!f.exists()) {
					return;
				}
				Map<DatabaseModelBean, String> r = null;
				try {
					r = Utils.convertFile2TmModel(f, false);
				} catch (Exception e1) {
					MessageDialog.openError(getShell(), Messages.getString("tm.dialog.addFileTm.errorTitle"),
							e1.getMessage());
				}
				if (r == null) {
					return;
				}
				Iterator<DatabaseModelBean> it = r.keySet().iterator();
				if (it.hasNext()) {
					DatabaseModelBean selectedVal = it.next();
					List<DatabaseModelBean> dbList = new ArrayList<DatabaseModelBean>();
					dbList.add(selectedVal);
					addToCurrDbList(dbList);
				}
			}
		});
		MenuItem serverItem = new MenuItem(addMenu, SWT.PUSH);
		serverItem.setText(Messages.getString("tm.dialog.addTm.DropDownButton.AddServerTm"));
		serverItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TmDbManagerDialog dialog = new TmDbManagerDialog(getShell());
				dialog.setDialogUseFor(TmDbManagerDialog.TYPE_DBSELECTED);
				if (dialog.open() == Window.OK) {
					Iterator<DatabaseModelBean> it = dialog.getHasSelectedDatabase().keySet().iterator();
					List<DatabaseModelBean> dbList = new ArrayList<DatabaseModelBean>();
					while (it.hasNext()) {
						dbList.add(it.next());
					}
					addToCurrDbList(dbList);
				}
			}
		});

		Button createBtn = new Button(composite, SWT.NONE);
		createBtn.setText(Messages.getString("newproject.NewProjectTmPage.createBtn"));
		createBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				NewTmDbWizard wizard = new NewTmDbWizard();
				ImportTmxWizardDialog dlg = new ImportTmxWizardDialog(getShell(), wizard);
				if (dlg.open() == 0) {
					DatabaseModelBean dbModel = wizard.getCreateDb();
					List<DatabaseModelBean> dbList = new ArrayList<DatabaseModelBean>(1);
					dbList.add(dbModel);
					addToCurrDbList(dbList);
				}
			}
		});

		Button removeBtn = new Button(composite, SWT.NONE);
		removeBtn.setText(Messages.getString("newproject.NewProjectTmPage.removeBtn"));
		removeBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeForCurrDbList((IStructuredSelection) tableViewer.getSelection());
			}
		});

		Button importTmxBtn = new Button(composite, SWT.NONE);
		importTmxBtn.setText(Messages.getString("newproject.NewProjectTmPage.importTmxBtn"));
		importTmxBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Iterator<?> it = selection.iterator();
				if (it.hasNext()) {
					DatabaseModelBean dbModel = (DatabaseModelBean) it.next();
					ImportTmxWizard wizard = new ImportTmxWizard(dbModel);
					ImportTmxWizardDialog dlg = new ImportTmxWizardDialog(getShell(), wizard);
					if (dlg.open() == 0) {
						checkDbHashMatch(dbModel, "M");
						tableViewer.refresh();
					}
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("newproject.NewProjectTmPage.msgTitle"),
							Messages.getString("newproject.NewProjectTmPage.msg3"));
				}
			}
		});
		addBtn.setFocus();
		setControl(container);
	}

	/**
	 * 将选中的内容添加到列表中
	 * @param selection
	 *            ;
	 */
	private void addToCurrDbList(List<DatabaseModelBean> hasSelection) {
		StringBuffer existDbNames = new StringBuffer();
		for (int i = 0; i < hasSelection.size(); i++) {
			DatabaseModelBean dbModel = hasSelection.get(i);

			checkDbHashMatch(dbModel, "M");
			if (!checkDbIsExist(curDbList, dbModel)) {
				if (curDbList.size() == 0) { // 第一个添加的库为默认库
					dbModel.setDefault(true);
				}
				curDbList.add(dbModel);
				this.tableViewer.refresh();
			} else {
				existDbNames.append(dbModel.getDbName());
				existDbNames.append("\n");
			}
		}
		if (existDbNames.length() != 0) {
			MessageDialog.openInformation(getShell(), Messages.getString("newproject.NewProjectTmPage.msgTitle"),
					Messages.getString("newproject.NewProjectTmPage.msg2") + existDbNames.toString());
		}
	}

	/**
	 * 检查当前库是否已经存在
	 * @param b
	 * @return ;
	 */
	private boolean checkDbIsExist(List<Object> curDbList, DatabaseModelBean b) {
		for (int i = 0; i < curDbList.size(); i++) {
			DatabaseModelBean a = (DatabaseModelBean) curDbList.get(i);
			String dbname = a.getDbName();
			String host = a.getHost();
			String port = a.getPort();
			String instance = a.getInstance();
			String localPath = a.getItlDBLocation();
			if (b.getDbName().equals(dbname) && b.getHost().equals(host) && b.getItlDBLocation().equals(localPath)
					&& b.getPort().equals(port) && b.getInstance().equals(instance)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 从当前列表中删除选中的库
	 * @param selection
	 *            ;
	 */
	private void removeForCurrDbList(IStructuredSelection selection) {
		curDbList.removeAll(selection.toList());
		this.tableViewer.remove(selection.toArray());
	}

	/**
	 * 创建Table列
	 * @param viewer
	 *            ;
	 */
	private void createColumn(final TableViewer viewer) {
		String[] clmnTitles = { Messages.getString("newproject.NewProjectTmPage.clmnTitles1"),
				Messages.getString("newproject.NewProjectTmPage.clmnTitles2"),
				Messages.getString("newproject.NewProjectTmPage.clmnTitles3"),
				Messages.getString("newproject.NewProjectTmPage.clmnTitles4"),
				Messages.getString("newproject.NewProjectTmPage.clmnTitles5") };
		int[] clmnBounds = { 100, 100, 200, 90, 70 };

		TableViewerColumn col = createTableViewerColumn(viewer, clmnTitles[0], clmnBounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseModelBean dbModel = (DatabaseModelBean) element;
				return dbModel.getDbName();
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[1], clmnBounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseModelBean dbModel = (DatabaseModelBean) element;
				if (dbModel.getDbType().equals(Constants.DBTYPE_SQLITE)) {
					return Messages.getString("tm.dbtype.sqlite");
				}
				return dbModel.getDbType();
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[2], clmnBounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseModelBean dbModel = (DatabaseModelBean) element;
				if (dbModel.getDbType().equals(Constants.DBTYPE_INTERNALDB)
						|| dbModel.getDbType().equals(Constants.DBTYPE_SQLITE)) {
					return dbModel.getItlDBLocation();
				}
				return dbModel.getHost();
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[3], clmnBounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseModelBean dbModel = (DatabaseModelBean) element;
				if (dbModel.isHasMatch()) {
					return Messages.getString("newproject.NewProjectTmPage.yes");
				} else {
					return Messages.getString("newproject.NewProjectTmPage.no");
				}
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[4], clmnBounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			public Image getImage(Object element) {
				DatabaseModelBean dbModel = (DatabaseModelBean) element;
				if (dbModel.isDefault()) {
					return checkedImage;
				} else {
					return uncheckedImage;
				}
			}

			public String getText(Object element) {
				return null;
			}
		});
		col.setEditingSupport(new ReadableEditingSupport(viewer));
	}

	/**
	 * 设置TableViewer 列属性
	 * @param viewer
	 * @param title
	 *            列标题
	 * @param bound
	 *            列宽
	 * @param colNumber
	 *            列序号
	 * @return {@link TableViewerColumn};
	 */
	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE | SWT.Resize);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;

	}

	/**
	 * Cell Editing Support for readable
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	private class ReadableEditingSupport extends EditingSupport {
		private final TableViewer viewer;

		public ReadableEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			DatabaseModelBean dbModel = (DatabaseModelBean) element;
			return dbModel.isDefault();
		}

		@Override
		protected void setValue(Object element, Object value) {
			// 实现单选
			List<?> dbList = (List<?>) viewer.getInput();
			for (int i = 0; i < dbList.size(); i++) {
				((DatabaseModelBean) dbList.get(i)).setDefault(false);
			}
			// 实现单选

			// 执行修改
			DatabaseModelBean dbModel = (DatabaseModelBean) element;
			dbModel.setDefault((Boolean) value);

			// 刷新
			viewer.refresh();
		}
	}

	/**
	 * 需要调用Database模块 检查当前项目在库中是否有语言对的匹配
	 * @param dbModel
	 *            数据库信息;
	 */
	private void checkDbHashMatch(DatabaseModelBean dbModel, String type) {
		DBOperator dbOp = DatabaseService.getDBOperator(dbModel.toDbMetaData());
		try {
			dbOp.start();
			dbModel.setHasMatch(dbOp.checkHasMatchs(super.projSourceLang.getCode(), type));
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
			dbModel.setHasMatch(false);
		} finally {
			try {
				if (dbOp != null) {
					dbOp.end();
				}
			} catch (SQLException e) {
				LOGGER.error("", e);
			}
		}
	}

	@Override
	public List<DatabaseModelBean> getSelectedDatabase() {
		List<DatabaseModelBean> tmDbList = new ArrayList<DatabaseModelBean>();
		for (int i = 0; i < curDbList.size(); i++) {
			tmDbList.add((DatabaseModelBean) curDbList.get(i));
		}
		return tmDbList;
	}

	@Override
	public void dispose() {
		if (checkedImage != null && !checkedImage.isDisposed()) {
			checkedImage.dispose();
		}
		if (uncheckedImage != null && !uncheckedImage.isDisposed()) {
			uncheckedImage.dispose();
		}
		super.dispose();
	}
}
