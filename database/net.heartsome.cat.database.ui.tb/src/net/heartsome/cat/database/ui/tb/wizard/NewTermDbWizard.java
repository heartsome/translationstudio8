/**
 * NewTMBWizard.java
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

import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.ui.tb.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class NewTermDbWizard extends Wizard implements INewWizard {

	private NewTermDbBaseInfoPage createDbPage;
	private NewTermDbImportPage importPage;

	/**
	 * 
	 */
	public NewTermDbWizard() {
		setWindowTitle(Messages.getString("wizard.NewTermDbWizard.title"));
	}

	@Override
	public void addPages() {
		createDbPage = new NewTermDbBaseInfoPage();
		addPage(createDbPage);
		importPage = new NewTermDbImportPage(createDbPage);
		addPage(importPage);
		setNeedsProgressMonitor(true);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	@Override
	public boolean canFinish() {
		return super.canFinish();
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {		
		final String tbxFile = importPage.getTBXFile();
		final SystemDBOperator dbOp = createDbPage.getCurrDbOp();
		if (dbOp.getDBConfig().getDefaultType().equals(Constants.DBTYPE_SQLITE)) {
			String dbName = dbOp.getMetaData().getDatabaseName();
			dbName += ".hstb";
			dbOp.getMetaData().setDatabaseName(dbName);
		}
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				importPage.performFinish(tbxFile, dbOp, monitor);
			}
		};

		try {
			getContainer().run(true, true, runnable);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public DatabaseModelBean getCreateDb() {
		DatabaseModelBean db = createDbPage.getDbModel();
		if (db.getDbType().equals(Constants.DBTYPE_SQLITE)) {
			String dbName = db.getDbName();
			dbName += ".hstb";
			db.setDbName(dbName);
		}
		return db;
	}
}
