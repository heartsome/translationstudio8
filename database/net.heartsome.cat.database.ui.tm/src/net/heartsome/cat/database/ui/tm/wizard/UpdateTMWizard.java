package net.heartsome.cat.database.ui.tm.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.database.bean.TMPreferenceConstants;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tm.Activator;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 更新记忆库 wizard
 * @author peason
 * @version
 * @since JDK1.6
 */
public class UpdateTMWizard extends Wizard {

	private ArrayList<IFile> lstXLIFF;

	private UpdateTMWizardPage page;

	private boolean canFinish = false;

	public UpdateTMWizard(ArrayList<IFile> lstXLIFF) {
		this.lstXLIFF = lstXLIFF;
		page = new UpdateTMWizardPage("Update TM", lstXLIFF);
	}

	public boolean performFinish() {
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final boolean isDraft = page.isDraft();
		final boolean isApproved = page.isApproved();
		final boolean isSignedOff = page.isSignedOff();
		final boolean isTranslated = page.isTranslated();
		final boolean isLocked = page.isLocked();
		if (!isDraft && !isApproved && !isSignedOff && !isTranslated && !isLocked) {
			MessageDialog.openInformation(getShell(), Messages.getString("wizard.UpdateTMWizard.msgTitle"),
					Messages.getString("wizard.UpdateTMWizard.msg"));
			return false;
		}
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		final int contextSize = ps.getInt(TMPreferenceConstants.CONTEXT_MATCH);
		final int tmxImportStrategy = ps.getInt(TMPreferenceConstants.TM_UPDATE);
		IRunnableWithProgress progress = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) {
				monitor.setTaskName(Messages.getString("dialog.UpdateTMDialog.jobTask1"));
				monitor.beginTask(Messages.getString("dialog.UpdateTMDialog.jobTask1"), lstXLIFF.size() * 2);
				for (final IFile xliffFile : lstXLIFF) {
					ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(xliffFile.getProject());
					DatabaseModelBean modelBean = projectConfig.getDefaultTMDb();
					FileEditorInput editorInput = new FileEditorInput(xliffFile);
					IEditorPart editorPart = activePage.findEditor(editorInput);

					// 选择所有语言
					XLFHandler handler = null;
					if (editorPart != null && editorPart instanceof IXliffEditor) {
						// xliff 文件已用 XLIFF 编辑器打开
						IXliffEditor xliffEditor = (IXliffEditor) editorPart;
						handler = xliffEditor.getXLFHandler();
					} else {
						// xliff 文件未打开
						handler = new XLFHandler();
					}

					monitor.subTask(Messages.getString("dialog.UpdateTMDialog.jobTask2"));
					// 修改获取系统用户方式/*System.getProperty("user.name");*/
					String systemUser = PlatformUI.getPreferenceStore()
							.getString(IPreferenceConstants.SYSTEM_USER);
					String[] arrTempTMX = handler.generateTMXToUpdateTM(xliffFile, isApproved, isSignedOff,
							isTranslated, isDraft, isLocked, contextSize, systemUser);
					monitor.worked(1);

					if (arrTempTMX != null) {
						monitor.subTask(Messages.getString("dialog.UpdateTMDialog.jobTask3"));
						// int state = DatabaseService.importTmxWithString(modelBean.toDbMetaData(), arrTempTMX[1],
						// new SubProgressMonitor(monitor, 1), tmxImportStrategy, false, arrTempTMX[0]);
						// if (state != ImportAbstract.SUCCESS && state != ImportAbstract.CANCEL) {
						// Display.getDefault().syncExec(new Runnable() {
						//
						// public void run() {
						// MessageDialog.openInformation(getShell(),
						// Messages.getString("dialog.UpdateTMDialog.job.msgTitle"),
						// Messages.getString("dialog.UpdateTMDialog.job.msg"));
						// }
						// });
						// canFinish = false;
						// return;
						// }
						try {
							DatabaseService.importTmxWithString(modelBean.toDbMetaData(), arrTempTMX[1],
									new SubProgressMonitor(monitor, 1), tmxImportStrategy, false, arrTempTMX[0]);
						} catch (ImportException e) {
							final String msg = e.getMessage();
							Display.getDefault().syncExec(new Runnable() {

								public void run() {
									MessageDialog.openInformation(getShell(),
											Messages.getString("dialog.UpdateTMDialog.job.msgTitle"), msg);
								}
							});
							canFinish = false;
							return;
						}
					}
				}
				monitor.done();
				// 刷新项目
				ResourceUtils.refreshCurentSelectProject();
				canFinish = true;
			}
		};
		try {
			getContainer().run(true, true, progress);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return canFinish;
	}

	@Override
	public void addPages() {
		setWindowTitle(Messages.getString("dialog.UpdateTMDialog.title"));
		addPage(page);
		setNeedsProgressMonitor(true);
	}
}
