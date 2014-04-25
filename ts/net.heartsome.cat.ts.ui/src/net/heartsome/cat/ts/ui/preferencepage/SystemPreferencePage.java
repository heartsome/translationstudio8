package net.heartsome.cat.ts.ui.preferencepage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.heartsome.cat.common.ui.HSFontSettingComposite;
import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.resource.ImageConstant;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * 首选项的系统界面
 * @author peason
 * @version
 * @since JDK1.6
 */
public class SystemPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.ui.preferencepage.SystemPreferencePage";

	private IPreferenceStore preferenceStore;

	/** 启动时检查更新单选按钮 */
	private Button btnCheckUpdateWithStartup;

	/** 每月检查更新单选按钮 */
	private Button btnCheckUpdateWithMonthly;

	/** 每周检查更新单选按钮 */
	private Button btnCheckUpdateWithWeekly;

	/** 从不检查更新单选按钮 */
	private Button btnCheckUpdateWithNever;

	/** 日期选择按钮 */
	private Spinner selectDateSpi;

	/** 星期选择按钮 */
	private Combo cmbSelectWeek;

	/** 用户界面语言 > 英文单选按钮 */
	private Button btnLanguageWithEN;

	/** 用户界面语言 > 中文单选按钮 */
	private Button btnLanguageWithZHCN;

	/** 系统用户文本框 */
	private Text txtSystemUser;

	private HSFontSettingComposite editorFontSetting;
	private HSFontSettingComposite matchViewFontSetting;

	/** 组件是否初始化 --robert */
	private boolean isInit = false;

	private Composite cmpMonthly;

	private Composite cmpWeekly;

	private String[] arrWeek = new String[] { Messages.getString("preferencepage.SystemPreferencePage.Sun"),
			Messages.getString("preferencepage.SystemPreferencePage.Mon"),
			Messages.getString("preferencepage.SystemPreferencePage.Tue"),
			Messages.getString("preferencepage.SystemPreferencePage.Wed"),
			Messages.getString("preferencepage.SystemPreferencePage.Thu"),
			Messages.getString("preferencepage.SystemPreferencePage.Fri"),
			Messages.getString("preferencepage.SystemPreferencePage.Sat"), };

	/** 初始语言 */
	private int initLang;

	/**
	 * 构造函数
	 */
	public SystemPreferencePage() {
		setTitle(Messages.getString("preferencepage.SystemPreferencePage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}

	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group groupCommon = new Group(tparent, SWT.NONE);
		groupCommon.setLayout(new GridLayout());
		groupCommon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupCommon.setText(Messages.getString("preferencepage.SystemPreferencePage.groupCommon"));

		HsImageLabel imageLabel1 = new HsImageLabel(
				Messages.getString("preferencepage.SystemPreferencePage.imageLabel1"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_UPDATE));
		Composite cmpCommon = imageLabel1.createControl(groupCommon);
		cmpCommon.setLayout(new GridLayout());
		cmpCommon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnCheckUpdateWithStartup = new Button(cmpCommon, SWT.RADIO);
		btnCheckUpdateWithStartup.setText(Messages
				.getString("preferencepage.SystemPreferencePage.btnCheckUpdateWithStartup"));
		GridDataFactory.fillDefaults().applyTo(btnCheckUpdateWithStartup);
		btnCheckUpdateWithStartup.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (btnCheckUpdateWithStartup.getSelection()) {
					btnCheckUpdateWithMonthly.setSelection(false);
					btnCheckUpdateWithWeekly.setSelection(false);
					btnCheckUpdateWithNever.setSelection(false);
					selectDateSpi.setEnabled(false);
					cmbSelectWeek.setEnabled(false);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		cmpMonthly = new Composite(cmpCommon, SWT.None);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).margins(0, 0).spacing(0, 0).applyTo(cmpMonthly);
		cmpMonthly.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCheckUpdateWithMonthly = new Button(cmpMonthly, SWT.RADIO);
		btnCheckUpdateWithMonthly.setText(Messages
				.getString("preferencepage.SystemPreferencePage.btnCheckUpdateWithMonthly1"));
		GridData spinnaData = new GridData();
		spinnaData.widthHint = 20;
		selectDateSpi = new Spinner(cmpMonthly, SWT.BORDER);
		selectDateSpi.setMinimum(1);
		selectDateSpi.setMaximum(31);
		selectDateSpi.setTextLimit(2);
		selectDateSpi.setEnabled(false);
		selectDateSpi.setLayoutData(spinnaData);
		selectDateSpi.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String text = selectDateSpi.getText();
				if (text != null && !text.trim().equals("")) {
					if (Integer.parseInt(text.trim()) > 31) {
						selectDateSpi.setSelection(31);
					} else if (Integer.parseInt(text.trim()) < 1) {
						selectDateSpi.setSelection(1);
					}
				}
			}
		});
		new Label(cmpMonthly, SWT.None).setText(Messages
				.getString("preferencepage.SystemPreferencePage.btnCheckUpdateWithMonthly2"));
		btnCheckUpdateWithMonthly.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				selectDateSpi.setEnabled(btnCheckUpdateWithMonthly.getSelection());
				if (btnCheckUpdateWithMonthly.getSelection()) {
					btnCheckUpdateWithStartup.setSelection(false);
					btnCheckUpdateWithWeekly.setSelection(false);
					btnCheckUpdateWithNever.setSelection(false);
					cmbSelectWeek.setEnabled(false);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		cmpWeekly = new Composite(cmpCommon, SWT.None);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).margins(0, 0).spacing(0, 0).applyTo(cmpWeekly);
		cmpWeekly.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCheckUpdateWithWeekly = new Button(cmpWeekly, SWT.RADIO);
		btnCheckUpdateWithWeekly.setText(Messages
				.getString("preferencepage.SystemPreferencePage.btnCheckUpdateWithWeekly1"));
		cmbSelectWeek = new Combo(cmpWeekly, SWT.READ_ONLY);
		cmbSelectWeek.setItems(arrWeek);
		cmbSelectWeek.setEnabled(false);
		cmbSelectWeek.select(0);
		GridDataFactory.swtDefaults().applyTo(cmbSelectWeek);
		new Label(cmpWeekly, SWT.NONE).setText(Messages
				.getString("preferencepage.SystemPreferencePage.btnCheckUpdateWithWeekly2"));
		btnCheckUpdateWithWeekly.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				cmbSelectWeek.setEnabled(btnCheckUpdateWithWeekly.getSelection());
				if (btnCheckUpdateWithWeekly.getSelection()) {
					btnCheckUpdateWithStartup.setSelection(false);
					btnCheckUpdateWithMonthly.setSelection(false);
					btnCheckUpdateWithNever.setSelection(false);
					selectDateSpi.setEnabled(false);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		btnCheckUpdateWithNever = new Button(cmpCommon, SWT.RADIO);
		btnCheckUpdateWithNever.setText(Messages
				.getString("preferencepage.SystemPreferencePage.btnCheckUpdateWithNever"));
		btnCheckUpdateWithNever.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridDataFactory.fillDefaults().applyTo(btnCheckUpdateWithNever);
		btnCheckUpdateWithNever.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (btnCheckUpdateWithNever.getSelection()) {
					btnCheckUpdateWithMonthly.setSelection(false);
					btnCheckUpdateWithWeekly.setSelection(false);
					btnCheckUpdateWithStartup.setSelection(false);
					selectDateSpi.setEnabled(false);
					cmbSelectWeek.setEnabled(false);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Group groupLanguage = new Group(tparent, SWT.NONE);
		groupLanguage.setLayout(new GridLayout());
		groupLanguage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupLanguage.setText(Messages.getString("preferencepage.SystemPreferencePage.groupLanguage"));

		HsImageLabel imageLabel2 = new HsImageLabel(
				Messages.getString("preferencepage.SystemPreferencePage.imageLabel3"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_LANGUAGE));
		Composite cmpLang = imageLabel2.createControl(groupLanguage);
		cmpLang.setLayout(new GridLayout());
		cmpLang.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnLanguageWithEN = new Button(cmpLang, SWT.RADIO);
		btnLanguageWithEN.setText(Messages.getString("preferencepage.SystemPreferencePage.btnLanguageWithEN"));
		btnLanguageWithEN.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnLanguageWithZHCN = new Button(cmpLang, SWT.RADIO);
		btnLanguageWithZHCN.setText(Messages.getString("preferencepage.SystemPreferencePage.btnLanguageWithZHCN"));
		btnLanguageWithZHCN.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group groupFont = new Group(tparent, SWT.NONE);
		groupFont.setLayout(new GridLayout());
		groupFont.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupFont.setText(Messages.getString("preferencepage.SystemPreferencePage.groupFont"));
		HsImageLabel imageLabel4 = new HsImageLabel(
				Messages.getString("preferencepage.SystemPreferencePage.groupFont.desc"),
				Activator.getImageDescriptor("images/preference/system/font.png"));
		Composite cmpFont = imageLabel4.createControl(groupFont);
		GridLayout cmpFontGl = new GridLayout(2, true);
		cmpFontGl.marginLeft = 0;
		cmpFontGl.marginRight = 0;
		cmpFontGl.marginTop = 0;
		cmpFontGl.marginBottom = 0;
		cmpFontGl.marginWidth = 0;
		cmpFontGl.marginHeight = 0;
		cmpFont.setLayout(cmpFontGl);
		GridData cmpFontGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		cmpFont.setLayoutData(cmpFontGd);

		editorFontSetting = new HSFontSettingComposite(cmpFont, SWT.NONE,
				Messages.getString("preferencepage.fontsetting.editor.title"));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		editorFontSetting.setLayoutData(gd);
		matchViewFontSetting = new HSFontSettingComposite(cmpFont, SWT.NONE,
				Messages.getString("preferencepage.fontsetting.matchView.title"));
		matchViewFontSetting.setLayoutData(gd);

		Group groupSystemUser = new Group(tparent, SWT.NONE);
		groupSystemUser.setLayout(new GridLayout());
		groupSystemUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupSystemUser.setText(Messages.getString("preferencepage.SystemPreferencePage.groupSystemUser"));

		HsImageLabel imageLabel3 = new HsImageLabel(
				Messages.getString("preferencepage.SystemPreferencePage.imageLabel4"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_USER));
		Composite cmpUser = imageLabel3.createControl(groupSystemUser);
		cmpUser.setLayout(new GridLayout(2, false));
		cmpUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(cmpUser, SWT.NONE).setText(Messages.getString("preferencepage.SystemPreferencePage.lblUser"));

		txtSystemUser = new Text(cmpUser, SWT.BORDER);
		txtSystemUser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		imageLabel1.computeSize();
		imageLabel2.computeSize();
		imageLabel3.computeSize();
		imageLabel4.computeSize();
		setInitValues(false);
		return parent;
	}

	@Override
	protected void performDefaults() {
		setInitValues(true);
	}

	/**
	 * 修改产品 ini 文件中的语言
	 */
	private void changeLocale(String locale) {
		Location configArea = Platform.getInstallLocation();
		if (configArea == null) {
			return;
		}

		URL location = null;
		try {
			location = new URL(configArea.getURL().toExternalForm() + "configuration" + File.separator + "config.ini");
		} catch (MalformedURLException e) {
			// This should never happen
		}
		// System.out.println("LanguageSwitchHandler.loadConfigurationInfo(): "
		// + location);

		try {
			String fileName = location.getFile();
			File file = new File(fileName);
			fileName += ".bak";
			file.renameTo(new File(fileName));
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			BufferedWriter out = new BufferedWriter(new FileWriter(location.getFile()));
			try {
				String line = in.readLine();
				while (line != null) {
					if (line.startsWith("osgi.nl=")) {
						out.write("osgi.nl=" + locale);
					} else {
						out.write(line);
					}
					out.newLine();
					line = in.readLine();
				}
				out.flush();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				File tmpFile = new File(location.getFile() + ".bak");
				if (tmpFile.exists()) {
					tmpFile.delete();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}

		if (btnCheckUpdateWithStartup.getSelection()) {
			preferenceStore.setValue(IPreferenceConstants.SYSTEM_AUTO_UPDATE,
					IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_STARTUP);
		} else if (btnCheckUpdateWithMonthly.getSelection()) {
			preferenceStore.setValue(IPreferenceConstants.SYSTEM_AUTO_UPDATE,
					IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY);
			preferenceStore.setValue(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY_DATE,
					selectDateSpi.getSelection());
		} else if (btnCheckUpdateWithWeekly.getSelection()) {
			preferenceStore.setValue(IPreferenceConstants.SYSTEM_AUTO_UPDATE,
					IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY);
			preferenceStore.setValue(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE,
					cmbSelectWeek.getSelectionIndex() + 1);
		} else if (btnCheckUpdateWithNever.getSelection()) {
			preferenceStore.setValue(IPreferenceConstants.SYSTEM_AUTO_UPDATE,
					IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_NEVER);
		}

		preferenceStore.setValue(IPreferenceConstants.SYSTEM_USER,
				txtSystemUser.getText() == null ? "" : txtSystemUser.getText());
		//将用户保存到平台首选项中
		PlatformUI.getPreferenceStore().setValue(IPreferenceConstants.SYSTEM_USER,
				preferenceStore.getString(IPreferenceConstants.SYSTEM_USER));
		if (btnLanguageWithEN.getSelection()) {
			preferenceStore
					.setValue(IPreferenceConstants.SYSTEM_LANGUAGE, IPreferenceConstants.SYSTEM_LANGUAGE_WITH_EN);
			CommonFunction.setSystemLanguage("en");
			if (initLang != IPreferenceConstants.SYSTEM_LANGUAGE_WITH_EN) {
				changeLocale("en");
				if (MessageDialog.openConfirm(getShell(),
						Messages.getString("preferencepage.SystemPreferencePage.msgTitle"),
						Messages.getString("preferencepage.SystemPreferencePage.msgInfo"))) {
					PlatformUI.getWorkbench().restart();
				}
			}
		} else if (btnLanguageWithZHCN.getSelection()) {
			preferenceStore.setValue(IPreferenceConstants.SYSTEM_LANGUAGE,
					IPreferenceConstants.SYSTEM_LANGUAGE_WITH_ZH_CN);
			CommonFunction.setSystemLanguage("zh");
			if (initLang != IPreferenceConstants.SYSTEM_LANGUAGE_WITH_ZH_CN) {
				changeLocale("zh");
				if (MessageDialog.openConfirm(getShell(),
						Messages.getString("preferencepage.SystemPreferencePage.msgTitle"),
						Messages.getString("preferencepage.SystemPreferencePage.msgInfo"))) {
					PlatformUI.getWorkbench().restart();
				}
			}
		}

		FontData[] fontData = editorFontSetting.getFontSetingFont();
		JFaceResources.getFontRegistry().put(Constants.XLIFF_EDITOR_TEXT_FONT, fontData);
		preferenceStore.setValue(IPreferenceConstants.XLIFF_EDITOR_FONT_NAME, fontData[0].getName());
		preferenceStore.setValue(IPreferenceConstants.XLIFF_EDITOR_FONT_SIZE, fontData[0].getHeight());

		fontData = matchViewFontSetting.getFontSetingFont();
		JFaceResources.getFontRegistry().put(Constants.MATCH_VIEWER_TEXT_FONT, fontData);
		preferenceStore.setValue(IPreferenceConstants.MATCH_VIEW_FONT_NAME, fontData[0].getName());
		preferenceStore.setValue(IPreferenceConstants.MATCH_VIEW_FONT_SIZE, fontData[0].getHeight());

		return true;
	}

	/**
	 * 对控件设置值
	 * @param blnIsApplyDefault
	 *            ;
	 */
	private void setInitValues(boolean blnIsApplyDefault) {
		int intAutoUpdate;
		int intLanguage;
		String strSystemUser;
		String strEditorFontName;
		int intEdutorFontSize;
		String strMatchViewFontName;
		int intMatchViewFontSize;
		if (blnIsApplyDefault) {
			intAutoUpdate = preferenceStore.getDefaultInt(IPreferenceConstants.SYSTEM_AUTO_UPDATE);
			intLanguage = preferenceStore.getDefaultInt(IPreferenceConstants.SYSTEM_LANGUAGE);
			strSystemUser = preferenceStore.getDefaultString(IPreferenceConstants.SYSTEM_USER);
			strEditorFontName = preferenceStore.getDefaultString(IPreferenceConstants.XLIFF_EDITOR_FONT_NAME);
			intEdutorFontSize = preferenceStore.getDefaultInt(IPreferenceConstants.XLIFF_EDITOR_FONT_SIZE);
			strMatchViewFontName = preferenceStore.getDefaultString(IPreferenceConstants.MATCH_VIEW_FONT_NAME);
			intMatchViewFontSize = preferenceStore.getDefaultInt(IPreferenceConstants.MATCH_VIEW_FONT_SIZE);
		} else {
			intAutoUpdate = preferenceStore.getInt(IPreferenceConstants.SYSTEM_AUTO_UPDATE);
			intLanguage = preferenceStore.getInt(IPreferenceConstants.SYSTEM_LANGUAGE);
			initLang = intLanguage;
			strSystemUser = preferenceStore.getString(IPreferenceConstants.SYSTEM_USER);
			strEditorFontName = preferenceStore.getString(IPreferenceConstants.XLIFF_EDITOR_FONT_NAME);
			intEdutorFontSize = preferenceStore.getInt(IPreferenceConstants.XLIFF_EDITOR_FONT_SIZE);
			strMatchViewFontName = preferenceStore.getString(IPreferenceConstants.MATCH_VIEW_FONT_NAME);
			intMatchViewFontSize = preferenceStore.getInt(IPreferenceConstants.MATCH_VIEW_FONT_SIZE);
		}
		if (intAutoUpdate == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_STARTUP) {
			btnCheckUpdateWithStartup.setSelection(true);
			btnCheckUpdateWithMonthly.setSelection(false);
			selectDateSpi.setEnabled(false);
			selectDateSpi.setSelection(1);
			btnCheckUpdateWithWeekly.setSelection(false);
			cmbSelectWeek.setEnabled(false);
			cmbSelectWeek.select(0);
			btnCheckUpdateWithNever.setSelection(false);
		} else if (intAutoUpdate == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY) {
			btnCheckUpdateWithStartup.setSelection(false);
			btnCheckUpdateWithMonthly.setSelection(true);
			btnCheckUpdateWithWeekly.setSelection(false);
			cmbSelectWeek.setEnabled(false);
			cmbSelectWeek.select(0);
			btnCheckUpdateWithNever.setSelection(false);
			int selDate;
			if (blnIsApplyDefault) {
				selDate = preferenceStore.getDefaultInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY_DATE);
			} else {
				selDate = preferenceStore.getInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY_DATE);
			}
			selectDateSpi.setEnabled(true);
			selectDateSpi.setSelection(selDate);
		} else if (intAutoUpdate == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY) {
			btnCheckUpdateWithStartup.setSelection(false);
			btnCheckUpdateWithMonthly.setSelection(false);
			selectDateSpi.setEnabled(false);
			selectDateSpi.setSelection(1);
			btnCheckUpdateWithWeekly.setSelection(true);
			btnCheckUpdateWithNever.setSelection(false);
			int selWeek;
			if (blnIsApplyDefault) {
				selWeek = preferenceStore.getDefaultInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE);
			} else {
				selWeek = preferenceStore.getInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE);
			}
			cmbSelectWeek.setEnabled(true);
			// ArrayList<String> list = new ArrayList<String>(Arrays.asList(arrWeek));
			cmbSelectWeek.select(selWeek - 1);
		} else if (intAutoUpdate == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_NEVER) {
			btnCheckUpdateWithStartup.setSelection(false);
			btnCheckUpdateWithMonthly.setSelection(false);
			selectDateSpi.setEnabled(false);
			btnCheckUpdateWithWeekly.setSelection(false);
			cmbSelectWeek.setEnabled(false);
			cmbSelectWeek.select(0);
			btnCheckUpdateWithNever.setSelection(true);
		}

		if (intLanguage == IPreferenceConstants.SYSTEM_LANGUAGE_WITH_EN) {
			btnLanguageWithEN.setSelection(true);
			btnLanguageWithZHCN.setSelection(false);
		} else if (intLanguage == IPreferenceConstants.SYSTEM_LANGUAGE_WITH_ZH_CN) {
			btnLanguageWithEN.setSelection(false);
			btnLanguageWithZHCN.setSelection(true);
		}
		txtSystemUser.setText(strSystemUser);
		editorFontSetting.initFont(strEditorFontName, intEdutorFontSize);
		matchViewFontSetting.initFont(strMatchViewFontName, intMatchViewFontSize);
	}
}
