/**
 * ImportWizardTmxPage.java
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
package net.heartsome.cat.database.ui.tm.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.common.ui.HSDropDownButton;
import net.heartsome.cat.database.bean.TMPreferenceConstants;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tm.Activator;
import net.heartsome.cat.database.ui.tm.Utils;
import net.heartsome.cat.database.ui.tm.dialog.TmDbManagerDialog;
import net.heartsome.cat.database.ui.tm.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmDbManagerImportWizardTmxPage extends WizardPage {

	Logger logger = LoggerFactory.getLogger(TmDbManagerImportWizardTmxPage.class);
	private Text tmxFileText;
	private DatabaseModelBean dbModel;
	private Text text;

	/**
	 * Create the wizard.
	 */
	public TmDbManagerImportWizardTmxPage() {
		super("wizardPage");
		setTitle(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.title"));
		setDescription(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.desc"));
		setImageDescriptor(Activator.getImageDescriptor("images/dialog/import-tmx-logo.png"));
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));

		Label lblTmx = new Label(container, SWT.NONE);
		lblTmx.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTmx.setText(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.lblTmx"));

		tmxFileText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		tmxFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tmxFileText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validator();
			}
		});

		Button tmxFileBorwserBtn = new Button(container, SWT.NONE);
		tmxFileBorwserBtn.setText(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.tmxFileBorwserBtn"));
		tmxFileBorwserBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell());
				String[] filterExt = {  "*.tmx;*.xlsx;*.txt" };
				dlg.setFilterExtensions(filterExt);
				String path = dlg.open();
				// String path = NativeDialogFactory.fileSelectionDialog(getShell(),
				// Messages.getString("wizard.TmDbManagerImportWizardTmxPage.openFile"), SWT.OPEN);
				if (path != null) {
					tmxFileText.setText(path);
				}
			}
		});

		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.lblNewLabel"));

		text = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validator();
			}
		});

		HSDropDownButton selectedBtn = new HSDropDownButton(container, SWT.NONE);
		selectedBtn.setText(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.button"));
		Menu selectMenu = selectedBtn.getMenu();
		MenuItem item = new MenuItem(selectMenu, SWT.PUSH);
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
					dbModel = it.next();
					text.setText(f.getAbsolutePath());
				}
			}
		});
		MenuItem serverItem = new MenuItem(selectMenu, SWT.PUSH);
		serverItem.setText(Messages.getString("tm.dialog.addTm.DropDownButton.AddServerTm"));
		serverItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TmDbManagerDialog dialog = new TmDbManagerDialog(getShell());
				dialog.setDialogUseFor(TmDbManagerDialog.TYPE_DBSELECTED);
				if (dialog.open() == Window.OK) {
					Iterator<DatabaseModelBean> it = dialog.getHasSelectedDatabase().keySet().iterator();
					List<DatabaseModelBean> list = new ArrayList<DatabaseModelBean>();
					while (it.hasNext()) {
						list.add(it.next());
					}
					if (list.size() > 0) {
						dbModel = list.get(0); // 只取第一个.
						text.setText(dbModel.getDbName());
					}
				}
			}
		});
		setControl(container);
		initValue();
	}

	private void initValue() {
		if (getDbModel() != null) {
			text.setText(getDbModel().getDbName());
		}
		validator();
	}

	/**
	 * 执行导入
	 * @param tmxFile
	 * @param tbxFile
	 * @param dbMetaData
	 * @param monitor
	 *            ;
	 * @throws InterruptedException
	 */
	public void executeImport(String tmxFile, IProgressMonitor monitor) throws InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		monitor.setTaskName(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.task1"));
		int tmxResult = -10;

		String message = "";
		if (tmxFile != null && dbModel != null) {
			try {
				tmxResult = DatabaseService.importTmxWithFile(dbModel.toDbMetaData(), tmxFile, new SubProgressMonitor(
						monitor, 100), getTmxImportStrategy(), isNeedCheckContext());
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
				throw new InterruptedException();
			}
		}

		StringBuffer resultMessage = new StringBuffer();
		if (tmxResult != DatabaseService.SUCCESS) {
			if (tmxResult == DatabaseService.FAILURE_1) {
				resultMessage.append(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg1"));
			} else if (tmxResult == DatabaseService.FAILURE_2) {
				resultMessage.append(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg2"));
			} else if (tmxResult == DatabaseService.FAILURE_3) {
				resultMessage.append(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg3"));
			} else if (tmxResult == DatabaseService.FAILURE_4) {
				resultMessage.append(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg4"));
			} else if (tmxResult == DatabaseService.FAILURE) {
				resultMessage.append(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg5"));
			} else if (tmxResult == DatabaseService.CANCEL) {
				resultMessage.append(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg6"));
			}

			if (!resultMessage.toString().equals("")) {
				final String _message = resultMessage.toString();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setErrorMessage(_message);
					}
				});
			}
			throw new InterruptedException();
		}
		monitor.done();
	}

	public String getTMXFile() {
		String file = tmxFileText.getText().trim();
		if (file == null || file.length() == 0) {
			return null;
		}
		return file;
	}

	/**
	 * 输入验证器 ;
	 */
	private void validator() {
		setErrorMessage(null);
		setMessage(null);
		String tmxFile = getTMXFile();
		if (tmxFile == null) {
			setErrorMessage(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg8"));
			setPageComplete(false);
			return;
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}

		if (this.dbModel == null) {
			setErrorMessage(Messages.getString("wizard.TmDbManagerImportWizardTmxPage.msg9"));
			setPageComplete(false);
			return;
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	/**
	 * 从首选项中读取导入策略
	 * @return ;
	 */
	public int getTmxImportStrategy() {
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		return ps.getInt(TMPreferenceConstants.TM_UPDATE);
	}

	/**
	 * 判断在导入的时候是否需要检查上下文
	 * @return ;
	 */
	public boolean isNeedCheckContext() {
		return false;
	}

	/** @return the dbModel */
	public DatabaseModelBean getDbModel() {
		return dbModel;
	}

	/**
	 * @param dbModel
	 *            the dbModel to set
	 */
	public void setDbModel(DatabaseModelBean dbModel) {
		this.dbModel = dbModel;
	}

}
