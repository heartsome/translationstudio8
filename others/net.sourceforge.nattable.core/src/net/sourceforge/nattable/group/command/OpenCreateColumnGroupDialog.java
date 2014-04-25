package net.sourceforge.nattable.group.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;
import net.sourceforge.nattable.group.gui.CreateColumnGroupDialog;
import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class OpenCreateColumnGroupDialog extends AbstractContextFreeCommand implements IColumnGroupCommand {
	
	private final CreateColumnGroupDialog dialog;
	private final MessageBox messageBox;

	public OpenCreateColumnGroupDialog(Shell parentShell) {
		this.dialog = CreateColumnGroupDialog.createColumnGroupDialog(parentShell);
		 messageBox = new MessageBox(parentShell, SWT.INHERIT_DEFAULT | SWT.ICON_ERROR | SWT.OK);
	}
	
	public CreateColumnGroupDialog getDialog() {
		return dialog;
	}

	public void openDialog(ILayer contextLayer) {
		dialog.setContextLayer(contextLayer);
		dialog.open();
	}
	
	public void openErrorBox(String errMessage) {		
		messageBox.setText("Error Message");
		messageBox.setMessage(errMessage);
		messageBox.open();
	}
	
}