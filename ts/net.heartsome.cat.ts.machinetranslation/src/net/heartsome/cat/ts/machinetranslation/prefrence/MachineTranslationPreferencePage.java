/**
 * GooglePreferencePage.java
 *
 * Version information :
 *
 * Date:2012-5-13
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.machinetranslation.prefrence;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.machinetranslation.Activator;
import net.heartsome.cat.ts.machinetranslation.BingTransUtils;
import net.heartsome.cat.ts.machinetranslation.GoogleTransUtils;
import net.heartsome.cat.ts.machinetranslation.bean.IPreferenceConstant;
import net.heartsome.cat.ts.machinetranslation.resource.Messages;
import net.heartsome.cat.ts.ui.bean.TranslateParameter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.api.GoogleAPI;
import com.google.api.GoogleAPIException;
import com.google.api.translate.Translate;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class MachineTranslationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.machinetranslation.prefrence.MachineTranslationPreferencePage";
	private IPreferenceStore ps;

	private Label googleStateLabel;

	private Label bingStateLable;

	private Image rightImage;
	private Image errorImage;

	private Text googleKeyText;
	private boolean googleState;

	private boolean bingState;
	private Text idText;
	private Text bingKeyText;

	// accessibility widget
	private Button alwaysAccessBtn;
	private Button manualAccessBtn;
	private Button ignoreExactMatchBtn;
	private Button ignoreLockBtn;

	/**
	 * constructor
	 */
	public MachineTranslationPreferencePage() {

		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		ps = getPreferenceStore();

		googleState = ps.getBoolean(IPreferenceConstant.GOOGLE_STATE);
		bingState = ps.getBoolean(IPreferenceConstant.BING_STATE);

		rightImage = Activator.getImageDescriptor("images/right.png").createImage();
		errorImage = Activator.getImageDescriptor("images/error.png").createImage();
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}

	// private Label label;
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout(1, false));
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		createGoogleTranslateArea(tparent);

		createBingTranslateArea(tparent);

		createIgnoreArea(tparent);

		createTranslateSettingArea(tparent);
		// 设置界面的值
		setValues();
		// 设置界面状态
		setComponentsState();
		return parent;
	}

	private Composite createGoogleTranslateArea(Composite tparent) {
		Group apiKeySettingGroup = new Group(tparent, SWT.NONE);
		apiKeySettingGroup.setText(Messages.getString("preference.GooglePreferencePage.apiKeySettingGroup"));
		apiKeySettingGroup.setLayout(new GridLayout(1, false));
		apiKeySettingGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		HsImageLabel test = new HsImageLabel(Messages.getString("preference.GooglePreferencePage.lbKeySetting"),
				Activator.getImageDescriptor("images/trans_google_key_32.png"));
		Composite com = test.createControl(apiKeySettingGroup);

		com.setLayout(new GridLayout(3, false));

		Label lblApiKey = new Label(com, SWT.NONE);
		lblApiKey.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblApiKey.setText(Messages.getString("preference.GooglePreferencePage.lblApiKey"));

		googleKeyText = new Text(com, SWT.BORDER);
		googleKeyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		googleStateLabel = new Label(com, SWT.NONE);
		googleStateLabel.setImage(errorImage);

		new Label(com, SWT.NONE);
		Button validateKey = new Button(com, SWT.NONE);
		validateKey.setText(Messages.getString("preference.GooglePreferencePage.validateKey"));
		validateKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String key = googleKeyText.getText();
				if (key == null || key.equals("")) {
					MessageDialog.openError(getShell(), Messages.getString("preference.GooglePreferencePage.msgTitle"),
							Messages.getString("preference.GooglePreferencePage.msg1"));
					return;
				}
				googleValidator();
				setComponentsState();
				if (!googleState) {
					MessageDialog.openError(getShell(), Messages.getString("preference.GooglePreferencePage.msgTitle"),
							Messages.getString("preference.GooglePreferencePage.msg2"));
					return;
				}
			}
		});
		new Label(com, SWT.NONE);

		new Label(com, SWT.NONE);
		Link link = new Link(com, SWT.NONE);
		link.setText("<a>" + Messages.getString("preference.GooglePreferencePage.link") + "</a>");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://code.google.com/apis/language/translate/v2/getting_started.html");
			}
		});
		link.setToolTipText("http://code.google.com/apis/language/translate/v2/getting_started.html");

		new Label(com, SWT.NONE);
		test.computeSize();
		return tparent;
	}

	private Composite createBingTranslateArea(Composite tparent) {
		Group apiKeySettingGroup = new Group(tparent, SWT.NONE);
		apiKeySettingGroup.setText(Messages.getString("preference.BingPreferencePage.apiKeySettingGroup"));
		apiKeySettingGroup.setLayout(new GridLayout(1, false));
		apiKeySettingGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		HsImageLabel lbKeySetting = new HsImageLabel(Messages.getString("preference.BingPreferencePage.lbKeySetting"),
				Activator.getImageDescriptor("images/trans_bing_key_32.png"));
		Composite com = lbKeySetting.createControl(apiKeySettingGroup);

		com.setLayout(new GridLayout(3, false));

		Label lblId = new Label(com, SWT.NONE);
		lblId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblId.setText(Messages.getString("preference.BingPreferencePage.lblId"));

		idText = new Text(com, SWT.BORDER);
		idText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(com, SWT.NONE);

		Label lblApiKey = new Label(com, SWT.NONE);
		lblApiKey.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblApiKey.setText(Messages.getString("preference.BingPreferencePage.lblApiKey"));

		bingKeyText = new Text(com, SWT.BORDER);
		bingKeyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		bingStateLable = new Label(com, SWT.NONE);
		bingStateLable.setImage(errorImage);

		new Label(com, SWT.NONE);
		Button validateKey = new Button(com, SWT.NONE);
		validateKey.setText(Messages.getString("preference.BingPreferencePage.validateKey"));
		validateKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String id = idText.getText();
				String bingKey = bingKeyText.getText();
				if (id == null || id.equals("")) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preference.BingPreferencePage.msgTitle"),
							Messages.getString("preference.BingPreferencePage.msg1"));
					return;
				}
				if (bingKey == null || bingKey.equals("")) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preference.BingPreferencePage.msgTitle"),
							Messages.getString("preference.BingPreferencePage.msg2"));
					return;
				}
				bingValidator();
				setComponentsState();
				if (!bingState) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preference.BingPreferencePage.msgTitle"),
							Messages.getString("preference.BingPreferencePage.msg3"));
					return;
				}
			}
		});
		new Label(com, SWT.NONE);

		new Label(com, SWT.NONE);
		Link link = new Link(com, SWT.NONE);
		link.setText("<a>" + Messages.getString("preference.BingPreferencePage.link") + "</a>");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://msdn.microsoft.com/en-us/library/hh454950.aspx");
			}
		});
		link.setToolTipText("http://msdn.microsoft.com/en-us/library/hh454950.aspx");
		new Label(com, SWT.NONE);
		lbKeySetting.computeSize();
		return tparent;
	}

	private Composite createIgnoreArea(Composite tparent) {
		Group ignoreGroup = new Group(tparent, SWT.NONE);
		GridDataFactory.createFrom(new GridData(GridData.FILL_HORIZONTAL)).applyTo(ignoreGroup);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(ignoreGroup);
		ignoreGroup.setText(Messages.getString("dialog.PreMachineTranslationDialog.ignore"));
		ignoreExactMatchBtn = new Button(ignoreGroup, SWT.CHECK);
		ignoreExactMatchBtn.setText(Messages.getString("dialog.PreMachineTranslationDialog.exactmatch"));
		ignoreLockBtn = new Button(ignoreGroup, SWT.CHECK);
		ignoreLockBtn.setText(Messages.getString("dialog.PreMachineTranslationDialog.lock"));
		return tparent;
	}

	private Composite createTranslateSettingArea(Composite tparent) {
		Group apiAccessibilityGroup = new Group(tparent, SWT.NONE);
		apiAccessibilityGroup.setLayout(new GridLayout(1, false));
		apiAccessibilityGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		apiAccessibilityGroup.setText(Messages.getString("preference.machinetranslattionPage.accessMachineServer"));

		HsImageLabel accessibility = new HsImageLabel(
				Messages.getString("preference.machinetranslattionPage.accessMachineServer.msg"),
				Activator.getImageDescriptor("images/trans_google_set_32.png"));
		Composite accessibilityComp = accessibility.createControl(apiAccessibilityGroup);
		accessibilityComp.setLayout(new GridLayout());

		alwaysAccessBtn = new Button(accessibilityComp, SWT.RADIO);
		alwaysAccessBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		alwaysAccessBtn.setText(Messages.getString("preference.GooglePreferencePage.alwaysAccessBtn"));
		alwaysAccessBtn.setEnabled(false);
		alwaysAccessBtn.setSelection(false);

		manualAccessBtn = new Button(accessibilityComp, SWT.RADIO);
		manualAccessBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		manualAccessBtn.setText(Messages.getString("preference.GooglePreferencePage.neverAccessBtn"));
		manualAccessBtn.setEnabled(false);
		manualAccessBtn.setSelection(false);
		accessibility.computeSize();
		return tparent;
	}

	@Override
	protected void performDefaults() {
		setDefaultValues();
	}

	@Override
	protected void performApply() {
		super.performApply();

	}

	@Override
	public boolean performOk() {
		googleValidator();
		bingValidator();
		// if(this.bingState){
		ps.setValue(IPreferenceConstant.BING_STATE, this.bingState);
		ps.setValue(IPreferenceConstant.BING_ID, idText.getText());
		ps.setValue(IPreferenceConstant.BING_KEY, bingKeyText.getText());
		// }
		// if(this.googleState){
		ps.setValue(IPreferenceConstant.GOOGLE_KEY, googleKeyText.getText());
		ps.setValue(IPreferenceConstant.GOOGLE_STATE, this.googleState);
		// }
		ps.setValue(IPreferenceConstant.ALWAYS_ACCESS, alwaysAccessBtn.getSelection());
		ps.setValue(IPreferenceConstant.MANUAL_ACCESS, manualAccessBtn.getSelection());

		ps.setValue(IPreferenceConstant.IGNORE_EXACT_MATCH, ignoreExactMatchBtn.getSelection());
		ps.setValue(IPreferenceConstant.INGORE_LOCK, ignoreLockBtn.getSelection());
		setComponentsState();
		return true;
	}

	/**
	 * 访问goolge api以验证 Key是否可用。 ;
	 */
	private void googleValidator() {
		final String googleKey = googleKeyText.getText();
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				if (googleKey != null && !googleKey.trim().equals("")) {
					GoogleAPI.setHttpReferrer("http://www.heartsome.net");
					GoogleAPI.setKey(googleKey);
					try {
						String result = Translate.DEFAULT.execute("test", GoogleTransUtils.processLanguage("en-US"),
								GoogleTransUtils.processLanguage("zh-CN"));
						if (result.equals("测试")) {
							googleState = true;
						}
					} catch (GoogleAPIException e) {
						googleState = false;
					}
				} else {
					googleState = false;
				}
			}
		});
	}

	/**
	 * 访问bing api是否可用 ;
	 */
	private void bingValidator() {

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				String id = idText.getText();
				String bingKey = bingKeyText.getText();
				if (bingKey != null && !bingKey.trim().equals("") && id != null && !id.equals("")) {
					com.memetix.mst.translate.Translate.setClientId(id);
					com.memetix.mst.translate.Translate.setClientSecret(bingKey);
					try {
						String result = com.memetix.mst.translate.Translate.execute("test",
								BingTransUtils.processLanguage("en-US"), BingTransUtils.processLanguage("zh-CN"));
						if (result.equals("测试")) {
							bingState = true;
						} else {
							bingState = false;
						}
					} catch (Exception e) {
						bingState = false;
					}
				} else {
					bingState = false;
				}
			}
		});

	}

	private void setDefaultValues() {

		boolean goolgeDefault = ps.getDefaultBoolean(IPreferenceConstant.GOOGLE_STATE);
		// ps.setValue(IPreferenceConstant.GOOGLE_STATE, goolgeDefault);
		this.googleState = goolgeDefault;

		String goolgeKey = ps.getDefaultString(IPreferenceConstant.GOOGLE_KEY);
		// ps.setValue(IPreferenceConstant.GOOGLE_KEY, goolgeKey);
		this.googleKeyText.setText(goolgeKey);

		boolean bingState = ps.getDefaultBoolean(IPreferenceConstant.BING_STATE);
		// ps.setValue(IPreferenceConstant.BING_STATE, bingState);
		this.bingState = bingState;

		String bingId = ps.getDefaultString(IPreferenceConstant.BING_ID);
		// ps.setValue(IPreferenceConstant.BING_ID, bingId);
		this.idText.setText(bingId);

		String bingKey = ps.getDefaultString(IPreferenceConstant.BING_KEY);
		// ps.setValue(IPreferenceConstant.BING_KEY, bingKey);
		this.bingKeyText.setText(bingKey);

		boolean awaysAccess = ps.getDefaultBoolean(IPreferenceConstant.ALWAYS_ACCESS);
		this.alwaysAccessBtn.setSelection(awaysAccess);

		boolean mamualAccess = ps.getDefaultBoolean(IPreferenceConstant.MANUAL_ACCESS);
		this.manualAccessBtn.setSelection(mamualAccess);

		this.ignoreExactMatchBtn.setSelection(ps.getDefaultBoolean(IPreferenceConstant.IGNORE_EXACT_MATCH));
		this.ignoreLockBtn.setSelection(ps.getDefaultBoolean(IPreferenceConstant.INGORE_LOCK));

		setComponentsState();
	}

	private void setValues() {

		this.googleState = ps.getBoolean(IPreferenceConstant.GOOGLE_STATE);
		this.googleKeyText.setText(ps.getString(IPreferenceConstant.GOOGLE_KEY));

		this.bingState = ps.getBoolean(IPreferenceConstant.BING_STATE);
		this.bingKeyText.setText(ps.getString(IPreferenceConstant.BING_KEY));
		this.idText.setText(ps.getString(IPreferenceConstant.BING_ID));

		this.ignoreExactMatchBtn.setSelection(ps.getBoolean(IPreferenceConstant.IGNORE_EXACT_MATCH));
		this.ignoreLockBtn.setSelection(ps.getBoolean(IPreferenceConstant.INGORE_LOCK));

		setComponentsState();
	}

	/**
	 * 设置组件的状态 ;
	 */
	private void setComponentsState() {
		// 设置google标签的状态
		if (this.googleState) {
			googleStateLabel.setImage(rightImage);
		} else {
			googleStateLabel.setImage(errorImage);
		}

		if (this.bingState) {
			bingStateLable.setImage(rightImage);
		} else {
			bingStateLable.setImage(errorImage);
		}
		// 设置访问策略的状态
		if (this.googleState || this.bingState) {
			alwaysAccessBtn.setEnabled(true);
			alwaysAccessBtn.setSelection(ps.getBoolean(IPreferenceConstant.ALWAYS_ACCESS));
			manualAccessBtn.setEnabled(true);
			manualAccessBtn.setSelection(ps.getBoolean(IPreferenceConstant.MANUAL_ACCESS));
		} else {
			alwaysAccessBtn.setEnabled(false);
			manualAccessBtn.setEnabled(false);
			// alwaysAccessBtn.setSelection(false);
			// manualAccessBtn.setSelection(false);
		}
	}

	@Override
	public void dispose() {
		if (rightImage != null) {
			rightImage.dispose();
		}
		if (errorImage != null) {
			errorImage.dispose();
		}
		super.dispose();
	}
}
