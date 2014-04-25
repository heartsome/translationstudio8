package net.sourceforge.nattable.group.gui;

import net.sourceforge.nattable.group.command.CreateColumnGroupCommand;
import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CreateColumnGroupDialog extends Dialog {

	private Button createButton;
	private Text groupNameText;
	private ILayer contextLayer;

	private CreateColumnGroupDialog(Shell parentShell) {
		super(parentShell);		
		setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
	}
	
	public static CreateColumnGroupDialog createColumnGroupDialog(Shell shell) {
		return new CreateColumnGroupDialog(shell);
	}

	public void setContextLayer(ILayer layer) {
		this.contextLayer = layer;
	}
	
	@Override
	public void create() {
		super.create();
		getShell().setText("Create Column Group");		
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		GridDataFactory.fillDefaults().minSize(200, 100).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(createInputPanel(composite));

		Composite buttonPanel = createButtonSection(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, true).applyTo(buttonPanel);
		
		return composite;
	}

	private Composite createButtonSection(Composite composite) {

		Composite panel = new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout(1,false);
		panel.setLayout(layout);
		
		Label spacer = new Label(panel, SWT.LEFT);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(spacer);
		
		createButton = createButton(panel, IDialogConstants.CLIENT_ID, "&Group", false);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(52, SWT.DEFAULT).applyTo(createButton);
		
		createButton.setEnabled(false);
		getShell().setDefaultButton(createButton);
		
		createButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doColumnGrouping();
			}
		});
 		
		Button closeButton = createButton(panel, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(52, SWT.DEFAULT).applyTo(closeButton);
		
		return panel;
	}

	private Composite createInputPanel(final Composite composite) {
		final Composite row = new Composite(composite, SWT.NONE);
		row.setLayout(new GridLayout(2,false));
		
		final Label createLabel = new Label(row, SWT.NONE);
		createLabel.setText("G&roup Name:");
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(createLabel);
		
		groupNameText = new Text(row, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupNameText);
		groupNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				createButton.setEnabled(groupNameText.getText().length() > 0);
			}
		});
		groupNameText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (createButton.isEnabled()) {
					doColumnGrouping();
				}
			}
		});
		
		return row;
	}
	
	public void terminateDialog() {		
		close();
	}
	
	@Override
	public boolean close() {
		return super.close();
	}
	
	private void doColumnGrouping() {		
		BusyIndicator.showWhile(super.getShell().getDisplay(), new Runnable() {
			public void run() {
				final CreateColumnGroupCommand command = new CreateColumnGroupCommand(groupNameText.getText());
				try {
					contextLayer.doCommand(command);
				} finally {
					terminateDialog();
				}
			}			
		});
	}
}