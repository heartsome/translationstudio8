/**
 * DatabaseManagerDialog.java
 *
 * Version information :
 *
 * Date:Dec 1, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tb.dialog;

import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.bean.DatabaseManagerDbListBean;
import net.heartsome.cat.database.ui.core.DatabaseConfiger;
import net.heartsome.cat.database.ui.core.DbValidator;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.database.ui.tb.ImageConstants;
import net.heartsome.cat.database.ui.tb.resource.Messages;
import net.heartsome.cat.database.ui.tb.wizard.TbxImportWizard;
import net.heartsome.cat.database.ui.tb.wizard.TermDbManagerImportWizardDialog;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TermDbManagerDialog extends TrayDialog {
	// The last known tree width
	private static int lastTreeWidth = 180;

	private Point lastShellSize;

	/** The tree viewer. */
	private TreeViewer treeViewer;

	private Text hostText;
	private Text usernameText;
	private Text passwordText;
	private Text instanceText;
	private Text portText;
	private Button searchBtn;

	private TableViewer dbTableViewer;

	private static String currDbType;
	private static String lastSelectedServerId;

	/**
	 * 和界面相关的输入模型,与界面输入组件进行绑定.与会与选择具体的配置进行关联
	 */
	private DatabaseModelBean currServer;

	private Map<String, List<DatabaseModelBean>> treeInputMap;
	private DatabaseConfiger configer;

	private Map<String, MetaData> dbMetaDataMap;
	private Text locationText;
	private Button borwserBtn;
	private List<DatabaseManagerDbListBean> currServerdbList;
	private WritableList currServerdbListInput;

	private Image internalDbImg = Activator.getImageDescriptor(ImageConstants.INTERNALDB).createImage();
	private Image mySqlImg = Activator.getImageDescriptor(ImageConstants.MYSQL).createImage();
	private Image oracleImg = Activator.getImageDescriptor(ImageConstants.ORACLE).createImage();
	private Image sqlServerImg = Activator.getImageDescriptor(ImageConstants.SQLSERVER).createImage();
	private Image postgreImg = Activator.getImageDescriptor(ImageConstants.POSTGRESQL).createImage();
	private Image ipImg = Activator.getImageDescriptor(ImageConstants.IP).createImage();
	private Image sqliteImg = Activator.getImageDescriptor(ImageConstants.SQLITE).createImage();

	/**
	 * tree pop menu
	 * @see #initTreePopMenu()
	 */
	private Menu treePopMenu;

	private Logger logger = LoggerFactory.getLogger(TermDbManagerDialog.class);

	private List<DatabaseModelBean> needUpdateToFile = new ArrayList<DatabaseModelBean>();

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public TermDbManagerDialog(Shell parentShell) {
		super(parentShell);
		this.currServer = new DatabaseModelBean();
		this.currServerdbList = new ArrayList<DatabaseManagerDbListBean>();

		this.configer = new DatabaseConfiger();
		this.treeInputMap = configer.getAllServerConfig();
		this.dbMetaDataMap = DatabaseService.getSystemSuportDbMetaData();

		List<String> dbTypeList = DatabaseService.getSystemSuportDbType();
		for (int i = 0; i < dbTypeList.size(); i++) {
			String type = dbTypeList.get(i);
			if (treeInputMap.containsKey(type)) {
				continue;
			} else {
				treeInputMap.put(type, new ArrayList<DatabaseModelBean>());
			}
		}
		treeInputMap.remove(Constants.DBTYPE_SQLITE);
		setHelpAvailable(true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.TermDbManagerDialog.title"));
		newShell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent e) {
				if (lastShellSize == null) {
					lastShellSize = getShell().getSize();
				}
			}
		});
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createContents(final Composite parent) {
		Control control = super.createContents(parent);
		selectSaveItem();
		return control;
	}

	/**
	 * 添加帮助按钮 robert 2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 术语库管理
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch06s04.html#tb-management", language);
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem helpItem = new ToolItem(toolBar, SWT.NONE);
		helpItem.setImage(helpImage);
		helpItem.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		helpItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
			}
		});
		return toolBar;
	}

	/**
	 * 选择当前选中的内容 ;
	 */
	private void selectSaveItem() {
		if (getCurrDbType() != null) {
			boolean expanded = getTreeViewer().getExpandedState(getCurrDbType());
			if (!expanded) {
				getTreeViewer().setExpandedState(getCurrDbType(), !expanded);
			}
			DatabaseModelBean lastSelectItem = findServerBean(getCurrDbType(), getLastSelectedServer());
			if (lastSelectItem != null) {
				getTreeViewer().setSelection(new StructuredSelection(lastSelectItem), true);
				getTreeViewer().getControl().setFocus();
			} else {
				getTreeViewer().setSelection(new StructuredSelection(getCurrDbType()), true);
				getTreeViewer().getControl().setFocus();
			}
		}
	}

	/**
	 * 查找上次的选中的项
	 * @param dbType
	 *            数据库类型
	 * @param id
	 *            服务器ID
	 * @return ;
	 */
	private DatabaseModelBean findServerBean(String dbType, String id) {
		List<DatabaseModelBean> list = treeInputMap.get(dbType);
		if (list == null) {
			return null;
		}
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId().equals(id)) {
				return list.get(i);
			}
		}
		return null;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout parentLayout = ((GridLayout) composite.getLayout());
		parentLayout.numColumns = 4;
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.marginTop = 0;
		parentLayout.verticalSpacing = 0;
		parentLayout.horizontalSpacing = 0;

		Control treeControl = createTreeAreaContents(composite);
		createSash(composite, treeControl);

		Label versep = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		GridData verGd = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);

		versep.setLayoutData(verGd);
		versep.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		pageAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		pageAreaComposite.setLayout(layout);

		// Build the Page container
		Composite pageContainer = createPageContainer(pageAreaComposite);
		GridData pageContainerData = new GridData(GridData.FILL_BOTH);
		pageContainerData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		pageContainer.setLayoutData(pageContainerData);
		// Build the separator line
		Label bottomSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		bottomSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		return composite;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (dialogType == TYPE_DBMANAGE) {
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		} else {
			super.createButtonsForButtonBar(parent);
		}

		getShell().setDefaultButton(searchBtn);
		initDataBindings();
	}

	private int dialogType = 0; // default is DatabaseManager
	public static final int TYPE_DBMANAGE = 0;
	public static final int TYPE_DBSELECTED = 1;

	/**
	 * 设置对话框的用处，决定了生成的按钮
	 * @param type
	 *            ;
	 */
	public void setDialogUseFor(int type) {
		this.dialogType = type;
	}

	public int getDialogUseFor() {
		return this.dialogType;
	}

	/**
	 * 创建右侧页面内容
	 * @param parent
	 *            页面容器
	 * @return ;
	 */
	protected Composite createPageContainer(Composite parent) {

		Composite outer = new Composite(parent, SWT.NONE);

		GridData outerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		outerData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;

		outer.setLayout(new GridLayout());
		outer.setLayoutData(outerData);

		// Create an outer composite for spacing
		ScrolledComposite scrolled = new ScrolledComposite(outer, SWT.V_SCROLL | SWT.H_SCROLL);

		// always show the focus control
		scrolled.setShowFocusedControl(true);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);

		scrolled.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		GridLayout gld = new GridLayout(1, false);
		gld.marginWidth = 0;
		gld.marginHeight = 0;
		scrolled.setLayout(gld);

		Composite result = new Composite(scrolled, SWT.NONE);

		GridData resultData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		result.setLayoutData(resultData);
		GridLayout gl_result = new GridLayout(1, false);
		gl_result.marginWidth = 0;
		gl_result.marginHeight = 0;
		result.setLayout(gl_result);

		Group parameterGroup = new Group(result, SWT.NONE);
		parameterGroup.setText(Messages.getString("dialog.TermDbManagerDialog.parameterGroup"));
		GridLayout parameterLayout = new GridLayout(4, false);
		parameterGroup.setLayout(parameterLayout);

		GridData parameterGridData = new GridData(GridData.FILL_HORIZONTAL);
		parameterGroup.setLayoutData(parameterGridData);

		Label label = new Label(parameterGroup, SWT.RIGHT);
		label.setText(Messages.getString("dialog.TermDbManagerDialog.lblHost"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

		hostText = new Text(parameterGroup, SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(parameterGroup, SWT.RIGHT);
		label.setText(Messages.getString("dialog.TermDbManagerDialog.lblPort"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

		portText = new Text(parameterGroup, SWT.BORDER);

		label = new Label(parameterGroup, SWT.RIGHT);
		label.setText(Messages.getString("dialog.TermDbManagerDialog.lblInstance"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

		instanceText = new Text(parameterGroup, SWT.BORDER);
		instanceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parameterGroup, SWT.NONE);
		new Label(parameterGroup, SWT.NONE);

		label = new Label(parameterGroup, SWT.RIGHT);
		label.setText(Messages.getString("dialog.TermDbManagerDialog.lblLocation"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

		locationText = new Text(parameterGroup, SWT.BORDER);
		locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		borwserBtn = new Button(parameterGroup, SWT.NONE);
		borwserBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		borwserBtn.setText(Messages.getString("dialog.TermDbManagerDialog.borwserBtn"));
		borwserBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				String path = dlg.open();
				if (path != null) {
					locationText.setText(path);
				}
			}
		});
		label = new Label(parameterGroup, SWT.RIGHT);
		label.setText(Messages.getString("dialog.TermDbManagerDialog.lblUsername"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

		usernameText = new Text(parameterGroup, SWT.BORDER);
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(parameterGroup, SWT.RIGHT);
		label.setText(Messages.getString("dialog.TermDbManagerDialog.lblPwd"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

		passwordText = new Text(parameterGroup, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite btnComposite = new Composite(parameterGroup, SWT.NONE);
		btnComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		GridLayout btnCompositeLayout = new GridLayout(1, false);
		btnCompositeLayout.marginHeight = 0;
		btnCompositeLayout.marginWidth = 0;
		btnComposite.setLayout(btnCompositeLayout);

		// remenmberBtn = new Button(btnComposite, SWT.CHECK|SWT.BORDER);
		// remenmberBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		// remenmberBtn.setText("将本次连接信息添加到数据库类型的快捷连接方式(&R)");
		// remenmberBtn.setSelection(true);

		searchBtn = new Button(btnComposite, SWT.NONE);
		searchBtn.setText(Messages.getString("dialog.TermDbManagerDialog.searchBtn"));
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						// 输入合法性检查
						IStatus status = validator();
						if (status.getSeverity() != IStatus.OK) {
							MessageDialog.openInformation(getShell(),
									Messages.getString("dialog.TermDbManagerDialog.msgTitle"), status.getMessage());
							return;
						}

						SystemDBOperator sysDbOp = getCurrSysDbOp();
						if (sysDbOp == null) {
							return;
						}
						// 连接检查
						if (!sysDbOp.checkDbConnection()) {
							MessageDialog.openInformation(getShell(),
									Messages.getString("dialog.TermDbManagerDialog.msgTitle"),
									Messages.getString("dialog.TermDbManagerDialog.msg1"));
							return;
						}

						// if (remenmberBtn.getSelection() == true) {
						// 当前数据库类型下的所有服务器
						List<DatabaseModelBean> currDbTypeServers = treeInputMap.get(getCurrDbType());
						if (currServer.getId().equals("")) {
							addServerWithExistCheck(currServer, currDbTypeServers);
							getTreeViewer().refresh();
							selectSaveItem(); // 在树上选择当前操作的节点
						}

						// ISelection selection = getTreeViewer().getSelection();
						// if (selection.isEmpty()) {
						// return;
						// }
						// } else { // 不记住信息
						executeSearch(sysDbOp);
						// }
					}
				});
			}
		});

		Group tableComposite = new Group(result, SWT.NONE);
		tableComposite.setText(Messages.getString("dialog.TermDbManagerDialog.tableComposite"));
		tableComposite.setLayout(new GridLayout(1, false));
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		dbTableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);

		Table table = dbTableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableGd.heightHint = 180;
		table.setLayoutData(tableGd);

		createColumn(dbTableViewer);
		if (getDialogUseFor() == TYPE_DBSELECTED) {
			dbTableViewer.addDoubleClickListener(new IDoubleClickListener() {

				public void doubleClick(DoubleClickEvent event) {
					okPressed();
				}
			});
		}

		dbTableViewer.setContentProvider(new ArrayContentProvider());
		currServerdbListInput = new WritableList(currServerdbList, DatabaseManagerDbListBean.class);
		dbTableViewer.setInput(currServerdbListInput);

		Composite composite = new Composite(tableComposite, SWT.NONE);
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		Button btnc = new Button(composite, SWT.NONE);
		btnc.setText(Messages.getString("dialog.TermDbManagerDialog.btnc"));
		btnc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createNewDatabase();
			}
		});

		Button btnd_1 = new Button(composite, SWT.NONE);
		btnd_1.setText(Messages.getString("dialog.TermDbManagerDialog.btnd_1"));
		btnd_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						ISelection selection = getDbTableViewer().getSelection();
						if (selection.isEmpty()) {
							return;
						}
						if (MessageDialog.openConfirm(getShell(),
								Messages.getString("dialog.TermDbManagerDialog.msgTitle"),
								Messages.getString("dialog.TermDbManagerDialog.msg2"))) {
							IStructuredSelection structuredSelection = (IStructuredSelection) selection;
							@SuppressWarnings("unchecked")
							List<DatabaseManagerDbListBean> needDeletes = structuredSelection.toList();
							SystemDBOperator dbop = getCurrSysDbOp();
							for (int i = 0; i < needDeletes.size(); i++) {
								try {
									String dbName = needDeletes.get(i).getDbName();
									dbop.dropDb(dbName);
									dbop.removeSysDb(dbName);
								} catch (Exception e1) {
									logger.error(Messages.getString("dialog.TermDbManagerDialog.logger1"), e1);
									MessageDialog.openError(getShell(),
											Messages.getString("dialog.TermDbManagerDialog.msgTitle"),
											Messages.getString("dialog.TermDbManagerDialog.msg3") + e1.getMessage());
									break;
								}
								currServerdbListInput.remove(needDeletes.get(i));
							}
						}
					}
				});
			}
		});

		Button importBtn = new Button(composite, SWT.NONE);
		importBtn.setText(Messages.getString("dialog.TermDbManagerDialog.importBtn"));
		importBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				ISelection selection = getDbTableViewer().getSelection();
				if (selection.isEmpty()) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialog.TermDbManagerDialog.msgTitle"),
							Messages.getString("dialog.TermDbManagerDialog.msg4"));
					return;
				}
				IStructuredSelection stcSel = (IStructuredSelection) selection;
				DatabaseManagerDbListBean dbBean = (DatabaseManagerDbListBean) stcSel.getFirstElement();
				DatabaseModelBean dbModelBean = new DatabaseModelBean();
				currServer.copyToOtherIntance(dbModelBean);
				dbModelBean.setDbName(dbBean.getDbName());

				TbxImportWizard wizard = new TbxImportWizard(dbModelBean);
				TermDbManagerImportWizardDialog dlg = new TermDbManagerImportWizardDialog(getShell(), wizard);
				if (dlg.open() == 0) {
					executeSearch(getCurrSysDbOp()); // 重新加载内容
				}
			}
		});

		Point searchPoint = searchBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point createPoint = btnc.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point remPoint = btnd_1.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point importPoint = importBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		int width = Math.max(importPoint.x, Math.max(remPoint.x, Math.max(searchPoint.x, createPoint.x)));
		GridData btnData = new GridData();
		btnData.widthHint = width + 10;
		btnc.setLayoutData(btnData);
		btnd_1.setLayoutData(btnData);
		importBtn.setLayoutData(btnData);
		searchBtn.getLayoutData();
		GridData searchData = new GridData(SWT.RIGHT, SWT.CENTER, true, true, 4, 1);
		searchData.widthHint = width + 10;
		searchBtn.setLayoutData(searchData);

		scrolled.setContent(result);
		scrolled.setMinSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return result;
	}

	/**
	 * 创建左侧树
	 * @param parent
	 *            the SWT parent for the tree area controls.
	 * @return the new <code>Control</code>.
	 */
	protected Control createTreeAreaContents(Composite parent) {
		// Build the tree an put it into the composite.
		treeViewer = createTreeViewer(parent);
		treeViewer.setInput(treeInputMap);
		updateTreeFont(JFaceResources.getDialogFont());
		layoutTreeAreaControl(treeViewer.getControl());
		initTreePopMenu();
		return treeViewer.getControl();
	}

	/**
	 * 初始化树右键菜单 ;
	 */
	private void initTreePopMenu() {
		MenuManager menuManager = new MenuManager("");
		menuManager.add(new Action(Messages.getString("dialog.TermDbManagerDialog.deleteAction")) {
			@Override
			public void run() {
				ISelection selection = getTreeViewer().getSelection();
				if (selection.isEmpty()) {
					return;
				}
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object obj = structuredSelection.getFirstElement();
				if (obj instanceof DatabaseModelBean) {
					List<DatabaseModelBean> currDbTypeServers = treeInputMap.get(currDbType);
					configer.deleteServerById(((DatabaseModelBean) obj).getId());
					int i = currDbTypeServers.indexOf(obj);
					currDbTypeServers.remove(i);
					getTreeViewer().refresh();
					// selectSaveItem();
					// setLastSelectedServer(null);

					if (currDbTypeServers.size() != 0) {
						if (i > currDbTypeServers.size() - 1) {
							setLastSelectedServer(currDbTypeServers.get(i - 1).getId());
						} else {
							setLastSelectedServer(currDbTypeServers.get(i).getId());
						}
						initUI(false);
					} else {
						setLastSelectedServer(null);
						initUI(true);
					}
					selectSaveItem();

				}
			}
		});
		Tree tree = treeViewer.getTree();
		this.treePopMenu = menuManager.createContextMenu(tree);
	}

	/**
	 * @param control
	 *            the <code>Control</code> to lay out.
	 */
	protected void layoutTreeAreaControl(Control control) {
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = getLastRightWidth();
		gd.verticalSpan = 1;
		control.setLayoutData(gd);
	}

	/**
	 * Create a new <code>TreeViewer</code>.
	 * @param parent
	 *            the parent <code>Composite</code>.
	 * @return the <code>TreeViewer</code>.
	 */
	protected TreeViewer createTreeViewer(Composite parent) {
		final TreeViewer viewer = new TreeViewer(parent, SWT.NONE);
		addListeners(viewer);
		viewer.setLabelProvider(new TreeLableProvider());
		viewer.setContentProvider(new TreeContentProvider());
		return viewer;
	}

	/**
	 * Add Selection Listener to tree viewer
	 * @param viewer
	 *            ;
	 */
	private void addListeners(final TreeViewer viewer) {
		// 选择事件
		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object obj = structuredSelection.getFirstElement();
				if (obj instanceof DatabaseModelBean) {
					DatabaseModelBean bean = (DatabaseModelBean) obj;
					setCurrDbType(bean.getDbType());
					bean.copyToOtherIntance(currServer);
					SystemDBOperator dbop = getCurrSysDbOp();
					if (dbop != null) {
						executeSearch(dbop);
					}
					initUI(false); // 当数据库类型发生改变时重新初始化界面

				} else if (obj instanceof String) {
					setCurrDbType((String) obj);
					resetInputValue();
					currServerdbListInput.clear();
					initUI(true);
				}
			}
		});
		// 双击展开事件
		((Tree) viewer.getControl()).addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(final SelectionEvent event) {
				ISelection selection = viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object obj = structuredSelection.getFirstElement();
				if (obj instanceof String) {
					String type = (String) obj;
					boolean expanded = viewer.getExpandedState(type);
					viewer.setExpandedState(type, !expanded);
				}
			}
		});

		// 右键菜单事件,判断何时出现右键菜单
		viewer.getControl().addMenuDetectListener(new MenuDetectListener() {
			public void menuDetected(MenuDetectEvent e) {
				ISelection selection = viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object obj = structuredSelection.getFirstElement();
				Tree tree = treeViewer.getTree();
				if (obj instanceof DatabaseModelBean) {
					tree.setMenu(treePopMenu); // 将菜单挂到树上
				} else {
					tree.setMenu(null);
				}
			}
		});
	}

	/**
	 * 获取当前服务器操作对象
	 * @return ;
	 */
	private SystemDBOperator getCurrSysDbOp() {
		return DatabaseService.getSysDbOperateByMetaData(currServer.toDbMetaData());
	}

	/**
	 * Update the tree to use the specified <code>Font</code>.
	 * @param dialogFont
	 *            the <code>Font</code> to use.
	 */
	protected void updateTreeFont(Font dialogFont) {
		getTreeViewer().getControl().setFont(dialogFont);
	}

	/**
	 * @return the <code>TreeViewer</code> for this dialog.
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * @return The <code>TableViewer</code> for database list in dialog ;
	 */
	public TableViewer getDbTableViewer() {
		return this.dbTableViewer;
	}

	/**
	 * @return the need update to file server configure
	 */
	public List<DatabaseModelBean> getNeedUpdateToFile() {
		return this.needUpdateToFile;
	}

	/**
	 * Create the sash with right control on the right. Note that this method assumes GridData for the layout data of
	 * the rightControl.
	 * @param composite
	 * @param rightControl
	 * @return Sash
	 */
	protected Sash createSash(final Composite composite, final Control rightControl) {
		final Sash sash = new Sash(composite, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		sash.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		// the following listener resizes the tree control based on sash deltas.
		// If necessary, it will also grow/shrink the dialog.
		sash.addListener(SWT.Selection, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt. widgets.Event)
			 */
			public void handleEvent(Event event) {
				if (event.detail == SWT.DRAG) {
					return;
				}
				int shift = event.x - sash.getBounds().x;
				GridData data = (GridData) rightControl.getLayoutData();
				int newWidthHint = data.widthHint + shift;
				if (newWidthHint < 20) {
					return;
				}
				Point computedSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point currentSize = getShell().getSize();
				// if the dialog wasn't of a custom size we know we can shrink
				// it if necessary based on sash movement.
				boolean customSize = !computedSize.equals(currentSize);
				data.widthHint = newWidthHint;
				setLastTreeWidth(newWidthHint);
				composite.layout(true);
				// recompute based on new widget size
				computedSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				// if the dialog was of a custom size then increase it only if
				// necessary.
				if (customSize) {
					computedSize.x = Math.max(computedSize.x, currentSize.x);
				}
				computedSize.y = Math.max(computedSize.y, currentSize.y);
				if (computedSize.equals(currentSize)) {
					return;
				}
				setShellSize(computedSize.x, computedSize.y);
				lastShellSize = getShell().getSize();
			}
		});
		return sash;
	}

	/**
	 * Get the last known right side width.
	 * @return the width.
	 */
	protected int getLastRightWidth() {
		return lastTreeWidth;
	}

	/**
	 * Save the last known tree width.
	 * @param width
	 *            the width.
	 */
	private void setLastTreeWidth(int width) {
		lastTreeWidth = width;
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}

	/**
	 * Changes the shell size to the given size, ensuring that it is no larger than the display bounds.
	 * @param width
	 *            the shell width
	 * @param height
	 *            the shell height
	 */
	private void setShellSize(int width, int height) {
		Rectangle preferred = getShell().getBounds();
		preferred.width = width;
		preferred.height = height;
		getShell().setBounds(getConstrainedShellBounds(preferred));
	}

	/**
	 * The TreeViewer label provider
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	class TreeLableProvider extends LabelProvider {
		/**
		 * @param element
		 *            must be an instance of <code>IPreferenceNode</code>.
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof String) {
				String dbType = (String) element;
				if (dbType.equals(Constants.DBTYPE_MYSQL)) {
					dbType = Constants.DBTYPE_MYSQL_FOR_UI;
				} else if (dbType.equals(Constants.DBTYPE_MSSQL2005)) {
					dbType = Constants.DBTYPE_MSSQL2005_FOR_UI;
				} else if (dbType.equals(Constants.DBTYPE_SQLITE)) {
					dbType = Messages.getString("dialog.db.recommend");
				}
				return dbType;
			} else {
				DatabaseModelBean bean = (DatabaseModelBean) element;
				StringBuffer urlStr = new StringBuffer();
				if (bean.getDbType().equals(Constants.DBTYPE_INTERNALDB)
						|| bean.getDbType().equals(Constants.DBTYPE_SQLITE)) {
					urlStr.append(bean.getItlDBLocation());
				} else {
					urlStr.append(bean.getHost());
					urlStr.append(":");
					urlStr.append(bean.getPort());
					if (bean.getInstance() != null && !bean.getInstance().equals("")) {
						urlStr.append("/");
						urlStr.append(bean.getInstance());
					}
				}
				return urlStr.toString();
			}
		}

		/**
		 * @param element
		 *            must be an instance of String or <code>DatabaseModelBean</code>.
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			if (element instanceof String) {
				String dbtype = (String) element;
				if (dbtype.equals(Constants.DBTYPE_INTERNALDB)) {
					return internalDbImg;
				} else if (dbtype.equals(Constants.DBTYPE_MYSQL)) {
					return mySqlImg;
				} else if (dbtype.equals(Constants.DBTYPE_MSSQL2005)) {
					return sqlServerImg;
				} else if (dbtype.equals(Constants.DBTYPE_POSTGRESQL)) {
					return postgreImg;
				} else if (dbtype.equals(Constants.DBTYPE_Oracle)) {
					return oracleImg;
				} else if (dbtype.equals(Constants.DBTYPE_SQLITE)) {
					return sqliteImg;
				} else {
					return null;
				}
			} else if (element instanceof DatabaseModelBean) {
				return ipImg;
			} else {
				return null;
			}
		}
	}

	/**
	 * The TreeViewer content provider
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	class TreeContentProvider implements ITreeContentProvider {

		private Map<String, List<DatabaseModelBean>> map;

		public void dispose() {

		}

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.map = (Map<String, List<DatabaseModelBean>>) newInput;
		}

		public Object[] getElements(Object inputElement) {
			List<Object> t = new ArrayList<Object>(map.keySet());
			int size = t.size();
			Object[] temp = new Object[size];
			if (t.contains(Constants.DBTYPE_INTERNALDB)) {
				temp[size - 1] = Constants.DBTYPE_INTERNALDB;
			} 
			if (t.contains(Constants.DBTYPE_MYSQL)) {
				temp[size - 5] = Constants.DBTYPE_MYSQL;
			} 
			if (t.contains(Constants.DBTYPE_MSSQL2005)) {
				temp[size - 4] = Constants.DBTYPE_MSSQL2005;
			} 
			if (t.contains(Constants.DBTYPE_POSTGRESQL)) {
				temp[size - 2] = Constants.DBTYPE_POSTGRESQL;
			} 
			if (t.contains(Constants.DBTYPE_Oracle)) {
				temp[size - 3] = Constants.DBTYPE_Oracle;
			} 
			if(t.contains(Constants.DBTYPE_SQLITE)){
				temp[0] = Constants.DBTYPE_SQLITE;
			}
			return temp;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof String) {
				String dbType = (String) parentElement;
				List<DatabaseModelBean> servers = map.get(dbType);
				Collections.sort(servers, new Comparator<DatabaseModelBean>() {

					public int compare(DatabaseModelBean o1, DatabaseModelBean o2) {
						return o1.getHost().compareTo(o2.getHost());
					}
				});
				return servers.toArray();
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof String) {
				if (map.get(element).size() != 0) {
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * 初始化数据绑定 将界面与<code>currServer</code>进行绑定
	 * @return ;
	 */
	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();

		IObservableValue widgetValue = WidgetProperties.text(SWT.Modify).observe(instanceText);
		final IObservableValue instanceModelValue = BeanProperties.value("instance").observe(currServer);
		bindingContext.bindValue(widgetValue, instanceModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(hostText);
		final IObservableValue hostModelValue = BeanProperties.value("host").observe(currServer);
		bindingContext.bindValue(widgetValue, hostModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(portText);
		final IObservableValue protModelValue = BeanProperties.value("port").observe(currServer);
		bindingContext.bindValue(widgetValue, protModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(locationText);
		final IObservableValue locationModelValue = BeanProperties.value("itlDBLocation").observe(currServer);
		bindingContext.bindValue(widgetValue, locationModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(usernameText);
		final IObservableValue usernameModelValue = BeanProperties.value("userName").observe(currServer);
		bindingContext.bindValue(widgetValue, usernameModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(passwordText);
		final IObservableValue passwordModelValue = BeanProperties.value("password").observe(currServer);
		bindingContext.bindValue(widgetValue, passwordModelValue, null, null);

		ViewerSupport.bind(dbTableViewer, currServerdbListInput,
				BeanProperties.values(new String[] { "index", "dbName", "langs" }));

	}

	/**
	 * 创建Table列
	 * @param viewer
	 *            ;
	 */
	private void createColumn(final TableViewer viewer) {
		String[] clmnTitles = { Messages.getString("dialog.TermDbManagerDialog.clmnTitles1"),
				Messages.getString("dialog.TermDbManagerDialog.clmnTitles2"),
				Messages.getString("dialog.TermDbManagerDialog.clmnTitles3") };
		int[] clmnBounds = { 50, 100, 100 };

		TableViewerColumn col = createTableViewerColumn(viewer, clmnTitles[0], clmnBounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseManagerDbListBean bean = (DatabaseManagerDbListBean) element;
				return bean.getIndex();
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[1], clmnBounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseManagerDbListBean bean = (DatabaseManagerDbListBean) element;
				return bean.getDbName();
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[2], clmnBounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				DatabaseManagerDbListBean bean = (DatabaseManagerDbListBean) element;
				return bean.getLangs();
			}
		});

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
	 * 输入验证器 ;
	 */
	private IStatus validator() {
		String instance = currServer.getInstance();
		String host = currServer.getHost();
		String port = currServer.getPort();
		String location = currServer.getItlDBLocation();
		String username = currServer.getUserName();
		MetaData dbMetaData = dbMetaDataMap.get(currServer.getDbType());
		if (dbMetaData.dataPathSupported()) {
			File f = new File(location);
			if (location == null || location.trim().length() == 0) {
				if (dbMetaData.getDbType().equals(Constants.DBTYPE_INTERNALDB)) {
					return ValidationStatus.error(Messages.getString("dialog.TermDbManagerDialog.msg5"));
				} else if (dbMetaData.getDbType().equals(Constants.DBTYPE_SQLITE)) {
					return ValidationStatus.error(Messages.getString("dialog.TermDbManagerDialog.msg11"));
				}
			} else if (!f.exists()) {
				return ValidationStatus.error(Messages.getString("dialog.TermDbManagerDialog.msg12"));
			}
		}
		if (dbMetaData.serverNameSupported()) {
			if (host == null || host.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("dialog.TermDbManagerDialog.msg6"));
			}
		}
		if (dbMetaData.portSupported()) {
			if (port == null || port.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("dialog.TermDbManagerDialog.msg7"));
			}
		}
		if (dbMetaData.userNameSupported()) {
			if (username == null || username.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("dialog.TermDbManagerDialog.msg8"));
			}
		}

		if (dbMetaData.instanceSupported()) {
			if (instance == null || instance.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("dialog.TermDbManagerDialog.msg9"));
			}
		}

		return ValidationStatus.ok();
	}

	/**
	 * 检查当前配置是否发生了改变
	 * @param list
	 * @param b
	 * @param metaData
	 * @return ;
	 */
	private DatabaseModelBean configIsChanged(List<DatabaseModelBean> list, DatabaseModelBean b, MetaData metaData) {
		for (int i = 0; i < list.size(); i++) {
			DatabaseModelBean a = list.get(i);
			if (a.getId().equals(b.getId())) {
				if (metaData.dataPathSupported()) {
					if (!a.getItlDBLocation().equals(b.getItlDBLocation())) {
						return a;
					}
				} else {
					if (!a.getHost().equals(b.getHost()) || !a.getPort().equals(a.getPort())) {
						return a;
					}
					if (metaData.instanceSupported()) {
						if (!a.getInstance().equals(b.getInstance())) {
							return a;
						}
					}
					if (metaData.userNameSupported()) {
						if (!a.getUserName().equals(b.getUserName()) || !a.getPassword().equals(b.getPassword())) {
							return a;
						}
					}
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * 将当前操作的服务器配置信息添加到配置文件中<br>
	 * 在添加前,先判断该服务器是否已经存在,如果已经存在则更新
	 * @param currServer
	 *            需要处理的配置信息
	 * @param currDbTypeServers
	 *            当前数据库类型下的所有服务器,用于显示在界面上;
	 */
	private void addServerWithExistCheck(DatabaseModelBean currServer, List<DatabaseModelBean> currDbTypeServers) {
		DatabaseModelBean bean = currServer.copyToOtherIntance(new DatabaseModelBean());
		String existSerId = configer.isServerExist(bean, getCurrDbMetaData());
		if (existSerId != null && !existSerId.equals("")) { // 当前服务器配置信息已经存在

			if (!bean.getId().equals(existSerId)) {
				configer.deleteServerById(bean.getId());
				for (DatabaseModelBean temp : currDbTypeServers) {
					if (temp.getId().equals(bean.getId())) {
						currDbTypeServers.remove(temp);
						break;
					}
				}
			}
			bean.setId(existSerId);
			currServer.setId(existSerId);
			updateServer(bean, currDbTypeServers);

			return;
		}
		configer.addServerConfig(bean);
		currDbTypeServers.add(bean);
		setLastSelectedServer(bean.getId());
	}

	/**
	 * 更新当前操作的服务器配置信息到配置文件中
	 * @param currServer
	 *            需要处理的服务器配置信息
	 * @param currDbTypeServers
	 *            当前数据库类型下的所有服务器,用于显示在界面上;
	 */
	private void updateServer(DatabaseModelBean currServer, List<DatabaseModelBean> currDbTypeServers) {
		MetaData metaDataWithCheck = dbMetaDataMap.get(currServer.getDbType());
		DatabaseModelBean hasChanged = configIsChanged(currDbTypeServers, currServer, metaDataWithCheck);
		if (hasChanged != null) {
			currServer.copyToOtherIntance(hasChanged);
			configer.updateServerConfigById(hasChanged.getId(), hasChanged);
		}
		setLastSelectedServer(currServer.getId());
	}

	/**
	 * 设置最后选中的服务器
	 * @param serverId
	 *            ;
	 */
	private void setLastSelectedServer(String serverId) {
		lastSelectedServerId = serverId;
	}

	/**
	 * 获最后选中的服务器
	 * @return ;
	 */
	private String getLastSelectedServer() {
		return lastSelectedServerId;
	}

	/**
	 * 获取当前操作的数据库类型
	 * @return ;
	 */
	private String getCurrDbType() {
		return currDbType;
	}

	/**
	 * 设置当前操作的数据库类型
	 * @param dbType
	 *            当前操作的数据库类型 ;
	 */
	private void setCurrDbType(String dbType) {
		currDbType = dbType;
	}

	/**
	 * 获取当前数据库类型的元数据
	 * @return ;
	 */
	protected MetaData getCurrDbMetaData() {
		return dbMetaDataMap.get(getCurrDbType());
	}

	/**
	 * 当选择数据库类型节点时,重置所有输入 ;
	 */
	private void resetInputValue() {
		MetaData metaData = dbMetaDataMap.get(getCurrDbType());
		currServer.setId("");
		currServer.setDbType(metaData.getDbType());

		currServer.setItlDBLocation("");
		if (metaData.dataPathSupported()) {
			currServer.setItlDBLocation(metaData.getDataPath());
		}
		currServer.setHost("");
		if (metaData.serverNameSupported()) {
			currServer.setHost(metaData.getServerName());
		}
		currServer.setInstance("");
		if (metaData.instanceSupported()) {
			currServer.setInstance(metaData.getInstance());
		}

		currServer.setPort("");
		if (metaData.portSupported()) {
			currServer.setPort(metaData.getPort());
		}
		currServer.setUserName("");
		if (metaData.userNameSupported()) {
			currServer.setUserName(metaData.getUserName());
		}

		currServer.setPassword("");
		if (metaData.passwordSupported()) {
			currServer.setPassword(metaData.getPassword());
		}
	}

	/**
	 * 根据当前操作不同的数据库类型,初始化界面 ;
	 */
	private void initUI(boolean isAdd) {
		MetaData curDbMetaData = dbMetaDataMap.get(getCurrDbType());

		if (!isAdd) {
			Control[] childrens = locationText.getParent().getChildren();
			for (Control c : childrens) {
				if (!(c instanceof Composite)) {
					if (c instanceof Text || c instanceof Button) {
						c.setEnabled(false);
					}
				} else {
					Composite com = (Composite) c;
					Control[] ch = com.getChildren();
					for (Control chl : ch) {
						if (c instanceof Text || c instanceof Button) {
							chl.setEnabled(false);
						}
					}
				}
			}
		} else {
			if (curDbMetaData.dataPathSupported()) {
				locationText.setEnabled(true);
				borwserBtn.setEnabled(true);
			} else {
				locationText.setEnabled(false);
				borwserBtn.setEnabled(false);
			}

			if (curDbMetaData.serverNameSupported()) {
				hostText.setEnabled(true);
			} else {
				hostText.setEnabled(false);
			}

			if (curDbMetaData.instanceSupported()) {
				instanceText.setEnabled(true);
			} else {
				instanceText.setEnabled(false);
			}

			if (curDbMetaData.portSupported()) {
				portText.setEnabled(true);
			} else {
				portText.setEnabled(false);
			}

			if (curDbMetaData.userNameSupported()) {
				usernameText.setEnabled(true);
			} else {
				usernameText.setEnabled(false);
			}

			if (curDbMetaData.passwordSupported()) {
				passwordText.setEnabled(true);
			} else {
				passwordText.setEnabled(false);
			}
		}
	}

	private List<DatabaseManagerDbListBean> searchCurrServerDatabase(SystemDBOperator sysDbOp,
			DatabaseModelBean currServer) {
		List<DatabaseManagerDbListBean> temp = new ArrayList<DatabaseManagerDbListBean>();
		// 检查是否创建了系统库
		if (!sysDbOp.checkSysDb()) {
			// MessageDialog.openInformation(getShell(), "提示信息",
			// "当前服务器上没有创建任何库");
			setLastSelectedServer(null);
			return null;
		}

		// 检查是否创建了库
		List<String> dbNames = sysDbOp.getSysDbNames(Constants.DB_TYPE_TB);
		if (dbNames.size() == 0) {
			// MessageDialog.openInformation(getShell(), "提示信息",
			// "当前服务器上没有创建任何库");
			setLastSelectedServer(null);
			return null;
		}

		// 获取数据库相关资料,封装了库名称和语言
		MetaData metaData = currServer.toDbMetaData();
		DBOperator dbop = DatabaseService.getDBOperator(metaData);
		for (int i = 0; i < dbNames.size(); i++) {
			DatabaseManagerDbListBean bean = new DatabaseManagerDbListBean();
			String dbName = dbNames.get(i);
			bean.setIndex(i + 1 + "");
			bean.setDbName(dbName);
			metaData.setDatabaseName(dbName);
			dbop.setMetaData(metaData);
			String lang = "";
			try {
				dbop.start();
				List<String> langs = dbop.getLanguages();
				for (int j = 0; j < langs.size(); j++) {
					lang += langs.get(j);
					if (j != langs.size() - 1) {
						lang += ", ";
					}
				}
			} catch (Exception e1) {
				logger.error("", e1);
				continue;
			} finally {
				try {
					if (dbop != null) {
						dbop.end();
					}
				} catch (SQLException e) {
					logger.error("", e);
				}
			}
			if (lang.equals("")) {
				bean.setLangs(Messages.getString("dialog.TermDbManagerDialog.msg10"));
			} else {
				bean.setLangs(lang);
			}
			temp.add(bean);
		}
		return temp;
	}

	/**
	 * 执行查询
	 * @param sysDbOp
	 *            ;
	 */
	private void executeSearch(final SystemDBOperator sysDbOp) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// 连接检查
				if (!sysDbOp.checkDbConnection()) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialog.TermDbManagerDialog.msgTitle"),
							Messages.getString("dialog.TermDbManagerDialog.msg1"));
					setLastSelectedServer(null);
					return;
				}

				// 获取数据库信息,包括名称和语言
				List<DatabaseManagerDbListBean> temp = searchCurrServerDatabase(sysDbOp, currServer);

				currServerdbListInput.clear();
				if (temp != null) {
					currServerdbListInput.addAll(temp);
					if (temp.size() > 0) {
						getDbTableViewer().setSelection(new StructuredSelection(temp.get(0)));
					}
					setLastSelectedServer(currServer.getId());
				}
			}
		});
	}

	/**
	 * 创建新库 ;
	 */
	private void createNewDatabase() {
		// 数据库连接参数输入合法性检查
		IStatus status = validator();
		if (status.getSeverity() != IStatus.OK) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TermDbManagerDialog.msgTitle"),
					status.getMessage());
			return;
		}
		SystemDBOperator sysDbOp = getCurrSysDbOp();

		if (sysDbOp == null) {
			return;
		}

		// 连接检查
		if (!sysDbOp.checkDbConnection()) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TermDbManagerDialog.msgTitle"),
					Messages.getString("dialog.TermDbManagerDialog.msg1"));
			return;
		}

		TermDbNameInputDialog inputDbNameialog = new TermDbNameInputDialog(getShell(),
				Messages.getString("dialog.TermDbManagerDialog.inputDbNameialogTitle"),
				Messages.getString("dialog.TermDbManagerDialog.inputDbNameialogMsg"), "", new IInputValidator() {
					public String isValid(String newText) {
						String vRs = DbValidator.valiateDbName(newText);
						return vRs;
					}
				});
		inputDbNameialog.setSystemDbOp(sysDbOp);
		if (inputDbNameialog.open() == Window.OK) {
			executeSearch(sysDbOp); // 刷新界面
		}
	}

	/** key-数据库的数据库封装,value-库中的语言 */
	private Map<DatabaseModelBean, String> hasSelected;

	/**
	 * 当使用该对话框作为数据库选择时 ;
	 */
	private void executeSelectDatabase() {
		ISelection selection = getDbTableViewer().getSelection();
		if (selection.isEmpty()) {
			return;
		}
		hasSelected = new HashMap<DatabaseModelBean, String>();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		Iterator<?> it = structuredSelection.iterator();
		while (it.hasNext()) {
			DatabaseManagerDbListBean dbBean = (DatabaseManagerDbListBean) it.next();
			DatabaseModelBean dbModelBean = new DatabaseModelBean();
			currServer.copyToOtherIntance(dbModelBean);
			dbModelBean.setDbName(dbBean.getDbName());
			// Fix Bug #3290 导出TMX/TBX--导出内容异常 去掉语言前后的空格
			hasSelected.put(dbModelBean, dbBean.getLangs().replace(" ", ""));
		}
	}

	protected void okPressed() {
		if (getDialogUseFor() == TYPE_DBSELECTED) {
			executeSelectDatabase();
		}
		super.okPressed();
	}

	@Override
	public boolean close() {
		if (internalDbImg != null && !internalDbImg.isDisposed()) {
			internalDbImg.dispose();
		}
		if (mySqlImg != null && !mySqlImg.isDisposed()) {
			mySqlImg.dispose();
		}
		if (oracleImg != null && !oracleImg.isDisposed()) {
			oracleImg.dispose();
		}
		if (sqlServerImg != null && !sqlServerImg.isDisposed()) {
			sqlServerImg.dispose();
		}
		if (postgreImg != null && !postgreImg.isDisposed()) {
			postgreImg.dispose();
		}
		if (ipImg != null && !ipImg.isDisposed()) {
			ipImg.dispose();
		}
		if (sqliteImg != null && !sqliteImg.isDisposed()) {
			sqliteImg.dispose();
		}
		return super.close();
	}

	/**
	 * 获取当前选择的库
	 * @return ;
	 */
	public Map<DatabaseModelBean, String> getHasSelectedDatabase() {
		if (hasSelected != null) {
			return hasSelected;
		} else {
			return new HashMap<DatabaseModelBean, String>();
		}
	}
}
