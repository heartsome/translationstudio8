package net.heartsome.cat.ts.ui.plugin.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.ui.composite.LanguageLabelProvider;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 打开 CSV 文件对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class CSVSettingDialog extends Dialog {

	/** 列分隔符集合 */
	private String[] arrColSeparator = { "", ",", ";", ":", "|", "Tab" };

	/** 文本定界符集合 */
	private String[] arrTextDelimiter = { "", "\"", "'" };

	private boolean isTBXConverter;

	/** Logo 图片路径 */
	private String imgPath;

	/** XCS 模板文件名集合 */
	private String[] xcsTemplates;

	/** CSV 文本框 */
	private Text txtCSV;

	/** 浏览按钮 */
	private Button btnBrowse;

	/** 列分隔符下拉框，可编辑 */
	private Combo cmbColSeparator;

	/** 文本定界符下拉框，可编辑 */
	private Combo cmbTextDelimiter;

	/** 字符集下拉框 */
	private Combo cmbEncoding;

	/** 主语言下拉框 */
	private TableComboViewer cmbLang;

	/** XCS 模板下拉框 */
	private Combo cmbXCSTemplate;

	/** CSV 路径 */
	private String csvPath;

	/** 列分隔符 */
	private String colSeparator;

	/** 文本定界符 */
	private String textDelimiter;

	/** 字符集下拉框 */
	private String encoding;

	/** 主语言 */
	private String lang;

	/** XCS 模板 */
	private String xcsTemplate;

	/**
	 * 构造方法
	 * @param parentShell
	 * @param isTBXConverter
	 *            是否是 TBX 转换器
	 * @param imgPath
	 *            Logo 图片路径
	 * @param xcsTemplates
	 *            xcs 模板集合
	 */
	protected CSVSettingDialog(Shell parentShell, boolean isTBXConverter, String imgPath, String[] xcsTemplates) {
		super(parentShell);
		this.isTBXConverter = isTBXConverter;
		this.imgPath = imgPath;
		this.xcsTemplates = xcsTemplates;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.CSVSettingDialog.title"));
		newShell.setImage(new Image(Display.getDefault(), imgPath));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(tparent);
		int height = 160;
		if (isTBXConverter) {
			height = 230;
		}
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(320, height).grab(true, true).applyTo(tparent);

		Composite cmpSelFile = new Composite(tparent, SWT.None);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).extendedMargins(0, 0, 0, 0)
				.applyTo(cmpSelFile);
		GridDataFactory.fillDefaults().applyTo(cmpSelFile);

		new Label(cmpSelFile, SWT.None).setText(Messages.getString("dialog.CSVSettingDialog.lblFile"));
		txtCSV = new Text(cmpSelFile, SWT.BORDER);
		txtCSV.setEditable(false);
		txtCSV.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnBrowse = new Button(cmpSelFile, SWT.None);
		btnBrowse.setText(Messages.getString("dialog.CSVSettingDialog.btnBrowse"));
		btnBrowse.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent arg0) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.getString("dialog.CSVSettingDialog.dialogTitle"));
				String[] extensions = { "*.csv", "*.txt", "*" };
				String[] filters = { Messages.getString("dialog.CSVSettingDialog.filters1"),
						Messages.getString("dialog.CSVSettingDialog.filters2"),
						Messages.getString("dialog.CSVSettingDialog.filters3") };
				dialog.setFilterExtensions(extensions);
				dialog.setFilterNames(filters);
				String fileSep = System.getProperty("file.separator");
				if (txtCSV.getText() != null && !txtCSV.getText().trim().equals("")) {
					dialog.setFilterPath(txtCSV.getText().substring(0, txtCSV.getText().lastIndexOf(fileSep)));
					dialog.setFileName(txtCSV.getText().substring(txtCSV.getText().lastIndexOf(fileSep) + 1));
				} else {
					dialog.setFilterPath(System.getProperty("user.home"));
				}
				String name = dialog.open();
				if (name != null) {
					txtCSV.setText(name);
				}
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		Composite cmpContent = new Composite(tparent, SWT.NONE);
		cmpContent.setLayout(new GridLayout(2, false));
		cmpContent.setLayoutData(new GridData(GridData.FILL_BOTH));

		createLabel(cmpContent, Messages.getString("dialog.CSVSettingDialog.cmbColSeparator"));
		cmbColSeparator = new Combo(cmpContent, SWT.NONE);
		cmbColSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbColSeparator.setItems(arrColSeparator);
		cmbColSeparator.select(1);

		createLabel(cmpContent, Messages.getString("dialog.CSVSettingDialog.cmbTextDelimiter"));
		cmbTextDelimiter = new Combo(cmpContent, SWT.NONE);
		cmbTextDelimiter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbTextDelimiter.setItems(arrTextDelimiter);
		cmbTextDelimiter.setText("\"");
		cmbTextDelimiter.setTextLimit(1);

		createLabel(cmpContent, Messages.getString("dialog.CSVSettingDialog.cmbEncoding"));
		cmbEncoding = new Combo(cmpContent, SWT.READ_ONLY);
		cmbEncoding.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String[] arrEncoding = LocaleService.getPageCodes();
		cmbEncoding.setItems(arrEncoding);
		cmbEncoding.select(indexOf(arrEncoding, "UTF-8"));

		if (isTBXConverter) {
			createLabel(cmpContent, Messages.getString("dialog.CSVSettingDialog.cmbLang"));
			cmbLang = new TableComboViewer(cmpContent, SWT.READ_ONLY | SWT.BORDER);
			TableCombo tableCombo = cmbLang.getTableCombo();
			tableCombo.setShowTableLines(false);
			tableCombo.setShowTableHeader(false);
			tableCombo.setDisplayColumnIndex(-1);
			tableCombo.setShowImageWithinSelection(true);
			tableCombo.setShowColorWithinSelection(false);
			tableCombo.setShowFontWithinSelection(false);
			tableCombo.setVisibleItemCount(20);
			cmbLang.getTableCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			ArrayList<Language> languages = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
			Collections.sort(languages, new Comparator<Language>() {
				public int compare(Language o1, Language o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
			cmbLang.setContentProvider(new ArrayContentProvider());
			cmbLang.setLabelProvider(new LanguageLabelProvider());
			cmbLang.setInput(languages);
			cmbLang.getTableCombo().select(0);

			createLabel(cmpContent, Messages.getString("dialog.CSVSettingDialog.cmbXCSTemplate"));
			cmbXCSTemplate = new Combo(cmpContent, SWT.READ_ONLY);
			cmbXCSTemplate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			if (xcsTemplates.length > 0) {
				cmbXCSTemplate.setItems(xcsTemplates);
				cmbXCSTemplate.select(0);
			}
		}

		return tparent;
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

	/**
	 * 获取字符串 string 在数组 array 中的索引
	 * @param array
	 * @param string
	 * @return ;
	 */
	public int indexOf(String[] array, String string) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(string)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void okPressed() {
		String strCSVPath = txtCSV.getText();
		if (strCSVPath == null || strCSVPath.trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.CSVSettingDialog.msgTitle"),
					Messages.getString("dialog.CSVSettingDialog.msg1"));
			return;
		}
		setCsvPath(strCSVPath);
		if (isTBXConverter) {
			String strMainLang = TextUtil.getLanguageCode(cmbLang.getTableCombo().getText());
			if (strMainLang == null || strMainLang.trim().equals("")) {
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.CSVSettingDialog.msgTitle"),
						Messages.getString("dialog.CSVSettingDialog.msg2"));
				return;
			}
			setLang(strMainLang);
			setXcsTemplate(cmbXCSTemplate.getText());
		}
		String colSeparator = cmbColSeparator.getText();
		if (colSeparator.equals("Tab")) {
			colSeparator = "\t";
		}
		setColSeparator(colSeparator);
		setTextDelimiter(cmbTextDelimiter.getText());
		setEncoding(cmbEncoding.getText());

		close();
	}

	public String getCsvPath() {
		return csvPath;
	}

	public void setCsvPath(String csvPath) {
		this.csvPath = csvPath;
	}

	public String getColSeparator() {
		return colSeparator;
	}

	public void setColSeparator(String colSeparator) {
		this.colSeparator = colSeparator;
	}

	public String getTextDelimiter() {
		return textDelimiter;
	}

	public void setTextDelimiter(String textDelimiter) {
		this.textDelimiter = textDelimiter;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getXcsTemplate() {
		return xcsTemplate;
	}

	public void setXcsTemplate(String xcsTemplate) {
		this.xcsTemplate = xcsTemplate;
	}

}
