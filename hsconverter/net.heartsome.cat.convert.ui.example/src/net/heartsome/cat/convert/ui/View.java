package net.heartsome.cat.convert.ui;

import java.util.Map;

import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.converter.Converter;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class View extends ViewPart {
	public static final String ID = "net.heartsome.cat.convert.ui.view";

	private ConverterViewModel positiveConverterViewModel;
	private ConverterViewModel reverseConverterViewModel;
	private ComboViewer positiveSupportList;

	private ComboViewer reverseSupportList;

	public void createPartControl(Composite parent) {
		positiveConverterViewModel = new ConverterViewModel(Activator
				.getContext(), Converter.DIRECTION_POSITIVE);
		reverseConverterViewModel = new ConverterViewModel(Activator
				.getContext(), Converter.DIRECTION_REVERSE);

		GridLayout layout = new GridLayout(2, true);
		parent.setLayout(layout);

		Composite left = new Composite(parent, SWT.NONE);
		left.setLayout(new GridLayout(2, false));

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		left.setLayoutData(gridData);

		positiveSupportList = createConvertControl(
				"Convert from Suport format to Xliff", left, true);

		Composite right = new Composite(parent, SWT.NONE);
		right.setLayout(new GridLayout(2, false));

		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		right.setLayoutData(gridData);

		reverseSupportList = createConvertControl(
				"Convert from Xliff to Support format", right, false);

		bindValue();
	}

	private ComboViewer createConvertControl(String title, Composite composite,
			boolean isPositive) {
		Label positiveConvertLabel = new Label(composite, SWT.NONE);
		GridData positiveConvertLabelData = new GridData();
		positiveConvertLabelData.horizontalSpan = 2;
		positiveConvertLabelData.horizontalAlignment = SWT.CENTER;
		positiveConvertLabelData.grabExcessHorizontalSpace = true;
		positiveConvertLabel.setLayoutData(positiveConvertLabelData);
		positiveConvertLabel.setText(title);

		Label suportFormat = new Label(composite, SWT.NONE);
		suportFormat.setText("Suport Format");

		ComboViewer supportList = new ComboViewer(composite, SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		supportList.getCombo().setLayoutData(gridData);

		final Button button = new Button(composite, SWT.BORDER);
		button.setText("Convert");
		GridData buttonData = new GridData();
		buttonData.horizontalAlignment = SWT.FILL;
		button.setLayoutData(buttonData);

		if (isPositive) {
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String type = positiveConverterViewModel.getSelectedType();
					if (type != null && !type.equals("")) {
						Map<String, String> result = positiveConverterViewModel
								.convert(null);
						if (result != null) {
							MessageDialog.openInformation(button.getShell(),
									"Convert", "used '" + result.get("name")
											+ "' to convert.");
						} else {
							MessageDialog
									.openWarning(button.getShell(), "Warning",
											"Can't find selected Converter.");
						}
					}
				}
			});
		} else {
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String type = reverseConverterViewModel.getSelectedType();
					if (type != null && !type.equals("")) {
						Map<String, String> result = reverseConverterViewModel
								.convert(null);
						if (result != null) {
							MessageDialog.openInformation(button.getShell(),
									"Convert", "used '" + result.get("name")
											+ "' to convert.");
						} else {
							MessageDialog
									.openWarning(button.getShell(), "Warning",
											"Can't find selected Converter.");
						}
					}
				}
			});
		}

		return supportList;
	}

	private void bindValue() {
		DataBindingContext dbc = new DataBindingContext();
		ConverterUtil.bindValue(dbc, positiveSupportList,
				positiveConverterViewModel);
		ConverterUtil.bindValue(dbc, reverseSupportList,
				reverseConverterViewModel);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {

	}

	@Override
	public void dispose() {
		super.dispose();
		positiveConverterViewModel.close();
		reverseConverterViewModel.close();
	}
}