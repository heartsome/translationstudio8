package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSV to TMX Converter 对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class CSV2TMXConverterDialog extends Dialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);

	/** 表格对象，用于展示 CSV 文件的内容 */
	private Table table;

	/** 显示行数的标签 */
	private Label lblRowCount;

	/** 显示列数的标签 */
	private Label lblColCount;

	/** Logo 图片路径 */
	private String imagePath;

	/** CSV 文件路径 */
	private String csvPath;

	/** CSV 文件的列分隔符 */
	private String colSeparator;

	/** CSV 文件的文本定界符 */
	private String textDelimiter;

	/** CSV 文件的字符集 */
	private String encoding;

	/** CSV 文件的列数 */
	private int cols;

	/** CSV 文件的行数 */
	private int rows;

	/** 导出 CSV 文件时所用到的流对象 */
	private FileOutputStream output;

	/**
	 * 构造方法
	 * @param parentShell
	 */
	public CSV2TMXConverterDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.CSV2TMXConverterDialog.title"));
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_CSV2TMX_PATH);
		newShell.setImage(new Image(Display.getDefault(), imagePath));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().spacing(0, 0).numColumns(1).applyTo(tparent);
		GridDataFactory.fillDefaults().hint(750, 500).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tparent);

		createMenu();
		createToolBar(tparent);

		table = new Table(tparent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite cmpStatus = new Composite(tparent, SWT.BORDER);
		cmpStatus.setLayout(new GridLayout(2, true));
		cmpStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblRowCount = new Label(cmpStatus, SWT.None);
		lblRowCount.setText(MessageFormat.format(Messages.getString("dialog.CSV2TMXConverterDialog.lblRowCount"), 0));
		lblRowCount.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		lblColCount = new Label(cmpStatus, SWT.None);
		lblColCount.setText(MessageFormat.format(Messages.getString("dialog.CSV2TMXConverterDialog.lblColCount"), 0));
		lblColCount.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

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
		fileItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.fileMenu"));
		fileItem.setMenu(fileMenu);

		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.openItem"));
		String openCSVPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_OPEN_CSV_PATH);
		openItem.setImage(new Image(Display.getDefault(), openCSVPath));
		openItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		MenuItem exportItem = new MenuItem(fileMenu, SWT.PUSH);
		exportItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.exportItem"));
		String exportPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_EXPORT_TBX_PATH);
		exportItem.setImage(new Image(Display.getDefault(), exportPath));
		exportItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				export();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.exitItem"));
		exitItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Menu taskMenu = new Menu(menu);
		MenuItem taskItem = new MenuItem(menu, SWT.CASCADE);
		taskItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.taskMenu"));
		taskItem.setMenu(taskMenu);

		MenuItem deleteColItem = new MenuItem(taskMenu, SWT.PUSH);
		deleteColItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.deleteColItem"));
		String deleteColPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_DELETE_COLUMN_PATH);
		deleteColItem.setImage(new Image(Display.getDefault(), deleteColPath));
		deleteColItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				removeColumn();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		MenuItem colPropertyItem = new MenuItem(taskMenu, SWT.PUSH);
		colPropertyItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.colPropertyItem"));
		String setColPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_SET_COLUMN_PATH);
		colPropertyItem.setImage(new Image(Display.getDefault(), setColPath));
		colPropertyItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				selectLanguage();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Menu helpMenu = new Menu(menu);
		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
		helpItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.helpMenu"));
		helpItem.setMenu(helpMenu);

		MenuItem helpContentItem = new MenuItem(helpMenu, SWT.PUSH);
		helpContentItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.helpContentItem"));
		String helpPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_HELP_PATH);
		helpContentItem.setImage(new Image(Display.getDefault(), helpPath));
		helpContentItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				displayHelp();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.CSV2TMXConverterDialog.aboutItem"));
		String imgPath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_CSV2TMX_MENU_PATH);
		aboutItem.setImage(new Image(Display.getDefault(), imgPath));
		aboutItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(getShell(), Messages
						.getString("dialog.CSV2TMXConverterDialog.aboutItemName"), imagePath, Messages
						.getString("dialog.CSV2TMXConverterDialog.version") + " " + PluginConstants.CSV2TMX_VERSION);
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

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
		GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(3).equalWidth(false).applyTo(cmpToolBar);
		cmpToolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ToolBar toolBar = new ToolBar(cmpToolBar, SWT.NO_FOCUS | SWT.FLAT);

		ToolItem openToolItem = new ToolItem(toolBar, SWT.PUSH);
		openToolItem.setToolTipText(Messages.getString("dialog.CSV2TMXConverterDialog.openToolItem"));
		String openCSVPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_OPEN_CSV_PATH);
		openToolItem.setImage(new Image(Display.getDefault(), openCSVPath));
		openToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		ToolItem exporToolItem = new ToolItem(toolBar, SWT.PUSH);
		exporToolItem.setToolTipText(Messages.getString("dialog.CSV2TMXConverterDialog.exporToolItem"));
		String exportPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_EXPORT_TBX_PATH);
		exporToolItem.setImage(new Image(Display.getDefault(), exportPath));
		exporToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				export();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		ToolItem deleteColToolItem = new ToolItem(toolBar, SWT.PUSH);
		deleteColToolItem.setToolTipText(Messages.getString("dialog.CSV2TMXConverterDialog.deleteColToolItem"));
		String deleteColPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_DELETE_COLUMN_PATH);
		deleteColToolItem.setImage(new Image(Display.getDefault(), deleteColPath));
		deleteColToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				removeColumn();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		ToolItem setColToolItem = new ToolItem(toolBar, SWT.PUSH);
		setColToolItem.setToolTipText(Messages.getString("dialog.CSV2TMXConverterDialog.setColToolItem"));
		String setColPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_SET_COLUMN_PATH);
		setColToolItem.setImage(new Image(Display.getDefault(), setColPath));
		setColToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				selectLanguage();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		new Label(cmpToolBar, SWT.None)
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		ToolBar helpToolBar = new ToolBar(cmpToolBar, SWT.NO_FOCUS | SWT.FLAT);
		ToolItem helpToolItem = new ToolItem(helpToolBar, SWT.RIGHT);
		helpToolItem.setToolTipText(Messages.getString("dialog.CSV2TMXConverterDialog.helpToolBar"));
		String helpPath = PluginUtil.getAbsolutePath(PluginConstants.PIC_HELP_PATH);
		helpToolItem.setImage(new Image(Display.getDefault(), helpPath));
		helpToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				displayHelp();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void displayHelp() {
		String curLang = CommonFunction.getSystemLanguage();
		if (Util.isWindows()) {
			String help = "help" + File.separator + "csvconvdocs" + File.separator + "csvconverter.chm";
			if (curLang.equalsIgnoreCase("zh")) {
				help = "help" + File.separator + "csvconvdocs" + File.separator + "csvconverter_zh-cn.chm";
			}
			Program.launch(PluginUtil.getConfigurationFilePath(help));
		} else {
			String help = "help" + File.separator + "csvconvdocs" + File.separator + "en" + File.separator + "toc.xml";
			if (curLang.equalsIgnoreCase("zh")) {
				help = "help" + File.separator + "csvconvdocs" + File.separator + "zh-cn" + File.separator + "toc.xml";
			}
			PluginHelpDialog dialog = new PluginHelpDialog(getShell(), PluginUtil.getConfigurationFilePath(help),
					Messages.getString("dialog.CSV2TMXConverterDialog.helpDialogTitle"));
			dialog.open();
		}

	}

	/**
	 * 打开 CSV 文件 ;
	 */
	private void openFile() {
		CSVSettingDialog dialog = new CSVSettingDialog(getShell(), false, imagePath, null);
		if (dialog.open() == IDialogConstants.OK_ID) {
			csvPath = dialog.getCsvPath();
			colSeparator = dialog.getColSeparator();
			textDelimiter = dialog.getTextDelimiter();
			encoding = dialog.getEncoding();
			try {
				cols = maxColumns();
				if (cols < 2) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialog.CSV2TMXConverterDialog.msgTitle"),
							Messages.getString("dialog.CSV2TMXConverterDialog.msg1"));
					return;
				}
				rows = 0;
				int width = (table.getClientArea().width - table.getBorderWidth() * 2 - table.getVerticalBar()
						.getSize().x) / cols;
				if (width < 100) {
					width = 100;
				}
				table.removeAll();
				int count = table.getColumnCount();
				for (int i = 0; i < count; i++) {
					table.getColumn(0).dispose();
				}

				for (int i = 0; i < cols; i++) {
					new TableColumn(table, SWT.NONE);
					table.getColumn(i).setWidth(width);
					table.getColumn(i).setText("" + (i + 1)); //$NON-NLS-1$
				}
				fillTable();
				table.layout(true);
				lblRowCount.setText(MessageFormat.format(
						Messages.getString("dialog.CSV2TMXConverterDialog.lblRowCount"), rows));
				lblColCount.setText(MessageFormat.format(
						Messages.getString("dialog.CSV2TMXConverterDialog.lblColCount"), cols));
			} catch (IOException e) {
				LOGGER.error(Messages.getString("dialog.CSV2TMXConverterDialog.logger1"), e);
			} catch (Exception e) {
				LOGGER.error(Messages.getString("dialog.CSV2TMXConverterDialog.logger1"), e);
			}
		}
	}

	/**
	 * 导出为 TMX 文件 ;
	 */
	private void export() {
		if (table.getColumnCount() < 2) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.CSV2TMXConverterDialog.msgTitle"),
					Messages.getString("dialog.CSV2TMXConverterDialog.msg2"));
			return;
		}
		Vector<String> languages = new Vector<String>(cols);
		for (int i = 0; i < cols; i++) {
			languages.add(table.getColumn(i).getText());
		}
		if (!LocaleService.verifyLanguages(languages)) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.CSV2TMXConverterDialog.msgTitle"),
					Messages.getString("dialog.CSV2TMXConverterDialog.msg3"));
			return;
		}

		ExportDialog exportDialog = new ExportDialog(getShell(), csvPath, languages);
		if (exportDialog.open() == IDialogConstants.OK_ID) {
			String exportFile = exportDialog.getFilePath();
			if (exportFile == null) {
				return;
			}
			try {
				File f = new File(exportFile);
				if (!f.exists() || f.isDirectory()) {
					f.createNewFile();
				}
				output = new FileOutputStream(f);
				String version = exportDialog.getTmxVersion();
				writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
				writeString("<tmx version=\"" + version + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
				writeString("<header \n" + //$NON-NLS-1$
						"      creationtool=\"CSVConverter\" \n" + //$NON-NLS-1$
						"      creationtoolversion=\"" + PluginConstants.CSV2TMX_VERSION + "\"  \n" + //$NON-NLS-1$ //$NON-NLS-2$
						"      srclang=\"" + exportDialog.getLang() + "\" \n" + //$NON-NLS-1$ //$NON-NLS-2$
						"      adminlang=\"en\"  \n" + //$NON-NLS-1$
						"      datatype=\"xml\" \n" + //$NON-NLS-1$
						"      o-tmf=\"CSV\" \n" + //$NON-NLS-1$
						"      segtype=\"block\"\n" + //$NON-NLS-1$
						">\n" + //$NON-NLS-1$
						"</header>\n"); //$NON-NLS-1$
				writeString("<body>\n"); //$NON-NLS-1$

				for (int r = 0; r < rows; r++) {
					int count = 0;
					StringBuffer tuBuffer = new StringBuffer("<tu tuid=\"" + createId() + "\">\n");
					TableItem item = table.getItem(r);
					for (int c = 0; c < cols; c++) {
						String string = item.getText(c);
						if (!string.trim().equals("")) { //$NON-NLS-1$
							if (version.equals("1.1") || version.equals("1.2")) { //$NON-NLS-1$ //$NON-NLS-2$
								tuBuffer.append("<tuv lang=\"" + languages.get(c) + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								tuBuffer.append("<tuv xml:lang=\"" + languages.get(c) + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
							}
							tuBuffer.append("<seg>" + TextUtil.cleanString(string.trim()) + "</seg>\n</tuv>\n"); //$NON-NLS-1$ //$NON-NLS-2$
							count++;
						}
					}
					tuBuffer.append("</tu>\n"); //$NON-NLS-1$
					if (count > 0) {
						writeString(tuBuffer.toString());
					}
				}
				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</tmx>\n"); //$NON-NLS-1$
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.CSV2TMXConverterDialog.msgTitle"),
						Messages.getString("dialog.CSV2TMXConverterDialog.msg4"));
			} catch (FileNotFoundException e) {
				LOGGER.error(Messages.getString("dialog.CSV2TMXConverterDialog.logger2"), e);
			} catch (IOException e) {
				LOGGER.error(Messages.getString("dialog.CSV2TMXConverterDialog.logger2"), e);
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException e) {
					LOGGER.error(Messages.getString("dialog.CSV2TMXConverterDialog.logger2"), e);
				}
			}
		}
	}

	/**
	 * 删除列 ;
	 */
	private void removeColumn() {
		if (cols < 2) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.CSV2TMXConverterDialog.msgTitle"),
					Messages.getString("dialog.CSV2TMXConverterDialog.msg2"));
			return;
		}
		Vector<String> columns = new Vector<String>();
		int count = table.getColumnCount();
		for (int i = 0; i < count; i++) {
			columns.add(table.getColumn(i).getText());
		}
		ColumnRemoveDialog removeDialog = new ColumnRemoveDialog(getShell(), columns, imagePath);
		if (removeDialog.open() == IDialogConstants.OK_ID) {
			Vector<String> columnVector = removeDialog.getColumnVector();
			if (columnVector.size() == columns.size()) {
				return;
			}
			for (int i = 0; i < count; i++) {
				TableColumn col = table.getColumn(i);
				boolean found = false;
				for (int j = 0; j < columnVector.size(); j++) {
					if (col.getText().equals(columnVector.get(j))) {
						found = true;
						break;
					}
				}
				if (!found) {
					table.getColumn(i).dispose();
					count--;
					i--;
				}
			}
			cols = table.getColumnCount();
			lblColCount.setText(MessageFormat.format(
					Messages.getString("dialog.CSV2TMXConverterDialog.lblColCount"), cols)); //$NON-NLS-1$
			int width = (table.getClientArea().width - table.getBorderWidth() * 2 - table.getVerticalBar().getSize().x)
					/ cols;
			if (width < 100) {
				width = 100;
			}

			for (int i = 0; i < cols; i++) {
				TableColumn column = table.getColumn(i);
				column.setWidth(width);
			}
		}
	}

	/**
	 * 选择语言 ;
	 */
	private void selectLanguage() {
		if (cols < 2) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.CSV2TMXConverterDialog.msgTitle"),
					Messages.getString("dialog.CSV2TMXConverterDialog.msg4"));
			return;
		}
		Vector<String> languages = new Vector<String>(cols);
		for (int i = 0; i < cols; i++) {
			languages.add(table.getColumn(i).getText());
		}

		LanguageSettingDialog langDialog = new LanguageSettingDialog(getShell(), languages);
		if (langDialog.open() == IDialogConstants.OK_ID) {
			languages = langDialog.getResultLang();
			for (int i = 0; i < cols; i++) {
				table.getColumn(i).setText(languages.get(i));
			}
		}
	}

	/**
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void fillTable() throws UnsupportedEncodingException, IOException {
		InputStreamReader input = new InputStreamReader(new FileInputStream(csvPath), encoding);
		BufferedReader buffer = new BufferedReader(input);
		String line;
		while ((line = buffer.readLine()) != null) {
			createItems(line);
		}
	}

	private String createId() {
		Date now = new Date();
		long lng = now.getTime();
		// wait until we are in the next millisecond
		// before leaving to ensure uniqueness
		Date next = new Date();
		while (next.getTime() == lng) {
			next = null;
			next = new Date();
		}
		return "" + lng; //$NON-NLS-1$
	}

	/**
	 * @param line
	 */
	private void createItems(String line) {
		int size = line.length();
		if (size == 0) {
			return;
		}
		if (countColumns(line) != cols) {
			return;
		}
		StringBuffer buffer = new StringBuffer();
		Vector<String> vector = new Vector<String>();
		String[] strings = new String[cols];
		boolean inDelimiters = false;
		for (int i = 0; i < size; i++) {
			String c = "" + line.charAt(i); //$NON-NLS-1$
			if (c.equals(textDelimiter)) {
				inDelimiters = !inDelimiters;
				continue;
			}
			if (!inDelimiters && c.equals(colSeparator)) {
				vector.add(buffer.toString());
				buffer = null;
				buffer = new StringBuffer();
				continue;
			}
			buffer.append(c);
		}
		if (!buffer.toString().equals("")) { //$NON-NLS-1$
			vector.add(buffer.toString());
		}
		for (int i = 0; i < vector.size(); i++) {
			strings[i] = vector.get(i);
		}
		for (int i = vector.size(); i < cols; i++) {
			strings[i] = ""; //$NON-NLS-1$
		}

		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(strings);
		rows++;
	}

	private int maxColumns() throws IOException {
		int max = 0;
		InputStreamReader input = new InputStreamReader(new FileInputStream(csvPath), encoding);
		BufferedReader buffer = new BufferedReader(input);
		Hashtable<String, Integer> table1 = new Hashtable<String, Integer>();
		String line = buffer.readLine();
		while (line != null) {
			int i = countColumns(line);
			if (table1.containsKey("" + i)) { //$NON-NLS-1$
				int count = table1.get("" + i).intValue() + 1; //$NON-NLS-1$
				table1.put("" + i, new Integer(count)); //$NON-NLS-1$
			} else {
				table1.put("" + i, new Integer(1)); //$NON-NLS-1$
			}
			line = buffer.readLine();
		}
		Enumeration<String> e = table1.keys();
		String key = ""; //$NON-NLS-1$
		while (e.hasMoreElements()) {
			String s = e.nextElement();
			int value = table1.get(s).intValue();
			if (value > max) {
				max = value;
				key = s;
			}
		}
		return Integer.parseInt(key);
	}

	private int countColumns(String line) {
		int size = line.length();
		if (size == 0) {
			return 0;
		}
		int count = 1;
		boolean inDelimiters = false;
		for (int i = 0; i < size; i++) {
			String c = "" + line.charAt(i); //$NON-NLS-1$
			if (c.equals(textDelimiter)) {
				inDelimiters = !inDelimiters;
			}
			if (!inDelimiters && c.equals(colSeparator)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * @param string
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void writeString(String string) throws UnsupportedEncodingException, IOException {
		output.write(string.getBytes("UTF-8")); //$NON-NLS-1$
	}

	/**
	 * 导出为 TMX 文件对话框
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class ExportDialog extends Dialog {

		/** 导出文件路径 */
		private String filePath;

		/** 语言集合 */
		private Vector<String> languages;

		/** 路径文本框 */
		private Text txtFile;

		/** 源语言下拉框 */
		private Combo cmbLang;

		/** TMX 版本下拉框 */
		private Combo cmbTMXVersion;

		/** 源语言 */
		private String lang;

		/** TMX 版本 */
		private String tmxVersion;

		protected ExportDialog(Shell parentShell, String filePath, Vector<String> languages) {
			super(parentShell);
			this.filePath = filePath;
			this.languages = languages;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.title"));
			newShell.setImage(new Image(Display.getDefault(), imagePath));
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite tparent = (Composite) super.createDialogArea(parent);
			tparent.setLayout(new GridLayout(3, false));
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(350, 150).grab(true, true).applyTo(tparent);

			new Label(tparent, SWT.None).setText(Messages
					.getString("dialog.CSV2TMXConverterDialog.ExportDialog.lblTMX"));
			txtFile = new Text(tparent, SWT.BORDER);
			txtFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			txtFile.setEditable(false);
			if (filePath != null) {
				if (filePath.indexOf(".") != -1) {
					txtFile.setText(filePath.substring(0, filePath.lastIndexOf(".")) + ".tmx");
				} else {
					txtFile.setText(filePath + ".tmx");
				}
				filePath = txtFile.getText();
			}
			Button btnBrowse = new Button(tparent, SWT.None);
			btnBrowse.setText(Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.btnBrowse"));
			btnBrowse.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent arg0) {
					FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
					dialog.setText(Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.dialogTitle"));
					String extensions[] = { "*.tmx", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
					String names[] = {
							Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.filterName1"), Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.filterName2") }; //$NON-NLS-1$ //$NON-NLS-2$
					dialog.setFilterNames(names);
					dialog.setFilterExtensions(extensions);
					String fileSep = System.getProperty("file.separator");
					if (txtFile.getText() != null && !txtFile.getText().trim().equals("")) {
						dialog.setFilterPath(txtFile.getText().substring(0, txtFile.getText().lastIndexOf(fileSep)));
						dialog.setFileName(txtFile.getText().substring(txtFile.getText().lastIndexOf(fileSep) + 1));
					} else {
						dialog.setFilterPath(System.getProperty("user.home"));
					}
					filePath = dialog.open();
					if (filePath != null) {
						txtFile.setText(filePath);
					}
				}

				public void widgetDefaultSelected(SelectionEvent arg0) {

				}
			});

			Group optionsGrp = new Group(tparent, SWT.None);
			optionsGrp.setText(Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.optionsGrp"));
			GridDataFactory.fillDefaults().span(3, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(optionsGrp);
			optionsGrp.setLayout(new GridLayout(2, false));
			createLabel(optionsGrp, Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.src"));
			cmbLang = new Combo(optionsGrp, SWT.READ_ONLY);
			cmbLang.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			String langList[] = new String[languages.size() + 1];
			langList[0] = "*all*";
			for (int i = 0; i < languages.size(); i++) {
				langList[i + 1] = languages.get(i);
			}
			cmbLang.setItems(langList);
			cmbLang.select(0);

			createLabel(optionsGrp, Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.versionTMX"));
			cmbTMXVersion = new Combo(optionsGrp, SWT.READ_ONLY);
			cmbTMXVersion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			String levels[] = { "TMX 1.4", "TMX 1.3", "TMX 1.2", "TMX 1.1" };
			cmbTMXVersion.setItems(levels);
			cmbTMXVersion.select(0);

			return tparent;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			getButton(IDialogConstants.OK_ID).setText(
					Messages.getString("dialog.CSV2TMXConverterDialog.ExportDialog.ok"));
		}

		@Override
		protected void okPressed() {
			setLang(cmbLang.getText());
			String version = "";
			if (cmbTMXVersion.getSelectionIndex() == 0) {
				version = "1.4";
			} else if (cmbTMXVersion.getSelectionIndex() == 1) {
				version = "1.3";
			} else if (cmbTMXVersion.getSelectionIndex() == 2) {
				version = "1.2";
			} else if (cmbTMXVersion.getSelectionIndex() == 3) {
				version = "1.1";
			}
			setTmxVersion(version);
			close();
		}

		public String getLang() {
			return lang;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

		public String getTmxVersion() {
			return tmxVersion;
		}

		public void setTmxVersion(String tmxVersion) {
			this.tmxVersion = tmxVersion;
		}

		/**
		 * 获得导出路径
		 * @return ;
		 */
		public String getFilePath() {
			return filePath;
		}
	}

	/**
	 * 创建 Label，文本右对齐
	 * @param parent
	 *            父控件
	 * @param text
	 *            Label 上显示的文本
	 */
	private void createLabel(Composite parent, String text) {
		Label lbl = new Label(parent, SWT.None);
		lbl.setText(text);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(lbl);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Composite cmpTemp = parent.getParent();
		parent.dispose();
		cmpTemp.layout();
	}

	/**
	 * 选择语言对话框
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class LanguageSettingDialog extends Dialog {

		/** 语言集合 */
		private Vector<String> languages;

		/** 列数 */
		private int size;

		/** 所设置的语言 */
		private Vector<String> resultLang;

		/** 语言下拉框集合 */
		private Combo[] arrCmbLangs;

		/**
		 * 构造方法
		 * @param parentShell
		 * @param languages
		 *            语言集合
		 */
		protected LanguageSettingDialog(Shell parentShell, Vector<String> languages) {
			super(parentShell);
			this.languages = languages;
			size = languages.size();
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.getString("dialog.CSV2TMXConverterDialog.LanguageSettingDialog.title"));
			newShell.setImage(new Image(Display.getDefault(), imagePath));
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite tparent = (Composite) super.createDialogArea(parent);
			tparent.setLayout(new GridLayout());
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(300, 250).grab(true, true).applyTo(tparent);

			ScrolledComposite cmpScrolled = new ScrolledComposite(tparent, SWT.V_SCROLL);
			cmpScrolled.setAlwaysShowScrollBars(false);
			cmpScrolled.setLayoutData(new GridData(GridData.FILL_BOTH));
			cmpScrolled.setExpandHorizontal(true);
			cmpScrolled.setShowFocusedControl(true);

			Composite cmpContent = new Composite(cmpScrolled, SWT.None);
			cmpScrolled.setContent(cmpContent);
			cmpContent.setLayout(new GridLayout(2, false));
			cmpContent.setLayoutData(new GridData(GridData.FILL_BOTH));

			arrCmbLangs = new Combo[size];
			for (int i = 0; i < size; i++) {
				createLabel(cmpContent, languages.get(i) + " : ");
				arrCmbLangs[i] = new Combo(cmpContent, SWT.READ_ONLY);
				arrCmbLangs[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				arrCmbLangs[i].setItems(LocaleService.getLanguages());
				String name = LocaleService.getLanguage(languages.get(i));
				if (!name.equals(languages.get(i))) {
					arrCmbLangs[i].setText(name);
				}
			}

			cmpContent.setSize(cmpContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			return tparent;
		}

		@Override
		protected void okPressed() {
			resultLang = new Vector<String>();
			for (int i = 0; i < size; i++) {
				String string = arrCmbLangs[i].getText();
				if (string.equals("")) { //$NON-NLS-1$
					resultLang.add(languages.get(i));
				} else {
					resultLang.add(LocaleService.getLanguageCodeByLanguage(string));
				}
			}
			close();
		}

		public Vector<String> getResultLang() {
			return resultLang;
		}

	}
}
