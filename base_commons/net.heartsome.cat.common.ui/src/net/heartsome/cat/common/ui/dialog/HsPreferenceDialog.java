/**
 * HsPerferenceDialog.java
 *
 * Version information :
 *
 * Date:2012-6-12
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.ui.dialog;

import net.heartsome.cat.common.ui.resource.Messages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IContributionService;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class HsPreferenceDialog extends PreferenceDialog {

	public HsPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		setMinimumPageSize(450, 450);
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout parentcomLayout = new GridLayout();
		parentcomLayout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		parentcomLayout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		parentcomLayout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		parentcomLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(parentcomLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(composite);

		GridLayout parentLayout = ((GridLayout) composite.getLayout());
		parentLayout.numColumns = 4;
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.verticalSpacing = 0;
		parentLayout.horizontalSpacing = 0;

		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		Control treeControl = createTreeAreaContents(composite);
		createSash(composite, treeControl);

		Label versep = new Label(composite, SWT.SEPARATOR | SWT.VERTICAL);
		GridData verGd = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);

		versep.setLayoutData(verGd);
		versep.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		pageAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		pageAreaComposite.setLayout(layout);

		// Build the Page container
		Composite pageContainer = createPageContainer(pageAreaComposite);
		GridData pageContainerData = new GridData(GridData.FILL_BOTH);
		pageContainerData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		pageContainer.setLayoutData(pageContainerData);

		super.setPageContainer(pageContainer);
		// Build the separator line
		Label bottomSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		bottomSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		return composite;
	}

	@Override
	protected Control createTreeAreaContents(Composite parent) {
		// 创建左侧树
		Control result = super.createTreeAreaContents(parent);
		TreeViewer treeViewer = getTreeViewer();

		// 设置排序器
		IContributionService cs = (IContributionService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getService(IContributionService.class);
		treeViewer.setComparator(cs.getComparatorFor(IContributionService.TYPE_PREFERENCE));

		treeViewer.expandAll(); // 展开所有

		return result;
	}

	public void setErrorMessage(String newErrorMessage) {
	}

	public void setMessage(String newMessage, int newType) {
	}

	public void updateMessage() {
	}

	public void updateTitle() {
	}

}
