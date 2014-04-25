package net.heartsome.license;

import java.io.File;
import java.text.MessageFormat;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.resource.Messages;
import net.heartsome.license.utils.StringUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 选择授权文件的对话框
 * @author karl
 * @version
 * @since JDK1.6
 */
public class SelectGrantFileDialog extends Dialog {

	private Text textPath;
	private Button btnScan;
	private static final String[] FILE_IMPORT_MASK = { "*.lic" };
	private Point p;

	protected SelectGrantFileDialog(Shell parentShell) {
		super(parentShell);
	}
	
	protected SelectGrantFileDialog(Shell parentShell, Point p) {
		super(parentShell);
		this.p = p;
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		if (p == null) {
			return super.getInitialLocation(initialSize);
		} else {
			return p;
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("license.LicenseManageDialog.activeBtn"), true);

		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("license.LicenseManageDialog.exitBtn"),
				false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginTop = 10;
		tparent.setLayout(layout);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(tparent);

		Composite compNav = new Composite(tparent, SWT.NONE);
		GridLayout navLayout = new GridLayout();
		compNav.setLayout(navLayout);

		createNavigation(compNav);

		Group groupFile = new Group(tparent, SWT.NONE);
		groupFile.setText(Messages.getString("license.SelectGrantFileDialog.grantfile"));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groupFile);
		GridLayout layoutGroup = new GridLayout(3, false);
		layoutGroup.marginWidth = 5;
		layoutGroup.marginHeight = 100;
		groupFile.setLayout(layoutGroup);

		Label label = new Label(groupFile, SWT.NONE);
		label.setText(Messages.getString("license.SelectGrantFileDialog.grantfile1"));

		textPath = new Text(groupFile, SWT.BORDER);
		textPath.setEditable(false);

		GridData pathData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		pathData.widthHint = new PixelConverter(textPath).convertWidthInCharsToPixels(25);
		textPath.setLayoutData(pathData);
		// browse button
		btnScan = new Button(groupFile, SWT.PUSH);
		btnScan.setText(Messages.getString("license.SelectGrantFileDialog.scan"));
		setButtonLayoutData(btnScan);

		btnScan.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse .swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(textPath.getShell(), SWT.SHEET);
				dialog.setFilterExtensions(FILE_IMPORT_MASK);
				dialog.setText(Messages.getString("license.SelectGrantFileDialog.selectgrantfile"));

				String fileName = textPath.getText().trim();

				if (fileName.length() != 0) {
					File path = new File(fileName).getParentFile();
					if (path != null && path.exists()) {
						dialog.setFilterPath(path.toString());
					}
				}

				String path = dialog.open();
				if (path != null) {
					textPath.setText(path);
				}
			}

		});

		return tparent;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(520, 470);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("license.LicenseManageDialog.title"));
	}

	private void createNavigation(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.operatenavigation"));
		
		RowLayout layout = new RowLayout();
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(layout);
		
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.inputlicenseid"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.getactivekey"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.getgrantfile"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.activefinish"));
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
	}

	
	
	@Override
	protected void okPressed() {
		String filepath = textPath.getText();
		if (filepath == null || "".equals(filepath)) {
			MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
					Messages.getString("license.SelectGrantFileDialog.selectfile"));
		} else {
			OffLineActiveService v = new OffLineActiveService();
			int result = v.activeByGrantFile(filepath);
			if (result == Constants.STATE_VALID) {
				super.okPressed();
				MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
						Messages.getString("license.LicenseManageDialog.activeSuccess"));
			} else if (result == Constants.STATE_INVALID) {
				MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
						Messages.getString("license.SelectGrantFileDialog.invalidfile"));
			} else if (result == Constants.EXCEPTION_INT2) {
				MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
						Messages.getString("license.SelectGrantFileDialog.invalidfile1"));
			} else if (result == Constants.EXCEPTION_INT8) {
				MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
						Messages.getString("license.LicenseManageDialog.activeByAdmin"));
			} else {
				MessageDialog.openInformation(getShell(), Messages.getString("license.LicenseManageDialog.notice"), 
						MessageFormat.format(Messages.getString("license.LicenseManageDialog.activeFail"), StringUtils.getErrorCode(result)));
			}
		}
	}
}
