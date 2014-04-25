package net.heartsome.cat.database.ui.tm.wizard;

import java.text.MessageFormat;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.database.ui.tm.preference.TMDatabasePage;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizard;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

public class UpdateTMWizardDialog extends TSWizardDialog {

	public UpdateTMWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		setHelpAvailable(true);
	}
	
	/**
	 * 添加帮助按钮
	 * robert	2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 更新记忆库
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch06.html#update-tm", language);
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
	};
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, -1, Messages.getString("dialog.UpdateTMDialog.btnSetting"), true);
		super.createButtonsForButtonBar(parent);
		getButton(-1).addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), TMDatabasePage.ID);
			}
		});
	}

}
