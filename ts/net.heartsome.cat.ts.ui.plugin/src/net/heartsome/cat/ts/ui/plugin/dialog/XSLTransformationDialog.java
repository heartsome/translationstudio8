package net.heartsome.cat.ts.ui.plugin.dialog;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.HSErrorHandler;

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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * XSL Transformation 对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class XSLTransformationDialog extends Dialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);

	/** 源文件文本框 */
	private Text txtSource;

	/** 源文件浏览按钮 */
	private Button btnSource;

	/** XSL 样式表文本框 */
	private Text txtXSL;

	/** XSL 样式表浏览按钮 */
	private Button btnXSL;

	/** 已转变文件文本框 */
	private Text txtTarget;

	/** 已转变文件浏览按钮 */
	private Button btnTarget;

	/** 是否打开文件复选框 */
	private Button btnOpenFile;

	/** Logo 图片路径 */
	private String imagePath;

	public XSLTransformationDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.XSLTransformationDialog.title"));
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_XSL_PATH);
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		tparent.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().hint(450, 230).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tparent);

		createMenu();

		Label lblSource = new Label(tparent, SWT.None);
		lblSource.setText(Messages.getString("dialog.XSLTransformationDialog.lblSource"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(lblSource);

		txtSource = new Text(tparent, SWT.BORDER);
		txtSource.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSource.setEditable(false);
		btnSource = new Button(tparent, SWT.None);
		btnSource.setText(Messages.getString("dialog.XSLTransformationDialog.btnSource"));

		Label lblXSL = new Label(tparent, SWT.None);
		lblXSL.setText(Messages.getString("dialog.XSLTransformationDialog.lblXSL"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(lblXSL);
		txtXSL = new Text(tparent, SWT.BORDER);
		txtXSL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtXSL.setEditable(false);
		btnXSL = new Button(tparent, SWT.None);
		btnXSL.setText(Messages.getString("dialog.XSLTransformationDialog.btnXSL"));

		Label lblTarget = new Label(tparent, SWT.None);
		lblTarget.setText(Messages.getString("dialog.XSLTransformationDialog.lblTarget"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(lblTarget);
		txtTarget = new Text(tparent, SWT.BORDER);
		txtTarget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtTarget.setEditable(false);
		btnTarget = new Button(tparent, SWT.None);
		btnTarget.setText(Messages.getString("dialog.XSLTransformationDialog.btnTarget"));

		btnOpenFile = new Button(tparent, SWT.CHECK);
		btnOpenFile.setText(Messages.getString("dialog.XSLTransformationDialog.btnOpenFile"));
		GridDataFactory.fillDefaults().span(3, 1).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(btnOpenFile);

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
		fileItem.setText(Messages.getString("dialog.XSLTransformationDialog.fileItem"));
		fileItem.setMenu(fileMenu);

		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText(Messages.getString("dialog.XSLTransformationDialog.exitItem"));
		exitItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Menu helpMenu = new Menu(menu);
		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
		helpItem.setText(Messages.getString("dialog.XSLTransformationDialog.helpMenu"));
		helpItem.setMenu(helpMenu);

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.XSLTransformationDialog.aboutItem"));
		String imgPath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_XSL_MENU_PATH);
		aboutItem.setImage(new Image(Display.getDefault(), imgPath));
		aboutItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(getShell(), Messages
						.getString("dialog.XSLTransformationDialog.aboutItemName"), imagePath, Messages
						.getString("dialog.XSLTransformationDialog.aboutItemVersion"));
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

	}

	/**
	 * 初始化浏览按钮的监听 ;
	 */
	private void initListener() {
		btnSource.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openFileDialog(
						txtSource,
						SWT.OPEN,
						Messages.getString("dialog.XSLTransformationDialog.fileDialogTitle1"),
						new String[] { "*.hsxliff", "*.sdlxliff", "*.tmx", "*.tbx", "*.xml", "*" },	//$extension$
						new String[] { Messages.getString("dialog.XSLTransformationDialog.filters1"),
								Messages.getString("dialog.XSLTransformationDialog.filters2"),
								Messages.getString("dialog.XSLTransformationDialog.filters3"),
								Messages.getString("dialog.XSLTransformationDialog.filters4"),
								Messages.getString("dialog.XSLTransformationDialog.filters5"),
								Messages.getString("dialog.XSLTransformationDialog.filters6") });
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnXSL.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openFileDialog(
						txtXSL,
						SWT.OPEN,
						Messages.getString("dialog.XSLTransformationDialog.fileDialogTitle2"),
						new String[] { "*.xsl", "*.xml", "*" },
						new String[] { Messages.getString("dialog.XSLTransformationDialog.filters7"),
								Messages.getString("dialog.XSLTransformationDialog.filters5"),
								Messages.getString("dialog.XSLTransformationDialog.filters6") });
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnTarget.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openFileDialog(txtTarget, SWT.SAVE,
						Messages.getString("dialog.XSLTransformationDialog.fileDialogTitle3"), new String[] { "*" },
						new String[] { Messages.getString("dialog.XSLTransformationDialog.filters6") });
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	/**
	 * 打开文件对话框
	 * @param txt
	 *            显示路径文本框
	 * @param style
	 *            对话框样式
	 * @param title
	 *            对话框标题
	 * @param extensions
	 * @param filterNames
	 *            ;
	 */
	private void openFileDialog(Text txt, int style, String title, String[] extensions, String[] filterNames) {
		FileDialog dialog = new FileDialog(getShell(), style);
		dialog.setText(title);
		dialog.setFilterExtensions(extensions);
		dialog.setFilterNames(filterNames);
		String fileSep = System.getProperty("file.separator");
		if (txt.getText() != null && !txt.getText().trim().equals("")) {
			dialog.setFilterPath(txt.getText().substring(0, txt.getText().lastIndexOf(fileSep)));
			dialog.setFileName(txt.getText().substring(txt.getText().lastIndexOf(fileSep) + 1));
		} else {
			dialog.setFilterPath(System.getProperty("user.home"));
		}
		String path = dialog.open();
		if (path != null) {
			txt.setText(path);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setText(Messages.getString("dialog.XSLTransformationDialog.ok"));
		getButton(IDialogConstants.CANCEL_ID).setText(Messages.getString("dialog.XSLTransformationDialog.cancel"));
		
		getDialogArea().getParent().layout();
		getShell().layout();
	}

	@Override
	protected void okPressed() {
		String strSourcePath = txtSource.getText();
		String strXSLPath = txtXSL.getText();
		String strTargetPath = txtTarget.getText();
		if (strSourcePath.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.XSLTransformationDialog.msgTitle"),
					Messages.getString("dialog.XSLTransformationDialog.msg1"));
			return;
		}
		if (strXSLPath.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.XSLTransformationDialog.msgTitle"),
					Messages.getString("dialog.XSLTransformationDialog.msg2"));
			return;
		}
		if (strTargetPath.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.XSLTransformationDialog.msgTitle"),
					Messages.getString("dialog.XSLTransformationDialog.msg3"));
			return;
		}

		try {
			transform(strSourcePath, strXSLPath, strTargetPath);
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.XSLTransformationDialog.msgTitle"),
					Messages.getString("dialog.XSLTransformationDialog.msg4"));
			if (btnOpenFile.getSelection()) {
				if (!Program.launch(strTargetPath)) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialog.XSLTransformationDialog.msgTitle"),
							Messages.getString("dialog.XSLTransformationDialog.msg5"));
				}
			}
			close();
		} catch (Exception e) {
			LOGGER.error(Messages.getString("dialog.XSLTransformationDialog.logger1"), e);
		}
	}

	/**
	 * 转换文件
	 * @param strSourcePath
	 *            源文件路径
	 * @param strXSLPath
	 *            XSL 文件路径
	 * @param strTargetPath
	 *            转变文件路径
	 * @throws Exception
	 *             ;
	 */
	private void transform(String strSourcePath, String strXSLPath, String strTargetPath) throws Exception {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		String catalogPath = PluginUtil.getCataloguePath();

		if (tfactory.getFeature(SAXSource.FEATURE)) {
			// Standard way of creating an XMLReader in JAXP 1.1.
			SAXParserFactory pfactory = SAXParserFactory.newInstance();
			pfactory.setNamespaceAware(true); // Very important!
			// Turn on validation.
			// pfactory.setValidating(true);
			// Get an XMLReader.
			XMLReader reader = pfactory.newSAXParser().getXMLReader();
			reader.setEntityResolver(new Catalogue(catalogPath));

			// Instantiate an error handler (see the Handler inner class below)
			// that will report any
			// errors or warnings that occur as the XMLReader is parsing the XML
			// input.
			reader.setErrorHandler(new HSErrorHandler());

			// Standard way of creating a transformer from a URL.
			Transformer t = tfactory.newTransformer(new StreamSource(strXSLPath));

			// Specify a SAXSource that takes both an XMLReader and a URL.
			SAXSource source = new SAXSource(reader, new InputSource(strSourcePath));

			// Transform to a file.
			t.transform(source, new StreamResult(strTargetPath));

		} else {
			throw new Exception(Messages.getString("dialog.XSLTransformationDialog.msg6")); //$NON-NLS-1$
		}
	}
}
