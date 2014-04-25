package net.heartsome.cat.ts.ui.preferencepage.languagecode;

import java.io.IOException;

import net.heartsome.cat.common.core.CoreActivator;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.resource.ImageConstant;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 添加/编辑语言代码对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class AddOrUpdateLanguageDialog extends Dialog {

	public static final int DIALOG_ADD = 0;

	public static final int DIALOG_EDIT = 1;

	/** 语言代码文本框 */
	public Text txtCode;

	public Label imageLabel;

	/** 语言名称文本框 */
	public Text txtName;

	/** 是否双向按钮 > 是 */
	public Button btnIsBidi;

	/** 是否双向按钮 > 否 */
	public Button btnIsNotBidi;

	/** 对话框类型 */
	private int intType;

	/** 语言代码 */
	private String strCode;

	// 图标资源
	private String imagePath = "";

	/** 语言名称 */
	private String strName;

	/** 是否双向 */
	private boolean blnIsBidi = false;

	private LanguageModel languageModel;

	private Image image;

	protected AddOrUpdateLanguageDialog(Shell parentShell, int intType) {
		super(parentShell);
		this.intType = intType;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (intType == DIALOG_ADD) {
			newShell.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.addTitle"));
		} else if (intType == DIALOG_EDIT) {
			newShell.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.editTitle"));
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		tparent.setLayoutData(data);

		GridLayout layout = new GridLayout(3, false);
		tparent.setLayout(layout);

		GridData txtData = new GridData(GridData.FILL_HORIZONTAL);
		txtData.horizontalSpan = 2;

		Label lbl = new Label(tparent, SWT.NONE);
		lbl.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.lblLangImage"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
		imageLabel = new Label(tparent, SWT.NONE);
		GridData imGd = new GridData();
		imGd.widthHint = 16;
		imGd.heightHint = 12;
		imageLabel.setLayoutData(imGd);
		imageLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		try {
			String bundlePath = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("")).getPath();
			if (this.imagePath != null && !this.imagePath.equals("")) {
				String imagePath = bundlePath + this.imagePath;
				if (image != null && !image.isDisposed()) {
					image.dispose();
				}
				image = new Image(getShell().getDisplay(), imagePath);
			}

			if (image != null) {
				ImageData imgData = image.getImageData().scaledTo(16, 12);
				if (image != null && !image.isDisposed()) {
					image.dispose();
				}
				image = new Image(getShell().getDisplay(), imgData);
				imageLabel.setData(this.imagePath);
			} else {
				if (image != null && !image.isDisposed()) {
					image.dispose();
				}
				image = new Image(getShell().getDisplay(), bundlePath + ImageConstant.LANG_EMPTYPIC);
			}
			imageLabel.setImage(image);
		} catch (IOException e) {
			e.printStackTrace();
		}

		imageLabel.setToolTipText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.imageLabel"));
		imageLabel.addListener(SWT.MouseUp, new Listener() {

			public void handleEvent(Event event) {
				FileDialog dlg = new FileDialog(getShell());
				dlg.setFilterExtensions(new String[] { "*.png" });
				String path = dlg.open();
				if (path != null) {
					ImageData data = new ImageData(path).scaledTo(16, 12);
					if (image != null && !image.isDisposed()) {
						image.dispose();
					}
					image = new Image(getShell().getDisplay(), data);
					imageLabel.setImage(image);
					imageLabel.setData(path);
				}
			}
		});
		new Label(tparent, SWT.NONE).setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.lblImage"));

		lbl = new Label(tparent, SWT.NONE);
		lbl.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.txtCode"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
		txtCode = new Text(tparent, SWT.BORDER);
		txtCode.setLayoutData(txtData);
		txtCode.setText(strCode == null ? "" : strCode);

		lbl = new Label(tparent, SWT.NONE);
		lbl.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.txtName"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
		txtName = new Text(tparent, SWT.BORDER);
		txtName.setLayoutData(txtData);
		txtName.setText(strName == null ? "" : strName);

		lbl = new Label(tparent, SWT.NONE);
		lbl.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.isBidi"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
		btnIsBidi = new Button(tparent, SWT.RADIO);
		btnIsBidi.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.btnIsBidi"));

		btnIsNotBidi = new Button(tparent, SWT.RADIO);
		btnIsNotBidi.setText(Messages.getString("languagecode.AddOrUpdateLanguageDialog.btnIsNotBidi"));

		if (blnIsBidi) {
			btnIsBidi.setSelection(true);
		} else {
			btnIsNotBidi.setSelection(true);
		}
		tparent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		txtCode.forceFocus();
		txtCode.selectAll();
		return tparent;
	}

	@Override
	public void okPressed() {
		if (txtCode.getText() == null || "".equals(txtCode.getText().trim())) { //$NON-NLS-1$
			MessageDialog.openInformation(getShell(),
					Messages.getString("languagecode.AddOrUpdateLanguageDialog.msgTitle"),
					Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg1"));
			txtCode.forceFocus();
			return;
		}

		if (intType == DIALOG_ADD) {
			if (languageModel.getLanguagesMap().containsKey(txtCode.getText())) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("languagecode.AddOrUpdateLanguageDialog.msgTitle"),
						Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg2"));
				return;
			}
		} else if (intType == DIALOG_EDIT) {
			if (!txtCode.getText().equals(strCode) && languageModel.getLanguagesMap().containsKey(txtCode.getText())) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("languagecode.AddOrUpdateLanguageDialog.msgTitle"),
						Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg2"));
				return;
			}
		}

		if (txtName.getText() == null || "".equals(txtName.getText().trim())) { //$NON-NLS-1$
			MessageDialog.openInformation(getShell(),
					Messages.getString("languagecode.AddOrUpdateLanguageDialog.msgTitle"),
					Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg3"));
			txtName.forceFocus();
			return;
		}

		String status = parse(txtCode.getText());
		if (!status.equals("")) { //$NON-NLS-1$
			MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			box.setMessage(status + Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg4")); //$NON-NLS-1$
			if (box.open() == SWT.NO) {
				return;
			}
		}

		imagePath = (String) imageLabel.getData();
		if (imagePath == null) {
			imagePath = "";
		}
		strCode = txtCode.getText();
		strName = txtName.getText();
		blnIsBidi = btnIsBidi.getSelection();
		close();
	}

	public void setStrCode(String strCode) {
		this.strCode = strCode;
	}

	public void setStrName(String strName) {
		this.strName = strName;
	}

	public void setBlnIsBidi(boolean blnIsBidi) {
		this.blnIsBidi = blnIsBidi;
	}

	public void setLanguageModel(LanguageModel languageModel) {
		this.languageModel = languageModel;
	}

	public String getStrCode() {
		return strCode;
	}

	public String getStrName() {
		return strName;
	}

	public boolean isBlnIsBidi() {
		return blnIsBidi;
	}

	/** @return the imagePath */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * @param imagePath
	 *            the imagePath to set
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	private String parse(String string) {
		if (string.length() == 2) {
			if (TextUtil.getISO639(string, CoreActivator.ISO639_1_PAHT).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				return Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg5"); //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}
		if (string.length() == 3) {
			if (TextUtil.getISO639(string, CoreActivator.ISO639_2_PAHT).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				return Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg6"); //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}
		if (string.length() == 5) {
			if (string.charAt(2) != '-') {
				return Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg7"); //$NON-NLS-1$
			}
			String lang = string.substring(0, 2);
			if (TextUtil.getISO639(lang, CoreActivator.ISO639_1_PAHT).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				return Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg5"); //$NON-NLS-1$
			}
			String country = string.substring(3);
			if (TextUtil.getCountryName(country).equals("")) { //$NON-NLS-1$
				return Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg8"); //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}
		return Messages.getString("languagecode.AddOrUpdateLanguageDialog.msg9"); //$NON-NLS-1$
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		if (image != null && !image.isDisposed()) {
			image.dispose();
		}
		return super.close();
	}

}
