package net.heartsome.cat.ts.ui.plugin.dialog;

import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.Martif2Tbx;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MARTIF to TBX Converter 对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class Martif2TBXConverterDialog extends Dialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);

	/** Logo 图片路径 */
	private String imagePath;

	/** Martif 路径文本框 */
	private Text txtMartif;

	/** Martif 文件选择按钮 */
	private Button btnMartifBrowse;

	/** TBX 路径文本框 */
	private Text txtTBX;

	/** TBX 文件选择按钮 */
	private Button btnTBXBrowse;

	/**
	 * 构造方法
	 * @param parentShell
	 */
	public Martif2TBXConverterDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.Martif2TBXConverterDialog.title"));
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_MARTIF2TBX_PATH);
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		tparent.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(400, 160).grab(true, true).applyTo(tparent);

		createMenu();

		PluginUtil.createLabel(tparent, Messages.getString("dialog.Martif2TBXConverterDialog.txtMartif"));
		txtMartif = new Text(tparent, SWT.BORDER);
		txtMartif.setEditable(false);
		txtMartif.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnMartifBrowse = new Button(tparent, SWT.None);
		btnMartifBrowse.setText(Messages.getString("dialog.Martif2TBXConverterDialog.btnMartifBrowse"));

		PluginUtil.createLabel(tparent, Messages.getString("dialog.Martif2TBXConverterDialog.txtTBX"));
		txtTBX = new Text(tparent, SWT.BORDER);
		txtTBX.setEditable(false);
		txtTBX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnTBXBrowse = new Button(tparent, SWT.None);
		btnTBXBrowse.setText(Messages.getString("dialog.Martif2TBXConverterDialog.btnTBXBrowse"));
		initListener();
		
		tparent.layout();
		getShell().layout();
		return tparent;
	}

	/**
	 * 创建菜单 ;
	 */
	private void createMenu() {
		Menu menu = new Menu(getShell(), SWT.BAR);
		getShell().setMenuBar(menu);
		getShell().pack();

		Rectangle screenSize = Display.getDefault().getClientArea();
		Rectangle frameSize = getShell().getBounds();
		getShell().setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		Menu fileMenu = new Menu(menu);
		MenuItem fileItem = new MenuItem(menu, SWT.CASCADE);
		fileItem.setText(Messages.getString("dialog.Martif2TBXConverterDialog.fileMenu"));
		fileItem.setMenu(fileMenu);
		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText(Messages.getString("dialog.Martif2TBXConverterDialog.exitItem"));
		exitItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Menu helpMenu = new Menu(menu);
		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
		helpItem.setText(Messages.getString("dialog.Martif2TBXConverterDialog.helpMenu"));
		helpItem.setMenu(helpMenu);

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.Martif2TBXConverterDialog.aboutItem"));
		String imgPath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_MARTIF2TBX_MENU_PATH);
		aboutItem.setImage(new Image(Display.getDefault(), imgPath));
		aboutItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(getShell(), Messages
						.getString("dialog.Martif2TBXConverterDialog.dialogName"), imagePath, Messages
						.getString("dialog.Martif2TBXConverterDialog.dialogVersion"));
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void initListener() {
		btnMartifBrowse.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.getString("dialog.Martif2TBXConverterDialog.dialogTitle1"));
				String extensions[] = { "*.mtf", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
				String names[] = {
						Messages.getString("dialog.Martif2TBXConverterDialog.names1"), Messages.getString("dialog.Martif2TBXConverterDialog.names2") }; //$NON-NLS-1$ //$NON-NLS-2$
				dialog.setFilterNames(names);
				dialog.setFilterExtensions(extensions);
				String fileSep = System.getProperty("file.separator");
				if (txtMartif.getText() != null && !txtMartif.getText().trim().equals("")) {
					dialog.setFilterPath(txtMartif.getText().substring(0, txtMartif.getText().lastIndexOf(fileSep)));
					dialog.setFileName(txtMartif.getText().substring(txtMartif.getText().lastIndexOf(fileSep) + 1));
				} else {
					dialog.setFilterPath(System.getProperty("user.home"));
				}
				String filePath = dialog.open();
				if (filePath != null) {
					txtMartif.setText(filePath);
					txtTBX.setText(filePath + ".tbx");
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnTBXBrowse.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setText(Messages.getString("dialog.Martif2TBXConverterDialog.dialogTitle2"));
				String extensions[] = { "*.tbx", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
				String names[] = {
						Messages.getString("dialog.Martif2TBXConverterDialog.filters1"), Messages.getString("dialog.Martif2TBXConverterDialog.filters2") }; //$NON-NLS-1$ //$NON-NLS-2$
				dialog.setFilterNames(names);
				dialog.setFilterExtensions(extensions);
				String fileSep = System.getProperty("file.separator");
				if (txtTBX.getText() != null && !txtTBX.getText().trim().equals("")) {
					dialog.setFilterPath(txtTBX.getText().substring(0, txtTBX.getText().lastIndexOf(fileSep)));
					dialog.setFileName(txtTBX.getText().substring(txtTBX.getText().lastIndexOf(fileSep) + 1));
				} else {
					dialog.setFilterPath(System.getProperty("user.home"));
				}
				String filePath = dialog.open();
				if (filePath != null) {
					txtTBX.setText(filePath);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setText(Messages.getString("dialog.Martif2TBXConverterDialog.ok"));
		getButton(IDialogConstants.CANCEL_ID).setText(Messages.getString("dialog.Martif2TBXConverterDialog.cancel"));
		
		getDialogArea().getParent().layout();
		getShell().layout();
	}

	@Override
	protected void okPressed() {
		String martifPath = txtMartif.getText();
		if (martifPath == null || martifPath.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.Martif2TBXConverterDialog.msgTitle"),
					Messages.getString("dialog.Martif2TBXConverterDialog.msg1"));
			return;
		}
		String tbxPath = txtTBX.getText();
		if (tbxPath == null || tbxPath.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.Martif2TBXConverterDialog.msgTitle"),
					Messages.getString("dialog.Martif2TBXConverterDialog.msg2"));
			return;
		}
		Martif2Tbx converter = new Martif2Tbx();
		try {
			converter.convertFile(martifPath, tbxPath);
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.Martif2TBXConverterDialog.msgTitle"),
					Messages.getString("dialog.Martif2TBXConverterDialog.msg3"));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.Martif2TBXConverterDialog.msgTitle"),
					e.getMessage());
			return;
		}

	}
}
