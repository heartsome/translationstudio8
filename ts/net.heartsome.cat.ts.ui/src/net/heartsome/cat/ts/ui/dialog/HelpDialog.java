package net.heartsome.cat.ts.ui.dialog;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

/**
 * 专门针对继承 dialog 而设置的帮助按钮
 * @author robert	2012-09-06
 */
public class HelpDialog extends TrayDialog{
	private String helpUrl = "";
	private ToolItem helpItem;
	protected HelpDialog(Shell parentShell) {
		super(parentShell);
		// 默认设置帮助的 toolbar 可用
		setHelpAvailable(true);
	}
	
	/**
	 * 设置帮助的键接
	 * @param helpUrl
	 */
	public void setHelpUrl(String helpUrl) {
		this.helpUrl = helpUrl;
		if (helpUrl == null || "".equals(helpUrl)) {
			helpItem.setEnabled(false);
		}else {
			helpItem.setEnabled(true);
		}
	}
	
	/**
	 * robert help 2012-08-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
        ((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});		
		helpItem = new ToolItem(toolBar, SWT.NONE);
		helpItem.setImage(helpImage);
		helpItem.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		helpItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
            }
        });
		return toolBar;
	}


}
