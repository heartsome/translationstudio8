/**
 * ImportWizardTbxPage.java
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
package net.heartsome.cat.database.ui.tb.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.common.ui.HSDropDownButton;
import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.database.ui.tb.Utils;
import net.heartsome.cat.database.ui.tb.dialog.TermDbManagerDialog;
import net.heartsome.cat.database.ui.tb.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
public class TermDbManagerImportWizardTbxPage extends WizardPage {
	Logger logger = LoggerFactory.getLogger(TermDbManagerImportWizardTbxPage.class);
	private Text tbxFileText;
	private DatabaseModelBean dbModel;
	private Text text;

	/**
	 * Create the wizard.
	 */
	public TermDbManagerImportWizardTbxPage() {
		super("wizardPage");
		setTitle(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.title"));
		setDescription(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.desc"));
		setImageDescriptor(Activator.getImageDescriptor("images/dialog/import-tbx-logo.png"));
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
		lblTmx.setText(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.lblTmx"));

		tbxFileText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		tbxFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tbxFileText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validator();
			}
		});

		Button tmxFileBorwserBtn = new Button(container, SWT.NONE);
		tmxFileBorwserBtn.setText(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.tmxFileBorwserBtn"));
		tmxFileBorwserBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell());
				String[] filterExt = { "*.tbx", "*.xlsx", "*.txt" };
				dlg.setFilterExtensions(filterExt);
				String path = dlg.open();
				// String path = NativeDialogFactory.fileSelectionDialog(getShell(),
				// Messages.getString("wizard.TermDbManagerImportWizardTbxPage.openFile"), SWT.OPEN);
				if (path != null) {
					tbxFileText.setText(path);
				}
			}
		});

		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.lblNewLabel"));

		text = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validator();
			}
		});

		HSDropDownButton addBtn = new HSDropDownButton(container, SWT.NONE);
		addBtn.setText(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.button"));
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
					dbModel = it.next();
					text.setText(f.getAbsolutePath());
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
	}

	public void executeImport(String tbxFile, IProgressMonitor monitor) throws InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		monitor.setTaskName(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.task1"));
		int tbxResult = -10;

		String message = "";
		try {
			tbxResult = DatabaseService.importTbxWithFile(tbxFile, monitor, dbModel.toDbMetaData(),
					getTbxImportStrategy());
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

		StringBuffer resultMessage = new StringBuffer();
		if (!resultMessage.toString().equals("")) {
			final String _message = resultMessage.toString();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setErrorMessage(_message);
				}
			});
			throw new InterruptedException();
		}

		if (tbxResult != DatabaseService.SUCCESS) {
			if (tbxResult == DatabaseService.FAILURE_1) {
				resultMessage.append(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg1"));
			} else if (tbxResult == DatabaseService.FAILURE_2) {
				resultMessage.append(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg2"));
			} else if (tbxResult == DatabaseService.FAILURE_3) {
				resultMessage.append(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg3"));
			} else if (tbxResult == DatabaseService.FAILURE_4) {
				resultMessage.append(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg4"));
			} else if (tbxResult == DatabaseService.FAILURE) {
				resultMessage.append(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg5"));
			} else if (tbxResult == DatabaseService.CANCEL) {
				resultMessage.append(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg6"));
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
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setErrorMessage(null);
					setMessage(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg7"));
				}
			});

		}
		monitor.done();
	}

	/**
	 * 输入验证器 ;
	 */
	private void validator() {
		setErrorMessage(null);
		setMessage(null);
		String tmxFile = getTBXFile();
		if (tmxFile == null) {
			setErrorMessage(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg8"));
			setPageComplete(false);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}

		if (this.dbModel == null) {
			setErrorMessage(Messages.getString("wizard.TermDbManagerImportWizardTbxPage.msg9"));
			setPageComplete(false);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	/**
	 * 从首选项中读取导入策略
	 * @return ;
	 */
	public int getTbxImportStrategy() {
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		return ps.getInt(TBPreferenceConstants.TB_UPDATE);
	}

	public String getTBXFile() {
		String file = tbxFileText.getText().trim();
		if (file == null || file.length() == 0) {
			return null;
		}
		return file;
	}
}
