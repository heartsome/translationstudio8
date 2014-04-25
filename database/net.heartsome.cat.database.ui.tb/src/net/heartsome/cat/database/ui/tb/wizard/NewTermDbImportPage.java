/**
 * NewDatabaseWizardImportPage.java
 *
 * Version information :
 *
 * Date:Oct 28, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tb.wizard;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.database.ui.tb.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 导入TBX文件向导页
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class NewTermDbImportPage extends WizardPage {
	private Text tbxFileText;

	private final NewTermDbBaseInfoPage createDbPage;

	/**
	 * Create the wizard.
	 */
	public NewTermDbImportPage(NewTermDbBaseInfoPage createDbPage) {
		super("importWizardPage");
		this.createDbPage = createDbPage;
		setTitle(Messages.getString("wizard.NewTermDbImportPage.title"));
		setDescription(Messages.getString("wizard.NewTermDbImportPage.desc"));
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(3, false));

		Label lblTbx = new Label(container, SWT.NONE);
		lblTbx.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTbx.setText(Messages.getString("wizard.NewTermDbImportPage.lblTbx"));

		tbxFileText = new Text(container, SWT.BORDER);
		tbxFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tbxFileText.setEditable(false);

		Button tbxFileBorwserBtn = new Button(container, SWT.NONE);
		tbxFileBorwserBtn.setText(Messages.getString("wizard.NewTermDbImportPage.tbxFileBorwserBtn"));
		tbxFileBorwserBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell());
				String[] filterExt = { "*.tbx", "*.xlsx", "*.txt" };
				dlg.setFilterExtensions(filterExt);
				String path = dlg.open();
				if (path != null) {
					tbxFileText.setText(path);
				}
			}
		});

	}

	/**
	 * 创建数据库,并导入文件
	 * @param tbxFile
	 *            TBX文件
	 * @param dbOp
	 * @param monitor
	 * @throws InterruptedException
	 *             ;
	 */
	public void performFinish(String tbxFile, SystemDBOperator dbOp, IProgressMonitor monitor)
			throws InterruptedException {
		if (tbxFile != null) {
			monitor.beginTask(Messages.getString("wizard.NewTermDbImportPage.task1"), 1000);
		}

		String message = createDbPage.canCreateDb(new SubProgressMonitor(monitor, 100));
		if (message != null) {
			final String _message = message;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setErrorMessage(_message);
				}
			});
			throw new InterruptedException();
		}

		message = createDbPage.executeCreateDB(dbOp, new SubProgressMonitor(monitor, 100));
		if (message != null) {
			final String _message = message;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setErrorMessage(_message);
				}
			});
			throw new InterruptedException();
		}
		if (tbxFile != null) {
			executeImport(tbxFile, dbOp.getMetaData(), new SubProgressMonitor(monitor, 800));
		}
		monitor.done();
	}

	/**
	 * 执行导入
	 * @param tmxFile
	 * @param tbxFile
	 * @param dbMetaData
	 * @param monitor
	 *            ;
	 */
	public void executeImport(String tbxFile, MetaData dbMetaData, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.setTaskName(Messages.getString("wizard.NewTermDbImportPage.task2"));
		monitor.beginTask("", 1);
		int tbxResult = -10;
		String message = "";
		if (tbxFile != null) {
			try {
				tbxResult = DatabaseService.importTbxWithFile(tbxFile, new SubProgressMonitor(monitor, 100),
						dbMetaData, getTbxImportStrategy());
			} catch (ImportException e) {
				message = e.getMessage();
			}
			if (!message.equals("")) {
				final String _message = message;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setErrorMessage(_message);
					}
				});
			}
		}

		StringBuffer resultMessage = new StringBuffer();
		if (tbxResult != DatabaseService.SUCCESS) {
			if (tbxResult == DatabaseService.FAILURE_1) {
				resultMessage.append(Messages.getString("wizard.NewTermDbImportPage.msg1"));
			} else if (tbxResult == DatabaseService.FAILURE_2) {
				resultMessage.append(Messages.getString("wizard.NewTermDbImportPage.msg2"));
			} else if (tbxResult == DatabaseService.FAILURE_3) {
				resultMessage.append(Messages.getString("wizard.NewTermDbImportPage.msg3"));
			} else if (tbxResult == DatabaseService.FAILURE_4) {
				resultMessage.append(Messages.getString("wizard.NewTermDbImportPage.msg4"));
			} else if (tbxResult == DatabaseService.FAILURE) {
				resultMessage.append(Messages.getString("wizard.NewTermDbImportPage.msg5"));
			} else if (tbxResult == DatabaseService.CANCEL) {
				resultMessage.append(Messages.getString("wizard.NewTermDbImportPage.msg6"));
			}

			if (!resultMessage.toString().equals("")) {
				final String _message = resultMessage.toString();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setErrorMessage(_message);
					}
				});
			}
		}
		monitor.done();
	}

	public String getTBXFile() {
		String file = tbxFileText.getText().trim();
		if (file == null || file.length() == 0) {
			return null;
		}
		return file;
	}

	/**
	 * 从首选项中读取TBX导入策略
	 * @return ;
	 */
	public int getTbxImportStrategy() {
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		return ps.getInt(TBPreferenceConstants.TB_UPDATE);
	}
}
