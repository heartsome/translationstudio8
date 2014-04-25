package net.heartsome.license;

import net.heartsome.license.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LicenseAgreementDialog extends Dialog {

	private Button agreeBtn;

	public LicenseAgreementDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		
		Button nextBtn = getButton(IDialogConstants.OK_ID);
		nextBtn.setEnabled(false);
		nextBtn.setText(Messages.getString("license.LicenseAgreementDialog.nextBtn"));
		Button exitBtn = getButton(IDialogConstants.CANCEL_ID);
		exitBtn.setText(Messages.getString("license.LicenseAgreementDialog.exitBtn"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		layout.marginWidth = 10;
		tparent.setLayout(layout);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.heightHint = 380;
		tparent.setLayoutData(parentData);
		
		Label lbl = new Label(tparent, SWT.NONE);
		lbl.setText(Messages.getString("license.LicenseAgreementDialog.label"));
		lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Text text = new Text(tparent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);
		text.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		text.setText(Messages.getString("license.LicenseAgreementDialog.agreement"));
		GridData textData = new GridData(GridData.FILL_BOTH);
		text.setLayoutData(textData);
		
		agreeBtn = new Button(tparent, SWT.CHECK);
		agreeBtn.setText(Messages.getString("license.LicenseAgreementDialog.agreeBtn"));
		agreeBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		agreeBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getButton(IDialogConstants.OK_ID).setEnabled(agreeBtn.getSelection());
			}
			
		});
		
		return super.createDialogArea(parent);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("license.LicenseAgreementDialog.title"));
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(500, 420);
	}

	@Override
	protected void okPressed() {
		Point p = getShell().getLocation();
		super.okPressed();
		ActiveMethodDialog dialog = new ActiveMethodDialog(getShell(), p);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID) {
			System.exit(0);
		}
	}
}
