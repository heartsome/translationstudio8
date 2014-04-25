package net.heartsome.cat.ts.handlexlf.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.handlexlf.Activator;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.handlexlf.split.MergeXliff;
import net.heartsome.cat.ts.handlexlf.split.SplitOrMergeXlfModel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeXliffWizard extends Wizard{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MergeXliffWizard.class);
	
	private SplitOrMergeXlfModel model;
	MergeXliff mergeXliff;
	private MergeXliffWizardPage mergeXliffWizardPage;
	private boolean canFinish = false;
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	
	public MergeXliffWizard(SplitOrMergeXlfModel model){
		this.model = model;
		mergeXliff = new MergeXliff(model);
		mergeXliffWizardPage = new MergeXliffWizardPage("merge xliff", model);
	}
	
	@Override
	public boolean performFinish() {
		final IRunnableWithProgress mergeProgress = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				Vector<String> mergeXlfPaths = new Vector<String>();
				for (int i = 0; i < model.getMergeXliffFile().size(); i++) {
					mergeXlfPaths.add(model.getMergeXliffFile().get(i).getLocation().toOSString());
				}
				canFinish = mergeXliff.merge(mergeXlfPaths, monitor);
			}
		};
		
		try {
			getContainer().run(true, true, mergeProgress);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (canFinish) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			FileEditorInput input = new FileEditorInput(ResourceUtils.fileToIFile(mergeXliff.getTargetFilePath()));
			try {
				page.openEditor(input, XLIFF_EDITOR_ID);
			} catch (PartInitException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}
		return canFinish;
	}
	
	public boolean canFinish(){
		return super.canFinish();
	}
	
	@Override
	public void addPages() {
		setWindowTitle(Messages.getString("wizard.MergeXliffWizard.windowTitle"));
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("images/2.PNG"));
		addPage(mergeXliffWizardPage);
		setNeedsProgressMonitor(true);
	}
}
