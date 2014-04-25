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

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.ui.plugin.Activator;
import net.heartsome.cat.ts.ui.plugin.ColProperties;
import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.PluginUtil;
import net.heartsome.cat.ts.ui.plugin.util.TBXTemplateUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TBXMaker 对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TBXMakerDialog extends Dialog {

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

	/** CSV 文件的主语言 */
	private String lang;

	/** CSV 文件的 XCS 模板 */
	private String xcsTemplate;

	/** CSV 文件的列数 */
	private int cols;

	/** CSV 文件的行数 */
	private int rows;

	/** 导出 CSV 文件时所用到的流对象 */
	private FileOutputStream output;

	/** XCS 模板 */
	private TBXTemplateUtil template;

	/**
	 * 构造方法
	 * @param parentShell
	 */
	public TBXMakerDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.TBXMakerDialog.title"));
		imagePath = PluginUtil.getAbsolutePath(PluginConstants.LOGO_TBXMAKER_PATH);
		newShell.setImage(Activator.getImageDescriptor(PluginConstants.LOGO_TBXMAKER_PATH).createImage());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		// tparent.setLayout(new GridLayout());
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
		lblRowCount.setText(MessageFormat.format(Messages.getString("dialog.TBXMakerDialog.lblRowCount"), 0));
		lblRowCount.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		lblColCount = new Label(cmpStatus, SWT.None);
		lblColCount.setText(MessageFormat.format(Messages.getString("dialog.TBXMakerDialog.lblColCount"), 0));
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
		fileItem.setText(Messages.getString("dialog.TBXMakerDialog.fileMenu"));
		fileItem.setMenu(fileMenu);

		MenuItem openCSVItem = new MenuItem(fileMenu, SWT.PUSH);
		openCSVItem.setText(Messages.getString("dialog.TBXMakerDialog.openCSVItem"));
		openCSVItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_OPEN_CSV_PATH).createImage());
		openCSVItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		MenuItem exportTBXItem = new MenuItem(fileMenu, SWT.PUSH);
		exportTBXItem.setText(Messages.getString("dialog.TBXMakerDialog.exportTBXItem"));
		exportTBXItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_EXPORT_TBX_PATH).createImage());
		exportTBXItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				export();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText(Messages.getString("dialog.TBXMakerDialog.exitItem"));
		exitItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Menu taskMenu = new Menu(menu);
		MenuItem taskItem = new MenuItem(menu, SWT.CASCADE);
		taskItem.setText(Messages.getString("dialog.TBXMakerDialog.taskMenu"));
		taskItem.setMenu(taskMenu);

		MenuItem deleteColItem = new MenuItem(taskMenu, SWT.PUSH);
		deleteColItem.setText(Messages.getString("dialog.TBXMakerDialog.deleteColItem"));
		deleteColItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_DELETE_COLUMN_PATH).createImage());
		deleteColItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				removeColumn();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		MenuItem colPropertyItem = new MenuItem(taskMenu, SWT.PUSH);
		colPropertyItem.setText(Messages.getString("dialog.TBXMakerDialog.colPropertyItem"));
		colPropertyItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_SET_COLUMN_PATH).createImage());
		colPropertyItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				setColumnType();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Menu helpMenu = new Menu(menu);
		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
		helpItem.setText(Messages.getString("dialog.TBXMakerDialog.helpMenu"));
		helpItem.setMenu(helpMenu);

		MenuItem helpContentItem = new MenuItem(helpMenu, SWT.PUSH);
		helpContentItem.setText(Messages.getString("dialog.TBXMakerDialog.helpContentItem"));
		helpContentItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_HELP_PATH).createImage());
		helpContentItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				displayHelp();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
		aboutItem.setText(Messages.getString("dialog.TBXMakerDialog.aboutItem"));
		aboutItem.setImage(Activator.getImageDescriptor(PluginConstants.LOGO_TBXMAKER_MENU_PATH).createImage());
		aboutItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(getShell(), Messages
						.getString("dialog.TBXMakerDialog.aboutItemName"), imagePath, Messages
						.getString("dialog.TBXMakerDialog.aboutItemVersion"));
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
		openToolItem.setToolTipText(Messages.getString("dialog.TBXMakerDialog.openToolItem"));
		openToolItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_OPEN_CSV_PATH).createImage());
		openToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				openFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		ToolItem exporToolItem = new ToolItem(toolBar, SWT.PUSH);
		exporToolItem.setToolTipText(Messages.getString("dialog.TBXMakerDialog.exporToolItem"));
		exporToolItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_EXPORT_TBX_PATH).createImage());
		exporToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				export();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		ToolItem deleteColToolItem = new ToolItem(toolBar, SWT.PUSH);
		deleteColToolItem.setToolTipText(Messages.getString("dialog.TBXMakerDialog.deleteColToolItem"));
		deleteColToolItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_DELETE_COLUMN_PATH).createImage());
		deleteColToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				removeColumn();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		ToolItem setColToolItem = new ToolItem(toolBar, SWT.PUSH);
		setColToolItem.setToolTipText(Messages.getString("dialog.TBXMakerDialog.setColToolItem"));
		setColToolItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_SET_COLUMN_PATH).createImage());
		setColToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				setColumnType();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		new Label(cmpToolBar, SWT.None)
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		ToolBar helpToolBar = new ToolBar(cmpToolBar, SWT.NO_FOCUS | SWT.FLAT);
		ToolItem helpToolItem = new ToolItem(helpToolBar, SWT.RIGHT);
		helpToolItem.setToolTipText(Messages.getString("dialog.TBXMakerDialog.helpToolItem"));
		helpToolItem.setImage(Activator.getImageDescriptor(PluginConstants.PIC_HELP_PATH).createImage());
		helpToolItem.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				displayHelp();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Composite cmpTemp = parent.getParent();
		parent.dispose();
		cmpTemp.layout();
	}

	private void displayHelp() {
		String curLang = CommonFunction.getSystemLanguage();
		StringBuffer sbHelp = new StringBuffer("help");
		if (Util.isWindows()) {
			sbHelp.append(File.separator).append("csv2tbxdoc").append(File.separator);
			if (curLang.equalsIgnoreCase("zh")) {
				sbHelp.append("tbxmaker_zh-cn.chm");
			} else {
				sbHelp.append("tbxmaker.chm");
			}
			Program.launch(PluginUtil.getConfigurationFilePath(sbHelp.toString()));
		} else {
			sbHelp.append(File.separator).append("csv2tbxdoc").append(File.separator);
			if (curLang.equalsIgnoreCase("zh")) {
				sbHelp.append("zh-cn");
			} else {
				sbHelp.append("en");
			}
			sbHelp.append(File.separator).append("toc.xml");
			PluginHelpDialog dialog = new PluginHelpDialog(getShell(), PluginUtil.getConfigurationFilePath(sbHelp.toString()),
					Messages.getString("dialog.TBXMakerDialog.helpDialogTitle"));
			dialog.open();
		}

	}

	/**
	 * 打开 CSV 文件 ;
	 */
	private void openFile() {
		String[] templates = TBXTemplateUtil.getTemplateFiles(PluginUtil.getCataloguePath(),
				PluginUtil.getTemplatePath());
		CSVSettingDialog dialog = new CSVSettingDialog(getShell(), true, imagePath, templates);
		if (dialog.open() == IDialogConstants.OK_ID) {
			csvPath = dialog.getCsvPath();
			colSeparator = dialog.getColSeparator();
			textDelimiter = dialog.getTextDelimiter();
			encoding = dialog.getEncoding();
			lang = dialog.getLang();
			xcsTemplate = dialog.getXcsTemplate();
			try {
				template = new TBXTemplateUtil(xcsTemplate, PluginUtil.getTemplatePath(), "");
				cols = maxColumns();
				if (cols < 2) {
					MessageDialog.openInformation(getShell(), Messages.getString("dialog.TBXMakerDialog.msgTitle"),
							Messages.getString("dialog.TBXMakerDialog.msg1"));
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
					TableColumn col = new TableColumn(table, SWT.NONE);
					col.setData(new ColProperties("" + (i + 1))); //$NON-NLS-1$
					table.getColumn(i).setWidth(width);
					table.getColumn(i).setText("" + (i + 1)); //$NON-NLS-1$
				}
				fillTable();
				table.layout(true);
				lblRowCount
						.setText(MessageFormat.format(Messages.getString("dialog.TBXMakerDialog.lblRowCount"), rows));
				lblColCount
						.setText(MessageFormat.format(Messages.getString("dialog.TBXMakerDialog.lblColCount"), cols));
			} catch (IOException e) {
				LOGGER.error(Messages.getString("dialog.TBXMakerDialog.logger1"), e);
			} catch (Exception e) {
				LOGGER.error(Messages.getString("dialog.TBXMakerDialog.logger1"), e);
			}
		}
	}

	/**
	 * 导出文件 ;
	 */
	private void export() {
		if (cols < 2) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TBXMakerDialog.msgTitle"),
					Messages.getString("dialog.TBXMakerDialog.msg2"));
			return;
		}
		String[] langs = getUserLangs();
		if (langs.length < 1) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TBXMakerDialog.msgTitle"),
					Messages.getString("dialog.TBXMakerDialog.msg3"));
			return;
		}

		ExportDialog exportDialog = new ExportDialog(getShell(), csvPath);
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
				rows = table.getItemCount();
				writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
				writeString("<!DOCTYPE martif PUBLIC \"ISO 12200:1999A//DTD MARTIF core (DXFcdV04)//EN\" \"TBXcdv04.dtd\">\n"); //$NON-NLS-1$
				writeString("<martif type=\"TBX\" xml:lang=\"" + lang + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
				writeString("<martifHeader><fileDesc><sourceDesc><p>CSV to TBX Converter</p></sourceDesc></fileDesc>\n"); //$NON-NLS-1$
				writeString("<encodingDesc><p type=\"DCSName\">" + template.getTemplateFileName() + "</p></encodingDesc>\n"); //$NON-NLS-1$ //$NON-NLS-2$
				writeString("</martifHeader>\n<text>\n<body>\n"); //$NON-NLS-1$

				for (int r = 0; r < rows; r++) {
					TableItem item = table.getItem(r);
					if (checkConcept(langs, item)) {
						writeString("<termEntry id=\"" + createId() + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
						writeConceptLevelProps(item);
						for (int l = 0; l < langs.length; l++) {
							if (checkTerm(langs[l], item)) {
								writeString("<langSet id=\"" + createId() + "\" xml:lang=\"" + langs[l] + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								writeString("<tig>\n"); //$NON-NLS-1$
								writeLangLevel(langs[l], item);
								writeString("</tig>\n"); //$NON-NLS-1$
								writeString("</langSet>\n"); //$NON-NLS-1$
							}
						}
						writeString("</termEntry>\n"); //$NON-NLS-1$
					}
				}
				writeString("</body>\n"); //$NON-NLS-1$
				writeString("</text>\n"); //$NON-NLS-1$
				writeString("</martif>"); //$NON-NLS-1$
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.TBXMakerDialog.msgTitle"),
						Messages.getString("dialog.TBXMakerDialog.msg4"));
			} catch (FileNotFoundException e) {
				LOGGER.error(Messages.getString("dialog.TBXMakerDialog.logger2"), e);
			} catch (IOException e) {
				LOGGER.error(Messages.getString("dialog.TBXMakerDialog.logger2"), e);
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException e) {
					LOGGER.error(Messages.getString("dialog.TBXMakerDialog.logger2"), e);
				}
			}

		}
	}

	/**
	 * 删除列 ;
	 */
	private void removeColumn() {
		if (cols < 2) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TBXMakerDialog.msgTitle"),
					Messages.getString("dialog.TBXMakerDialog.msg2"));
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
			lblColCount.setText(MessageFormat.format(
					Messages.getString("dialog.TBXMakerDialog.lblColCount"), table.getColumnCount())); //$NON-NLS-1$
			cols = table.getColumnCount();
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
	 * 设置列属性 ;
	 */
	private void setColumnType() {
		if (cols < 2) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TBXMakerDialog.msgTitle"),
					Messages.getString("dialog.TBXMakerDialog.msg2"));
			return;
		}
		Vector<ColProperties> colTypes = new Vector<ColProperties>(cols);
		for (int i = 0; i < cols; i++) {
			colTypes.add((ColProperties) table.getColumn(i).getData());
		}

		ColumnTypeDialog selector = new ColumnTypeDialog(getShell(), colTypes, template, imagePath);
		if (selector.open() == IDialogConstants.OK_ID) {
			for (int i = 0; i < cols; i++) {
				TableColumn col = table.getColumn(i);
				ColProperties type = colTypes.get(i);
				if (!type.getColName().equals("") && !type.getLanguage().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
					col.setText(type.getColName());
				}
			}
		}
	}

	private void writeConceptLevelProps(TableItem item) throws IOException {
		// Write concept level descrips
		for (int c = 0; c < cols; c++) {
			ColProperties properties = (ColProperties) table.getColumn(c).getData();
			if (properties.getLevel().equals(ColProperties.conceptLevel)
					&& properties.getPropName().equals(ColProperties.descripName)) {
				String descrip = item.getText(c);
				if (!descrip.trim().equals("")) { //$NON-NLS-1$
					writeString("<descrip type=\"" + properties.propType + "\">" + TextUtil.cleanString(descrip).trim() + "</descrip>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		for (int c = 0; c < cols; c++) {
			ColProperties properties = (ColProperties) table.getColumn(c).getData();
			if (properties.getLevel().equals(ColProperties.conceptLevel)
					&& properties.getPropName().equals(ColProperties.noteName)) {
				String note = item.getText(c);
				if (!note.trim().equals("")) { //$NON-NLS-1$
					writeString("<note>" + TextUtil.cleanString(note).trim() + "</note>\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	private void writeLangLevelProps(String lang, TableItem item) throws IOException {
		// Write concept level descrips
		for (int c = 0; c < cols; c++) {
			ColProperties properties = (ColProperties) table.getColumn(c).getData();
			if (properties.getLevel().equals(ColProperties.langLevel)
					&& properties.getPropName().equals(ColProperties.termNoteName)
					&& properties.getLanguage().equals(lang)) {

				String termNote = item.getText(c);
				if (!termNote.trim().equals("")) { //$NON-NLS-1$
					writeString("<termNote type=\"" + properties.propType + "\">" + TextUtil.cleanString(termNote).trim() + "</termNote>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		for (int c = 0; c < cols; c++) {
			ColProperties properties = (ColProperties) table.getColumn(c).getData();
			if (properties.getLevel().equals(ColProperties.langLevel)
					&& properties.getPropName().equals(ColProperties.descripName)
					&& properties.getLanguage().equals(lang)) {

				String termNote = item.getText(c);
				if (!termNote.trim().equals("")) { //$NON-NLS-1$
					writeString("<descrip type=\"" + properties.propType + "\">" + TextUtil.cleanString(termNote).trim() + "</descrip>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}

	}

	private void writeLangLevel(String lang, TableItem item) throws IOException {
		// Write Term data
		for (int c = 0; c < cols; c++) {
			ColProperties properties = (ColProperties) table.getColumn(c).getData();
			if (properties.getLevel().equals(ColProperties.langLevel)
					&& properties.getPropName().equals(ColProperties.termName) && properties.getLanguage().equals(lang)) {

				String term = item.getText(c);
				writeString("<term>" + TextUtil.cleanString(term).trim() + "</term>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		writeLangLevelProps(lang, item);
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
		return "_" + lng; //$NON-NLS-1$
	}

	private boolean checkConcept(String[] langs, TableItem item) {
		for (int l = 0; l < langs.length; l++) {
			if (checkTerm(langs[l], item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method check if the term contains valid text
	 * @param lang
	 * @param item
	 * @return
	 */
	private boolean checkTerm(String lang, TableItem item) {
		// Write Term data
		for (int c = 0; c < cols; c++) {
			ColProperties properties = (ColProperties) table.getColumn(c).getData();
			if (properties.getLevel().equals(ColProperties.langLevel)
					&& properties.getPropName().equals(ColProperties.termName) && properties.getLanguage().equals(lang)) {
				String term = item.getText(c);
				return !term.trim().equals(""); //$NON-NLS-1$
			}
		}
		return false;
	}

	private String[] getUserLangs() {
		Hashtable<String, String> langTable = new Hashtable<String, String>();
		for (int c = 0; c < cols; c++) {
			ColProperties properties = (ColProperties) table.getColumn(c).getData();
			if (properties.level.equals(ColProperties.langLevel)) {
				langTable.put(properties.getLanguage(), ""); //$NON-NLS-1$
			}
		}

		String[] result = new String[langTable.size()];
		Enumeration<String> keys = langTable.keys();
		int index = 0;
		while (keys.hasMoreElements()) {
			result[index] = keys.nextElement();
			index++;
		}
		return result;
	}

	private void writeString(String string) throws UnsupportedEncodingException, IOException {
		output.write(string.getBytes("UTF-8"));
	}

	private int maxColumns() throws IOException {
		int max = 0;
		InputStreamReader input = new InputStreamReader(new FileInputStream(csvPath), encoding);
		BufferedReader buffer = new BufferedReader(input);
		Hashtable<String, Integer> table1 = new Hashtable<String, Integer>();
		String line = buffer.readLine();
		while (line != null) {
			int i = countColumns(line);
			if (table1.containsKey("" + i)) {
				int count = table1.get("" + i).intValue() + 1;
				table1.put("" + i, new Integer(count));
			} else {
				table1.put("" + i, new Integer(1));
			}
			line = buffer.readLine();
		}
		Enumeration<String> e = table1.keys();
		String key = "";
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
			String c = "" + line.charAt(i);
			if (c.equals(textDelimiter)) {
				inDelimiters = !inDelimiters;
			}
			if (!inDelimiters && c.equals(colSeparator)) {
				count++;
			}
		}
		return count;
	}

	private void fillTable() throws UnsupportedEncodingException, IOException {
		InputStreamReader input = new InputStreamReader(new FileInputStream(csvPath), encoding);
		BufferedReader buffer = new BufferedReader(input);
		String line;
		while ((line = buffer.readLine()) != null) {
			createItems(line);
		}
	}

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

	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * 导出为 TBX 文件的对话框
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class ExportDialog extends Dialog {

		/** 导出文件路径 */
		private String filePath;

		/** 路径文本框 */
		private Text txtFile;

		/**
		 * 构造方法
		 * @param parentShell
		 * @param filePath
		 *            默认导出路径
		 */
		protected ExportDialog(Shell parentShell, String filePath) {
			super(parentShell);
			this.filePath = filePath;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.getString("dialog.TBXMakerDialog.ExportDialog.title"));
			newShell.setImage(new Image(Display.getDefault(), imagePath));
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite tparent = (Composite) super.createDialogArea(parent);
			tparent.setLayout(new GridLayout(3, false));
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(400, 50).grab(true, true).applyTo(tparent);

			new Label(tparent, SWT.None).setText(Messages.getString("dialog.TBXMakerDialog.ExportDialog.lblTBX"));
			txtFile = new Text(tparent, SWT.BORDER);
			txtFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			txtFile.setEditable(false);
			if (filePath != null) {
				if (filePath.indexOf(".") != -1) {
					txtFile.setText(filePath.substring(0, filePath.lastIndexOf(".")) + ".tbx");
				} else {
					txtFile.setText(filePath + ".tbx");
				}
				filePath = txtFile.getText();
			}
			Button btnBrowse = new Button(tparent, SWT.None);
			btnBrowse.setText(Messages.getString("dialog.TBXMakerDialog.ExportDialog.btnBrowse"));
			btnBrowse.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent arg0) {
					FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
					dialog.setText(Messages.getString("dialog.TBXMakerDialog.ExportDialog.dialogTitle"));
					String extensions[] = { "*.tbx", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
					String names[] = {
							Messages.getString("dialog.TBXMakerDialog.ExportDialog.names1"), Messages.getString("dialog.TBXMakerDialog.ExportDialog.names2") }; //$NON-NLS-1$ //$NON-NLS-2$
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
			return tparent;
		}

		/**
		 * 获得导出路径
		 * @return ;
		 */
		public String getFilePath() {
			return filePath;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			getButton(IDialogConstants.OK_ID).setText(Messages.getString("dialog.TBXMakerDialog.ok"));
		}
	}
}
