package net.heartsome.cat.convert.ui.dialog;

import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.converter.Converter;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 转换文件配置对话框
 * @author cheney
 * @since JDK1.6
 */
public class ConfigConversionDialog extends TitleAreaDialog {

	private ConverterViewModel converterViewModel;
	private ComboViewer supportList;

	private Button okButton;

	/**
	 * 构造函数
	 * @param parentShell
	 * @param converterViewModel
	 */
	public ConfigConversionDialog(Shell parentShell, ConverterViewModel converterViewModel) {
		super(parentShell);
		this.converterViewModel = converterViewModel;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);

		Composite contents = new Composite(parentComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		contents.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		contents.setLayoutData(gridData);

		Composite converterComposite = new Composite(contents, SWT.NONE);
		converterComposite.setLayout(new GridLayout(2, false));

		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		converterComposite.setLayoutData(gridData);

		String direction = converterViewModel.getDirection();
		if (direction.equals(Converter.DIRECTION_POSITIVE)) {
			supportList = createConvertControl(Messages.getString("dialog.ConfigConversionDialog.direction1"), converterComposite);
		} else {
			supportList = createConvertControl(Messages.getString("dialog.ConfigConversionDialog.direction2"), converterComposite);
		}
		supportList.getCombo().setFocus();

		bindValue();

		Dialog.applyDialogFont(parentComposite);

		Point defaultMargins = LayoutConstants.getMargins();
		GridLayoutFactory.fillDefaults().numColumns(2).margins(defaultMargins.x, defaultMargins.y).generateLayout(
				contents);

		return contents;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * 创建文件类型列表
	 * @param title
	 * @param composite
	 * @return ;
	 */
	private ComboViewer createConvertControl(String title, Composite composite) {
		Label positiveConvertLabel = new Label(composite, SWT.NONE);
		GridData positiveConvertLabelData = new GridData();
		positiveConvertLabelData.horizontalSpan = 2;
		positiveConvertLabelData.horizontalAlignment = SWT.CENTER;
		positiveConvertLabelData.grabExcessHorizontalSpace = true;
		positiveConvertLabel.setLayoutData(positiveConvertLabelData);
		positiveConvertLabel.setText(title);

		Label suportFormat = new Label(composite, SWT.NONE);
		suportFormat.setText(Messages.getString("dialog.ConfigConversionDialog.suportFormat"));

		ComboViewer supportList = new ComboViewer(composite, SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		supportList.getCombo().setLayoutData(gridData);
		supportList.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		});

		return supportList;
	}

	/**
	 * 对 UI 和 View Model 进行绑定 ;
	 */
	private void bindValue() {
		DataBindingContext dbc = new DataBindingContext();
		ConverterUtil.bindValue(dbc, supportList, converterViewModel);
	}

}
