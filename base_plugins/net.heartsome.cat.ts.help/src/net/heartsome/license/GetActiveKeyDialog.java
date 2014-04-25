package net.heartsome.license;

import net.heartsome.cat.ts.help.Activator;
import net.heartsome.license.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 获取激活码的对话框
 * @author karl
 * @version
 * @since JDK1.6
 */
public class GetActiveKeyDialog extends Dialog {

	private Text textActivekey;
	private String activekey;
	private Point p;

	protected GetActiveKeyDialog(Shell parentShell, String activekey) {
		super(parentShell);
		this.activekey = activekey;
	}
	
	protected GetActiveKeyDialog(Shell parentShell, String activekey, Point p) {
		super(parentShell);
		this.activekey = activekey;
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
		super.createButtonsForButtonBar(parent);

		Button nextBtn = getButton(IDialogConstants.OK_ID);
		nextBtn.setText(Messages.getString("license.LicenseAgreementDialog.nextBtn"));
		Button exitBtn = getButton(IDialogConstants.CANCEL_ID);
		exitBtn.setText(Messages.getString("license.LicenseAgreementDialog.exitBtn"));
		exitBtn.setFocus();
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

		Group groupActivekey = new Group(tparent, SWT.NONE);
		groupActivekey.setText(Messages.getString("license.GetActiveKeyDialog.activekey"));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groupActivekey);
		GridLayout layoutGroup = new GridLayout(2, false);
		layoutGroup.marginWidth = 5;
		layoutGroup.marginHeight = 20;
		groupActivekey.setLayout(layoutGroup);

		StyledText text = new StyledText(groupActivekey, SWT.WRAP | SWT.READ_ONLY);
		text.setBackground(text.getParent().getBackground());
		text.setText(Messages.getString("license.GetActiveKeyDialog.activemessage"));
		GridData dataText = new GridData();
		dataText.horizontalSpan = 2;
		dataText.widthHint = 470;
		text.setLayoutData(dataText);
		int start = Messages.getString("license.GetActiveKeyDialog.activemessage").indexOf(
				Messages.getString("license.GetActiveKeyDialog.ts"));
		int length = Messages.getString("license.GetActiveKeyDialog.ts").length();
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;
		styleRange.fontStyle = SWT.BOLD;
		styleRange.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		text.setStyleRange(styleRange);

		Label label = new Label(groupActivekey, SWT.WRAP | SWT.NONE);
		label.setText(Messages.getString("license.GetActiveKeyDialog.activemessage1"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		textActivekey = new Text(groupActivekey, SWT.MULTI | SWT.WRAP);
		textActivekey.setEditable(false);
		textActivekey.setText(activekey);
		textActivekey.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 150).applyTo(textActivekey);

		Button btnCopy = new Button(groupActivekey, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.END).applyTo(btnCopy);
		btnCopy.setImage(Activator.getImageDescriptor("images/help/copy.png").createImage());
		btnCopy.setToolTipText(Messages.getString("license.GetActiveKeyDialog.copytoclipboard"));
		btnCopy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Clipboard cb = new Clipboard(Display.getCurrent());
				String textData = textActivekey.getText();
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { textData }, new Transfer[] { textTransfer });
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
		label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.getgrantfile"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.seperate"));
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("license.OfflineActiveDialog.activefinish"));
	}

	@Override
	protected void okPressed() {
		Point p = getShell().getLocation();
		super.okPressed();
		SelectGrantFileDialog dialog = new SelectGrantFileDialog(getShell(), p);
		int result = dialog.open();
		if (result != IDialogConstants.OK_ID) {
			System.exit(0);
		}
	}

}
