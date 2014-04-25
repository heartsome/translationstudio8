package net.heartsome.cat.ts.ui.handlers;

import java.text.MessageFormat;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.extensionpoint.AbstractNewProjectWizardPage;
import net.heartsome.cat.ts.ui.resource.Messages;
import net.heartsome.cat.ts.ui.wizards.NewProjectWizard;
import net.heartsome.cat.ts.ui.wizards.NewProjectWizardDialog;
import net.heartsome.cat.ts.ui.wizards.NewProjectWizardLanguagePage;
import net.heartsome.cat.ts.ui.wizards.NewProjectWizardProjInfoPage;
import net.heartsome.cat.ts.ui.wizards.NewProjectWizardSourceFilePage;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.slf4j.LoggerFactory;

/**
 * 新建项目的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NewProjectHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		NewProjectWizard wizard = new NewProjectWizard();
		TSWizardDialog dialog = new NewProjectWizardDialog(window.getShell(), wizard){
			
			@Override
			protected void createButtonsForButtonBar(Composite parent) {
				super.createButtonsForButtonBar(parent);
				getButton(IDialogConstants.FINISH_ID).setText(Messages.getString("handlers.NewProjectHandler.finishLbl"));
			}
			
			// robert help 2012-09-06
			@Override
			protected Control createHelpControl(Composite parent) {
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
		            	String language = CommonFunction.getSystemLanguage();
		        		String helpUrl = "";
		            	
		            	if (getCurrentPage() instanceof NewProjectWizardProjInfoPage) {
		            		// ROBERTHELP 新建项目－项目信息
		            		helpUrl = MessageFormat.format(
		        					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s02.html#create-project-wizard-project-info", language);
		            		PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
						}else if (getCurrentPage() instanceof NewProjectWizardLanguagePage) {
							// ROBERTHELP 新建项目－语言信息
							helpUrl = MessageFormat.format(
		        					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s02.html#create-project-wizard-languages", language);
							PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
						}else if (getCurrentPage() instanceof AbstractNewProjectWizardPage) {
							if ("TM".equals(((AbstractNewProjectWizardPage)getCurrentPage()).getPageType())) {
								// ROBERTHELP 新建项目－记忆库
								helpUrl = MessageFormat.format(
			        					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s02.html#create-project-wizard-tm", language);
								PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
							}else {
								// ROBERTHELP 新建项目－术语库
								helpUrl = MessageFormat.format(
			        					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s02.html#create-project-wizard-tb", language);
								PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
							}
						}else if (getCurrentPage() instanceof NewProjectWizardSourceFilePage) {
							// ROBERTHELP 新建项目－源文件
							helpUrl = MessageFormat.format(
		        					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s02.html#create-project-wizard-source-file", language);
							PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
						}
		            }
		        });
				return toolBar;
			}
		};
		dialog.setHelpAvailable(true);
		dialog.open();
		return null;
	}

}
