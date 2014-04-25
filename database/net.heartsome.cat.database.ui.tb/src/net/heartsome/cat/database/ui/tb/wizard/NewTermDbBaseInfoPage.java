/**
 * NewTermDbBaseInfoPage.java
 *
 * Version information :
 *
 * Date:Oct 25, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tb.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.core.DatabaseConfiger;
import net.heartsome.cat.database.ui.core.DbValidator;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.database.ui.tb.resource.Messages;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class NewTermDbBaseInfoPage extends WizardPage {
	private static final Logger logger = LoggerFactory.getLogger(NewTermDbBaseInfoPage.class);

	private ComboViewer dbTypeComboViewer;
	private Text dbNameText;
	private Text instanceText;
	private Text locationText;
	private Button borwserBtn;
	private Text hostText;
	private Text portText;
	private Text usernameText;
	private Text passwordText;

	private DatabaseModelBean dbModel;
	private IPreferenceStore pStore;
	private List<SystemDBOperator> dbTypeList;

	/** @return the dbModel */
	public DatabaseModelBean getDbModel() {
		return dbModel;
	}

	/**
	 * 当数据库类型改变时 用于保存当前操作的数据库对象
	 * @see #dbTypeChangeEvent(SystemDBOperator)
	 */
	private SystemDBOperator dbOp;
	private MetaData dbMetaData;

	/**
	 * Create the wizard.
	 */
	public NewTermDbBaseInfoPage() {
		super("newTermDbConnectionInfo");
		setTitle(Messages.getString("wizard.NewTermDbBaseInfoPage.title"));
		setDescription(Messages.getString("wizard.NewTermDbBaseInfoPage.desc"));
		setImageDescriptor(Activator.getImageDescriptor("images/dialog/new-termdb-logo.png"));
		dbModel = new DatabaseModelBean();
		dbTypeList = DatabaseService.getSystemDbOperaterList();

		pStore = Activator.getDefault().getPreferenceStore();
		String dbType = pStore.getString(TBPreferenceConstants.TB_RM_DBTYPE);
		if (dbType != null && !dbType.equals("")) {
			for (SystemDBOperator dbOp : dbTypeList) {
				if (dbOp.getMetaData().getDbType().equals(dbType)) {
					this.dbOp = dbOp;
					break;
				}
			}

			if (this.dbOp != null) {
				dbModel.setDbType(dbType);
				dbModel.setInstance(pStore.getString(TBPreferenceConstants.TB_RM_INSTANCE));
				dbModel.setHost(pStore.getString(TBPreferenceConstants.TB_RM_SERVER));
				dbModel.setPort(pStore.getString(TBPreferenceConstants.TB_RM_PORT));
				dbModel.setUserName(pStore.getString(TBPreferenceConstants.TB_RM_USERNAME));
				dbModel.setItlDBLocation(pStore.getString(TBPreferenceConstants.TB_RM_PATH));
				this.dbMetaData = dbOp.getMetaData();
				dbMetaData.setInstance(dbModel.getInstance());
				dbMetaData.setServerName(dbModel.getHost());
				dbMetaData.setPort(dbModel.getPort());
				dbMetaData.setUserName(dbModel.getUserName());
				dbMetaData.setDataPath(dbModel.getItlDBLocation());
			}
		}
		setPageComplete(false);
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		setControl(container);

		int labelWidth = 100;
		GridData gdLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLabel.widthHint = labelWidth;

		Group dbGroup1 = new Group(container, SWT.NONE);
		dbGroup1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		dbGroup1.setLayout(new GridLayout(2, false));
		dbGroup1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		dbGroup1.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.dbGroup1"));

		Label label = new Label(dbGroup1, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.lblType"));

		dbTypeComboViewer = new ComboViewer(dbGroup1, SWT.READ_ONLY);
		Combo combo = dbTypeComboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// //
		label = new Label(dbGroup1, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.dbNameText"));

		dbNameText = new Text(dbGroup1, SWT.BORDER);
		dbNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(dbGroup1, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.instanceText"));

		instanceText = new Text(dbGroup1, SWT.BORDER);
		instanceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		dbTypeComboViewer.setContentProvider(new ArrayContentProvider());

		dbTypeComboViewer.setInput(dbTypeList);
		dbTypeComboViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				SystemDBOperator dbOp = (SystemDBOperator) element;
				String dbType = dbOp.getDBConfig().getDefaultType();
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

		dbTypeComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection != null && selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					Object selObj = sel.getFirstElement();
					if (selObj != null && selObj instanceof SystemDBOperator) {
						SystemDBOperator selDbOp = (SystemDBOperator) selObj;
						dbTypeChangeEvent(selDbOp);
					}
				}
			}
		});

		Group dbGroup = new Group(container, SWT.NONE);
		dbGroup.setLayout(new GridLayout(5, false));
		dbGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		dbGroup.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.dbGroup"));

		label = new Label(dbGroup, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.hostText"));

		hostText = new Text(dbGroup, SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(dbGroup, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.portText"));

		portText = new Text(dbGroup, SWT.BORDER);
		new Label(dbGroup, SWT.NONE);

		label = new Label(dbGroup, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.locationText"));

		locationText = new Text(dbGroup, SWT.BORDER);
		locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		locationText.setEnabled(false);

		borwserBtn = new Button(dbGroup, SWT.NONE);
		borwserBtn.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.borwserBtn"));
		borwserBtn.setEnabled(false);
		borwserBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				String path = dlg.open();
				if (path != null) {
					locationText.setText(path);
				}
			}
		});

		Group authorityGroup = new Group(container, SWT.NONE);
		authorityGroup.setLayout(new GridLayout(2, false));
		authorityGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		authorityGroup.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.authorityGroup"));

		label = new Label(authorityGroup, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.usernameText"));

		usernameText = new Text(authorityGroup, SWT.BORDER);
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(authorityGroup, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewTermDbBaseInfoPage.passwordText"));

		passwordText = new Text(authorityGroup, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		initDataBindings();
		if (this.dbOp != null) {
			dbTypeComboViewer.setSelection(new StructuredSelection(this.dbOp));
		} else {
			dbTypeComboViewer.setSelection(new StructuredSelection(dbTypeList.get(0)));
		}
		IStatus state = validator();
		if (!state.isOK()) {
			setErrorMessage(state.getMessage());
			setPageComplete(false);
		}
	}

	/**
	 * 当数据库类型发生改变时，实始化当选择类型的
	 * @param changedDbOp
	 *            ;
	 */
	private void dbTypeChangeEvent(SystemDBOperator changedDbOp) {
		dbOp = changedDbOp;
		dbMetaData = dbOp.getMetaData();
		dbModel.setDbType(dbOp.getDBConfig().getDefaultType());

		if (dbMetaData.databaseNameSupported()) {
			dbNameText.setEnabled(true);
			// fix bug2672
			String dbname = dbMetaData.getDatabaseName();
			// if (dbname.equals("")) {
			// // 如果设置的内容都是"",则不会触发binding的验证方法。所以先设置成test，再修改为""
			// dbNameText.setText("test"/* dbMetaData.getDatabaseName() */);
			// }
			dbNameText.setText(dbname);
		} else {
			dbNameText.setEnabled(false);
			dbNameText.setText("");
		}

		if (dbMetaData.dataPathSupported()) {
			locationText.setEnabled(true);
			borwserBtn.setEnabled(true);
			locationText.setText(locationText.getText());
		} else {
			locationText.setEnabled(false);
			borwserBtn.setEnabled(false);
			locationText.setText("");
		}

		if (dbMetaData.serverNameSupported()) {
			hostText.setEnabled(true);
			hostText.setText(dbMetaData.getServerName());
		} else {
			hostText.setEnabled(false);
			hostText.setText("");
		}

		if (dbMetaData.instanceSupported()) {
			instanceText.setEnabled(true);
			instanceText.setText(dbMetaData.getInstance());
		} else {
			instanceText.setEnabled(false);
			instanceText.setText("");
		}

		if (dbMetaData.portSupported()) {
			portText.setEnabled(true);
			portText.setText(dbMetaData.getPort());
		} else {
			portText.setEnabled(false);
			portText.setText("");
		}

		if (dbMetaData.userNameSupported()) {
			usernameText.setEnabled(true);
			usernameText.setText(dbMetaData.getUserName());
		} else {
			usernameText.setEnabled(false);
			usernameText.setText("");
		}

		if (dbMetaData.passwordSupported()) {
			passwordText.setEnabled(true);
			passwordText.setText(dbMetaData.getPassword());
		} else {
			passwordText.setEnabled(false);
			passwordText.setText("");
		}
	}

	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		WizardPageSupport.create(this, bindingContext);

		IObservableValue widgetValue = WidgetProperties.text(SWT.Modify).observe(dbNameText);
		final IObservableValue dbNameModelValue = BeanProperties.value("dbName").observe(dbModel);
		bindingContext.bindValue(widgetValue, dbNameModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(instanceText);
		final IObservableValue instanceModelValue = BeanProperties.value("instance").observe(dbModel);
		bindingContext.bindValue(widgetValue, instanceModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(hostText);
		final IObservableValue hostModelValue = BeanProperties.value("host").observe(dbModel);
		bindingContext.bindValue(widgetValue, hostModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(portText);
		final IObservableValue protModelValue = BeanProperties.value("port").observe(dbModel);
		bindingContext.bindValue(widgetValue, protModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(locationText);
		final IObservableValue locationModelValue = BeanProperties.value("itlDBLocation").observe(dbModel);
		bindingContext.bindValue(widgetValue, locationModelValue, null, null);
		//
		widgetValue = WidgetProperties.text(SWT.Modify).observe(usernameText);
		final IObservableValue usernameModelValue = BeanProperties.value("userName").observe(dbModel);
		bindingContext.bindValue(widgetValue, usernameModelValue, null, null);

		widgetValue = WidgetProperties.text(SWT.Modify).observe(passwordText);
		final IObservableValue passwordModelValue = BeanProperties.value("password").observe(dbModel);
		bindingContext.bindValue(widgetValue, passwordModelValue, null, null);

		MultiValidator myValidator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				dbNameModelValue.getValue();
				instanceModelValue.getValue();
				hostModelValue.getValue();
				protModelValue.getValue();
				locationModelValue.getValue();
				usernameModelValue.getValue();
				passwordModelValue.getValue();
				return validator();
			}
		};
		bindingContext.addValidationStatusProvider(myValidator);
	}

	/**
	 * 输入验证器 ;
	 */
	private IStatus validator() {
		// 选择数据库类型的时候初始化dbMetaDate
		if (null == dbMetaData) {
			return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.msg1"));
		}
		//

		String dbName = dbModel.getDbName();
		String instance = dbModel.getInstance();
		String host = dbModel.getHost();
		String port = dbModel.getPort();
		String location = dbModel.getItlDBLocation();
		String username = dbModel.getUserName();
		String password = dbModel.getPassword();

		if (dbName == null || dbName.trim().length() == 0) {
			return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.msg2"));
		}

		String vRs = DbValidator.valiateDbName(dbName);
		if (vRs != null) {
			return ValidationStatus.error(vRs);
		}

		if (dbMetaData.instanceSupported()) {
			if (instance == null || instance.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.instancemsg"));
			}
			dbMetaData.setInstance(instance == null ? "" : instance);
		}

		if (dbMetaData.dataPathSupported()) {
			if (location == null || location.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.msg3"));
			}
			File f = new File(location);
			if (!f.isDirectory() || !f.exists()) {
				return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.msg11"));
			}
		}

		if (dbMetaData.serverNameSupported()) {
			if (host == null || host.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.msg4"));
			}
		}
		if (dbMetaData.portSupported()) {
			if (port == null || !port.matches("[0-9]+")) {
				return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.msg5"));
			}
		}
		if (dbMetaData.userNameSupported()) {
			if (username == null || username.trim().length() == 0) {
				return ValidationStatus.error(Messages.getString("wizard.NewTermDbBaseInfoPage.msg6"));
			}
		}

		dbMetaData.setDatabaseName(dbName == null ? "" : dbName);
		dbMetaData.setDataPath(location == null ? "" : location);
		dbMetaData.setServerName(host == null ? "" : host);
		dbMetaData.setPort(port == null ? "" : port);
		dbMetaData.setUserName(username == null ? "" : username);
		dbMetaData.setPassword(password == null ? "" : password); // 密码可以为空

		return ValidationStatus.ok();
	}

	/**
	 * 获取数据库操作对象
	 * @return ;
	 */
	public SystemDBOperator getCurrDbOp() {
		return dbOp;
	}

	/**
	 * 检查服务器型数据库
	 * @return ;
	 */
	public String checkDb4Server(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 3);
		monitor.setTaskName(Messages.getString("wizard.NewTermDbBaseInfoPage.task1"));
		String message = null;
		if (!dbOp.checkDbConnection()) {
			message = Messages.getString("wizard.NewTermDbBaseInfoPage.msg7");
			return message;
		}

		if (!dbOp.checkSysDb()) { // 检查是否创建了系统库,没创建则创建
			try {
				dbOp.createSysDb();
			} catch (Exception e) {
				logger.error(Messages.getString("wizard.NewTermDbBaseInfoPage.logger1"));
				message = Messages.getString("wizard.NewTermDbBaseInfoPage.msg8");
				return message;
			}
		}
		monitor.worked(1);
		if (dbOp.checkDbExistOnServer()) {
			message = Messages.getString("wizard.NewTermDbBaseInfoPage.msg9") + dbOp.getMetaData().getDatabaseName();
			return message;
		}

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.done();
		return message;
	}

	public String canCreateDb(IProgressMonitor monitor) {
		String message = null;
		if (dbOp == null) {
			message = Messages.getString("wizard.NewTermDbBaseInfoPage.msg1");
		}

		message = checkDb4Server(monitor);
		if (message != null) {
			final String _message = message;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setErrorMessage(_message);
				}
			});
		} else {
			pStore.setValue(TBPreferenceConstants.TB_RM_DBTYPE, dbModel.getDbType());
			pStore.setValue(TBPreferenceConstants.TB_RM_INSTANCE, dbModel.getInstance());
			pStore.setValue(TBPreferenceConstants.TB_RM_SERVER, dbModel.getHost());
			pStore.setValue(TBPreferenceConstants.TB_RM_PORT, dbModel.getPort());
			pStore.setValue(TBPreferenceConstants.TB_RM_USERNAME, dbModel.getUserName());
			pStore.setValue(TBPreferenceConstants.TB_RM_PATH, dbModel.getItlDBLocation());
		}

		return message;
	}

	/**
	 * 创建数据库
	 * @return ;
	 */
	public String executeCreateDB(SystemDBOperator dbOp, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 3);
		monitor.setTaskName(Messages.getString("wizard.NewTermDbBaseInfoPage.task2"));
		String message = null;
		try {
			if (Constants.FAILURE == dbOp.createDB()) {
				message = Messages.getString("wizard.NewTermDbBaseInfoPage.msg8");
			} else {
				if (!dbOp.getDBConfig().getDefaultType().equals(Constants.DBTYPE_SQLITE)) {
					dbOp.updataSysDb(Constants.DB_TYPE_TB); // 更新系统库
					saveToServerConfigFile(dbOp); // 处理本地服务器列表
				}
			}
			monitor.worked(3);
		} catch (SQLException e) {
			logger.error("", e);
			e.printStackTrace();
			message = Messages.getString("wizard.NewTermDbBaseInfoPage.msg10") + e.getMessage();
			return message;
		} finally {
			if (message != null) {
				final String _message = message;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setErrorMessage(_message);
					}
				});
			}
			monitor.done();
		}
		return message;
	}

	private void saveToServerConfigFile(SystemDBOperator dbOp) {
		DatabaseConfiger cf = new DatabaseConfiger();
		DatabaseModelBean bean = new DatabaseModelBean();
		MetaData md = dbOp.getMetaData();
		bean.metaDatatToBean(md);

		String serverId = cf.isServerExist(bean, md); // 判断当前服务器是否已经存在
		if (serverId == null || serverId.equals("")) { // 不存在
			cf.addServerConfig(bean);
		} else {
			bean.setId(serverId);
			cf.updateServerConfigById(serverId, bean);
		}
	}

	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	public IWizardPage getNextPage() {
		WizardPage nextPage = (WizardPage) super.getNextPage();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				if (canCreateDb(monitor) != null) {
					throw new InterruptedException();
				}
			}
		};

		try {
			getContainer().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			logger.error("", e);
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		nextPage.setErrorMessage(null);
		return nextPage;
	}
}
