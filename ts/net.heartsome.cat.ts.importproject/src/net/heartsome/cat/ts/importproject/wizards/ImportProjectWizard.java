package net.heartsome.cat.ts.importproject.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class ImportProjectWizard extends Wizard implements IImportWizard {
	
    private static final String IMPORT_PROJECT_SECTION = "ImportProjectWizard";//$NON-NLS-1$
    
    private Logger LOGGER = LoggerFactory.getLogger(ImportProjectWizard.class);
    
//	private ImportProjectWizardPage mainPage;
	private ImportProjectWizardPage2 mainPage;
	
	private IStructuredSelection currentSelection = null;
	
	private String initialPath = null;
	
	private boolean canFinish;
	
    /**
     * Constructor for ExternalProjectImportWizard.
     */
    public ImportProjectWizard() {
    	this(null);
    }

    /**
     * Constructor for ExternalProjectImportWizard.
     * 
     * @param initialPath Default path for wizard to import
     * @since 3.5
     */
	public ImportProjectWizard(String initialPath)
    {
        super();
        this.initialPath = initialPath;
        setWindowTitle(DataTransferMessages.DataTransfer_importTitle);
        setNeedsProgressMonitor(true);
        IDialogSettings workbenchSettings = IDEWorkbenchPlugin.getDefault()
        		.getDialogSettings();
        
		IDialogSettings wizardSettings = workbenchSettings
		        .getSection(IMPORT_PROJECT_SECTION);
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings
		            .addNewSection(IMPORT_PROJECT_SECTION);
		}
		setDialogSettings(wizardSettings);        
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();
//		mainPage = new ImportProjectWizardPage("wizardExternalProjectsPage", initialPath, currentSelection); //$NON-NLS-1$
		mainPage = new ImportProjectWizardPage2("wizardExternalProjectsPage", initialPath, currentSelection); //$NON-NLS-1$
        addPage(mainPage);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        
        setDefaultPageImageDescriptor(
				IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/importproj_wiz.png")); //$NON-NLS-1$
        this.currentSelection = currentSelection;
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean performCancel() {
    	mainPage.performCancel();
        return true;
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean performFinish() {
    	final IRunnableWithProgress importProgress = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				canFinish = mainPage.createProjects(monitor);
				
			}
		};
		
		try {
			Display.getDefault().syncExec(new Runnable() {				
				public void run() {				
					try {
						getContainer().run(true, true, importProgress);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						LOGGER.error("",e);
					} catch (InterruptedException e) {
						e.printStackTrace();
						LOGGER.error("",e);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("",e);
		}
    	
        return canFinish;
    }

}
