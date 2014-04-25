package net.sourceforge.nattable.widget;

import static org.eclipse.swt.layout.GridData.CENTER;
import net.sourceforge.nattable.util.GUIHelper;
import net.sourceforge.nattable.util.ObjectUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class WaitDialog extends Dialog {

	private String msg;
	private Image iconImage;
	private Label textLabel;

	public WaitDialog(Shell parent, int shellStyle, String msg, Image iconImg) {
		super(parent);
		this.msg = msg;
		this.iconImage = iconImg;
		setShellStyle(shellStyle | SWT.APPLICATION_MODAL);
	}

	private void centerDialogOnScreen(Shell shell) {
		shell.setSize(250, 75);
		Rectangle parentSize = getParentShell().getBounds();
		Rectangle mySize = shell.getBounds();
		int locationX, locationY;
		locationX = (parentSize.width - mySize.width)/2+parentSize.x;
		locationY = (parentSize.height - mySize.height)/2+parentSize.y;
		shell.setLocation(locationX, locationY);
	}

	@Override
	protected Control createContents(Composite parent) {
		centerDialogOnScreen(getShell());
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(CENTER, CENTER, true, true));
		composite.setRedraw(true);

		Label imgLabel = new Label(composite, SWT.NONE);
		imgLabel.setImage(iconImage);
		
		textLabel = new Label(composite, SWT.NONE);
		textLabel.setLayoutData(new GridData(CENTER, CENTER, true, true));
		textLabel.setFont(GUIHelper.getFont(new FontData("Arial", 9, SWT.BOLD)));
		textLabel.setRedraw(true);
		textLabel.setText(msg);

		return composite;
	}

	public void setMsg(String msg) {
		this.msg = msg;
		textLabel.setText(msg);
		getShell().layout(new Control[]{textLabel});
	}

	@Override
	public boolean close() {
		if(ObjectUtils.isNotNull(iconImage)){
			iconImage.dispose();
		}
		return super.close();
	}
}