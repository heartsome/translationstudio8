package net.heartsome.cat.p2update.ui;

import net.heartsome.cat.p2update.util.P2UpdateUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.ui.ProvUIActivator;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.p2.ui.dialogs.ILayoutConstants;
import org.eclipse.equinox.internal.p2.ui.dialogs.IUDetailsGroup;
import org.eclipse.equinox.internal.p2.ui.dialogs.ResolutionStatusPage;
import org.eclipse.equinox.internal.p2.ui.model.IUElementListRoot;
import org.eclipse.equinox.internal.p2.ui.model.QueriedElement;
import org.eclipse.equinox.internal.p2.ui.viewers.IUColumnConfig;
import org.eclipse.equinox.internal.p2.ui.viewers.IUDetailsLabelProvider;
import org.eclipse.equinox.internal.p2.ui.viewers.ProvElementContentProvider;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
import org.eclipse.equinox.p2.operations.ProfileModificationJob;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * 更新向导页，用于显示需要更新的程序内容
 * @author  Jason
 * @version 
 * @since   JDK1.6
 */
public class UpdateWizardPage extends ResolutionStatusPage {
	ProvisioningUI ui;
	UpdateOperation operation;
	IUElementListRoot input;
	TreeViewer treeViewer;
	
	ProvElementContentProvider contentProvider;
	IUDetailsLabelProvider labelProvider;
	
	protected UpdateWizardPage(UpdateOperation operation,IUElementListRoot root, ProvisioningUI ui) {
		super("MyUpdasteWizardPage1", ui, null);
		this.ui = ui;
		this.operation = operation;
		this.input = root;
		setTitle(P2UpdateUtil.UI_WIZARD_PAGE_TITLE);
		setDescription(P2UpdateUtil.UI_WIZARD_PAGE_DESC);
	}

	public void createControl(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);
		
		treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(data);
		tree.setHeaderVisible(true);
		
		IUColumnConfig[] columns = getColumnConfig();		
		for (int i = 0; i < columns.length; i++) {
			TreeColumn tc = new TreeColumn(tree, SWT.LEFT, i);
			tc.setResizable(true);
			tc.setText(columns[i].getColumnTitle());
			tc.setWidth(columns[i].getWidthInPixels(tree));
		}
		
		contentProvider = new ProvElementContentProvider();
		treeViewer.setContentProvider(contentProvider);
		labelProvider = new IUDetailsLabelProvider(null, getColumnConfig(), getShell());
		treeViewer.setLabelProvider(labelProvider);
		
		setControl(composite);
		
		final Runnable runnable = new Runnable() {
			public void run() {				
//				updateStatus(input, operation);
				setDrilldownElements(input, operation);
				treeViewer.setInput(input);
			}
		};
		
		if (operation != null && !operation.hasResolved()) {
			try {
				getContainer().run(true, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						operation.resolveModal(monitor);
						parent.getDisplay().asyncExec(runnable);
					}
				});
			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, ProvUIActivator.PLUGIN_ID, e.getMessage(), e));
			}
		} else {
			runnable.run();
		}
	}
	
	public boolean performFinish() {
		if (operation.getResolutionResult().getSeverity() != IStatus.ERROR) {
			ProfileModificationJob job = (ProfileModificationJob) operation.getProvisioningJob(null);
			job.setName(P2UpdateUtil.EXECUTE_UPDATE_JOB_NAME);
			job.setTaskName(P2UpdateUtil.EXECUTE_UPDATE_Task_NAME);
			getProvisioningUI().schedule(job, StatusManager.SHOW | StatusManager.LOG);
			return true;
		}
		return false;
	}
	
	void setDrilldownElements(IUElementListRoot root, ProfileChangeOperation operation) {
		if (operation == null || operation.getProvisioningPlan() == null)
			return;
		Object[] elements = root.getChildren(root);
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof QueriedElement) {
				((QueriedElement) elements[i]).setQueryable(operation.getProvisioningPlan().getAdditions());
			}
		}
	}
	
	@Override
	protected void updateCaches(IUElementListRoot newRoot,
			ProfileChangeOperation op) {
		operation = (UpdateOperation) op;
		if (newRoot != null) {
			setDrilldownElements(newRoot, operation);
			if (treeViewer != null) {
				if (input != newRoot)
					treeViewer.setInput(newRoot);
				else
					treeViewer.refresh();
			}
			input = newRoot;
		}
	}
		
	@Override
	protected boolean isCreated() {
		return false;
	}

	@Override
	protected IUDetailsGroup getDetailsGroup() {
		return null;
	}

	@Override
	protected IInstallableUnit getSelectedIU() {
		return null;
	}

	@Override
	protected Object[] getSelectedElements() {
		return null;
	}

	@Override
	protected String getDialogSettingsName() {
		return null;
	}

	@Override
	protected SashForm getSashForm() {
		return null;
	}

	@Override
	protected int getColumnWidth(int index) {
		return 0;
	}

	@Override
	protected String getClipboardText(Control control) {
		return null;
	}

}
