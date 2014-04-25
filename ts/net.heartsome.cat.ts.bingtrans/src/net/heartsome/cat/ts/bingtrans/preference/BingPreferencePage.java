/**
 * BingPreferencePage.java
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
package net.heartsome.cat.ts.bingtrans.preference;

import net.heartsome.cat.ts.bingtrans.Activator;
import net.heartsome.cat.ts.bingtrans.BingTransUtils;
import net.heartsome.cat.ts.bingtrans.bean.IPreferenceConstant;
import net.heartsome.cat.ts.bingtrans.resource.Messages;
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

import com.memetix.mst.translate.Translate;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class BingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IPreferenceStore ps;

	private Text idText;
	private Text bingKeyText;
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
	public BingPreferencePage() {
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

		stateLabel = new Label(com, SWT.NONE);

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
				validator();
				enableComponent(state);
				if (!state) {
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

		Group apiAccessibilityGroup = new Group(tparent, SWT.NONE);
		apiAccessibilityGroup.setLayout(new GridLayout(1, false));
		apiAccessibilityGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		apiAccessibilityGroup.setText(Messages.getString("preference.BingPreferencePage.apiAccessibilityGroup"));

		HsImageLabel accessibility = new HsImageLabel(
				Messages.getString("preference.BingPreferencePage.accessibility"),
				Activator.getImageDescriptor("images/trans_bing_set_32.png"));
		Composite accessibilityComp = accessibility.createControl(apiAccessibilityGroup);
		accessibilityComp.setLayout(new GridLayout());
		noRepeatAccessBtn = new Button(accessibilityComp, SWT.RADIO);
		noRepeatAccessBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		noRepeatAccessBtn.setText(Messages.getString("preference.BingPreferencePage.noRepeatAccessBtn"));
		noRepeatAccessBtn.setEnabled(false);
		noRepeatAccessBtn.setSelection(false);

		alwaysAccessBtn = new Button(accessibilityComp, SWT.RADIO);
		alwaysAccessBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		alwaysAccessBtn.setText(Messages.getString("preference.BingPreferencePage.alwaysAccessBtn"));
		alwaysAccessBtn.setEnabled(false);
		alwaysAccessBtn.setSelection(false);

		manualAccessBtn = new Button(accessibilityComp, SWT.RADIO);
		manualAccessBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		manualAccessBtn.setText(Messages.getString("preference.BingPreferencePage.neverAccessBtn"));
		manualAccessBtn.setEnabled(false);
		manualAccessBtn.setSelection(false);
		accessibility.computeSize();

		if (CommonFunction.checkEdition("U")) {
			Group preTransGroup = new Group(tparent, SWT.NONE);
			preTransGroup.setLayout(new GridLayout(1, false));
			preTransGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			preTransGroup.setText(Messages.getString("preference.BingPreferencePage.preTransGroup"));

			HsImageLabel preTrans = new HsImageLabel(Messages.getString("preference.BingPreferencePage.preTrans"),
					Activator.getImageDescriptor("images/trans_bing_trans_32.png"));
			Composite group = preTrans.createControl(preTransGroup);
			group.setLayout(new GridLayout());

			suportPreTransBtn = new Button(group, SWT.CHECK);
			suportPreTransBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			suportPreTransBtn.setText(Messages.getString("preference.BingPreferencePage.suportPreTransBtn"));
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
			boolean never = ps.getBoolean(IPreferenceConstant.MANUAL_ACCESS);

			if (!(noRepeate || always || never)) {
				noRepeate = ps.getDefaultBoolean(IPreferenceConstant.NO_REPEATE_ACCESS);
				always = ps.getDefaultBoolean(IPreferenceConstant.ALWAYS_ACCESS);
				never = ps.getDefaultBoolean(IPreferenceConstant.MANUAL_ACCESS);
			}
			alwaysAccessBtn.setSelection(always);
			manualAccessBtn.setSelection(never);
			noRepeatAccessBtn.setSelection(noRepeate);
			boolean preTrans = ps.getBoolean(IPreferenceConstant.PRETRANS_STATE);

			if (CommonFunction.checkEdition("U")) {
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
		String id = idText.getText();
		String bingKey = bingKeyText.getText();
		if (id == null || id.equals("")) {
			state = false;
			enableComponent(state);
		} else 	if (bingKey == null || bingKey.equals("")) {
			state = false;
			enableComponent(state);
		} else {
			validator();
			enableComponent(state);
			if (!state) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("preference.BingPreferencePage.msgTitle"),
						Messages.getString("preference.BingPreferencePage.msg3"));
			}
		}		
	}

	@Override
	public boolean performOk() {

		String id = idText.getText();
		String key = bingKeyText.getText();
		
		if (id == null || id.equals("")) {
			state = false;
		} else 	if (key == null || key.equals("")) {
			state = false;
		} else {
			validator();
		}	
		
		boolean always = alwaysAccessBtn.getSelection();
		boolean manual = manualAccessBtn.getSelection();

		ps.setValue(IPreferenceConstant.STATE, state);
		ps.setValue(IPreferenceConstant.ID, id);
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

	private void validator() {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				String id = idText.getText();
				String bingKey = bingKeyText.getText();
				if (bingKey != null && !bingKey.trim().equals("") && id != null && !id.equals("")) {
					Translate.setClientId(id);
					Translate.setClientSecret(bingKey);
					try {
						String result = Translate.execute("test", BingTransUtils.processLanguage("en-US"),
								BingTransUtils.processLanguage("zh-CN"));
						if (result.equals("测试")) {
							state = true;
						} else {
							state = false;
						}
					} catch (Exception e) {
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
			this.idText.setText(ps.getDefaultString(IPreferenceConstant.ID));
			this.bingKeyText.setText(ps.getDefaultString(IPreferenceConstant.KEY));
			enableComponent(state); // 根据需要是否禁用其他选项
		} else {
			this.state = ps.getBoolean(IPreferenceConstant.STATE);
			enableComponent(state);
			this.idText.setText(ps.getString(IPreferenceConstant.ID));
			this.bingKeyText.setText(ps.getString(IPreferenceConstant.KEY));
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
