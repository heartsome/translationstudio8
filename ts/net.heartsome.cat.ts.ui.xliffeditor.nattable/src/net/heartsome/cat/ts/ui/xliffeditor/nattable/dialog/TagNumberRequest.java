package net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Rodolfo M. Raya copyright (c) 2002 - 2003 Heartsome Holdings Pte. Ltd. http://www.heartsome.net
 */

public class TagNumberRequest extends Dialog {

	Shell shell;
	Shell _parent;
	Text text;
	Button okButton;
	int maxValue;
	int value;
	Display display;

	/**
	 * Method numberRequest.
	 * @param display
	 */
	public TagNumberRequest(Shell parent, int maxTag) {

		super(parent, SWT.NONE);

		_parent = parent;
		maxValue = maxTag;
		value = 0;

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(Messages.getString("dialog.TagNumberRequest.title"));
		shell.setLayout(new GridLayout(1, false));
		display = shell.getDisplay();

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.getString("dialog.TagNumberRequest.lblNumber"));

		text = new Text(composite, SWT.BORDER);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		textData.widthHint = 100;
		text.setLayoutData(textData);

		Composite composite2 = new Composite(shell, SWT.BORDER);
		composite2.setLayout(new GridLayout(2, false));
		composite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		okButton = new Button(composite2, SWT.PUSH);
		okButton.setText(IDialogConstants.OK_LABEL);
		okButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		okButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				String number = text.getText();
				try {
					int i = Integer.parseInt(number);
					value = i;
					if (value < 1 || value > maxValue) {
						text.setText("");
						text.setFocus();
						return;
					}
				} catch (java.lang.NumberFormatException nfe) {
					value = 0;
					text.setText("");
					text.setFocus();
					return;
				}
				shell.close();
			}
		});

		Button cancelButton = new Button(composite2, SWT.PUSH | SWT.CANCEL);
		cancelButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		cancelButton.setText(IDialogConstants.CANCEL_LABEL);
		cancelButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				value = 0;
				shell.close();
			}
		});

		shell.pack();
		text.setFocus();
		shell.setDefaultButton(okButton);
	}

	/**
	 * Method show.
	 */
	public void show() {

		Rectangle bounds = _parent.getBounds();
		shell.setLocation(bounds.x + bounds.width / 3 - shell.getSize().x / 2, bounds.y + bounds.height / 3);

		shell.open();
		shell.forceActive();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Returns the number.
	 * @return String
	 */
	public int getNumber() {
		return value;
	}

}