package net.heartsome.cat.ts.handlexlf.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.handlexlf.split.SplitOrMergeXlfModel;
import net.heartsome.cat.ts.handlexlf.split.SplitXliff;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

public class SplitXliffWizard extends Wizard {
	private SplitOrMergeXlfModel model;
	private SplitXliff splitXliff;
	private SplitXliffWizardPage splitXliffWizardPage;
	private boolean canFinish = true;

	public SplitXliffWizard(SplitOrMergeXlfModel model) {
		this.model = model;
		splitXliff = new SplitXliff(this.model);
		splitXliffWizardPage = new SplitXliffWizardPage("split xliff", model, splitXliff);
	}

	@Override
	public boolean performFinish() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath containerIPath = root.getLocation().append(splitXliffWizardPage.getTargetXlfPathStr());
		IContainer splitXlfsContainer = root.getContainerForLocation(containerIPath);
		if (!splitXlfsContainer.exists()) {
			// 创建该路径
			File file = new File(splitXlfsContainer.getLocation().toOSString());
			file.mkdirs();
		}
		model.setSplitXlfsContainer(splitXlfsContainer);

		final IRunnableWithProgress splitProgress = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				// uicallback
				canFinish = splitXliff.splitTheXliff(monitor);
			}
		};

		try {
			getContainer().run(true, true, splitProgress);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// 创建项目后刷新资源视图
		try {
			model.getSplitFile().getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return canFinish;
	}

	public boolean canFinish() {
		return super.canFinish();
	}

	@Override
	public void addPages() {
		setWindowTitle(Messages.getString("wizard.SplitXliffWizard.windowTitle"));
		// setDefaultPageImageDescriptor(Activator.getImageDescriptor("images/file/file-split-logo.png"));
		addPage(splitXliffWizardPage);
		setNeedsProgressMonitor(true);
	}

	public SplitOrMergeXlfModel getSplitOrMergeXlfModel() {
		return model;
	}
}
