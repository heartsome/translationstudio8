package net.heartsome.license;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.heartsome.license.resource.Messages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class CustomMessageDialog extends IconAndMessageDialog{

	private String editionInput;
	private String title;
	private StyledText text;
	private List<HashMap<String, Integer>> list = new ArrayList<HashMap<String, Integer>>();
	private Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	
	public CustomMessageDialog(Shell parentShell, String title, String message, 
			List<HashMap<String, Integer>> list, String editionInput) {
		super(parentShell);
		this.list = list;
		this.title = title;
        this.message = message;
        this.editionInput = editionInput;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		 createMessageArea(parent);
		return super.createDialogArea(parent);
	}

	@Override
	protected Control createMessageArea(Composite composite) {
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			addAccessibleListeners(imageLabel, image);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
					.applyTo(imageLabel);
		}
		// create message
		if (message != null) {
			text = new StyledText(composite, getMessageLabelStyle());
			text.setBackground(composite.getBackground());
			text.setText(message);
			text.setEditable(false);
			GridDataFactory
					.fillDefaults()
					.align(SWT.FILL, SWT.BEGINNING)
					.grab(true, false)
					.hint(
							convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
							SWT.DEFAULT).applyTo(text);
			setStyle();
		}
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button btnDownload =  createButton(parent, -1, Messages.getString("license.CustomMessageDialog.download"), false);
		btnDownload.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(MessageFormat.format(Messages.getString("license.CustomMessageDialog.urlDownload"), 
						ProtectionFactory.getProduct(editionInput), ProtectionFactory.getPlatform(), ProtectionFactory.getVersion()));
			}
			
		});
		
		if (!"L".equals(System.getProperty("TSEdition"))) {
			Button btnBuy = createButton(parent, -1, Messages.getString("license.LicenseManageDialog.link1"), false);
			btnBuy.addSelectionListener(new SelectionAdapter() {
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					Program.launch(Messages.getString("license.LicenseManageDialog.urlr8buy") + 
							"&PRODUCT=" + ProtectionFactory.getProduct() + "&PLATFORM=" + ProtectionFactory.getPlatform());
				}
				
			});
		}
		
		Button btnTrial =  createButton(parent, -1, Messages.getString("license.LicenseManageDialog.link2"), false);
		btnTrial.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Messages.getString("license.LicenseManageDialog.urlr8trial") + 
						"&PRODUCT=" + ProtectionFactory.getProduct() + "&PLATFORM=" + ProtectionFactory.getPlatform());
			}
			
		});
		
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("license.CustomMessageDialog.inputAgain"),
				true).setFocus();
	}

	@Override
	protected Image getImage() {
		return getInfoImage();
	}

	private void addAccessibleListeners(Label label, final Image image) {
		label.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent event) {
				final String accessibleMessage = getAccessibleMessageFor(image);
				if (accessibleMessage == null) {
					return;
				}
				event.result = accessibleMessage;
			}
		});
	}
	
	private String getAccessibleMessageFor(Image image) {
		if (image.equals(getErrorImage())) {
			return JFaceResources.getString("error");//$NON-NLS-1$
		}

		if (image.equals(getWarningImage())) {
			return JFaceResources.getString("warning");//$NON-NLS-1$
		}

		if (image.equals(getInfoImage())) {
			return JFaceResources.getString("info");//$NON-NLS-1$
		}

		if (image.equals(getQuestionImage())) {
			return JFaceResources.getString("question"); //$NON-NLS-1$
		}

		return null;
	}
	
	private void setStyle() {
		for (HashMap<String, Integer> map : list) {
			StyleRange styleRange = new StyleRange();
			styleRange.start = map.get("start");
			styleRange.length = map.get("length");
			styleRange.fontStyle = SWT.BOLD;
			styleRange.foreground = red;
			text.setStyleRange(styleRange);
		}
	}
}
