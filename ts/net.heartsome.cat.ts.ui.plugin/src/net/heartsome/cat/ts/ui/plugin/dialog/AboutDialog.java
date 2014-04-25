package net.heartsome.cat.ts.ui.plugin.dialog;

import net.heartsome.cat.ts.ui.plugin.AboutComposite;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 关于对话框（表头为"关于..."，第一行为标题，第二行为 Logo，第三行为版本信息，第四行为版权信息，第五行为网址信息)
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class AboutDialog extends Dialog {
	
	/** 第一行标题 */
	private String name;
	
	/** Logo 图片路径 */
	private String imagePath;
	
	/** 版本信息 */
	private String version;

	/**
	 * 构造方法
	 * @param parentShell
	 * @param name
	 * 				第一行标题
	 * @param imagePath
	 * 				Logo 图片路径
	 * @param version
	 * 				版本信息
	 */
	protected AboutDialog(Shell parentShell, String name, String imagePath, String version) {
		super(parentShell);
		this.name = name;
		this.imagePath = imagePath;
		this.version = version;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.AboutDialog.title"));
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(0, 0, 5, 5).spacing(0, 0).applyTo(parent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(200, 250).grab(true, true).applyTo(parent);
		new AboutComposite(parent, SWT.BORDER, name, imagePath, version);
		return parent;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Composite cmp = parent.getParent();
		parent.dispose();
		cmp.layout();
	}
}
