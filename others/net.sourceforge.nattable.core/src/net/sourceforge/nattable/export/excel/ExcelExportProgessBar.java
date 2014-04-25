package net.sourceforge.nattable.export.excel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ExcelExportProgessBar {

	private Shell shell;
	private Shell childShell;
	private ProgressBar progressBar;

	public ExcelExportProgessBar(Shell shell) {
		this.shell = shell;
	}

	public void open(int minValue, int maxValue) {
		childShell = new Shell(shell.getDisplay(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		childShell.setText("Exporting to Excel.. please wait");

		progressBar = new ProgressBar(childShell, SWT.SMOOTH);
		progressBar.setMinimum(minValue);
		progressBar.setMaximum(maxValue);
		progressBar.setBounds(0, 0, 400, 25);
		progressBar.setFocus();

		childShell.pack();
		childShell.open();
	}

	public void dispose() {
		progressBar.dispose();
		childShell.dispose();
	}

	public void setSelection(int value) {
		progressBar.setSelection(value);
	}
}
