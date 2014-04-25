/**
 * DatabaseNameInputDialog.java
 *
 * Version information :
 *
 * Date:Dec 7, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tm.dialog;

import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.ui.tm.resource.Messages;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于创建数据库时，输入数据库名称
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class DatabaseNameInputDialog extends InputDialog {

	private Logger logger = LoggerFactory.getLogger(DatabaseNameInputDialog.class);
	private SystemDBOperator dbop;

	public DatabaseNameInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
			IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}

	public SystemDBOperator getSystemDbOp() {
		return this.dbop;
	}

	public void setSystemDbOp(SystemDBOperator dbop) {
		this.dbop = dbop;
	}

	@Override
	protected void okPressed() {
		SystemDBOperator dbop = getSystemDbOp();
		createNewDatabase(dbop, super.getValue());		
		if(getReturnCode() == 2){
			super.okPressed();
		}
	}

	private void createNewDatabase(final SystemDBOperator sysDbOp, final String dbName) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {

				// 检查是否创建了系统库,没创建则创建
				if (!sysDbOp.checkSysDb()) {
					try {
						sysDbOp.createSysDb();
					} catch (Exception e) {
						logger.error(Messages.getString("dialog.DatabaseNameInputDialog.logger1"), e);
						setReturnCode(1);
						setErrorMessage(Messages.getString("dialog.DatabaseNameInputDialog.msg1"));
						return;
					}
				}

				sysDbOp.getMetaData().setDatabaseName(dbName);
				if (sysDbOp.checkDbExistOnServer()) {
					setReturnCode(1);
					setErrorMessage(Messages.getString("dialog.DatabaseNameInputDialog.msg2"));
					return;
				}

				try {
					if (Constants.FAILURE == sysDbOp.createDB()) {
						setReturnCode(1);
						logger.error(Messages.getString("dialog.DatabaseNameInputDialog.logger2"));
						setErrorMessage(Messages.getString("dialog.DatabaseNameInputDialog.msg1"));
						return;
					}
					sysDbOp.updataSysDb(Constants.DB_TYPE_TM);
					setReturnCode(2);
				} catch (Exception e) {
					setReturnCode(1);
					logger.error(Messages.getString("dialog.DatabaseNameInputDialog.logger2"), e);
					setErrorMessage(Messages.getString("dialog.DatabaseNameInputDialog.msg1"));
					return;
				}
			}
		});
	}
}
