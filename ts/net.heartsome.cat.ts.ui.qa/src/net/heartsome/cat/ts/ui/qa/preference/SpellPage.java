package net.heartsome.cat.ts.ui.qa.preference;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.composite.LanguageLabelProvider;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.resource.ImageConstant;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 拼写检查设置首选项界面
 * @author robert	2013-01-30
 */
public class SpellPage extends PreferencePage implements IWorkbenchPreferencePage {
	public final static String ID = "net.heartsome.cat.ts.ui.qa.preference.SpellPage";
	private IPreferenceStore preferenceStore;
	private static final Logger LOGGER = LoggerFactory.getLogger(SpellPage.class.getName());
	/** 组件是否初始化 */
	private boolean isInit = false;
	
	/** 拼写检查器 > Aspell单选按钮 */
	private Button aspellBtn;
	/** 拼写检查器 > 内置拼写检查器 hunspell 单选按钮 */
	private Button hunspellBtn;
	/** 是否实时检查 */
	private Button realTimeSpellBtn;
	
	/** 装载 hunspell 与 aspell 设置的 tab folder */
	private TabFolder tabFolder;
	private TabItem hunspellTabItem;
	private TabItem aspellTabItem;
	
	//----------------- 下面是 hunspell 设置组件 --------------------------//
	/** 忽略非译元素 */
	private Button ignoreNontransBtn;
	/** 忽略单词首字母为数字 */
	private Button ignoreDigitalFirstBtn;
	/** 忽略单词首字母为大写 */
	private Button ignoreUpperCaseFirstBtn;
	/** 忽略所有都是大写的单词 */
	private Button ignoreAllUpperCaseBtn;
	
	//------------------ 下面是 aspell 设置的组件 --------------------------//
	/** 语言集合 */
	private List<Language> languages;
	/** Aspell 配置文件路径 */
	private String configFilePath;
	/** Aspell 命令文本框 */
	private Text txtCommandPath;
	/** 浏览按钮 */
	private Button btnBrowse;
	/** 是否使用 UTF-8 复选框 */
	private Button btnUTF8;
	/** 刷新按钮 */
	private Button btnRefresh;
	/** 语言下拉框 */
	private TableComboViewer cmbLang;
	/** 默认词典下拉框 */
	private ComboViewer cmbDefaultDic;
	/** 添加到列表按钮 */
	private Button btnAdd;
	/** 从列表删除按钮 */
	private Button btnRemove;
	/** 词典表格 */
	private Table table;
	private Map<String, Language> langMap = null;
	private String bundlePath;
	private QAXmlHandler xmlHandler;
	

	public SpellPage() {
		setTitle("拼写检查");
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
		
		try {
			bundlePath = FileLocator.toFileURL(net.heartsome.cat.ts.ui.Activator.getDefault().getBundle().getEntry("")).getPath();
		} catch (IOException e) {
			LOGGER.error(Messages.getString("qa.preference.SpellPage.LOG1"), e);
		}
		langMap = LocaleService.getDefaultLanguage();
		languages = new ArrayList<Language>(langMap.values());
		Collections.sort(languages, new Comparator<Language>() {
			public int compare(Language o1, Language o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		checkAspellConfigureFile();
	}

	public void init(IWorkbench workbench) {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;
		Composite tParent = new Composite(parent, SWT.BORDER);
		tParent.setLayoutData(new GridData(GridData.FILL_BOTH));
		tParent.setLayout(new GridLayout());
		
		addSpellInstalGroup(tParent);
		
		tabFolder = new TabFolder(tParent, SWT.NONE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		hunspellTabItem = new TabItem(tabFolder, SWT.NONE);
		hunspellTabItem.setText(Messages.getString("qa.preference.SpellPage.hunspellTab"));
		
		aspellTabItem = new TabItem(tabFolder, SWT.NONE);
		aspellTabItem.setText(Messages.getString("qa.preference.SpellPage.aspellTab"));
		
		createHunspellCmp();
		createAspellCmp();
		
		initData();
		return parent;
	}

	
	/**
	 * 创建 aspell 的初始化数据
	 */
	private void checkAspellConfigureFile(){
		// 先检查首选项目录是否存在
		String preferencePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(QAConstant.QA_SPELL_preferenceFolder).toOSString();
		File preferenceFolder = new File(preferencePath);
		if (!preferenceFolder.exists() || preferenceFolder.isFile()) {
			preferenceFolder.mkdirs();
		}
		configFilePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(QAConstant.QA_SPELL_ASPELLCONFIGFILE).toOSString();
		File configureFile = new File(configFilePath);
		if (!configureFile.exists() || configureFile.isDirectory()) {
			try {
				FileOutputStream fos = new FileOutputStream(configureFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				StringBuffer bf = new StringBuffer();
				bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
				bf.append("<aspell>\n");
				bf.append("</aspell>");
				bos.write(bf.toString().getBytes());
				bos.close();
				fos.close();
			} catch (IOException e) {
				LOGGER.error(Messages.getString("qa.preference.SpellPage.LOG2"), e);
				MessageDialog.openInformation(getShell(),
						Messages.getString("qa.all.dialog.info"),
						Messages.getString("qa.preference.SpellPage.LOG3"));
			}
		}
	}
	
	/**
	 * 拼写检查词典设置
	 * @param tParent
	 */
	private void addSpellInstalGroup(Composite tParent) {
		Group groupSpellCheck = new Group(tParent, SWT.NONE);
		groupSpellCheck.setLayout(new GridLayout());
		groupSpellCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupSpellCheck.setText(Messages.getString("qa.preference.SpellPage.groupSpellCheck"));

		HsImageLabel imageLabel2 = new HsImageLabel(Messages.getString("qa.preference.SpellPage.imageLabel2"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_DICTIONARY));
		Composite cmpSpellCheck = imageLabel2.createControl(groupSpellCheck);
		cmpSpellCheck.setLayout(new GridLayout(2, false));
		cmpSpellCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		hunspellBtn = new Button(cmpSpellCheck, SWT.RADIO);
		hunspellBtn.setText(Messages.getString("qa.preference.SpellPage.hunspellBtn"));
		hunspellBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		aspellBtn = new Button(cmpSpellCheck, SWT.RADIO);
		aspellBtn.setText(Messages.getString("qa.preference.SpellPage.aspellBtn"));
		aspellBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		realTimeSpellBtn = new Button(cmpSpellCheck, SWT.CHECK);
		realTimeSpellBtn.setText(Messages.getString("qa.preference.SpellPage.realTimeSpellBtn"));
		realTimeSpellBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		imageLabel2.computeSize();
		
		hunspellBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolder.setSelection(hunspellTabItem);
			}
		});
		aspellBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tabFolder.setSelection(aspellTabItem);
			}
		});
	}
	
	/**
	 * 创建 hunspell 的界面
	 */
	private void createHunspellCmp() {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		HsImageLabel imageLabel = new HsImageLabel(Messages.getString("qa.preference.SpellPage.hunspellTab.label"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_IN_DIC));
		Composite cmp = imageLabel.createControl(composite);
		
		ignoreNontransBtn = new Button(cmp, SWT.CHECK);
		ignoreNontransBtn.setText(Messages.getString("qa.preference.SpellPage.ignoreNontransBtn"));
		
		ignoreDigitalFirstBtn = new Button(cmp, SWT.CHECK);
		ignoreDigitalFirstBtn.setText(Messages.getString("qa.preference.SpellPage.ignoreDigitalFirstBtn"));
		
		ignoreUpperCaseFirstBtn = new Button(cmp, SWT.CHECK);
		ignoreUpperCaseFirstBtn.setText(Messages.getString("qa.preference.SpellPage.ignoreUpperCaseFirstBtn"));
		
		ignoreAllUpperCaseBtn = new Button(cmp, SWT.CHECK);
		ignoreAllUpperCaseBtn.setText(Messages.getString("qa.preference.SpellPage.ignoreAllUpperBtn"));
		
		imageLabel.computeSize();
		hunspellTabItem.setControl(composite);
	}
	
	/**
	 * 创建 aspell 的配置界面
	 */
	private void createAspellCmp() {
		isInit = true;

		Composite groupParent = new Composite(tabFolder, SWT.NONE);
		groupParent.setLayout(new GridLayout());
		groupParent.setLayoutData(new GridData(GridData.FILL_BOTH));
		

//		Group groupParent = new Group(tparent, SWT.None);
//		groupParent.setLayout(new GridLayout());
//		groupParent.setLayoutData(new GridData(GridData.FILL_BOTH));
//		groupParent.setText(Messages.getString("qa.preference.SpellPage.groupParent"));

		HsImageLabel imageLabel = new HsImageLabel(
				Messages.getString("qa.preference.SpellPage.imageLabel"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_ASPELL_DIC));
		Composite cmp = imageLabel.createControl(groupParent);
		cmp.setLayout(new GridLayout());
		Composite cmpTemp = (Composite) imageLabel.getControl();
		cmpTemp.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite cmpContent = new Composite(cmpTemp, SWT.None);
		cmpContent.setLayout(new GridLayout(3, false));
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		cmpContent.setLayoutData(data);

		Label lbl = new Label(cmpContent, SWT.NONE);
		lbl.setText(Messages.getString("qa.preference.SpellPage.lblPath"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
		txtCommandPath = new Text(cmpContent, SWT.BORDER);
		txtCommandPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtCommandPath.setEditable(false);
		btnBrowse = new Button(cmpContent, SWT.NONE);
		btnBrowse.setText(Messages.getString("qa.preference.SpellPage.btnBrowse"));
//		GridData btnData = new GridData();
//		btnData.widthHint = 70;
//		btnBrowse.setLayoutData(btnData);

		new Label(cmpContent, SWT.NONE);
		btnUTF8 = new Button(cmpContent, SWT.CHECK);
		btnUTF8.setText(Messages.getString("qa.preference.SpellPage.btnUTF8"));
		new Label(cmpContent, SWT.NONE);

		lbl = new Label(cmpContent, SWT.NONE);
		lbl.setText(Messages.getString("qa.preference.SpellPage.lblDic"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
		cmbDefaultDic = new ComboViewer(cmpContent);
		cmbDefaultDic.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnRefresh = new Button(cmpContent, SWT.NONE);
		btnRefresh.setText(Messages.getString("qa.preference.SpellPage.btnRefresh"));

		lbl = new Label(cmpContent, SWT.NONE);
		lbl.setText(Messages.getString("qa.preference.SpellPage.lblLang"));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
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
		cmbLang.setLabelProvider(new LanguageLabelProvider());
		cmbLang.setContentProvider(new ArrayContentProvider());
		cmbLang.setInput(languages);
		cmbLang.getTableCombo().select(0);
		new Label(cmpContent, SWT.NONE);

		Composite cmpTableBtn = new Composite(cmpContent, SWT.NONE);
		GridLayout btnLayout = new GridLayout(2, false);
		btnLayout.marginWidth = 0;
		cmpTableBtn.setLayout(btnLayout);
		GridData btnData1 = new GridData(GridData.FILL_BOTH);
		btnData1.horizontalSpan = 3;
		cmpTableBtn.setLayoutData(btnData1);

		btnAdd = new Button(cmpTableBtn, SWT.NONE);
		btnAdd.setText(Messages.getString("qa.preference.SpellPage.btnAdd"));
		btnRemove = new Button(cmpTableBtn, SWT.NONE);
		btnRemove.setText(Messages.getString("qa.preference.SpellPage.btnRemove"));
		Point browsePoint = btnBrowse.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point refreshPoint = btnRefresh.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point addPoint = btnAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point remPoint = btnRemove.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		GridData btnData = new GridData();
		int width = Math.max(refreshPoint.x, Math.max(browsePoint.x, Math.max(addPoint.x, remPoint.x)));
		btnData.widthHint = width + 10;
		btnBrowse.setLayoutData(btnData);
		btnRefresh.setLayoutData(btnData);
		btnAdd.setLayoutData(btnData);
		btnRemove.setLayoutData(btnData);

		table = new Table(cmpTableBtn, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

		String[] arrTableHeader = new String[] {
				Messages.getString("qa.preference.SpellPage.arrTableHeader1"), "",
				Messages.getString("qa.preference.SpellPage.arrTableHeader2") };
		int[] arrWidth = new int[] { 195, 40, 195 };
		for (int i = 0; i < arrTableHeader.length; i++) {
			int style = SWT.NONE;
			if (i == 1) {
				style = SWT.CENTER;
			}
			TableColumn col = new TableColumn(table, style);
			col.setText(arrTableHeader[i]);
			col.setWidth(arrWidth[i]);
		}

		GridData dataTable = new GridData(GridData.FILL_BOTH);
		dataTable.horizontalSpan = 2;
		table.setLayoutData(dataTable);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		initProperty();
		initListener();
		imageLabel.computeSize();
		
		aspellTabItem.setControl(groupParent);
	}
	
	
	private void initProperty() {
		xmlHandler = new QAXmlHandler();
		Map<String, Object> openResultMap = xmlHandler.openFile(configFilePath);
		if (openResultMap != null
				&& QAConstant.RETURNVALUE_RESULT_RETURN.equals(openResultMap.get(QAConstant.RETURNVALUE_RESULT))) {
			return;
		}
		
		if (openResultMap == null
				|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) openResultMap
						.get(QAConstant.RETURNVALUE_RESULT)) {
			// 针对文件解析出错
			boolean response = MessageDialog.openConfirm(getShell(), Messages.getString("qa.all.dialog.info"), 
					Messages.getString("qa.preference.SpellPage.aspellLogCheck"));
			if (response) {
				checkAspellConfigureFile();
			}else {
				return;
			}
		}
		
		String commandPath = xmlHandler.getNodeText(configFilePath, "/aspell/commandLine", "");
		txtCommandPath.setText(commandPath == null ? "" : commandPath);

		cmbDefaultDic.getCombo().setItems(getDictionaries(commandPath));
		cmbDefaultDic.getCombo().select(0);
		
		String utf8 = xmlHandler.getNodeText(configFilePath, "/aspell/utf8", "");
		btnUTF8.setSelection(utf8 != null && utf8.equals("yes"));
		
		List<String[]> aspellDicList = null;
		try {
			aspellDicList = xmlHandler.getAspellDicConfig(configFilePath);
			addLangAndDicToTable(aspellDicList);
		} catch (Exception e) {
			LOGGER.error(Messages.getString("qa.preference.SpellPage.LOG4"), e);
		}
	}

	private void initListener() {
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog flg = new FileDialog(btnBrowse.getShell(), SWT.OPEN);
				flg.setText(Messages.getString("qa.preference.SpellPage.btnBrowse.Title"));
				String[] filter = new String[] { "*" };
				flg.setFilterExtensions(filter);
				if (txtCommandPath.getText() == null || txtCommandPath.getText().trim().equals("")) {
					flg.setFilterPath(System.getProperty("user.home"));
				} else {
					String path = txtCommandPath.getText();
					if (path.endsWith(" --encoding=utf-8")) {
						path = path.substring(0, path.indexOf(" --encoding=utf-8"));
					}
					flg.setFilterPath(path.substring(0, path.lastIndexOf(System.getProperty("file.separator"))));
					flg.setFileName(path.substring(path.lastIndexOf(System.getProperty("file.separator")) + 1));
				}
				String stropen = flg.open();
				String commandLine = stropen == null ? "" : stropen;
				if (commandLine.equals("")) {
					return;
				} else {
					boolean blnUseUTF8 = btnUTF8.getSelection();
					if (blnUseUTF8 && !commandLine.endsWith(" --encoding=utf-8")) {
						commandLine += " --encoding=utf-8";
					}
					if (!blnUseUTF8 && commandLine.endsWith(" --encoding=utf-8")) {
						commandLine = commandLine.substring(0, commandLine.indexOf(" --encoding=utf-8"));
					}

					txtCommandPath.setText(commandLine);
					cmbDefaultDic.getCombo().removeAll();
					cmbDefaultDic.getCombo().setItems(getDictionaries(commandLine));
					cmbDefaultDic.getCombo().select(0);
				}
				filter = null;
			}
		});

		btnUTF8.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean blnUseUTF8 = btnUTF8.getSelection();
				String commandLine = txtCommandPath.getText();
				if (blnUseUTF8 && !commandLine.endsWith(" --encoding=utf-8") && !commandLine.equals("")) {
					commandLine += " --encoding=utf-8";
				}
				if (!blnUseUTF8 && commandLine.endsWith(" --encoding=utf-8")) {
					commandLine = commandLine.substring(0, commandLine.indexOf(" --encoding=utf-8"));
				}
				txtCommandPath.setText(commandLine);
			}
		});

		btnRefresh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String commandLine = txtCommandPath.getText();
				cmbDefaultDic.getCombo().removeAll();
				cmbDefaultDic.getCombo().setItems(getDictionaries(commandLine));
				cmbDefaultDic.getCombo().select(0);
			}
		});

		btnAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (cmbLang.getTableCombo().getSelectionIndex() >= 0 && cmbDefaultDic.getCombo().getSelectionIndex() >= 0) {
					String lang = TextUtil.getLanguageCode(cmbLang.getTableCombo().getItem(
							cmbLang.getTableCombo().getSelectionIndex()));
					String fileName = cmbDefaultDic.getCombo().getItem(cmbDefaultDic.getCombo().getSelectionIndex());
					TableItem[] items = table.getItems();
					boolean blnIsUpdate = false;
					for (int i = 0; i < items.length; i++) {
						String itemLang = TextUtil.getLanguageCode(items[i].getText(0));
						if (lang.equals(itemLang)) {
							if (fileName.equals(items[i].getText(2))) {
								return;
							}
							blnIsUpdate = true;
							break;
						}
					}
					List<String[]> dicList = new ArrayList<String[]>();
					dicList.add(new String[]{lang, fileName});
					addLangAndDicToTable(dicList);
					
					try {
						xmlHandler.addAspellConfig(configFilePath, lang, fileName, blnIsUpdate);
					} catch (Exception e) {
						LOGGER.error(Messages.getString("qa.preference.SpellPage.LOG5"), e);
					}
				}
			}
		});

		btnRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (table.getSelectionCount() == 0) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("qa.all.dialog.info"),
							Messages.getString("qa.preference.SpellPage.msg1"));
					return;
				}
				Rectangle rect = table.getBounds();
				
				for (TableItem item : table.getSelection()) {
					String lang = TextUtil.getLanguageCode(item.getText(0));
					try {
						xmlHandler.removeAspellConfig(configFilePath, lang);
					} catch (Exception e) {
						LOGGER.error(Messages.getString("qa.preference.SpellPage.LOG6"), e);
					}
					table.remove(table.indexOf(item));
				}
				table.pack();
				table.setHeaderVisible(true);
				table.setBounds(rect);
			}
		});
	}
	
	/**
	 * 根据传入的 命令 从 aspell 中读取所有的词典
	 * @param command
	 * @return
	 */
	public String[] getDictionaries(String command) {
		String response;
		Process spellProcess;

		Runtime runtime = Runtime.getRuntime();
		try {
			spellProcess = runtime.exec(command + " dump dicts --encoding=utf-8");
			BufferedReader spellReader = new BufferedReader(new InputStreamReader(spellProcess.getInputStream()));
			response = spellReader.readLine();
			Vector<String> result = new Vector<String>();
			while (response != null && !response.equals("")) {
				result.add(response);
				response = spellReader.readLine();
			}
			String[] dicts = new String[result.size()];
			for (int i = 0; i < result.size(); i++) {
				dicts[i] = result.get(i);
			}
			return dicts;
		} catch (IOException e) {
			return new String[0];
		}
	}
	
	/**
	 * 将语言与词典对进行加载到列表中，
	 * @param aspellDicList	第一个数据为 语言， 第二个数据为 词典
	 */
	private void addLangAndDicToTable(List<String[]> aspellDicList) {
		for(String[] dicArray : aspellDicList){
			String lang = dicArray[0];
			String dic = dicArray[1];
			
			TableItem item = null;
			for (int i = 0; i < table.getItemCount(); i++) {
				TableItem currItem = table.getItem(i);
				if (TextUtil.getLanguageCode(currItem.getText(0)).equals(lang)) {
					item = currItem;
				}
			}
			if (item == null) {
				item = new TableItem(table, 0);
			}
			
			String[] data = { TextUtil.getLanguageName(lang), "->", dic }; //$NON-NLS-1$
			String imgPath = langMap.get(lang).getImagePath();
			if (imgPath != null && !imgPath.equals("")) {
				imgPath = bundlePath + imgPath;
				Image image = new Image(getShell().getDisplay(), imgPath);
				if (image != null) {
					ImageData imgData = image.getImageData().scaledTo(16, 12);
					image = new Image(getShell().getDisplay(), imgData);
					item.setImage(0, image);
				}
			}
			item.setText(data);
		}
	}

	/**
	 * 初始化数据
	 */
	private void initData(){
		if (preferenceStore.getBoolean(QAConstant.QA_PREF_isHunspell)) {
			hunspellBtn.setSelection(true);
		}else {
			aspellBtn.setSelection(true);
		}
		realTimeSpellBtn.setSelection(preferenceStore.getBoolean(QAConstant.QA_PREF_realTimeSpell));
		
		ignoreNontransBtn.setSelection(preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreNontrans));
		ignoreDigitalFirstBtn.setSelection(preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreDigitalFirst));
		ignoreUpperCaseFirstBtn.setSelection(preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreUpperCaseFirst));
		ignoreAllUpperCaseBtn.setSelection(preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreAllUpperCase));
		
		if (hunspellBtn.getSelection()) {
			tabFolder.setSelection(hunspellTabItem);
		}
		if (aspellBtn.getSelection()) {
			tabFolder.setSelection(aspellTabItem);
		}
	}
	
	@Override
	protected void performDefaults() {
		if (preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_isHunspell)) {
			hunspellBtn.setSelection(true);
			aspellBtn.setSelection(false);
		}else {
			hunspellBtn.setSelection(false);
			aspellBtn.setSelection(true);
		}
		realTimeSpellBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_realTimeSpell));
		
		ignoreNontransBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_ignoreNontrans));
		ignoreDigitalFirstBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_ignoreDigitalFirst));
		ignoreUpperCaseFirstBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_ignoreUpperCaseFirst));
		ignoreAllUpperCaseBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_ignoreAllUpperCase));
		
		if (hunspellBtn.getSelection()) {
			tabFolder.setSelection(hunspellTabItem);
		}
		if (aspellBtn.getSelection()) {
			tabFolder.setSelection(aspellTabItem);
		}
	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}
		preferenceStore.setValue(QAConstant.QA_PREF_isHunspell, hunspellBtn.getSelection());
		preferenceStore.setValue(QAConstant.QA_PREF_realTimeSpell, realTimeSpellBtn.getSelection());

		preferenceStore.setValue(QAConstant.QA_PREF_ignoreNontrans, ignoreNontransBtn.getSelection());
		preferenceStore.setValue(QAConstant.QA_PREF_ignoreDigitalFirst, ignoreDigitalFirstBtn.getSelection());
		preferenceStore.setValue(QAConstant.QA_PREF_ignoreUpperCaseFirst, ignoreUpperCaseFirstBtn.getSelection());
		preferenceStore.setValue(QAConstant.QA_PREF_ignoreAllUpperCase, ignoreAllUpperCaseBtn.getSelection());
		
		String commandLine = txtCommandPath.getText();
		try {
			xmlHandler.saveAspellConfig(configFilePath, commandLine, btnUTF8.getSelection());
			int oldValue = preferenceStore.getInt(QAConstant.QA_PREF_aspellConfig_changeTag);
			preferenceStore.setValue(QAConstant.QA_PREF_aspellConfig_changeTag, ++ oldValue);
		} catch (Exception e) {
			LOGGER.error(Messages.getString("qa.preference.SpellPage.LOG5"), e);
		}
		
		return true;
	}

}
