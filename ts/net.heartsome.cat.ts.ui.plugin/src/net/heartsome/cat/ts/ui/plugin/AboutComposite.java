package net.heartsome.cat.ts.ui.plugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * 关于面板（第一行为标题，第二行为 Logo，第三行为版本信息，第四行为版权信息，第五行为网址信息)
 * @author peason
 * @version
 * @since JDK1.6
 */
public final class AboutComposite {

	/** 父面板 */
	private Composite parent;

	/** 第一行标题 */
	private String name;

	/** Logo 图片路径 */
	private String imagePath;

	/** 版本信息 */
	private String version;

	private Cursor cursorHand = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
	/**
	 * 构造方法
	 * @param parent
	 * 				父面板
	 * @param style
	 * 				样式
	 * @param name
	 * 				第一行标题
	 * @param imagePath
	 * 				Logo 图片路径
	 * @param version
	 * 				版本信息
	 */
	public AboutComposite(Composite parent, int style, String name, String imagePath, String version) {
		this.parent = parent;
		this.name = name;
		this.imagePath = imagePath;
		this.version = version;
		init();
		parent.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				if(cursorHand != null && !cursorHand.isDisposed()){
					cursorHand.dispose();
				}
			}
		});
	}

	/**
	 * 初始化界面
	 *  ;
	 */
	private void init() {
		Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		parent.setBackground(white);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		Label lblName = new Label(parent, SWT.CENTER);
		lblName.setText(name);
		lblName.setLayoutData(data);
		lblName.setBackground(white);

		Image image = new Image(Display.getDefault(), imagePath);
		Label lblImage = new Label(parent, SWT.CENTER);
		lblImage.setImage(image);
		lblImage.setLayoutData(new GridData(GridData.FILL_BOTH));
		lblImage.setBackground(white);

		Label lblVersion = new Label(parent, SWT.CENTER);
		lblVersion.setText(version);
		lblVersion.setLayoutData(data);
		lblVersion.setBackground(white);

		new Label(parent, SWT.None).setForeground(white);

		Label lblCopyRight = new Label(parent, SWT.CENTER);
		lblCopyRight.setText(PluginConstants.PLUGIN_COPY_RIGHT);
		lblCopyRight.setLayoutData(data);
		lblCopyRight.setBackground(white);

		Label lblWebSite = new Label(parent, SWT.CENTER);
		lblWebSite.setText(PluginConstants.PLUGIN_WEB_SITE);
		lblWebSite.setLayoutData(data);
		lblWebSite.setBackground(white);
		lblWebSite.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		lblWebSite.setCursor(cursorHand);
		lblWebSite.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {

			}

			public void mouseDown(MouseEvent e) {
				Program.launch(PluginConstants.PLUGIN_WEB_SITE);
			}

			public void mouseDoubleClick(MouseEvent e) {

			}
		});
	}
}
