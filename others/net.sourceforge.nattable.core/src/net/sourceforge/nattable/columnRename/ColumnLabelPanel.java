package net.sourceforge.nattable.columnRename;

import net.sourceforge.nattable.style.editor.AbstractEditorPanel;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ColumnLabelPanel extends AbstractEditorPanel<String> {
	private Text textField;

	private final String columnLabel;
	private final String newColumnLabel;

	public ColumnLabelPanel(Composite parent, String columnLabel, String newColumnLabel) {
		super(parent, SWT.NONE);
		this.columnLabel = columnLabel;
		this.newColumnLabel = newColumnLabel;
		init();
	}

	private void init() {
		GridLayout gridLayout = new GridLayout(2, false);
		setLayout(gridLayout);

		// Original label
		Label label = new Label(this, SWT.NONE);
		label.setText("Original");

		Label originalLabel = new Label(this, SWT.NONE);
		originalLabel.setText(columnLabel);

		// Text field for new label
		Label renameLabel = new Label(this, SWT.NONE);
		renameLabel.setText("Rename");

		textField = new Text(this, SWT.BORDER);
		GridData gridData = new GridData(200, 15);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		textField.setLayoutData(gridData);

		if (StringUtils.isNotEmpty(newColumnLabel)) {
			textField.setText(newColumnLabel);
		}
	}

	@Override
	public void edit(String newColumnHeaderLabel) throws Exception {
		if (StringUtils.isNotEmpty(newColumnHeaderLabel)) {
			textField.setText(newColumnHeaderLabel);
		}
	}

	@Override
	public String getEditorName() {
		return "Column label";
	}

	@Override
	public String getNewValue() {
		if (textField.isEnabled() && StringUtils.isNotEmpty(textField.getText())) {
			return textField.getText();
		}
		return null;
	}
}
