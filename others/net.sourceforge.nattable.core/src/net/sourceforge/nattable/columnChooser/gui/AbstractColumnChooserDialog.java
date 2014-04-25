package net.sourceforge.nattable.columnChooser.gui;

import net.sourceforge.nattable.util.GUIHelper;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractColumnChooserDialog extends Dialog {

	protected ListenerList listeners = new ListenerList();;

	public AbstractColumnChooserDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Done", true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		composite.setLayout(new GridLayout(1, true));

		composite.getShell().setText("Column Chooser");
		composite.getShell().setImage(GUIHelper.getImage("preferences"));

		populateDialogArea(composite);

		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);

		return composite;
	}

	protected abstract void populateDialogArea(Composite composite);

	protected void createLabels(Composite parent, String availableStr, String selectedStr) {
		boolean availableSet = StringUtils.isNotEmpty(availableStr);
		boolean selectedSet = StringUtils.isNotEmpty(selectedStr);

		if (availableSet && selectedSet) {
			if (availableSet) {
				Label availableLabel = new Label(parent, SWT.NONE);
				availableLabel.setText(availableStr);
				GridDataFactory.swtDefaults().applyTo(availableLabel);
			}

			Label filler = new Label(parent, SWT.NONE);
			GridDataFactory.swtDefaults().span(availableSet ? 1 : 2, 1).applyTo(filler);

			if (selectedSet) {
				Label selectedLabel = new Label(parent, SWT.NONE);
				selectedLabel.setText(selectedStr);
				GridDataFactory.swtDefaults().span(2, 1).applyTo(selectedLabel);
			}
		}
	}

	public void addListener(Object listener) {
		listeners.add(listener);
	}

	public void removeListener(Object listener) {
		listeners.remove(listener);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 350);
	}

}
