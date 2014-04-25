package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractStyleEditorDialog extends Dialog {

	private boolean cancelPressed = false;

	public AbstractStyleEditorDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	/**
	 * Create all widgets to be displayed in the editor
	 */
	protected abstract void initComponents(Shell shell);

	/**
	 * Initialize and display the SWT shell. This is a blocking call.
	 */
	public void open() {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setImage(GUIHelper.getImage("preferences"));
		shell.setText(getText());

		initComponents(shell);
		createButtons(shell);

		shell.pack();
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}


	/**
	 * Create OK, Reset and Cancel buttons
	 */
	protected void createButtons(final Shell shell) {
		Composite buttonPanel = new Composite(shell, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginLeft = 65;
		buttonPanel.setLayout(gridLayout);

		GridData gridLayoutData = new GridData();
		gridLayoutData.horizontalAlignment = GridData.FILL_HORIZONTAL;
		buttonPanel.setLayoutData(gridLayoutData);

		Button okButton = new Button(buttonPanel, SWT.PUSH);
		okButton.setText("OK");
		okButton.setLayoutData(new GridData(70, 25));
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFormOK(shell);
			}
		});

		Button clearButton = new Button(buttonPanel, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.setToolTipText("Reset to original settings");
		clearButton.setLayoutData(new GridData(80, 25));
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFormClear(shell);
			}
		});

		Button cancelButton = new Button(buttonPanel, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(new GridData(80, 25));
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFormCancel(shell);
			}
		});

		shell.setDefaultButton(okButton);
	}

	/**
	 * Respond to the OK button press. Read new state from the form.
	 */
	protected abstract void doFormOK(Shell shell);

	protected void doFormCancel(Shell shell) {
		cancelPressed = true;
		shell.dispose();
	}

	protected void doFormClear(Shell shell) {
		shell.dispose();
	}

	public boolean isCancelPressed(){
		return cancelPressed;
	}
}
