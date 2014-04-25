/**
 * ImportWizard.java
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

import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.ui.tm.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmDbManagerImportWizard extends Wizard {
	private TmDbManagerImportWizardTmxPage tmxPage;

	public TmDbManagerImportWizard() {
		setNeedsProgressMonitor(true);
		tmxPage = new TmDbManagerImportWizardTmxPage();
	}

	public TmDbManagerImportWizard(DatabaseModelBean dbModel) {
		this();
		tmxPage.setDbModel(dbModel);
	}

	@Override
	public void addPages() {
		setWindowTitle(Messages.getString("wizard.TmDbManagerImportWizard.title"));
		addPage(tmxPage);
	}

	@Override
	public boolean performFinish() {

		final String tmxFile = tmxPage.getTMXFile();
		if (tmxFile == null || tmxFile.equals("")) {
			tmxPage.setErrorMessage(Messages.getString("wizard.TmDbManagerImportWizard.msg"));
			return false;
		}
		tmxPage.setErrorMessage(null);
		tmxPage.setMessage(null);
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				tmxPage.executeImport(tmxFile, monitor);
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
		// tmxPage.setPageComplete(false);

		return true;
	}

}
