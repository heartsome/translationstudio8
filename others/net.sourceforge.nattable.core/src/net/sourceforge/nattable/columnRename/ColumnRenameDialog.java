package net.sourceforge.nattable.columnRename;

import net.sourceforge.nattable.style.editor.AbstractStyleEditorDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ColumnRenameDialog extends AbstractStyleEditorDialog {
	private ColumnLabelPanel columnLabelPanel;
	private final String columnLabel;
	private String renamedColumnLabel;

	public ColumnRenameDialog(Shell parent, String columnLabel, String renamedColumnLabel) {
		super(parent);
		this.columnLabel = columnLabel;
		this.renamedColumnLabel = renamedColumnLabel;
	}

	@Override
	protected void initComponents(final Shell shell) {
		GridLayout shellLayout = new GridLayout();
		shell.setLayout(shellLayout);
		shell.setText("Rename column");

		// Closing the window is the same as canceling the form
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				doFormCancel(shell);
			}
		});

		// Tabs panel
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new GridLayout());

		GridData fillGridData = new GridData();
		fillGridData.grabExcessHorizontalSpace = true;
		fillGridData.horizontalAlignment = GridData.FILL;
		panel.setLayoutData(fillGridData);

		columnLabelPanel = new ColumnLabelPanel(panel, columnLabel, renamedColumnLabel);
		try {
			columnLabelPanel.edit(renamedColumnLabel);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	protected void doFormOK(Shell shell) {
		renamedColumnLabel = columnLabelPanel.getNewValue();
		shell.dispose();
	}

	@Override
	protected void doFormClear(Shell shell) {
		renamedColumnLabel = null;
		shell.dispose();
	}

	public String getNewColumnLabel() {
		return renamedColumnLabel;
	}
}
