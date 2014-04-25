package net.heartsome.cat.ts.exportproject.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.heartsome.cat.ts.exportproject.resource.Messages;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.FileSystemExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.IDataTransferHelpContextIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此类与 org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1 代码大多数是一样的，区别为删除了 Options 中的按钮。
 * @author peason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class WizardFileSystemResourceExportPage2 extends WizardExportResourcesPage2 implements Listener {

	// widgets
	private Text destinationNameField;

	private Button destinationBrowseButton;

	protected Button overwriteExistingFilesCheckbox;

	// protected Button createDirectoryStructureButton;

	// protected Button createSelectionOnlyButton;

	// dialog store id constants
	private static final String STORE_DESTINATION_NAMES_ID = "WizardFileSystemResourceExportPage2.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

	private static final String STORE_OVERWRITE_EXISTING_FILES_ID = "WizardFileSystemResourceExportPage2.STORE_OVERWRITE_EXISTING_FILES_ID"; //$NON-NLS-1$

	private static final String STORE_CREATE_STRUCTURE_ID = "WizardFileSystemResourceExportPage2.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$

	// messages
	private static final String SELECT_DESTINATION_MESSAGE = DataTransferMessages.FileExport_selectDestinationMessage;

	private static final String SELECT_DESTINATION_TITLE = DataTransferMessages.FileExport_selectDestinationTitle;

	private static final String SELECT_TM_EXPORTED = "WizardFileSystemResourceExportPage2.SELECT_TM_EXPORTED";

	private static final String SELECT_TB_EXPORTED = "WizardFileSystemResourceExportPage2.SELECT_TB_EXPORTED";

	/**
	 * Create an instance of this class
	 */
	protected WizardFileSystemResourceExportPage2(String name, IStructuredSelection selection) {
		super(name, selection);
	}

	/**
	 * Create an instance of this class.
	 * @param selection
	 *            the selection
	 */
	public WizardFileSystemResourceExportPage2(IStructuredSelection selection) {
		this("fileSystemExportPage2", selection); //$NON-NLS-1$
		setTitle(DataTransferMessages.DataTransfer_fileSystemTitle);
		setDescription(DataTransferMessages.FileExport_exportLocalFileSystem);
	}

	/**
	 * Add the passed value to self's destination widget's history
	 * @param value
	 *            java.lang.String
	 */
	// protected void addDestinationItem(String value) {
	// destinationNameField.add(value);
	// }

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		giveFocusToDestination();
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(getControl(), IDataTransferHelpContextIds.FILE_SYSTEM_EXPORT_WIZARD_PAGE);
	}

	/**
	 * Create the export destination specification widgets
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 */
	protected void createDestinationGroup(Composite parent) {

		Font font = parent.getFont();
		// destination specification group
		Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		destinationSelectionGroup.setLayout(layout);
		destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL));
		destinationSelectionGroup.setFont(font);

		Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
		destinationLabel.setText(getDestinationLabel());
		destinationLabel.setFont(font);

		// destination name entry field
		destinationNameField = new Text(destinationSelectionGroup, SWT.BORDER);
		destinationNameField.addListener(SWT.Modify, this);
		destinationNameField.addListener(SWT.Selection, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		destinationNameField.setLayoutData(data);
		destinationNameField.setFont(font);

		// destination browse button
		destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
		destinationBrowseButton.setText(DataTransferMessages.DataTransfer_browse);
		destinationBrowseButton.addListener(SWT.Selection, this);
		destinationBrowseButton.setFont(font);
		setButtonLayoutData(destinationBrowseButton);

		new Label(parent, SWT.NONE); // vertical spacer
	}

	/**
	 * Create the button for checking if we should ask if we are going to overwrite existing files.
	 * @param optionsGroup
	 * @param font
	 */
	protected void createOverwriteExisting(Group optionsGroup, Font font) {
		// overwrite... checkbox
		overwriteExistingFilesCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		overwriteExistingFilesCheckbox.setText(DataTransferMessages.ExportFile_overwriteExisting);
		overwriteExistingFilesCheckbox.setFont(font);
	}

	/**
	 * Attempts to ensure that the specified directory exists on the local file system. Answers a boolean indicating
	 * success.
	 * @return boolean
	 * @param directory
	 *            java.io.File
	 */
	protected boolean ensureDirectoryExists(File directory) {
		if (!directory.exists()) {
			if (!queryYesNoQuestion(DataTransferMessages.DataTransfer_createTargetDirectory)) {
				return false;
			}

			if (!directory.mkdirs()) {
				displayErrorDialog(DataTransferMessages.DataTransfer_directoryCreationError);
				giveFocusToDestination();
				return false;
			}
		}

		return true;
	}

	/**
	 * If the target for export does not exist then attempt to create it. Answer a boolean indicating whether the target
	 * exists (ie.- if it either pre-existed or this method was able to create it)
	 * @return boolean
	 */
	protected boolean ensureTargetIsValid(File targetDirectory) {
		if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
			displayErrorDialog(DataTransferMessages.FileExport_directoryExists);
			giveFocusToDestination();
			return false;
		}

		return ensureDirectoryExists(targetDirectory);
	}

	/**
	 * Set up and execute the passed Operation. Answer a boolean indicating success.
	 * @return boolean
	 */
	protected boolean executeExportOperation(FileSystemExportOperation op) {
		op.setCreateLeadupStructure(true);
		op.setOverwriteFiles(overwriteExistingFilesCheckbox.getSelection());

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

	private static final Logger LOGGER = LoggerFactory.getLogger(WizardFileSystemResourceExportPage2.class);

	/**
	 * The Finish button was pressed. Try to do the required work now and answer a boolean indicating success. If false
	 * is returned then the wizard will not close.
	 * @return boolean
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
		if (!ensureTargetIsValid(new File(getDestinationValue()))) {
			return false;
		}

		// Save dirty editors if possible but do not stop if not all are saved
		saveDirtyEditors();
		// about to invoke the operation so save our state
		saveWidgetValues();

		return executeExportOperation(new FileSystemExportOperation(null, resourcesToExport, getDestinationValue(),
				this));
	}

	/**
	 * Answer the string to display in self as the destination type
	 * @return java.lang.String
	 */
	protected String getDestinationLabel() {
		return DataTransferMessages.FileExport_toDirectory;
	}

	/**
	 * Answer the contents of self's destination specification widget
	 * @return java.lang.String
	 */
	protected String getDestinationValue() {
		return destinationNameField.getText().trim();
	}

	/**
	 * Set the current input focus to self's destination entry field
	 */
	protected void giveFocusToDestination() {
		destinationNameField.setFocus();
	}

	/**
	 * Open an appropriate destination browser so that the user can specify a source to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
		dialog.setMessage(SELECT_DESTINATION_MESSAGE);
		dialog.setText(SELECT_DESTINATION_TITLE);
		dialog.setFilterPath(getDestinationValue());
		String selectedDirectoryName = dialog.open();

		if (selectedDirectoryName != null) {
			setErrorMessage(null);
			setDestinationValue(selectedDirectoryName);
		}
	}

	/**
	 * Handle all events and enablements for widgets in this page
	 * @param e
	 *            Event
	 */
	public void handleEvent(Event e) {
		Widget source = e.widget;

		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}

		updatePageCompletion();
	}

	/**
	 * Hook method for saving widget values for restoration by the next instance of this class.
	 */
	protected void internalSaveWidgetValues() {
		// update directory names history
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) {
				directoryNames = new String[0];
			}

			directoryNames = addToHistory(directoryNames, getDestinationValue());
			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);

			// options
			if (null != overwriteExistingFilesCheckbox) {
				settings.put(STORE_OVERWRITE_EXISTING_FILES_ID, overwriteExistingFilesCheckbox.getSelection());
			}

			settings.put(STORE_CREATE_STRUCTURE_ID, true);
			settings.put(SELECT_TM_EXPORTED, tmCheck.getSelection());
			settings.put(SELECT_TB_EXPORTED, tbCheck.getSelection());
		}
	}

	/**
	 * Hook method for restoring widget values to the values that they held last time this wizard was used to
	 * completion.
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) {
				return; // ie.- no settings stored
			}

			// destination
			setDestinationValue(directoryNames[0]);
			// for (int i = 0; i < directoryNames.length; i++) {
			// addDestinationItem(directoryNames[i]);
			// }

			// options
			if(null!=overwriteExistingFilesCheckbox){			
				overwriteExistingFilesCheckbox.setSelection(settings.getBoolean(STORE_OVERWRITE_EXISTING_FILES_ID));
			}
			tmCheck.setSelection(settings.getBoolean(SELECT_TM_EXPORTED));
			tbCheck.setSelection(settings.getBoolean(SELECT_TB_EXPORTED));
		}
	}

	/**
	 * Set the contents of the receivers destination specification widget to the passed value
	 */
	protected void setDestinationValue(String value) {
		destinationNameField.setText(value);
	}

	/**
	 * Answer a boolean indicating whether the receivers destination specification widgets currently all contain valid
	 * values.
	 */
	protected boolean validateDestinationGroup() {
		String destinationValue = getDestinationValue();
		if (destinationValue.length() == 0) {
			setMessage(destinationEmptyMessage());
			return false;
		}

		String conflictingContainer = getConflictingContainerNameFor(destinationValue);
		if (conflictingContainer == null) {
			// no error message, but warning may exists
			String threatenedContainer = getOverlappingProjectName(destinationValue);
			if (threatenedContainer == null)
				setMessage(null);
			else
				setMessage(NLS.bind(DataTransferMessages.FileExport_damageWarning, threatenedContainer), WARNING);

		} else {
			setErrorMessage(NLS.bind(DataTransferMessages.FileExport_conflictingContainer, conflictingContainer));
			giveFocusToDestination();
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardDataTransferPage#validateSourceGroup()
	 */
	protected boolean validateSourceGroup() {
		// there must be some resources selected for Export
		boolean isValid = true;
		List resourcesToExport = getWhiteCheckedResources();
		if (resourcesToExport.size() == 0) {
			setErrorMessage(DataTransferMessages.FileExport_noneSelected);
			isValid = false;
		} else if (getDestinationValue() != null && !getDestinationValue().equals("")) {
			setDescription("");
		} else {
			setErrorMessage(null);

		}
		return super.validateSourceGroup() && isValid;
	}

	/**
	 * Get the message used to denote an empty destination.
	 */
	protected String destinationEmptyMessage() {
		return DataTransferMessages.FileExport_destinationEmpty;
	}

	/**
	 * Returns the name of a container with a location that encompasses targetDirectory. Returns null if there is no
	 * conflict.
	 * @param targetDirectory
	 *            the path of the directory to check.
	 * @return the conflicting container name or <code>null</code>
	 */
	protected String getConflictingContainerNameFor(String targetDirectory) {

		IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath testPath = new Path(targetDirectory);
		// cannot export into workspace root
		if (testPath.equals(rootPath))
			return rootPath.lastSegment();

		// Are they the same?
		if (testPath.matchingFirstSegments(rootPath) == rootPath.segmentCount()) {
			String firstSegment = testPath.removeFirstSegments(rootPath.segmentCount()).segment(0);
			if (!Character.isLetterOrDigit(firstSegment.charAt(0)))
				return firstSegment;
		}

		return null;

	}

	/**
	 * Returns the name of a {@link IProject} with a location that includes targetDirectory. Returns null if there is no
	 * such {@link IProject}.
	 * @param targetDirectory
	 *            the path of the directory to check.
	 * @return the overlapping project name or <code>null</code>
	 */
	private String getOverlappingProjectName(String targetDirectory) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath testPath = new Path(targetDirectory);
		IContainer[] containers = root.findContainersForLocation(testPath);
		if (containers.length > 0) {
			return containers[0].getProject().getName();
		}
		return null;
	}

	private Button tmCheck;

	private Button tbCheck;

	/**
	 * 创建是否导出记忆库和术语库 (non-Javadoc)
	 * @see net.heartsome.cat.ts.exportproject.wizards.WizardExportResourcesPage2#createDBExportGroup(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createDBExportGroup(Composite parent) {
		// TODO Auto-generated method stub
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		tmCheck = new Button(composite, SWT.CHECK);
		tmCheck.setText(Messages.getString("wizardWizardFileSystemResourceExportPage2.exportTm"));
		tbCheck = new Button(composite, SWT.CHECK);
		tbCheck.setText(Messages.getString("wizardWizardFileSystemResourceExportPage2.exportTb"));

	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.exportproject.wizards.WizardExportResourcesPage2#isExportSQLiteTMs()
	 */
	@Override
	public boolean isExportSQLiteTMs() {
		if (null != tmCheck) {
			return tmCheck.getSelection();
		}
		return super.isExportSQLiteTMs();
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.exportproject.wizards.WizardExportResourcesPage2#isExportSQLiteTBs()
	 */
	@Override
	public boolean isExportSQLiteTBs() {
		if (null != tbCheck) {
			return tbCheck.getSelection();
		}
		return super.isExportSQLiteTBs();
	}
}
