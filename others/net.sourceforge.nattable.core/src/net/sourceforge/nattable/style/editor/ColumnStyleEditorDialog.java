package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ColumnStyleEditorDialog extends AbstractStyleEditorDialog {

	// Tabs in the dialog
	private CellStyleEditorPanel cellStyleEditorPanel;
	private BorderStyleEditorPanel borderStyleEditorPanel;

	// These are populated on OK button press
	protected Style newColumnCellStyle;
	protected BorderStyle newBorderStyle;

	private final Style columnStyle;

	public ColumnStyleEditorDialog(Shell parent, Style columnCellStyle) {
		super(parent);
		this.columnStyle = columnCellStyle;

		this.newColumnCellStyle = columnCellStyle;
		if (columnCellStyle != null) {
			this.newBorderStyle = columnStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE);
		}
	}

	@Override
	protected void initComponents(final Shell shell) {
		shell.setLayout(new GridLayout());
		shell.setText("Customize style");

		// Closing the window is the same as canceling the form
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				doFormCancel(shell);
			}

		});

		// Tabs panel
		Composite tabPanel = new Composite(shell, SWT.NONE);
		tabPanel.setLayout(new GridLayout());

		GridData fillGridData = new GridData();
		fillGridData.grabExcessHorizontalSpace = true;
		fillGridData.horizontalAlignment = GridData.FILL;
		tabPanel.setLayoutData(fillGridData);

		CTabFolder tabFolder = new CTabFolder(tabPanel, SWT.BORDER);
		tabFolder.setLayout(new GridLayout());
		tabFolder.setLayoutData(fillGridData);

		CTabItem columnTab = new CTabItem(tabFolder, SWT.NONE);
		columnTab.setText("Column");
		columnTab.setImage(GUIHelper.getImage("column"));
		columnTab.setControl(createColumnPanel(tabFolder));

		try {
			cellStyleEditorPanel.edit(columnStyle);
			borderStyleEditorPanel.edit(columnStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

/*	Grid level styling
 * private Composite createBlotterPanel(Composite parent) {
		Composite blotterPanel = new Composite(parent, SWT.NONE);
		GridLayout panelLayout = new GridLayout();
		blotterPanel.setLayout(panelLayout);

		GridData panelLayoutData = new GridData();
		panelLayoutData.grabExcessHorizontalSpace = true;
		panelLayoutData.grabExcessVerticalSpace = true;
		panelLayoutData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		panelLayoutData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		panelLayoutData.horizontalIndent = 20;
		blotterPanel.setLayoutData(panelLayoutData);

		new SeparatorPanel(blotterPanel, "Styling");
		gridColorsEditorPanel = new GridColorsEditorPanel(blotterPanel, gridStyle);

		return blotterPanel;
	}
*/
	private Composite createColumnPanel(Composite parent) {
		Composite columnPanel = new Composite(parent, SWT.NONE);
		columnPanel.setLayout(new GridLayout());

		new SeparatorPanel(columnPanel, "Styling");
		cellStyleEditorPanel = new CellStyleEditorPanel(columnPanel, SWT.NONE);

		new SeparatorPanel(columnPanel, "Border");
		borderStyleEditorPanel = new BorderStyleEditorPanel(columnPanel, SWT.NONE);
		return columnPanel;
	}

	@Override
	protected void doFormOK(Shell shell) {
		newColumnCellStyle = cellStyleEditorPanel.getNewValue();
		newBorderStyle = borderStyleEditorPanel.getNewValue();
		shell.dispose();
	}

	@Override
	protected void doFormClear(Shell shell) {
		this.newColumnCellStyle = null;
		shell.dispose();
	}

	// Getters for the modified style

	public Style getNewColumCellStyle() {
		return newColumnCellStyle;
	}

	public BorderStyle getNewColumnBorderStyle() {
		return newBorderStyle;
	}
}
