package net.heartsome.cat.ts.ui.plugin.dialog;

import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 帮助内容对话框中的查找文本对话框
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class HelpSearchDialog extends Dialog {

	boolean cancelled;
	boolean caseSensitive;
	String searchText;

	public HelpSearchDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.HelpSearchDialog.title"));
	}

	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		tparent.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tparent);

		Composite top = new Composite(tparent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		Label lblSearch = new Label(top, SWT.NONE);
		lblSearch.setText(Messages.getString("dialog.HelpSearchDialog.lblSearch"));

		final Text text = new Text(top, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		data.widthHint = 250;
		text.setLayoutData(data);
		searchText = "";
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				searchText = text.getText();
			}
		});

		final Button btnSensitive = new Button(tparent, SWT.CHECK);
		btnSensitive.setText(Messages.getString("dialog.HelpSearchDialog.btnSensitive"));
		btnSensitive.setSelection(true);
		caseSensitive = true;
		btnSensitive.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				caseSensitive = btnSensitive.getSelection();
			}
		});

		return tparent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setText(Messages.getString("dialog.HelpSearchDialog.ok"));
	}

	public String getText() {
		return searchText;
	}

	public boolean isSensitive() {
		return caseSensitive;
	}
}
