//package net.heartsome.cat.ts.pretranslation.preference;
//
//import net.heartsome.cat.ts.pretranslation.Activator;
//import net.heartsome.cat.ts.pretranslation.bean.IPreTransConstants;
//import net.heartsome.cat.ts.pretranslation.resource.Messages;
//import net.heartsome.cat.common.ui.HsImageLabel;
//import net.heartsome.cat.common.util.CommonFunction;
//
//import org.eclipse.jface.preference.IPreferenceStore;
//import org.eclipse.jface.preference.PreferencePage;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Group;
//import org.eclipse.ui.IWorkbench;
//import org.eclipse.ui.IWorkbenchPreferencePage;
//
///**
// * 预翻译首选项
// * @author peason
// * @version
// * @since JDK1.6
// */
//public class PreTranslationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
//
//	public static final String ID = "net.heartsome.cat.ts.pretranslation.preferencepage";
//
//	private IPreferenceStore preferenceStore;
//
//	private Button lockedFullMatchBtn;
//	private Button lockedContextMatchBtn;
//
//	// /** 是否覆盖 100% 匹配复选框 */
//	// private Button btnOverwriteMatch100;
//
//	/** 模糊匹配 > 保留现有匹配单选按钮 */
//	private Button btnKeepNowMatch;
//
//	/** 模糊匹配 > 覆盖现有匹配单选按钮 */
//	private Button btnOverwriteNowMatch;
//
//	/** 模糊匹配 > 始终覆盖现有匹配单选按钮 */
//	private Button btnAlwaysOverwriteNowMatch;
//
//	/**
//	 * 构造函数
//	 */
//	public PreTranslationPreferencePage() {
//		setTitle(Messages.getString("preference.PreTranslationPreferencePage.title"));
//		setPreferenceStore(Activator.getDefault().getPreferenceStore());
//		preferenceStore = getPreferenceStore();
//	}
//
//	public void init(IWorkbench workbench) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	protected Control createContents(Composite parent) {
//		Composite tparent = new Composite(parent, SWT.NONE);
//		tparent.setLayout(new GridLayout());
//		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));
//
//		Group settingGroup = new Group(tparent, SWT.NONE);
//		settingGroup.setLayout(new GridLayout());
//		settingGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		settingGroup.setText(Messages.getString("preference.PreTranslationPreferencePage.settingGroup"));
//		HsImageLabel settingImageLabel = new HsImageLabel(
//				Messages.getString("preference.PreTranslationPreferencePage.settingImageLabel"),
//				Activator.getImageDescriptor("images/preference/trans_pre_32.png"));
//		Composite settingComp = settingImageLabel.createControl(settingGroup);
//
//		lockedFullMatchBtn = new Button(settingComp, SWT.CHECK);
//		lockedFullMatchBtn.setText(Messages.getString("preference.PreTranslationPreferencePage.lockedFullMatchBtn"));
//		lockedFullMatchBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//		if (CommonFunction.checkEdition("U")) {
//			lockedContextMatchBtn = new Button(settingComp, SWT.CHECK);
//			lockedContextMatchBtn.setText(Messages
//					.getString("preference.PreTranslationPreferencePage.lockedContextMatchBtn"));
//			lockedContextMatchBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		}
//
//		settingImageLabel.computeSize();
//
//		Group matchGroup = new Group(tparent, SWT.NONE);
//		matchGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		matchGroup.setLayout(new GridLayout());
//		matchGroup.setText(Messages.getString("preference.PreTranslationPreferencePage.matchGroup"));
//
//		// HsImageLabel imageLabel = new HsImageLabel("", null);
//		// Composite matchComp = imageLabel.createControl(matchGroup);
//
//		btnKeepNowMatch = new Button(matchGroup, SWT.RADIO);
//		btnKeepNowMatch.setText(Messages.getString("preference.PreTranslationPreferencePage.btnKeepNowMatch"));
//		btnKeepNowMatch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//		btnOverwriteNowMatch = new Button(matchGroup, SWT.RADIO);
//		btnOverwriteNowMatch
//				.setText(Messages.getString("preference.PreTranslationPreferencePage.btnOverwriteNowMatch"));
//		btnOverwriteNowMatch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//		btnAlwaysOverwriteNowMatch = new Button(matchGroup, SWT.RADIO);
//		btnAlwaysOverwriteNowMatch.setText(Messages
//				.getString("preference.PreTranslationPreferencePage.btnAlwaysOverwriteNowMatch"));
//		btnAlwaysOverwriteNowMatch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		// imageLabel.computeSize();
//
//		setValues(false);
//		return parent;
//	}
//
//	@Override
//	protected void performDefaults() {
//		setValues(true);
//	}
//
//	@Override
//	public boolean performOk() {
//
//		preferenceStore.setValue(IPreTransConstants.LOCK_FULL_MATCH, lockedFullMatchBtn.getSelection());
//		if (CommonFunction.checkEdition("U")) {
//			preferenceStore.setValue(IPreTransConstants.LOCK_CONTEXT_MATCH, lockedContextMatchBtn.getSelection());
//		}
//
//		int intFuzzyMatch = -1;
//		if (btnKeepNowMatch.getSelection()) {
//			intFuzzyMatch = IPreTransConstants.UPDATE_KEEP_NOW;
//		} else if (btnOverwriteNowMatch.getSelection()) {
//			intFuzzyMatch = IPreTransConstants.UPDATE_OVERWRITE_BY_QUALITY;
//		} else if (btnAlwaysOverwriteNowMatch.getSelection()) {
//			intFuzzyMatch = IPreTransConstants.UPDATE_ALWAYS_OVERWRITE;
//		}
//		preferenceStore.setValue(IPreTransConstants.UPDATE_STRATEGY, intFuzzyMatch);
//		return true;
//	}
//
//	private void setValues(boolean isApplyDefault) {
//		int intFuzzyMatch;
//		if (isApplyDefault) {
//			if (CommonFunction.checkEdition("U")) {
//				lockedContextMatchBtn.setSelection(preferenceStore
//						.getDefaultBoolean(IPreTransConstants.LOCK_CONTEXT_MATCH));
//			}
//			lockedFullMatchBtn.setSelection(preferenceStore.getDefaultBoolean(IPreTransConstants.LOCK_FULL_MATCH));
//
//			intFuzzyMatch = preferenceStore.getDefaultInt(IPreTransConstants.UPDATE_STRATEGY);
//		} else {
//			if (CommonFunction.checkEdition("U")) {
//				lockedContextMatchBtn.setSelection(preferenceStore.getBoolean(IPreTransConstants.LOCK_CONTEXT_MATCH));
//			}
//			lockedFullMatchBtn.setSelection(preferenceStore.getBoolean(IPreTransConstants.LOCK_FULL_MATCH));
//			intFuzzyMatch = preferenceStore.getInt(IPreTransConstants.UPDATE_STRATEGY);
//		}
//
//		if (intFuzzyMatch == IPreTransConstants.UPDATE_KEEP_NOW) {
//			btnKeepNowMatch.setSelection(true);
//			btnOverwriteNowMatch.setSelection(false);
//			btnAlwaysOverwriteNowMatch.setSelection(false);
//		} else if (intFuzzyMatch == IPreTransConstants.UPDATE_OVERWRITE_BY_QUALITY) {
//			btnKeepNowMatch.setSelection(false);
//			btnOverwriteNowMatch.setSelection(true);
//			btnAlwaysOverwriteNowMatch.setSelection(false);
//		} else if (intFuzzyMatch == IPreTransConstants.UPDATE_ALWAYS_OVERWRITE) {
//			btnKeepNowMatch.setSelection(false);
//			btnOverwriteNowMatch.setSelection(false);
//			btnAlwaysOverwriteNowMatch.setSelection(true);
//		}
//	}
//}
