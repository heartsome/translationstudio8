package net.heartsome.cat.ts.dialog;

import java.text.MessageFormat;

import net.heartsome.cat.ts.Activator;
import net.heartsome.cat.ts.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

/**
 * 关于对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class AboutDialog extends Dialog {

	private Image image;
	private Font font;

	public AboutDialog(Shell parentShell) {
		super(parentShell);
		image = Activator.getImageDescriptor("images/help/aboutR8.png").createImage();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.AboutDialog.title"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().spacing(0, 0).extendedMargins(SWT.DEFAULT, SWT.DEFAULT, 0, 8).applyTo(tparent);
		GridData parentData = new GridData(GridData.FILL_BOTH);
		parentData.heightHint = 480;
		parentData.widthHint = 445;
		tparent.setLayoutData(parentData);

		Composite cmpMain = new Composite(tparent, SWT.None);
		cmpMain.setLayout(new GridLayout());
		cmpMain.setLayoutData(new GridData(GridData.FILL_BOTH));
		Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		cmpMain.setBackground(white);

		Label lblImage = new Label(cmpMain, SWT.CENTER);
		lblImage.setImage(image);
		// lblImage.setLayoutData(new GridData(GridData.FILL_BOTH));
		lblImage.setBackground(white);

		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);

		Label lblVersion = new Label(cmpMain, SWT.BOLD);
		FontData fontData = Display.getDefault().getSystemFont().getFontData()[0];
		fontData.setStyle(fontData.getStyle() | SWT.BOLD);
		this.font = new Font(lblVersion.getDisplay(), fontData);
		lblVersion.setFont(this.font);
		String version = System.getProperty("TSEdition");
		if (version.equals("U")) {
			lblVersion.setText(Messages.getString("dialog.AboutDialog.lblVersionU"));
		} else if (version.equals("F")) {
			lblVersion.setText(Messages.getString("dialog.AboutDialog.lblVersionF"));
		} else if (version.equals("P")) {
			lblVersion.setText(Messages.getString("dialog.AboutDialog.lblVersionP"));
		} else if (version.equals("L")) {
			lblVersion.setText(Messages.getString("dialog.AboutDialog.lblVersionL"));
		}
		lblVersion.setLayoutData(data);
		lblVersion.setBackground(white);

		Label lblVersion2 = new Label(cmpMain, SWT.None);
		String version2 = System.getProperty("TSVersionDate");
		lblVersion2.setText(MessageFormat.format(Messages.getString("dialog.AboutDialog.lblVersion2"),
				version2.substring(0, version2.lastIndexOf(".")), version2.substring(version2.lastIndexOf(".") + 1)));
		lblVersion2.setLayoutData(data);
		lblVersion2.setBackground(white);

		Composite cmpWeb = new Composite(cmpMain, SWT.None);
		cmpWeb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).extendedMargins(-5, 0, 0, 0).applyTo(cmpWeb);
		cmpWeb.setBackground(white);
		Label lblProduct = new Label(cmpWeb, SWT.None);
		lblProduct.setText(Messages.getString("dialog.AboutDialog.lblProduct"));
		lblProduct.setBackground(white);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblProduct);
		Link productLink = new Link(cmpWeb, SWT.NONE);
		productLink.setText("<a>" + Messages.getString("dialog.AboutDialog.productLink") + "</a>");
		productLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Messages.getString("dialog.AboutDialog.productLink"));
			}
		});
		productLink.setBackground(white);

		Label lblSupport = new Label(cmpWeb, SWT.None);
		lblSupport.setText(Messages.getString("dialog.AboutDialog.lblSupport"));
		lblSupport.setBackground(white);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblSupport);
		Link supportLink = new Link(cmpWeb, SWT.NONE);
		supportLink.setText("<a>" + Messages.getString("dialog.AboutDialog.supportLink") + "</a>");
		supportLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Messages.getString("dialog.AboutDialog.supportLink"));
			}
		});
		supportLink.setBackground(white);

		Label lblCopyRight = new Label(cmpMain, SWT.None);
		lblCopyRight.setText(Messages.getString("dialog.AboutDialog.lblCopyRight"));
		lblCopyRight.setLayoutData(data);
		lblCopyRight.setBackground(white);

		Composite cmpWarn = new Composite(tparent, SWT.None);
		cmpWarn.setLayout(new GridLayout());
		cmpWarn.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label lblWarn = new Label(cmpWarn, SWT.WRAP);
		lblWarn.setLayoutData(new GridData(GridData.FILL_BOTH));
		Point bodySize = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		GridData gd = (GridData) lblWarn.getLayoutData();
		gd.widthHint = bodySize.x;
		lblWarn.setText(Messages.getString("dialog.AboutDialog.lblWarn"));

		return tparent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	public boolean close() {
		if (image != null && !image.isDisposed()) {
			image.dispose();
		}
		if (font != null && !font.isDisposed()) {
			font.dispose();
		}
		return super.close();
	}
}
