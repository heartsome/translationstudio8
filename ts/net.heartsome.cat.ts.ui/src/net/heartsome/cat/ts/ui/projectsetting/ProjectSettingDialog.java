/**
 * ProjectConfigSetingDialog.java
 *
 * Version information :
 *
 * Date:Nov 29, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.projectsetting;

import java.text.MessageFormat;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ProjectSettingDialog extends PreferenceDialog {
	private IPreferenceNode lastSuccessfulNode;
	private PreferenceManager preferenceManager;
	public ProjectSettingDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		this.preferenceManager = manager;
		setMinimumPageSize(600, 400);
		setHelpAvailable(true);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("projectsetting.ProjectSettingDialog.title"));
	}
	
	/**
	 * 添加帮助按钮
	 * robert	2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 项目设置
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s03.html#project-setting", language);
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
        ((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});		
		ToolItem helpItem = new ToolItem(toolBar, SWT.NONE);
		helpItem.setImage(helpImage);
		helpItem.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		helpItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
            }
        });
		return toolBar;
	}

	protected void addListeners(final TreeViewer viewer) {
		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			private void handleError() {
				try {
					// remove the listener temporarily so that the events caused
					// by the error handling dont further cause error handling
					// to occur.
					viewer.removePostSelectionChangedListener(this);
					// showPageFlippingAbortDialog();

					// select Current PageAgain;
					if (lastSuccessfulNode == null) {
						return;
					}
					getTreeViewer().setSelection(new StructuredSelection(lastSuccessfulNode));
					getCurrentPage().setVisible(true);

					// clear Selected Node
					setSelectedNodePreference(null);
				} finally {
					viewer.addPostSelectionChangedListener(this);
				}
			}

			public void selectionChanged(SelectionChangedEvent event) {
				final Object selection = getSingleSelection(event.getSelection());
				if (selection instanceof IPreferenceNode) {
					BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
						public void run() {
							if (!isCurrentPageValid()) {
								handleError();
							} else if (!showPage((IPreferenceNode) selection)) {
								// Page flipping wasn't successful
								handleError();
							} else {
								// Everything went well
								lastSuccessfulNode = (IPreferenceNode) selection;
							}
						}
					});
				}
			}
		});
		((Tree) viewer.getControl()).addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(final SelectionEvent event) {
				ISelection selection = viewer.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				IPreferenceNode singleSelection = getSingleSelection(selection);
				boolean expanded = viewer.getExpandedState(singleSelection);
				viewer.setExpandedState(singleSelection, !expanded);
			}
		});
		// Register help listener on the tree to use context sensitive help
		viewer.getControl().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent event) {
				if (getCurrentPage() == null) { // no current page? open dialog's help
					openDialogHelp();
					return;
				}
				// A) A typical path: the current page has registered its own help link
				// via WorkbenchHelpSystem#setHelp(). When just call it and let
				// it handle the help request.
				Control pageControl = getCurrentPage().getControl();
				if (pageControl != null && pageControl.isListening(SWT.Help)) {
					getCurrentPage().performHelp();
					return;
				}

				// B) Less typical path: no standard listener has been created for the page.
				// In this case we may or may not have an override of page's #performHelp().
				// 1) Try to get default help opened for the dialog;
				openDialogHelp();
				// 2) Next call currentPage's #performHelp(). If it was overridden, it might switch help
				// to something else.
				getCurrentPage().performHelp();
			}

			private void openDialogHelp() {
				if (getPageContainer() == null)
					return;
				for (Control currentControl = getPageContainer(); currentControl != null; currentControl = currentControl
						.getParent()) {
					if (currentControl.isListening(SWT.Help)) {
						currentControl.notifyListeners(SWT.Help, new Event());
						break;
					}
				}
			}
		});
	}

	/**
	 * Selects the saved item in the tree of preference pages. If it cannot do this it saves the first one.
	 */
	protected void selectSavedItem() {
		IPreferenceNode node = findNodeMatching(getSelectedNodePreference());
		if (node == null) {
			IPreferenceNode[] nodes = preferenceManager.getRootSubNodes();
			ViewerComparator comparator = getTreeViewer().getComparator();
			if (comparator != null)	{
				comparator.sort(null, nodes);
			}			
			for (int i = 0; i < nodes.length; i++) {
				IPreferenceNode selectedNode = nodes[i];
				if (selectedNode != null) {
					node = selectedNode;
					break;
				}
			}
		}
		if (node != null) {
			getTreeViewer().setSelection(new StructuredSelection(node), true);
			// Keep focus in tree. See bugs 2692, 2621, and 6775.
			getTreeViewer().getControl().setFocus();
			boolean expanded = getTreeViewer().getExpandedState(node);
			getTreeViewer().setExpandedState(node, !expanded);
		}
	}

	public void update() {
		super.update();
	}
}
