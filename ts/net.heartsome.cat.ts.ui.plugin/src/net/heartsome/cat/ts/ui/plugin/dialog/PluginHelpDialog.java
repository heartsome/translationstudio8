package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.plugin.Activator;
import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.xml.sax.SAXException;

/**
 * 插件中的帮助链接对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class PluginHelpDialog extends Dialog {

	private SashForm mainSash;

	private Tree tree;

	private Browser browser;

	private Hashtable<TreeItem, String> hashTable;

	private String currentFilePath;

	private String helpFilePath;

	private FileOutputStream fosSearch;

	private boolean isFound;

	private String title;

	protected PluginHelpDialog(Shell parentShell, String helpFile, String title) {
		super(parentShell);
		this.helpFilePath = helpFile;
		this.title = title;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (title == null) {
			newShell.setText(Messages.getString("dialog.PluginHelpDialog.title"));
		} else {
			newShell.setText(title);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().spacing(0, 0).numColumns(1).applyTo(tparent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tparent);

		createMenu();
		createToolBar(tparent);

		mainSash = new SashForm(tparent, SWT.NONE);
		mainSash.setOrientation(SWT.HORIZONTAL);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainSash.setLayout(layout);
		mainSash.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		Composite navigation = new Composite(mainSash, SWT.BORDER);
		navigation.setLayout(new GridLayout(1, false));
		navigation.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

		tree = new Tree(navigation, SWT.NONE);
		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if (tree.getSelectionCount() > 0) {
					TreeItem item = tree.getSelection()[0];
					String url = hashTable.get(item);
					if (url != null && !url.equals("")) {
						browser.setUrl(url);
						browser.update();
					}
				}
			}
		});

		tree.addTreeListener(new TreeAdapter() {
			public void treeCollapsed(TreeEvent e) {
				TreeItem item = (TreeItem) e.item;
				if (item != null && item.getData() != null) {
					if (item.getData().equals("toc")) {
						item.setImage(Activator.getImageDescriptor(PluginConstants.HELP_TOC_CLOSED).createImage());
					}
					if (item.getData().equals("book")) {
						item.setImage(Activator.getImageDescriptor(PluginConstants.HELP_BOOK_CLOSED).createImage());
					}
				}
			}

			public void treeExpanded(TreeEvent e) {
				TreeItem item = (TreeItem) e.item;
				if (item != null && item.getData() != null) {
					if (item.getData().equals("toc")) {
						item.setImage(Activator.getImageDescriptor(PluginConstants.HELP_TOC_OPEN).createImage());
					}
					if (item.getData().equals("book")) {
						item.setImage(Activator.getImageDescriptor(PluginConstants.HELP_BOOK_OPEN).createImage());
					}
				}
			}
		});

		tree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		Composite contents = new Composite(mainSash, SWT.BORDER);
		contents.setLayout(new GridLayout(1, false));
		contents.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

		browser = new Browser(contents, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

		mainSash.setWeights(new int[] { 30, 70 });

		hashTable = new Hashtable<TreeItem, String>();
		if (!helpFilePath.equals("")) {
			loadTree(helpFilePath);
		}
		return tparent;
	}

	private void createMenu() {
		final Menu mainMenu = new Menu(getShell(), SWT.BAR);
		getShell().setMenuBar(mainMenu);

		Rectangle screenSize = Display.getDefault().getClientArea();
		Rectangle frameSize = getShell().getBounds();
		getShell().setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		Menu fileMenu = new Menu(mainMenu);
		MenuItem fileMenuItem = new MenuItem(mainMenu, SWT.CASCADE);
		fileMenuItem.setText(Messages.getString("dialog.PluginHelpDialog.fileMenuItem"));
		fileMenuItem.setMenu(fileMenu);

		MenuItem openFileItem = new MenuItem(fileMenu, SWT.PUSH);
		openFileItem.setText(Messages.getString("dialog.PluginHelpDialog.openFileItem"));
		openFileItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});

		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText(Messages.getString("dialog.PluginHelpDialog.exitItem"));
		exitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});

		MenuItem editMenu = new MenuItem(mainMenu, SWT.CASCADE);
		editMenu.setText(Messages.getString("dialog.PluginHelpDialog.editMenu"));
		Menu eMenu = new Menu(editMenu);
		editMenu.setMenu(eMenu);

		MenuItem searchItem = new MenuItem(eMenu, SWT.PUSH);
		searchItem.setText(Messages.getString("dialog.PluginHelpDialog.searchItem"));
		searchItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchText();
			}
		});
		MenuItem goMenu = new MenuItem(mainMenu, SWT.CASCADE);
		goMenu.setText(Messages.getString("dialog.PluginHelpDialog.goMenu"));
		Menu gMenu = new Menu(goMenu);
		goMenu.setMenu(gMenu);

		MenuItem backItem = new MenuItem(gMenu, SWT.PUSH);
		backItem.setText(Messages.getString("dialog.PluginHelpDialog.backItem"));
		backItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				goBack();
			}
		});

		MenuItem forwardItem = new MenuItem(gMenu, SWT.PUSH);
		forwardItem.setText(Messages.getString("dialog.PluginHelpDialog.forwardItem"));
		forwardItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				goForward();
			}
		});

		new MenuItem(gMenu, SWT.SEPARATOR);

		MenuItem homeItem = new MenuItem(gMenu, SWT.PUSH);
		homeItem.setText(Messages.getString("dialog.PluginHelpDialog.homeItem"));
		homeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				goHome();
			}
		});

		MenuItem helpMenu = new MenuItem(mainMenu, SWT.CASCADE);
		helpMenu.setText(Messages.getString("dialog.PluginHelpDialog.helpMenu"));
		Menu hMenu = new Menu(helpMenu);
		helpMenu.setMenu(hMenu);

		MenuItem aboutItem = new MenuItem(hMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.PluginHelpDialog.aboutItem"));
		aboutItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AboutDialog aboutDialog = new AboutDialog(getShell());
				aboutDialog.open();
			}
		});
	}

	/**
	 * 创建工具栏
	 * @param parent
	 *            ;
	 */
	private void createToolBar(Composite parent) {
		Composite cmpToolBar = new Composite(parent, SWT.None);
		GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(1).applyTo(cmpToolBar);
		cmpToolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ToolBar toolBar = new ToolBar(cmpToolBar, SWT.NO_FOCUS | SWT.FLAT);

		ToolItem openItem = new ToolItem(toolBar, SWT.PUSH | SWT.FLAT);
		openItem.setImage(Activator.getImageDescriptor(PluginConstants.HELP_OPEN_FILE).createImage()); //$NON-NLS-1$
		openItem.setToolTipText(Messages.getString("dialog.PluginHelpDialog.openFileItem"));
		openItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});

		new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem backItem = new ToolItem(toolBar, SWT.PUSH | SWT.FLAT);
		backItem.setImage(Activator.getImageDescriptor(PluginConstants.HELP_BACK).createImage());
		backItem.setToolTipText(Messages.getString("dialog.PluginHelpDialog.backItem"));
		backItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				goBack();
			}
		});

		ToolItem forwardItem = new ToolItem(toolBar, SWT.PUSH | SWT.FLAT);
		forwardItem.setImage(Activator.getImageDescriptor(PluginConstants.HELP_FORWARD).createImage());
		forwardItem.setToolTipText(Messages.getString("dialog.PluginHelpDialog.forwardItem"));
		forwardItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				goForward();
			}
		});
		new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem searchItem = new ToolItem(toolBar, SWT.PUSH | SWT.FLAT);
		searchItem.setImage(Activator.getImageDescriptor(PluginConstants.HELP_FIND).createImage());
		searchItem.setToolTipText(Messages.getString("dialog.PluginHelpDialog.searchItem")); //$NON-NLS-1$
		searchItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchText();
			}
		});
	}

	private void loadTree(String helpFile) {
		try {
			tree.removeAll();
			hashTable.clear();
			File file = new File(helpFile);
			String basePath = "";
			if (!file.isAbsolute()) {
				helpFile = file.getCanonicalPath();
				file = new File(helpFile);
			}
			basePath = file.getParent();
			currentFilePath = helpFile;
			SAXBuilder builder = new SAXBuilder();
			builder.setEntityResolver(new Catalogue(PluginUtil.getCataloguePath()));
			Document document = builder.build(helpFile);
			Element root = document.getRootElement();
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(root.getAttributeValue("title"));
			getShell().setText(root.getAttributeValue("title"));
			recurseTree(root, item, basePath);
			item.setExpanded(true);
			String url = root.getAttributeValue("url");
			if (!url.equals("")) {
				File f = new File(url);
				if (!f.exists()) {
					url = basePath + "/" + url;
				}
				browser.setUrl(new File(url).toURI().toURL().toString());
				hashTable.put(item, url);
			}
		} catch (Exception e) {

		}
	}

	private void recurseTree(Element elementParent, TreeItem itemParent, String basePath) throws Exception {
		List<Element> children = elementParent.getChildren();
		for (int i = 0; i < children.size(); i++) {
			Element element = children.get(i);
			String url = element.getAttributeValue("url", "");
			TreeItem item = new TreeItem(itemParent, SWT.NONE);
			if (url.startsWith(".")) {
				// relative path
				url = CommonFunction.getAbsolutePath(basePath, url);
			}
			if (!url.toLowerCase().startsWith("http:")) {
				File f = new File(url);
				if (!f.exists() || !f.isAbsolute()) {
					url = CommonFunction.getAbsolutePath(basePath, url);
				}
			}
			item.setData(element.getName());
			if (element.getName().equals("toc")) {
				item.setImage(Activator.getImageDescriptor(PluginConstants.HELP_TOC_CLOSED).createImage());
				item.setText(element.getText());
				if (url.startsWith(".")) {
					url = CommonFunction.getAbsolutePath(basePath, url);
				}
				loadToc(item, new File(url).getCanonicalPath());
			}
			if (element.getName().equals("book")) {
				item.setImage(Activator.getImageDescriptor(PluginConstants.HELP_BOOK_CLOSED).createImage());
				item.setText(element.getAttributeValue("title"));
				recurseTree(element, item, basePath);
				if (!url.equals("")) {
					hashTable.put(item, new File(url).toURI().toURL().toString());
				}
			}
			if (element.getName().equals("item")) {
				item.setImage(Activator.getImageDescriptor(PluginConstants.HELP_TOPIC).createImage());
				item.setText(element.getText());
				hashTable.put(item, new File(url).toURI().toURL().toString());
			}
		}
	}

	private void loadToc(TreeItem itemParent, String tocFile) throws Exception {
		File file = new File(tocFile);
		String basePath = "";
		if (file.exists()) {
			basePath = file.getParent();
		}

		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalogue(PluginUtil.getCataloguePath()));
		Document document = builder.build(tocFile);
		Element root = document.getRootElement();
		recurseTree(root, itemParent, basePath);
		String url = root.getAttributeValue("url");
		if (!url.equals("")) {
			File f = new File(url);
			if (!f.exists()) {
				url = basePath + "/" + url;
			}
			hashTable.put(itemParent, new File(url).toURI().toURL().toString());
		}
	}

	private void openFile() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		String[] extensions = { "*.xml", "*.*" };
		String[] filterNames = { Messages.getString("dialog.PluginHelpDialog.filterNames0"),
				Messages.getString("dialog.PluginHelpDialog.filterNames1") };
		fileDialog.setFilterExtensions(extensions);
		fileDialog.setFilterNames(filterNames);
		fileDialog.setFilterPath(System.getProperty("user.home"));
		String name = fileDialog.open();
		if (name == null) {
			return;
		}
		File file = new File(name);
		getShell().setText(file.getName());
		loadTree(file.getAbsolutePath());
	}

	private void searchText() {
		HelpSearchDialog searchDialog = new HelpSearchDialog(getShell());
		if (searchDialog.open() == IDialogConstants.OK_ID) {
			String text = searchDialog.getText();
			boolean sensitive = searchDialog.isSensitive();
			if (!sensitive) {
				text = text.toLowerCase();
			}
			try {
				isFound = false;
				File f = new File("searchresult.html"); //$NON-NLS-1$
				fosSearch = new FileOutputStream(f);
				f.deleteOnExit();
				writeString("<html>\n");
				writeString("<head>\n");
				writeString("<title>" + Messages.getString("dialog.PluginHelpDialog.searchTitle") + "</title>\n");
				writeString("</head>\n");
				writeString("<body>\n");

				ProgressDialog progressDialog = new ProgressDialog(getShell(),
						Messages.getString("dialog.PluginHelpDialog.progressDialogTitle"),
						Messages.getString("dialog.PluginHelpDialog.progressMessage"), ProgressDialog.SINGLE_BAR);
				progressDialog.open();
				Enumeration<TreeItem> keys = hashTable.keys();

				int count = hashTable.size();
				int i = 0;
				while (keys.hasMoreElements()) {
					String url = hashTable.get(keys.nextElement());
					String file = url.substring(url.lastIndexOf("/")); //$NON-NLS-1$
					progressDialog.updateProgressMessage(file);
					searchFile(url, text, sensitive);
					progressDialog.updateProgress((i * 100) / count);
					i++;
				}
				progressDialog.close();
				if (!isFound) {
					writeString("<p><b>" + Messages.getString("dialog.PluginHelpDialog.searchNone") + "</b></p>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</html>"); //$NON-NLS-1$
				fosSearch.close();
				browser.setUrl(f.toURI().toURL().toString());
			} catch (Exception e) {

			}
		}
	}

	private void searchFile(String file, String text, boolean sensitive) throws SAXException, IOException,
			ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalogue(PluginUtil.getCataloguePath())); //$NON-NLS-1$
		if (!file.startsWith("file:")) { //$NON-NLS-1$
			if (!file.startsWith("http:")) { //$NON-NLS-1$
				File f = new File(file);
				file = f.toURI().toURL().toString();
			}
		}
		URL url = new URL(file);
		Document doc = builder.build(url);

		Element root = doc.getRootElement();
		searchElement(root, file, text, sensitive);
	}

	private void searchElement(Element root, String file, String text, boolean sensitive)
			throws UnsupportedEncodingException, IOException {
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element e = it.next();
			if (!e.getAttributeValue("id", "").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String txt = e.getTextNormalize();
				if (!sensitive) {
					txt = txt.toLowerCase();
				}
				if (txt.indexOf(text) != -1) {
					if (e.getName().equals("p") //$NON-NLS-1$
							|| e.getName().equals("title") //$NON-NLS-1$
							|| e.getName().equals("h1") //$NON-NLS-1$
							|| e.getName().equals("h2") //$NON-NLS-1$
							|| e.getName().equals("h3") //$NON-NLS-1$
							|| e.getName().equals("h4") //$NON-NLS-1$
							|| e.getName().equals("h5") //$NON-NLS-1$
							|| e.getName().equals("h6")) { //$NON-NLS-1$
						writeString("<a href=\"" + file + "#" + e.getAttributeValue("id") + "\">" + file + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						writeString("<br/>"); //$NON-NLS-1$
						writeString(e.getTextNormalize());
						writeString("<hr/>"); //$NON-NLS-1$
						isFound = true;
					}
				}
			}
			searchElement(e, file, text, sensitive);
		}
	}

	private void writeString(String string) throws UnsupportedEncodingException, IOException {
		fosSearch.write(string.getBytes("UTF-8")); //$NON-NLS-1$
	}

	protected void goHome() {
		if (currentFilePath != null && !currentFilePath.equals("")) { //$NON-NLS-1$
			loadTree(currentFilePath);
		}
	}

	protected void goForward() {
		browser.forward();
	}

	protected void goBack() {
		browser.back();
	}

	class AboutDialog extends Dialog {

		protected AboutDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.getString("dialog.PluginHelpDialog.AboutDialog.title"));
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite tparent = (Composite) super.createDialogArea(parent);
			tparent.setLayout(new GridLayout());
			tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label logo = new Label(tparent, SWT.BORDER);
			logo.setImage(Activator.getImageDescriptor(PluginConstants.HELP_SPLASH).createImage());

			return tparent;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			Composite cmp = parent.getParent();
			parent.dispose();
			cmp.layout();
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Composite cmp = parent.getParent();
		parent.dispose();
		cmp.layout();
	}
}
