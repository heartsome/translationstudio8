package net.heartsome.cat.convert.ui.dialog;

import java.util.ArrayList;

import net.heartsome.cat.converter.ui.rcp.resource.Messages;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * 工作空间中的文件选择对话框
 * @author cheney
 * @since JDK1.6
 */
public class WorkspaceDialog extends TitleAreaDialog {
	private IFile workspaceFile;
	private TreeViewer wsTreeViewer;
	private Text wsFilenameText;
	private IContainer wsContainer;

	private Button okButton;

	/**
	 * 构建函数
	 * @param shell
	 */
	public WorkspaceDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		setTitle(Messages.getString("dialog.WorkspaceDialog.title"));
		setMessage(Messages.getString("dialog.WorkspaceDialog.msg"));

		return control;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		composite.setLayoutData(data);

		getShell().setText(Messages.getString("dialog.WorkspaceDialog.shell"));

		wsTreeViewer = new TreeViewer(composite, SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 550;
		gd.heightHint = 250;
		wsTreeViewer.getTree().setLayoutData(gd);

		wsTreeViewer.setContentProvider(new LocationPageContentProvider());
		wsTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
		wsTreeViewer.setInput(ResourcesPlugin.getWorkspace());

		final Composite group = new Composite(composite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Label label = new Label(group, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.getString("dialog.WorkspaceDialog.label"));

		wsFilenameText = new Text(group, SWT.BORDER);
		wsFilenameText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		setupListeners();

		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		// disable first
		okButton.setEnabled(false);
	}

	@Override
	protected void okPressed() {
		if (wsContainer == null) {
			getSelectedContainer();
		}

		workspaceFile = wsContainer.getFile(new Path(wsFilenameText.getText()));
		super.okPressed();
	}

	/**
	 * 获得当前选择的 container ;
	 */
	private void getSelectedContainer() {
		Object obj = ((IStructuredSelection) wsTreeViewer.getSelection()).getFirstElement();

		if (obj instanceof IContainer) {
			wsContainer = (IContainer) obj;
		} else if (obj instanceof IFile) {
			wsContainer = ((IFile) obj).getParent();
		}
	}

	@Override
	protected void cancelPressed() {
		// this.page.validatePage();
		getSelectedContainer();
		super.cancelPressed();
	}

	@Override
	public boolean close() {
		return super.close();
	}

	/**
	 * 注册监听 ;
	 */
	void setupListeners() {
		wsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				Object obj = s.getFirstElement();
				if (obj instanceof IContainer) {
					wsContainer = (IContainer) obj;
				} else if (obj instanceof IFile) {
					IFile tempFile = (IFile) obj;
					wsContainer = tempFile.getParent();
					wsFilenameText.setText(tempFile.getName());
				}
			}
		});

		wsTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection s = event.getSelection();
				if (s instanceof IStructuredSelection) {
					Object item = ((IStructuredSelection) s).getFirstElement();
					if (wsTreeViewer.getExpandedState(item)) {
						wsTreeViewer.collapseToLevel(item, 1);
					} else {
						wsTreeViewer.expandToLevel(item, 1);
					}
				}
			}
		});

		wsFilenameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String patchName = wsFilenameText.getText();
				if (patchName.trim().equals("")) { //$NON-NLS-1$
					okButton.setEnabled(false);
					setErrorMessage(Messages.getString("dialog.WorkspaceDialog.msg1"));
				} else if (!(ResourcesPlugin.getWorkspace().validateName(patchName, IResource.FILE)).isOK()) {
					// make sure that the filename does not contain more than one segment
					okButton.setEnabled(false);
					setErrorMessage(Messages.getString("dialog.WorkspaceDialog.msg2"));
				} else {
					okButton.setEnabled(true);
					setErrorMessage(null);
				}
			}
		});
	}

	/**
	 * 返回当前所选择的 IFile
	 * @return ;
	 */
	public IFile getSelectedFile() {
		return workspaceFile;
	}
}

/**
 * 显示工作空间中项目列表内容提供者
 * @author cheney
 * @since JDK1.6
 */
class LocationPageContentProvider extends BaseWorkbenchContentProvider {
	/**
	 * Never show closed projects
	 */
	boolean showClosedProjects = false;

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof IWorkspace) {
			// check if closed projects should be shown
			IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
			if (showClosedProjects) {
				return allProjects;
			}

			ArrayList<IProject> accessibleProjects = new ArrayList<IProject>();
			for (int i = 0; i < allProjects.length; i++) {
				if (allProjects[i].isOpen()) {
					accessibleProjects.add(allProjects[i]);
				}
			}
			return accessibleProjects.toArray();
		}

		return super.getChildren(element);
	}

}