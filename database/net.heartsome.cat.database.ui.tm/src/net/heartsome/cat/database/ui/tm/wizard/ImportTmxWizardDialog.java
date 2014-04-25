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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

/**
 * 导入 TMX 向导框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ImportTmxWizardDialog extends TSWizardDialog {

	private Button btnSetting;

	public ImportTmxWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSetting = createButton(parent, -1, Messages.getString("wizard.ImportTmxPage.settingBtn"), true);
		super.createButtonsForButtonBar(parent);
		btnSetting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
						TMDatabasePage.ID);
			}
		});
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createHelpControl(org.eclipse.swt.widgets.Composite)
	 */
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
				String helpUrl= MessageFormat.format(
    					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s03.html#create-tm-wizard-import-tmx", language);
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
			}
		});
		return toolBar;
	}

	@Override
	public void updateButtons() {
		super.updateButtons();
		btnSetting.setVisible(getCurrentPage() instanceof ImportTmxPage
				|| getCurrentPage() instanceof NewTmDbImportPage
				|| getCurrentPage() instanceof TmDbManagerImportWizardTmxPage);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#isHelpAvailable()
	 */
	@Override
	public boolean isHelpAvailable() {
		// TODO Auto-generated method stub
		return true;
	}
}
