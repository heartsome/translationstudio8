/**
 * AddSearchEntryDialog.java
 *
 * Version information :
 *
 * Date:2013-9-24
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.websearch.ui.dialog;

import net.heartsome.cat.ts.websearch.bean.SearchEntry;
import net.heartsome.cat.ts.websearch.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class AddSearchEntryDialog extends Dialog {
	public static final int ADD = 0;

	public static final int EDIT = 1;

	private Text nameText;

	private Text urlText;

	private Button btnYesRadioButton;

	private Button btnNoRadioButton;

	private SearchEntry searEntry;

	private int style = -1;

	private OKHandler handler;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AddSearchEntryDialog(Shell parentShell, SearchEntry searEntry, int style) {
		super(parentShell);
		this.searEntry = searEntry;
		this.style = style;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("Websearch.AddSearchEntryDialog.title"));
		setShellStyle(getShellStyle() & ~SWT.APPLICATION_MODAL);

	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = SWT.DEFAULT;
		gridData.widthHint = 450;
		container.setLayoutData(gridData);

		Composite nameSetArea = new Composite(container, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		nameSetArea.setLayout(gridLayout);
		nameSetArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLable = new Label(nameSetArea, SWT.NONE);
		GridData gd_nameLable = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_nameLable.widthHint = 38;
		nameLable.setLayoutData(gd_nameLable);
		nameLable.setText(Messages.getString("Websearch.AddSearchEntryDialog.NameLable"));
		nameText = new Text(nameSetArea, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (style == EDIT) {
			nameText.setText(searEntry.getSearchName());
		}

		nameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				setOkState();
			}
		});
		Composite urlSetArea = new Composite(container, SWT.NONE);
		GridLayout gridLayout2 = new GridLayout(2, false);
		urlSetArea.setLayout(gridLayout2);
		urlSetArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label urlLable = new Label(urlSetArea, SWT.NONE);
		GridData gd_urlLable = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_urlLable.widthHint = 38;
		urlLable.setLayoutData(gd_urlLable);
		urlLable.setText("URL");

		urlText = new Text(urlSetArea, SWT.BORDER);
		if (style == EDIT) {
			urlText.setText(searEntry.getSearchUrl());
		}
		urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		urlText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				setOkState();
			}
		});
		Composite setArea = new Composite(container, SWT.NONE);
		setArea.setLayout(new GridLayout(1, true));
		setArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Group group = new Group(setArea, SWT.NONE);
		group.setLayout(new GridLayout(2, true));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(Messages.getString("Websearch.AddSearchEntryDialog.GroupTitle"));
		btnYesRadioButton = new Button(group, SWT.RADIO);
		btnYesRadioButton.setText(Messages.getString("Websearch.AddSearchEntryDialog.GroupYes"));
		btnYesRadioButton.setSelection(true);
		btnYesRadioButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnNoRadioButton = new Button(group, SWT.RADIO);
		btnNoRadioButton.setText(Messages.getString("Websearch.AddSearchEntryDialog.GroupNo"));
		btnNoRadioButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (style == EDIT) {
			btnYesRadioButton.setSelection(searEntry.isChecked());
			btnNoRadioButton.setSelection(!searEntry.isChecked());

		}
		return container;
	}

	private Button okBtn;

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okBtn = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okBtn.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		setOkState();
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void okPressed() {
		if (validateValue()) {
			this.searEntry.setSearchName(nameText.getText());
			if (style == ADD) {
				this.searEntry.setDefault(false);
			}
			this.searEntry.setSearchUrl(urlText.getText());
			this.searEntry.setChecked(btnYesRadioButton.getSelection());
			Assert.isNotNull(handler, "OKhandler can not be null");
			if (!handler.doOk()) {
				return;
			}
		}
		super.okPressed();
	}

	public boolean validateValue() {
		String nameValue = nameText.getText();
		String urlValue = urlText.getText();
		if (nameValue.trim().isEmpty() || urlValue.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	private void setOkState() {
		if (validateValue()) {
			okBtn.setEnabled(true);
		} else {
			okBtn.setEnabled(false);
		}
	}

	

	public void setHandler(OKHandler handler) {
		this.handler = handler;
	}
	
	public  interface OKHandler {
		boolean doOk();
	}
}
