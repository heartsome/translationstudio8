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
package net.heartsome.cat.database.ui.tb.wizard;

import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.database.ui.tb.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TermDbManagerImportWizard extends Wizard {
	private TermDbManagerImportWizardTbxPage tbxPage;

	public TermDbManagerImportWizard() {
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		setWindowTitle(Messages.getString("wizard.TermDbManagerImportWizard.title"));
		tbxPage = new TermDbManagerImportWizardTbxPage();
		addPage(tbxPage);
	}

	@Override
	public boolean performFinish() {

		final String tbxFile = tbxPage.getTBXFile();
		if (tbxFile == null || tbxFile.equals("")) {
			tbxPage.setErrorMessage(Messages.getString("wizard.TermDbManagerImportWizard.msg"));
			return false;
		}
		tbxPage.setErrorMessage(null);
		tbxPage.setMessage(null);
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				tbxPage.executeImport(tbxFile, monitor);
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
//		tbxPage.setPageComplete(false);

		return true;
	}

}
