package net.heartsome.cat.ts.ui.preferencepage.translation;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 翻译首选项
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TranslationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.ui.preferencepage.translation.TranslationPreferencePage";

	private IPreferenceStore preferenceStore;

	/** 接受翻译时调整空格位置复选框 */
	private Button btnAutoAdaptSpacePosition;

	private Button btnAutoApplyTmMatch;

	/** 无匹配时复制源文本到目标复选框 */
	private Button btnCopyToTarget;

	private Button btnAutoQuickTranslation;

	/** 跳过不可翻译的文本段复选框 */
	// private Button btnSkipNotTranslateText;

	private Text txtPath;

	private Button btnBrowse;

	/**
	 * 构造函数
	 */
	public TranslationPreferencePage() {
		setTitle(Messages.getString("translation.TranslationPreferencePage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}

	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group group = new Group(tparent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText(Messages.getString("translation.TranslationPreferencePage.group"));

		HsImageLabel imageLabel = new HsImageLabel(
				Messages.getString("translation.TranslationPreferencePage.imageLabel"),
				Activator.getImageDescriptor("images/preference/translate/trans_32.png"));

		Composite comp = imageLabel.createControl(group);

		btnAutoAdaptSpacePosition = new Button(comp, SWT.CHECK);
		btnAutoAdaptSpacePosition.setText(Messages
				.getString("translation.TranslationPreferencePage.btnAutoAdaptSpacePosition"));
		btnAutoAdaptSpacePosition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnAutoApplyTmMatch = new Button(comp, SWT.CHECK);
		btnAutoApplyTmMatch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnAutoApplyTmMatch.setText(Messages.getString("translation.TranslationPreferencePage.btnAutoApplyTmMatch"));

		btnCopyToTarget = new Button(comp, SWT.CHECK);
		btnCopyToTarget.setText(Messages.getString("translation.TranslationPreferencePage.btnCopyToTarget"));
		btnCopyToTarget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// btnSkipNotTranslateText = new Button(comp, SWT.CHECK);
		// btnSkipNotTranslateText.setText("翻译时跳过锁定文本段");
		// btnSkipNotTranslateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnAutoQuickTranslation = new Button(comp, SWT.CHECK);
		btnAutoQuickTranslation.setText(Messages
				.getString("translation.TranslationPreferencePage.btnAutoQuickTranslation"));
		btnAutoQuickTranslation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		imageLabel.computeSize();

		Group openOfficeGroup = new Group(tparent, SWT.NONE);
		openOfficeGroup.setLayout(new GridLayout());
		openOfficeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		openOfficeGroup.setText(Messages.getString("translation.TranslationPreferencePage.openOfficeGroup"));

		HsImageLabel imageLabel2 = new HsImageLabel(
				Messages.getString("translation.TranslationPreferencePage.imageLabel2"),
				Activator.getImageDescriptor("images/preference/translate/trans_office_32.png"));

		Composite composite = imageLabel2.createControl(openOfficeGroup);
		GridLayout gd = new GridLayout(3, false);
		gd.marginLeft = 0;
		gd.marginTop = 0;
		composite.setLayout(gd);

		new Label(composite, SWT.NONE).setText(Messages.getString("translation.TranslationPreferencePage.lblOO"));
		txtPath = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		txtPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnBrowse = new Button(composite, SWT.NONE);
		btnBrowse.setText(Messages.getString("translation.TranslationPreferencePage.btnBrowse"));

		imageLabel2.computeSize();

		setValues(false);
		initListener();
		return tparent;
	}

	@Override
	protected void performDefaults() {
		setValues(true);
	}

	@Override
	public boolean performOk() {
		preferenceStore.setValue(ITranslationPreferenceConstants.AUTO_ADAPT_SPACE_POSITION,
				btnAutoAdaptSpacePosition.getSelection());
		preferenceStore.setValue(ITranslationPreferenceConstants.COPY_SOURCE_TO_TARGET, btnCopyToTarget.getSelection());
		preferenceStore.setValue(ITranslationPreferenceConstants.AUTO_QUICK_TRANSLATION,
				btnAutoQuickTranslation.getSelection());
		preferenceStore.setValue(ITranslationPreferenceConstants.AUTO_APPLY_TM_MATCH,
				btnAutoApplyTmMatch.getSelection());

		// preferenceStore.setValue(ITranslationPreferenceConstants.SKIP_NOT_TRANSLATE_TEXT,
		// btnSkipNotTranslateText.getSelection());

		preferenceStore.setValue(ITranslationPreferenceConstants.PATH_OF_OPENOFFICE, txtPath.getText() == null ? ""
				: txtPath.getText());
		preferenceStore.setValue(ITranslationPreferenceConstants.ENABLED_OF_OPENOFFICE,
				txtPath.getText() == null || txtPath.getText().trim().equals("") ? false : true);
		return true;
	}

	private void initListener() {

		btnBrowse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog flg = new FileDialog(btnBrowse.getShell(), SWT.OPEN);
				String[] filter = new String[] { "soffice;soffice.*", "*" };
				flg.setFilterExtensions(filter);
				String path = txtPath.getText();
				if (path == null || path.trim().equals("")) {
					flg.setFilterPath(System.getProperty("user.home"));
				} else {
					flg.setFilterPath(path.substring(0, path.lastIndexOf(System.getProperty("file.separator"))));
					flg.setFileName(path.substring(path.lastIndexOf(System.getProperty("file.separator")) + 1));
				}
				String stropen = flg.open();
				String flgstr = stropen == null ? "" : stropen;
				if (flgstr.equals("")) {
					return;
				} else {
					txtPath.setText(flgstr);
				}
				filter = null;

			}
		});
	}

	private void setValues(boolean blnIsApplyDefault) {
		if (blnIsApplyDefault) {
			btnAutoAdaptSpacePosition.setSelection(preferenceStore
					.getDefaultBoolean(ITranslationPreferenceConstants.AUTO_ADAPT_SPACE_POSITION));
			btnCopyToTarget.setSelection(preferenceStore
					.getDefaultBoolean(ITranslationPreferenceConstants.COPY_SOURCE_TO_TARGET));
			btnAutoQuickTranslation.setSelection(preferenceStore
					.getDefaultBoolean(ITranslationPreferenceConstants.AUTO_QUICK_TRANSLATION));

			btnAutoApplyTmMatch.setSelection(preferenceStore
					.getDefaultBoolean(ITranslationPreferenceConstants.AUTO_APPLY_TM_MATCH));
			//
			// btnSkipNotTranslateText.setSelection(preferenceStore
			// .getDefaultBoolean(ITranslationPreferenceConstants.SKIP_NOT_TRANSLATE_TEXT));

			txtPath.setText(preferenceStore.getDefaultString(ITranslationPreferenceConstants.PATH_OF_OPENOFFICE));
		} else {
			btnAutoAdaptSpacePosition.setSelection(preferenceStore
					.getBoolean(ITranslationPreferenceConstants.AUTO_ADAPT_SPACE_POSITION));
			btnCopyToTarget.setSelection(preferenceStore
					.getBoolean(ITranslationPreferenceConstants.COPY_SOURCE_TO_TARGET));
			btnAutoQuickTranslation.setSelection(preferenceStore
					.getBoolean(ITranslationPreferenceConstants.AUTO_QUICK_TRANSLATION));

			btnAutoApplyTmMatch.setSelection(preferenceStore
					.getBoolean(ITranslationPreferenceConstants.AUTO_APPLY_TM_MATCH));
			//
			// btnSkipNotTranslateText.setSelection(preferenceStore
			// .getBoolean(ITranslationPreferenceConstants.SKIP_NOT_TRANSLATE_TEXT));

			txtPath.setText(preferenceStore.getString(ITranslationPreferenceConstants.PATH_OF_OPENOFFICE));
		}
	}

}
