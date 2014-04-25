package net.heartsome.cat.ts.exportproject.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.heartsome.cat.ts.exportproject.resource.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.IDataTransferHelpContextIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导出项目向导页面
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
@SuppressWarnings("restriction")
public class ExportProjectWizardPage extends WizardFileSystemResourceExportPage2 {

	private final static String STORE_DESTINATION_NAMES_ID = "ExportProjectWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

	private final static String STORE_CREATE_STRUCTURE_ID = "ExportProjectWizardPage.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$

	private final static String STORE_COMPRESS_CONTENTS_ID = "ExportProjectWizardPage.STORE_COMPRESS_CONTENTS_ID"; //$NON-NLS-1$

	/**
	 * Create an instance of this class.
	 * @param name
	 *            java.lang.String
	 */
	protected ExportProjectWizardPage(String name, IStructuredSelection selection) {
		super(name, selection);
		setTitle(Messages.getString("wizard.ExportProjectWizardPage.title"));
		setDescription(Messages.getString("wizard.ExportProjectWizardPage.desc"));
	}

	/**
	 * Create an instance of this class
	 * @param selection
	 *            the selection
	 */
	public ExportProjectWizardPage(IStructuredSelection selection) {
		this("exportWizardPage", selection); //$NON-NLS-1$
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(getControl(), IDataTransferHelpContextIds.ZIP_FILE_EXPORT_WIZARD_PAGE);
	}

	/**
	 * Returns a boolean indicating whether the directory portion of the passed pathname is valid and available for use.
	 */
	protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
		int separatorIndex = fullPathname.lastIndexOf(File.separator);

		if (separatorIndex == -1) {
			return true;
		}

		return ensureTargetIsValid(new File(fullPathname.substring(0, separatorIndex)));
	}

	/**
	 * Returns a boolean indicating whether the passed File handle is is valid and available for use.
	 */
	protected boolean ensureTargetFileIsValid(File targetFile) {
		if (targetFile.exists() && targetFile.isDirectory()) {
			displayErrorDialog(DataTransferMessages.ZipExport_mustBeFile);
			giveFocusToDestination();
			return false;
		}

		if (targetFile.exists()) {
			if (targetFile.canWrite()) {
				if (!queryYesNoQuestion(DataTransferMessages.ZipExport_alreadyExists)) {
					return false;
				}
			} else {
				displayErrorDialog(DataTransferMessages.ZipExport_alreadyExistsError);
				giveFocusToDestination();
				return false;
			}
		}

		return true;
	}

	/**
	 * Ensures that the target output file and its containing directory are both valid and able to be used. Answer a
	 * boolean indicating validity.
	 */
	protected boolean ensureTargetIsValid() {
		String targetPath = getDestinationValue();

		if (!ensureTargetDirectoryIsValid(targetPath)) {
			return false;
		}

		if (!ensureTargetFileIsValid(new File(targetPath))) {
			return false;
		}

		return true;
	}

	/**
	 * Export the passed resource and recursively export all of its child resources (iff it's a container). Answer a
	 * boolean indicating success.
	 */
	protected boolean executeExportOperation(ArchiveFileExportOperation2 op) {
		op.setCreateLeadupStructure(true);
		op.setUseCompression(true);
		op.setUseTarFormat(false);
		op.setExportTb(isExportSQLiteTBs());
		op.setExportTm(isExportSQLiteTMs());

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getTargetException());
			return false;
		}

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(), DataTransferMessages.DataTransfer_exportProblems, null, // no
																														// special
																														// message
					status);
			return false;
		}

		return true;
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportProjectWizardPage.class);

	/**
	 * The Finish button was pressed. Try to do the required work now and answer a boolean indicating success. If false
	 * is returned then the wizard will not close.
	 * @returns boolean
	 */
	public boolean finish() {
		List resourcesToExport = getWhiteCheckedResources();
		List defaultExportItems = getDefaultExportItems();
		boolean isContain = false;
		boolean isBelongToSameProject = false;
		for (Object defaultObj : defaultExportItems) {
			if (defaultObj instanceof IResource) {
				try {
					((IResource) defaultObj).refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					e.printStackTrace();
					LOGGER.error("", e);
				}
				String defaultPath = ((IResource) defaultObj).getFullPath().toOSString();
				for (Object obj : resourcesToExport) {
					if (obj instanceof IProject) {
						String path = ((IProject) obj).getFullPath().toOSString();
						if (defaultPath.equals(path + System.getProperty("file.separator") + ".config")
								|| defaultPath.equals(path + System.getProperty("file.separator") + ".project")) {
							isContain = true;
							isBelongToSameProject = true;
							break;
						}
					}
					if (obj instanceof IResource) {
						String path = ((IResource) obj).getFullPath().toOSString();
						String projectPath = ((IResource) obj).getProject().getFullPath().toOSString();
						String defaultProjectPath = ((IResource) defaultObj).getProject().getFullPath().toOSString();
						if (projectPath.equals(defaultProjectPath)) {
							isBelongToSameProject = true;
						}
						if (path.equals(defaultPath)) {
							isContain = true;
							break;
						}
					}
				}
				if (!isContain && isBelongToSameProject) {
					resourcesToExport.add(defaultObj);
				} else {
					isContain = false;
				}
			}
			isBelongToSameProject = false;
		}
		if (!ensureTargetIsValid()) {
			return false;
		}

		// Save dirty editors if possible but do not stop if not all are saved
		saveDirtyEditors();
		// about to invoke the operation so save our state
		saveWidgetValues();
		
		return executeExportOperation(new ArchiveFileExportOperation2(null, resourcesToExport, getDestinationValue()));
	}

	/**
	 * Answer the string to display in the receiver as the destination type
	 */
	protected String getDestinationLabel() {
		return DataTransferMessages.ArchiveExport_destinationLabel;
	}

	/**
	 * Answer the contents of self's destination specification widget. If this value does not have a suffix then add it
	 * first.
	 */
	protected String getDestinationValue() {
		String idealSuffix = getOutputSuffix();
		String destinationText = super.getDestinationValue();

		// only append a suffix if the destination doesn't already have a . in
		// its last path segment.
		// Also prevent the user from selecting a directory. Allowing this will
		// create a ".zip" file in the directory
		if (destinationText.length() != 0 && !destinationText.endsWith(File.separator)) {
			int dotIndex = destinationText.lastIndexOf('.');
			if (dotIndex != -1) {
				// the last path seperator index
				int pathSepIndex = destinationText.lastIndexOf(File.separator);
				if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
					destinationText += idealSuffix;
				}
			} else {
				destinationText += idealSuffix;
			}
		}

		return destinationText;
	}

	/**
	 * Answer the suffix that files exported from this wizard should have. If this suffix is a file extension (which is
	 * typically the case) then it must include the leading period character.
	 */
	protected String getOutputSuffix() {
		// if (zipFormatButton.getSelection()) {
		return ".zip"; //$NON-NLS-1$
		// } else if (compressContentsCheckbox.getSelection()) {
		//			return ".tar.gz"; //$NON-NLS-1$
		// } else {
		//			return ".tar"; //$NON-NLS-1$
		// }
	}

	/**
	 * Open an appropriate destination browser so that the user can specify a source to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
		dialog.setFilterExtensions(new String[] { "*.hszip", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setText(DataTransferMessages.ArchiveExport_selectDestinationTitle);
		String currentSourceString = getDestinationValue();
		int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
		}
		String selectedFileName = dialog.open();

		if (selectedFileName != null) {
			setErrorMessage(null);
			setDestinationValue(selectedFileName);
			if (getWhiteCheckedResources().size() > 0) {
				setDescription(null);
			}
		}
	}

	/**
	 * Hook method for saving widget values for restoration by the next instance of this class.
	 */
	protected void internalSaveWidgetValues() {
		super.internalSaveWidgetValues();
		// update directory names history
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) {
				directoryNames = new String[0];
			}

			directoryNames = addToHistory(directoryNames, getDestinationValue());
			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);

			settings.put(STORE_CREATE_STRUCTURE_ID, true);

			settings.put(STORE_COMPRESS_CONTENTS_ID, true);
		}
	}

	/**
	 * Hook method for restoring widget values to the values that they held last time this wizard was used to
	 * completion.
	 */
	protected void restoreWidgetValues() {
		super.restoreWidgetValues();
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null || directoryNames.length == 0) {
				return; // ie.- no settings stored
			}

			// destination
			setDestinationValue(directoryNames[0]);
//			for (int i = 0; i < directoryNames.length; i++) {
//				addDestinationItem(directoryNames[i]);
//			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
	 */
	protected String destinationEmptyMessage() {
		return DataTransferMessages.ArchiveExport_destinationEmpty;
	}

	/**
	 * Answer a boolean indicating whether the receivers destination specification widgets currently all contain valid
	 * values.
	 */
	protected boolean validateDestinationGroup() {
		// String destinationValue = getDestinationValue();
		//		if (destinationValue.endsWith(".tar")) { //$NON-NLS-1$
		// compressContentsCheckbox.setSelection(false);
		// targzFormatButton.setSelection(true);
		// zipFormatButton.setSelection(false);
		//		} else if (destinationValue.endsWith(".tar.gz") //$NON-NLS-1$
		//				|| destinationValue.endsWith(".tgz")) { //$NON-NLS-1$
		// compressContentsCheckbox.setSelection(true);
		// targzFormatButton.setSelection(true);
		// zipFormatButton.setSelection(false);
		//		} else if (destinationValue.endsWith(".zip")) { //$NON-NLS-1$
		// zipFormatButton.setSelection(true);
		// targzFormatButton.setSelection(false);
		// }

		return super.validateDestinationGroup();
	}
}
