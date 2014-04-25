package net.sourceforge.nattable.edit.gui;

import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.ActiveCellEditor;
import net.sourceforge.nattable.edit.EditTypeEnum;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MultiCellEditDialog extends Dialog {

	private static final String SET = "Set";
	private static final String INCREASE_BY = "Increase by";
	private static final String DECREASE_BY = "Decrease by";
	private static final String [] OPTIONS = {SET, INCREASE_BY, DECREASE_BY};

	private final ICellEditor cellEditor;
	private final Object originalCanonicalValue;
	private final Character initialEditValue;
	private final IDisplayConverter dataTypeConverter;
	private final IStyle cellStyle;
	private final IDataValidator dataValidator;
	private final boolean allowIncrementDecrement;

	private Combo updateCombo;
	private int lastSelectedIndex = 0;

	private Object editorValue;

	public MultiCellEditDialog(Shell parentShell,
			final ICellEditor cellEditor,
			final IDisplayConverter dataTypeConverter,
			final IStyle cellStyle,
			final IDataValidator dataValidator,
			final Object originalCanonicalValue,
			final Character initialEditValue,
			final boolean allowIncrementDecrement) {

		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL| SWT.DIALOG_TRIM);

		this.cellEditor = cellEditor;
		this.dataTypeConverter = dataTypeConverter;
		this.cellStyle = cellStyle;
		this.dataValidator = dataValidator;
		this.originalCanonicalValue = originalCanonicalValue;
		this.initialEditValue = initialEditValue;
		this.allowIncrementDecrement = allowIncrementDecrement;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Enter new value");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

		GridLayout panelLayout = new GridLayout(allowIncrementDecrement ? 2 : 1,false);
		panel.setLayout(panelLayout);

		if (allowIncrementDecrement) {
			createUpdateCombo(panel);
		}

		ActiveCellEditor.close();
		ActiveCellEditor.activate(cellEditor, panel, originalCanonicalValue, initialEditValue, dataTypeConverter, cellStyle, dataValidator, new MultiEditHandler(), 0, 0, 0, 0);
		Control editorControl = ActiveCellEditor.getControl();
		// propagate the ESC event from the editor to the dialog
		editorControl.addKeyListener(getEscKeyListener());

		final GridDataFactory layoutData = GridDataFactory.fillDefaults().grab(true, false).hint(100, 20);
		if (allowIncrementDecrement) {
			layoutData.indent(5, 0);
		}
		layoutData.applyTo(editorControl);

		return panel;
	}

	/**
	 * Create a listener for the ESC key. Cancel and dispose dialog.
	 */
	private KeyListener getEscKeyListener() {
		return new KeyListener() {

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					closeDialog();
				}
			}

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					closeDialog();
				}
			}

			private void closeDialog() {
				setReturnCode(SWT.CANCEL);
				close();
			}
		};
	}

	private void createUpdateCombo(Composite composite) {
		updateCombo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);

		for (String option : OPTIONS) {
			updateCombo.add(option);
		}

		updateCombo.select(0);

		updateCombo.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent arg0) {
				lastSelectedIndex = updateCombo.getSelectionIndex();
			}

		});

		GridDataFactory.swtDefaults().applyTo(updateCombo);
	}

	@Override
	protected void okPressed() {
		if (ActiveCellEditor.isValid()) {
			Object canonicalValue = ActiveCellEditor.getCanonicalValue();
			if (ActiveCellEditor.validateCanonicalValue()) {
				editorValue = canonicalValue;
				super.okPressed();
			}
		}
	}

	public EditTypeEnum getEditType() {
		if (allowIncrementDecrement && updateCombo != null) {

			int selectionIndex = updateCombo.isDisposed() ? lastSelectedIndex : updateCombo.getSelectionIndex();

			switch (selectionIndex) {
			case 0:
				return EditTypeEnum.SET;
			case 1:
				return EditTypeEnum.INCREASE;
			case 2:
				return EditTypeEnum.DECREASE;
			}
		}
		return EditTypeEnum.SET;
	}

	public Object getEditorValue() {
		return editorValue;
	}

	class MultiEditHandler implements ICellEditHandler {
		public boolean commit(MoveDirectionEnum direction, boolean closeAfterCommit) {
			if (direction == MoveDirectionEnum.NONE) {
				if (closeAfterCommit) {
					okPressed();
					return true;
				}
			}
			return false;
		}
	}

}
