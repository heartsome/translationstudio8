/**
 * ProjectConfigTBPage.java
 *
 * Version information :
 *
 * Date:Nov 29, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tb.projectsetting;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.HSDropDownButton;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.database.ui.tb.ImageConstants;
import net.heartsome.cat.database.ui.tb.Utils;
import net.heartsome.cat.database.ui.tb.dialog.TermDbManagerDialog;
import net.heartsome.cat.database.ui.tb.resource.Messages;
import net.heartsome.cat.database.ui.tb.wizard.NewTermDbWizard;
import net.heartsome.cat.database.ui.tb.wizard.TbxImportWizard;
import net.heartsome.cat.database.ui.tb.wizard.TermDbManagerImportWizardDialog;
import net.heartsome.cat.ts.ui.extensionpoint.AbstractProjectSettingPage;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectSettingTBPage extends AbstractProjectSettingPage {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectSettingTBPage.class);

	private TableViewer tableViewer;
	private List<DatabaseModelBean> curDbList;

	private Image checkedImage = Activator.getImageDescriptor(ImageConstants.CHECKED).createImage();
	private Image uncheckedImage = Activator.getImageDescriptor(ImageConstants.UNCHECKED).createImage();

	/**
	 * Create the preference page.
	 * @param cfgBean
	 */
	public ProjectSettingTBPage() {
		super("TB");
		setTitle(Messages.getString("projectsetting.ProjectSettingTBPage.title"));
		noDefaultAndApplyButton();
	}

	@Override
	public void setProjectInfoBean(ProjectInfoBean projInfoBean) {
		super.projectInfoBean = projInfoBean;
		this.curDbList = projInfoBean.getTbDb();
		for (DatabaseModelBean bean : this.curDbList) {
			checkDbHashMatch(bean);
		}
	}

	/**
	 * Create contents of the preference page.
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
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
			     if(null == dbModel){
			    	 return;
			     }    
				if (Constants.DBTYPE_SQLITE.equals(dbModel.getDbType())) {
					String path = dbModel.getItlDBLocation() + File.separator + dbModel.getDbName();
					File file = new File(path);
					if (!file.exists()) {
						setMessage(Messages.getString("projectsetting.ProjectSettingTBPage.FileNotFoundMsg"));
						return;
					}else{
						setMessage(Messages.getString("projectsetting.ProjectSettingTBPage.title"));
					}
				}
				
				if (dbModel != null && !dbModel.isHasMatch()) {
					setMessage(Messages.getString("projectsetting.ProjectSettingTBPage.msg1"));
				}
			}
		});

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new GridLayout(5, false));
		new Label(composite, SWT.NONE);

		HSDropDownButton addBtn = new HSDropDownButton(composite, SWT.NONE);
		addBtn.setText(Messages.getString("projectsetting.ProjectSettingTBPage.addBtn"));
		Menu addMenu = addBtn.getMenu();
		MenuItem item = new MenuItem(addMenu, SWT.PUSH);
		item.setText(Messages.getString("tb.dialog.addTb.DropDownButton.AddFileTb"));
		item.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialg = new FileDialog(getShell());
				fileDialg.setFilterExtensions(new String[] { "*.hstb", "*.*" });
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
					r = Utils.convertFile2TbModel(f, false);
				} catch (Exception e1) {
					MessageDialog.openError(getShell(), Messages.getString("tb.dialog.addFileTb.errorTitle"),
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
		serverItem.setText(Messages.getString("tb.dialog.addTb.DropDownButton.AddServerTb"));
		serverItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TermDbManagerDialog dialog = new TermDbManagerDialog(getShell());
				dialog.setDialogUseFor(TermDbManagerDialog.TYPE_DBSELECTED);
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
		createBtn.setText(Messages.getString("projectsetting.ProjectSettingTBPage.createBtn"));
		createBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				NewTermDbWizard wizard = new NewTermDbWizard();
				TermDbManagerImportWizardDialog dlg = new TermDbManagerImportWizardDialog(getShell(), wizard);
				if (dlg.open() == 0) {
					DatabaseModelBean dbModel = wizard.getCreateDb();
					List<DatabaseModelBean> dbList = new ArrayList<DatabaseModelBean>();
					dbList.add(dbModel);
					addToCurrDbList(dbList);
				}
			}
		});

		Button removeBtn = new Button(composite, SWT.NONE);
		removeBtn.setText(Messages.getString("projectsetting.ProjectSettingTBPage.removeBtn"));
		removeBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeForCurrDbList((IStructuredSelection) tableViewer.getSelection());
			}
		});

		Button importTmxBtn = new Button(composite, SWT.NONE);
		importTmxBtn.setText(Messages.getString("projectsetting.ProjectSettingTBPage.importTmxBtn"));
		importTmxBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Iterator<?> it = selection.iterator();
				if (it.hasNext()) {
					DatabaseModelBean dbModel = (DatabaseModelBean) it.next();
					TbxImportWizard wizard = new TbxImportWizard(dbModel);
					TermDbManagerImportWizardDialog dlg = new TermDbManagerImportWizardDialog(getShell(), wizard);
					if (dlg.open() == 0) {
						checkDbHashMatch(dbModel);
						tableViewer.refresh();
					}
					// 刷新项目
					ResourceUtils.refreshCurentSelectProject();
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("projectsetting.ProjectSettingTBPage.msgTitle"),
							Messages.getString("projectsetting.ProjectSettingTBPage.msg2"));
				}
			}
		});

		addBtn.setFocus();

		Point addPoint = addBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point createPoint = createBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point remPoint = removeBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point importPoint = importTmxBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		int width = Math.max(importPoint.x, Math.max(remPoint.x, Math.max(addPoint.x, createPoint.x)));
		GridData btnData = new GridData();
		btnData.widthHint = width + 10;
		addBtn.setLayoutData(btnData);
		createBtn.setLayoutData(btnData);
		removeBtn.setLayoutData(btnData);
		importTmxBtn.setLayoutData(btnData);

		return container;
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
	 * 将选中的内容添加到列表中
	 * @param selection
	 *            ;
	 */
	private void addToCurrDbList(List<DatabaseModelBean> hasSelection) {
		StringBuffer dbNames = new StringBuffer();
		for (int i = 0; i < hasSelection.size(); i++) {
			DatabaseModelBean dbModel = hasSelection.get(i);

			checkDbHashMatch(dbModel);
			if (!checkDbIsExist(curDbList, dbModel)) {
				if (curDbList.size() == 0) { // 第一个添加的库为默认库
					dbModel.setDefault(true);
				}
				curDbList.add(dbModel);
				this.tableViewer.add(dbModel);
			} else {
				dbNames.append(dbModel.getDbName());
				dbNames.append("\n");
			}
		}
		if (dbNames.length() != 0) {
			MessageDialog.openInformation(getShell(),
					Messages.getString("projectsetting.ProjectSettingTBPage.msgTitle"),
					Messages.getString("projectsetting.ProjectSettingTBPage.msg3") + dbNames.toString());
		}
	}

	/**
	 * 检查当前库是否已经存在
	 * @param b
	 * @return ;
	 */
	public boolean checkDbIsExist(List<DatabaseModelBean> curDbList, DatabaseModelBean b) {
		for (int i = 0; i < curDbList.size(); i++) {
			DatabaseModelBean a = curDbList.get(i);
			String dbname = a.getDbName();
			String host = a.getHost();
			String port = a.getPort();
			String instance = a.getInstance();
			if (b.getDbName().equals(dbname) && b.getHost().equals(host) && b.getPort().equals(port)
					&& b.getInstance().equals(instance)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 需要调用Database模块 检查当前项目在库中是否有语言对的匹配
	 * @param dbModel
	 *            数据库信息;
	 */
	public void checkDbHashMatch(DatabaseModelBean dbModel) {
		Language srcLang = super.projectInfoBean.getSourceLang();
		DBOperator dbOp = DatabaseService.getDBOperator(dbModel.toDbMetaData());
		try {
			if (dbOp != null) {
				dbOp.start();
				dbModel.setHasMatch(dbOp.checkHasMatchs(srcLang.getCode(), "B"));
			}
		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
			dbModel.setHasMatch(false);
		} finally {
			if (dbOp != null) {
				try {
					if (dbOp != null) {
						dbOp.end();
					}
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
		}
	}

	/**
	 * 创建Table列
	 * @param viewer
	 *            ;
	 */
	private void createColumn(final TableViewer viewer) {
		String[] clmnTitles = { Messages.getString("projectsetting.ProjectSettingTBPage.clmnTitles1"),
				Messages.getString("projectsetting.ProjectSettingTBPage.clmnTitles2"),
				Messages.getString("projectsetting.ProjectSettingTBPage.clmnTitles3"),
				Messages.getString("projectsetting.ProjectSettingTBPage.clmnTitles4"),
				Messages.getString("projectsetting.ProjectSettingTBPage.clmnTitles5") };
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
				String dbType = dbModel.getDbType();
				if (dbType.equals(Constants.DBTYPE_MYSQL)) {
					dbType = Constants.DBTYPE_MYSQL_FOR_UI;
				} else if (dbType.equals(Constants.DBTYPE_MSSQL2005)) {
					dbType = Constants.DBTYPE_MSSQL2005_FOR_UI;
				} else if (dbType.equals(Constants.DBTYPE_SQLITE)) {
					dbType = Messages.getString("tb.dbtype.sqlite");
				}
				return dbType;
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[2], clmnBounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseModelBean dbModel = (DatabaseModelBean) element;
				if (dbModel.getDbType().equals("Internal DB") || dbModel.getDbType().equals("SQLite")) {
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
					return Messages.getString("projectsetting.ProjectSettingTBPage.yes");
				} else {
					return Messages.getString("projectsetting.ProjectSettingTBPage.no");
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
	protected class ReadableEditingSupport extends EditingSupport {
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
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference page
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
