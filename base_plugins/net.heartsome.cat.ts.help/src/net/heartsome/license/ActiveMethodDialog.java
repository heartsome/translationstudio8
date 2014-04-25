package net.heartsome.license;

import net.heartsome.license.constants.Constants;
import net.heartsome.license.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 激活方式选择对话框
 * @author  karl
 * @version 
 * @since   JDK1.6
 */
public class ActiveMethodDialog extends Dialog {

	private Point p;
	
	/**
	 * 构造器
	 * @param parentShell
	 */
	public ActiveMethodDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * 构造器
	 * @param parentShell
	 * @param p
	 */
	public ActiveMethodDialog(Shell parentShell, Point p) {
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
		createButton(parent, IDialogConstants.CANCEL_ID,
				Messages.getString("license.LicenseManageDialog.exitBtn"), true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("license.LicenseManageDialog.title"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginTop = 5;
		tparent.setLayout(layout);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tparent);
		
		Group groupActiveMethod = new Group(tparent, SWT.NONE);
		groupActiveMethod.setText(Messages.getString("license.ActiveMethodDialog.activemethod"));
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 20;
		groupLayout.marginHeight = 20;
		groupLayout.verticalSpacing = 10;
		groupLayout.numColumns = 2;
		groupActiveMethod.setLayout(groupLayout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groupActiveMethod);
		
		Button btnOnline = new Button(groupActiveMethod, SWT.NONE);
		GridData btnData = new GridData();
		btnData.widthHint = 200;
		btnData.heightHint = 40;
		btnOnline.setLayoutData(btnData);
		btnOnline.setText(Messages.getString("license.ActiveMethodDialog.activeonline"));
		btnOnline.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setReturnCode(OK);
				Point p = getShell().getLocation();
				close();
				LicenseManageDialog dialog = new LicenseManageDialog(getShell(), Constants.STATE_NOT_ACTIVATED, null, null, true, p);
				int result = dialog.open();
				if (result != IDialogConstants.OK_ID) {
					System.exit(0);
				}
			}
		});
		
		Label labelRecommend = new Label(groupActiveMethod, SWT.NONE);
		labelRecommend.setText(Messages.getString("license.ActiveMethodDialog.recommend"));
		
		Label labelOnline = new Label(groupActiveMethod, SWT.WRAP);
		labelOnline.setText(Messages.getString("license.ActiveMethodDialog.onlinemessage"));
		GridData dataLabel = new GridData();
		dataLabel.horizontalSpan = 2;
		dataLabel.widthHint = 450;
		labelOnline.setLayoutData(dataLabel);
		
		Label labelSpace = new Label(groupActiveMethod, SWT.NONE);
		GridData dataSpace = new GridData();
		dataSpace.horizontalSpan = 2;
		dataSpace.heightHint = 20;
		labelSpace.setLayoutData(dataSpace);
		
		Button btnOffline = new Button(groupActiveMethod, SWT.NONE);
		GridData btnData1 = new GridData();
		btnData1.widthHint = 200;
		btnData1.heightHint = 40;
		btnData1.horizontalSpan = 2;
		btnOffline.setLayoutData(btnData1);
		btnOffline.setText(Messages.getString("license.ActiveMethodDialog.activeoffline"));
		btnOffline.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setReturnCode(OK);
				Point p = getShell().getLocation();
				close();
				OfflineActiveDialog dialog = new OfflineActiveDialog(getShell(), p);
				int result = dialog.open();
				if (result != IDialogConstants.OK_ID) {
					System.exit(0);
				}
			}
		});
		
		Label labelOffline = new Label(groupActiveMethod, SWT.WRAP);
		labelOffline.setText(Messages.getString("license.ActiveMethodDialog.offlinemessage"));
		labelOffline.setLayoutData(dataLabel);
		
		Label labelOffline1 = new Label(groupActiveMethod, SWT.WRAP);
		labelOffline1.setText(Messages.getString("license.ActiveMethodDialog.offlinemessage1"));
		GridData dataLabel1 = new GridData();
		dataLabel1.horizontalSpan = 2;
		dataLabel1.widthHint = 450;
		labelOffline1.setLayoutData(dataLabel1);
		
		return super.createDialogArea(parent);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(520, 470);
	}
}
