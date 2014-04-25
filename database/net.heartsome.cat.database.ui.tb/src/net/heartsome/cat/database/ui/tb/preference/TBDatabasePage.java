package net.heartsome.cat.database.ui.tb.preference;

import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.database.ui.tb.resource.Messages;
import net.heartsome.cat.common.ui.HsImageLabel;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * 首选项>术语库设置界面
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TBDatabasePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.database.ui.tb.preference.tbpage";

	private IPreferenceStore preferenceStore;

	/** 术语库更新设置 > 始终增加单选按钮 */
	private Button btnAlwaysAdd;

	/** 术语库更新设置 > 重复覆盖单选按钮 */
	private Button btnRepeatOverwrite;

	/** 术语库更新设置 > 重复合并单选按钮 */
	private Button btnRepeatMerge;

	/** 术语库更新设置 > 重复忽略单选按钮 */
	private Button btnRepeatIgnore;


	private Button btnCaseSensitive;
	
	/**
	 * 构造函数
	 */
	public TBDatabasePage() {
		setTitle(Messages.getString("preference.TBDatabasePage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));
        // 添加术语匹配是否忽略大小写
		{
			Group commonGroup1 = new Group(tparent, SWT.NONE);
			commonGroup1.setLayout(new GridLayout());
			commonGroup1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			commonGroup1.setText(Messages.getString("preference.TBDatabasePage.commontitle"));

			HsImageLabel imageLbale1 = new HsImageLabel(Messages.getString("preference.TBDatabasePage.commonsearchterm"),
					Activator.getImageDescriptor("images/preference/tb/tb_update_32.png"));
			Composite comp1 = imageLbale1.createControl(commonGroup1);
			
			btnCaseSensitive = new Button(comp1, SWT.CHECK);
			btnCaseSensitive.setText(Messages.getString("preference.TBDatabasePage.commonCasesensitive"));
			btnCaseSensitive.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));	
			imageLbale1.computeSize();
		}
		Group commonGroup = new Group(tparent, SWT.NONE);
		commonGroup.setLayout(new GridLayout());
		commonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		commonGroup.setText(Messages.getString("preference.TBDatabasePage.commonGroup"));

		HsImageLabel imageLbale = new HsImageLabel(Messages.getString("preference.TBDatabasePage.imageLbale"),
				Activator.getImageDescriptor("images/preference/tb/tb_update_32.png"));
		Composite comp = imageLbale.createControl(commonGroup);

		btnAlwaysAdd = new Button(comp, SWT.RADIO);
		btnAlwaysAdd.setText(Messages.getString("preference.TBDatabasePage.btnAlwaysAdd"));
		btnAlwaysAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRepeatOverwrite = new Button(comp, SWT.RADIO);
		btnRepeatOverwrite.setText(Messages.getString("preference.TBDatabasePage.btnRepeatOverwrite"));
		btnRepeatOverwrite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRepeatMerge = new Button(comp, SWT.RADIO);
		btnRepeatMerge.setText(Messages.getString("preference.TBDatabasePage.btnRepeatMerge"));
		btnRepeatMerge.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRepeatIgnore = new Button(comp, SWT.RADIO);
		btnRepeatIgnore.setText(Messages.getString("preference.TBDatabasePage.btnRepeatIgnore"));
		btnRepeatIgnore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		parent.pack();
		imageLbale.computeSize();

		int intUpdateTB = preferenceStore.getInt(TBPreferenceConstants.TB_UPDATE);
		setInitValues(intUpdateTB);
		return parent;
	}

	@Override
	public boolean performOk() {
		if (btnAlwaysAdd.getSelection()) {
			preferenceStore.setValue(TBPreferenceConstants.TB_UPDATE, TBPreferenceConstants.TB_ALWAYS_ADD);
		} else if (btnRepeatOverwrite.getSelection()) {
			preferenceStore.setValue(TBPreferenceConstants.TB_UPDATE, TBPreferenceConstants.TB_REPEAT_OVERWRITE);
		} else if (btnRepeatMerge.getSelection()) {
			preferenceStore.setValue(TBPreferenceConstants.TB_UPDATE, TBPreferenceConstants.TB_REPEAT_MERGE);
		} else if (btnRepeatIgnore.getSelection()) {
			preferenceStore.setValue(TBPreferenceConstants.TB_UPDATE, TBPreferenceConstants.TB_REPEAT_IGNORE);
		}
		// 设置是否忽略大小写
		preferenceStore.setDefault(TBPreferenceConstants.TB_CASE_SENSITIVE, btnCaseSensitive.getSelection());
		// 
		PlatformUI.getPreferenceStore().setDefault(TBPreferenceConstants.TB_CASE_SENSITIVE, btnCaseSensitive.getSelection());
		return true;
	}

	@Override
	protected void performDefaults() {
		int intUpdateTB = preferenceStore.getDefaultInt(TBPreferenceConstants.TB_UPDATE);
		setInitValues(intUpdateTB);
	}

	private void setInitValues(int intUpdateTB) {
		if (intUpdateTB == TBPreferenceConstants.TB_ALWAYS_ADD) {
			btnAlwaysAdd.setSelection(true);
			btnRepeatOverwrite.setSelection(false);
			btnRepeatMerge.setSelection(false);
			btnRepeatIgnore.setSelection(false);
		} else if (intUpdateTB == TBPreferenceConstants.TB_REPEAT_OVERWRITE) {
			btnAlwaysAdd.setSelection(false);
			btnRepeatOverwrite.setSelection(true);
			btnRepeatMerge.setSelection(false);
			btnRepeatIgnore.setSelection(false);
		} else if (intUpdateTB == TBPreferenceConstants.TB_REPEAT_MERGE) {
			btnAlwaysAdd.setSelection(false);
			btnRepeatOverwrite.setSelection(false);
			btnRepeatMerge.setSelection(true);
			btnRepeatIgnore.setSelection(false);
		} else if (intUpdateTB == TBPreferenceConstants.TB_REPEAT_IGNORE) {
			btnAlwaysAdd.setSelection(false);
			btnRepeatOverwrite.setSelection(false);
			btnRepeatMerge.setSelection(false);
			btnRepeatIgnore.setSelection(true);
		}
		
		// 设置是否忽略大小写
		
		btnCaseSensitive.setSelection(preferenceStore.getDefaultBoolean(TBPreferenceConstants.TB_CASE_SENSITIVE));
		
	}
}
