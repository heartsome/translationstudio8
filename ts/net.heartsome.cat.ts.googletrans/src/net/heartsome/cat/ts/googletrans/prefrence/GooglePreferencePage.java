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
package net.heartsome.cat.ts.googletrans.prefrence;

import net.heartsome.cat.ts.googletrans.Activator;
import net.heartsome.cat.ts.googletrans.GoogleTransUtils;
import net.heartsome.cat.ts.googletrans.bean.IPreferenceConstant;
import net.heartsome.cat.ts.googletrans.resource.Messages;
import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.jface.dialogs.MessageDialog;
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
public class GooglePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IPreferenceStore ps;

	private Text googleKeyText;
	private Label stateLabel;

	// accessibility widget
	private Button noRepeatAccessBtn;
	private Button alwaysAccessBtn;
	private Button manualAccessBtn;

	// preTranslation suport
	private Button suportPreTransBtn;

	private Image rightImage;
	private Image errorImage;
	private boolean state;

	/**
	 * constructor
	 */
	public GooglePreferencePage() {
		// setDescription(des);
		// setImageDescriptor(Activator.getImageDescriptor("images/google-translation.png"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		ps = getPreferenceStore();
		state = ps.getBoolean(IPreferenceConstant.STATE);
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

		stateLabel = new Label(com, SWT.NONE);
		stateLabel.setImage(errorImage);

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
				validator();
				enableComponent(state);
				if (!state) {
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

		Group apiAccessibilityGroup = new Group(tparent, SWT.NONE);
		apiAccessibilityGroup.setLayout(new GridLayout(1, false));
		apiAccessibilityGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		apiAccessibilityGroup.setText(Messages.getString("preference.GooglePreferencePage.apiAccessibilityGroup"));

		HsImageLabel accessibility = new HsImageLabel(
				Messages.getString("preference.GooglePreferencePage.accessibility"),
				Activator.getImageDescriptor("images/trans_google_set_32.png"));
		Composite accessibilityComp = accessibility.createControl(apiAccessibilityGroup);
		accessibilityComp.setLayout(new GridLayout());
		noRepeatAccessBtn = new Button(accessibilityComp, SWT.RADIO);
		noRepeatAccessBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		noRepeatAccessBtn.setText(Messages.getString("preference.GooglePreferencePage.noRepeatAccessBtn"));
		noRepeatAccessBtn.setEnabled(false);
		noRepeatAccessBtn.setSelection(false);

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

		if (CommonFunction.checkEdition("U")) {
			Group preTransGroup = new Group(tparent, SWT.NONE);
			preTransGroup.setLayout(new GridLayout(1, false));
			preTransGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			preTransGroup.setText(Messages.getString("preference.GooglePreferencePage.preTransGroup"));

			HsImageLabel preTrans = new HsImageLabel(Messages.getString("preference.GooglePreferencePage.preTrans"),
					Activator.getImageDescriptor("images/trans_google_trans_32.png"));
			Composite group = preTrans.createControl(preTransGroup);
			group.setLayout(new GridLayout());

			suportPreTransBtn = new Button(group, SWT.CHECK);
			suportPreTransBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			suportPreTransBtn.setText(Messages.getString("preference.GooglePreferencePage.suportPreTransBtn"));
			suportPreTransBtn.setEnabled(false);
			suportPreTransBtn.setSelection(false);
			preTrans.computeSize();
		}

		setValues(false);

		return tparent;
	}

	private void enableComponent(boolean currentState) {
		alwaysAccessBtn.setEnabled(currentState);
		manualAccessBtn.setEnabled(currentState);
		noRepeatAccessBtn.setEnabled(currentState);
		if (CommonFunction.checkEdition("U")) {
			suportPreTransBtn.setEnabled(currentState);
		}

		if (currentState) {
			stateLabel.setImage(rightImage);
			boolean noRepeate = ps.getBoolean(IPreferenceConstant.NO_REPEATE_ACCESS);
			boolean always = ps.getBoolean(IPreferenceConstant.ALWAYS_ACCESS);
			boolean manual = ps.getBoolean(IPreferenceConstant.MANUAL_ACCESS);

			if (!(noRepeate || always || manual)) {
				noRepeate = ps.getDefaultBoolean(IPreferenceConstant.NO_REPEATE_ACCESS);
				always = ps.getDefaultBoolean(IPreferenceConstant.ALWAYS_ACCESS);
				manual = ps.getDefaultBoolean(IPreferenceConstant.MANUAL_ACCESS);
			}
			alwaysAccessBtn.setSelection(always);
			manualAccessBtn.setSelection(manual);
			noRepeatAccessBtn.setSelection(noRepeate);

			if (CommonFunction.checkEdition("U")) {
				boolean preTrans = ps.getBoolean(IPreferenceConstant.PRETRANS_STATE);
				suportPreTransBtn.setSelection(preTrans);
			}
		} else {
			stateLabel.setImage(errorImage);
			alwaysAccessBtn.setSelection(false);
			manualAccessBtn.setSelection(false);
			noRepeatAccessBtn.setSelection(false);
			if (CommonFunction.checkEdition("U")) {
				suportPreTransBtn.setSelection(false);
			}
		}
		// ps.setValue(IPreferenceConstant.STATE, currentState); // 记录当前通过了验证
	}

	@Override
	protected void performDefaults() {
		setValues(true);
	}

	@Override
	protected void performApply() {
		super.performApply();
		String key = googleKeyText.getText();
		if (key == null || key.equals("")) {
			state = false;
		} else {
			validator();
			if (!state) {
				MessageDialog.openError(getShell(), Messages.getString("preference.GooglePreferencePage.msgTitle"),
						Messages.getString("preference.GooglePreferencePage.msg2"));
				return;
			}
		}
		enableComponent(state);
	}

	@Override
	public boolean performOk() {

		String key = googleKeyText.getText();
		
		if (key == null || key.equals("")) {
			state = false;
		} else {
			validator();			
		}
		
		boolean always = alwaysAccessBtn.getSelection();
		boolean manual = manualAccessBtn.getSelection();

		ps.setValue(IPreferenceConstant.STATE, state);
		ps.setValue(IPreferenceConstant.KEY, key);
		ps.setValue(IPreferenceConstant.ALWAYS_ACCESS, always);
		ps.setValue(IPreferenceConstant.MANUAL_ACCESS, manual);
		boolean noRepeat = noRepeatAccessBtn.getSelection();
		ps.setValue(IPreferenceConstant.NO_REPEATE_ACCESS, noRepeat);
		if (CommonFunction.checkEdition("U")) {
			boolean preTrans = suportPreTransBtn.getSelection();
			ps.setValue(IPreferenceConstant.PRETRANS_STATE, preTrans);
		}

		return true;
	}

	/** @return the checkResult */
	public boolean isCheckResult() {
		return state;
	}

	/**
	 * @param checkResult
	 *            the checkResult to set
	 */
	public void setCheckResult(boolean checkResult) {
		this.state = checkResult;
	}

	/**
	 * 访问api以验证 Key是否可用。 ;
	 */
	private void validator() {
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
							state = true;
						}
					} catch (GoogleAPIException e) {
						state = false;
					}
				} else {
					state = false;
				}
			}
		});
	}

	private void setValues(boolean blnIsApplyDefault) {
		if (blnIsApplyDefault) {
			this.state = ps.getDefaultBoolean(IPreferenceConstant.STATE);
			this.googleKeyText.setText(ps.getDefaultString(IPreferenceConstant.KEY));
			enableComponent(false); // 在这里面禁用其他选项
		} else {
			this.state = ps.getBoolean(IPreferenceConstant.STATE);
			enableComponent(state);
			this.googleKeyText.setText(ps.getString(IPreferenceConstant.KEY));
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
