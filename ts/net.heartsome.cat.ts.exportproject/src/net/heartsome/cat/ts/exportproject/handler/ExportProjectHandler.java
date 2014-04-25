package net.heartsome.cat.ts.exportproject.handler;

import java.text.MessageFormat;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.exportproject.wizards.ExportProjectWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导出项目的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ExportProjectHandler extends AbstractHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(ExportProjectHandler.class);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ExportProjectWizard wizard = new ExportProjectWizard();
		TSWizardDialog dialog = new TSWizardDialog(window.getShell(), wizard) {
			/**
			 * 添加帮助按钮 robert 2012-09-06
			 */
			@Override
			protected Control createHelpControl(Composite parent) {
				// ROBERTHELP 导出项目
				String language = CommonFunction.getSystemLanguage();
				final String helpUrl = MessageFormat.format(
						"/net.heartsome.cat.ts.ui.help/html/{0}/ch07s02.html#export-project", language);
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
		};

		dialog.setHelpAvailable(true);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (null != root) {
			try {
				root.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				LOGGER.error("", e);
			}
		}

		dialog.open();
		return null;
	}

}
