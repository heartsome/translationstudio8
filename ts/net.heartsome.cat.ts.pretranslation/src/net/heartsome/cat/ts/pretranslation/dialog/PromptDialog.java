//package net.heartsome.cat.ts.pretranslation.dialog;
//
//import java.util.List;
//
//import net.heartsome.cat.ts.pretranslation.resource.Messages;
//
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.dialogs.IDialogConstants;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Shell;
//
//public class PromptDialog extends Dialog {
//
//	private List<String> toolIds;
//	private String choiceResult;
//
//	/**
//	 * Create the dialog.
//	 * @param parentShell
//	 */
//	public PromptDialog(Shell parentShell, List<String> toolIds) {
//		super(parentShell);
//		this.toolIds = toolIds;
//	}
//
//	@Override
//	protected boolean canHandleShellCloseEvent() {
//		return false;
//	}
//
//	/**
//	 * Create contents of the dialog.
//	 * @param parent
//	 */
//	@Override
//	protected Control createDialogArea(Composite parent) {
//		Composite container = (Composite) super.createDialogArea(parent);
//
//		Label label = new Label(container, SWT.NONE);
//		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
//		label.setText(Messages.getString("dialog.PromptDialog.label"));
//
//		for (String toolId : toolIds) {
//			final Button btn = new Button(container, SWT.RADIO);
//			btn.setText(toolId);
//			btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//			btn.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					if (btn.getSelection()) {
//						choiceResult = btn.getText();
//					}
//				}
//			});
//		}
//
//		return container;
//	}
//
//	/**
//	 * Create contents of the button bar.
//	 * @param parent
//	 */
//	@Override
//	protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
//	}
//
//	/**
//	 * Return the initial size of the dialog.
//	 */
//	@Override
//	protected Point getInitialSize() {
//		return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
//	}
//
//	@Override
//	protected void okPressed() {
//		if (choiceResult == null) {
//			MessageDialog.openInformation(getShell(), Messages.getString("dialog.PromptDialog.msgTitle"),
//					Messages.getString("dialog.PromptDialog.msg"));
//			return;
//		}
//		super.okPressed();
//	}
//
//	public String getChoiceResult() {
//		return choiceResult;
//	}
//}
