package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.File;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.cat.ts.ui.plugin.AboutComposite;
import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;
import net.heartsome.cat.ts.ui.plugin.util.RTFCleaner;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * RTFCleaner 对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class RTFCleanerDialog extends Dialog {

	/** Logo 图片路径 */
	private String imagePath;

	/**
	 * 构造方法
	 * @param parentShell
	 */
	public RTFCleanerDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.RTFCleanerDialog.title"));
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_RTFCLEANER_PATH);
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		tparent.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(230, 330).grab(true, true).applyTo(tparent);

		createMenu();
		createToolBar(tparent);
		Composite cmpAbout = new Composite(tparent, SWT.None);
		cmpAbout.setLayout(new GridLayout());
		cmpAbout.setLayoutData(new GridData(GridData.FILL_BOTH));
		new AboutComposite(cmpAbout, SWT.BORDER, Messages.getString("dialog.RTFCleanerDialog.aboutName"), imagePath,
				Messages.getString("dialog.RTFCleanerDialog.aboutVersion"));

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
		fileItem.setMenu(fileMenu);
		fileItem.setText(Messages.getString("dialog.RTFCleanerDialog.fileMenu"));

		MenuItem addStylesItem = new MenuItem(fileMenu, SWT.PUSH);
		addStylesItem.setText(Messages.getString("dialog.RTFCleanerDialog.addStylesItem"));
		addStylesItem.setImage(new Image(Display.getDefault(), PluginUtil
				.getAbsolutePath(PluginConstants.PIC_OPEN_CSV_PATH)));
		addStylesItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				handleFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText(Messages.getString("dialog.RTFCleanerDialog.exitItem"));
		exitItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	/**
	 * 创建工具栏
	 * @param parent
	 *            父控件
	 */
	private void createToolBar(Composite parent) {
		ToolBar toolBar = new ToolBar(parent, SWT.NO_FOCUS | SWT.None);
		toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolItem addStyleItem = new ToolItem(toolBar, SWT.PUSH);
		addStyleItem.setToolTipText(Messages.getString("dialog.RTFCleanerDialog.addStyleItem"));
		addStyleItem.setImage(new Image(Display.getDefault(), PluginUtil
				.getAbsolutePath(PluginConstants.PIC_OPEN_CSV_PATH)));
		addStyleItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				handleFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	/**
	 * 处理 RTF 文件 ;
	 */
	private void handleFile() {
		FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
		dialog.setText(Messages.getString("dialog.RTFCleanerDialog.dialogTitle"));
		String[] extensions = { "*.rtf", "*" };
		String[] filters = { Messages.getString("dialog.RTFCleanerDialog.filters1"),
				Messages.getString("dialog.RTFCleanerDialog.filters2") };
		dialog.setFilterExtensions(extensions);
		dialog.setFilterNames(filters);
		dialog.setFilterPath(System.getProperty("user.home"));
		dialog.open();
		String[] arrSource = dialog.getFileNames();
		if (arrSource == null || arrSource.length == 0) {
			return;
		}
		int errors = 0;
		for (int i = 0; i < arrSource.length; i++) {
			File f = new File(dialog.getFilterPath() + System.getProperty("file.separator") + arrSource[i]); //$NON-NLS-1$
			Hashtable<String, String> params = new Hashtable<String, String>();
			params.put("source", f.getAbsolutePath()); //$NON-NLS-1$
			params.put("output", f.getAbsolutePath()); //$NON-NLS-1$

			Vector<String> result = RTFCleaner.run(params);

			if (!"0".equals(result.get(0))) { //$NON-NLS-1$
				String msg = MessageFormat.format(Messages.getString("dialog.RTFCleanerDialog.msg1"), arrSource[i])
						+ (String) result.get(1);
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.RTFCleanerDialog.msgTitle"), msg);
				errors++;
			}
		}
		if (errors < arrSource.length) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.RTFCleanerDialog.msgTitle"),
					Messages.getString("dialog.RTFCleanerDialog.msg2"));
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Composite cmp = parent.getParent();
		parent.dispose();
		cmp.layout();
	}
}
