package net.heartsome.cat.database.ui.tm.preference;

import net.heartsome.cat.database.bean.TMPreferenceConstants;
import net.heartsome.cat.database.ui.tm.Activator;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 首选项 > 记忆库设置界面
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TMDatabasePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.database.ui.tm.preference.tmpage";

	private IPreferenceStore preferenceStore;

	/** 上下文匹配文本段选择按钮 */
	private Spinner contextSpi;

	/** <div style='color:red'>备注，之前的 btnCaseSensitive 是区分大小写，而后来改成忽略大小写，意思恰好相反，故应取反	--robert 2012-12-10</div> */
	private Button btnCaseSensitive;

	/** 是否忽略标记复选框 */
	private Button btnIsIgnoreMark;

	/** 最大匹配数选择按钮 */
	private Spinner matchNumberSpi;

	private Label tagPenaltyLbl;
	/** 标记罚分 */
	private Spinner tagPenaltySpi;
	
	/** 最低匹配率选择按钮 */
	private Spinner minMatchSpi;

	/** 默认库优先单选按钮 */
	private Button btnDefaultDBPrecedence;

	/** 更新时间倒序排列单选按钮 */
	private Button btnDateReverse;

	/** 记忆库更新策略 > 始终增加单选按钮 */
	private Button btnAlwaysAdd;

	/** 记忆库更新策略 > 重复覆盖单选按钮 */
	private Button btnRepeatOverwrite;

	/** 记忆库更新策略 > 重复忽略单选按钮 */
	private Button btnRepeatIgnore;

	/** 组件是否初始化 -- robert，修改新建术语或记忆库点调置按钮所出现的组件未初始化就调用的BUG */
	private boolean isInit = false;

	/**
	 * 构造函数
	 */
	public TMDatabasePage() {
		setTitle(Messages.getString("preference.TMDatabasePage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;

		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group commonGroup = new Group(tparent, SWT.NONE);
		commonGroup.setLayout(new GridLayout());
		commonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		commonGroup.setText(Messages.getString("preference.TMDatabasePage.commonGroup"));
		// commonGroup.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));

		HsImageLabel setLabel = new HsImageLabel(Messages.getString("preference.TMDatabasePage.setLabel"),
				Activator.getImageDescriptor("images/preference/tm/tm_database_32.png"));
		Composite commonComp = setLabel.createControl(commonGroup);
		commonComp.setLayout(new GridLayout());

		btnCaseSensitive = new Button(commonComp, SWT.CHECK);
		btnCaseSensitive.setText(Messages.getString("preference.TMDatabasePage.btnCaseSensitive"));
		btnCaseSensitive.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnIsIgnoreMark = new Button(commonComp, SWT.CHECK);
		btnIsIgnoreMark.setText(Messages.getString("preference.TMDatabasePage.btnIsIgnoreMark"));
		btnIsIgnoreMark.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnIsIgnoreMark.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setTagPenaltyEnable();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				setTagPenaltyEnable();
			}
		});
		
		//罚分制度
		Composite tagPenaltyCom = new Composite(commonComp, SWT.NONE);
		tagPenaltyLbl = new Label(tagPenaltyCom, SWT.NONE);
		tagPenaltyLbl.setText(Messages.getString("preference.TMDatabasePage.tagPenalty"));
		
		tagPenaltySpi = new Spinner(tagPenaltyCom, SWT.BORDER);
		GridLayout pGl = new GridLayout(2, false);
		pGl.marginHeight = 0;
		pGl.marginWidth = 15;
		tagPenaltyCom.setLayout(pGl);
		tagPenaltySpi.setMinimum(1);
		tagPenaltySpi.setMaximum(100);

		
		Composite composite5 = new Composite(commonComp, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		composite5.setLayout(gl);
		Label minLabel = new Label(composite5, SWT.NONE);
		minLabel.setText(Messages.getString("preference.TMDatabasePage.minLabel"));
		minMatchSpi = new Spinner(composite5, SWT.BORDER);
		minMatchSpi.setMinimum(35);
		minMatchSpi.setMaximum(100);
		new Label(composite5, SWT.NONE).setText("%");
		minMatchSpi.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String value = minMatchSpi.getText();
				if (null == value || "".equals(value.trim())) {
					setValid(false);
					setErrorMessage(Messages.getString("preference.TMDatabasePage.msg1"));
					return;
				}
				try {
					float floatValue = Float.parseFloat(value);
					if (floatValue < 35 || floatValue > 100) {
						setValid(false);
						setErrorMessage(Messages.getString("preference.TMDatabasePage.msg2"));
						return;
					}
				} catch (NumberFormatException ex) {
					setValid(false);
					setErrorMessage(Messages.getString("preference.TMDatabasePage.msg3"));
					return;
				}
				setValid(true);
				setErrorMessage(null);
			}
		});

		if (CommonFunction.checkEdition("U")) {
			Composite composite4 = new Composite(commonComp, SWT.NONE);
			composite4.setLayout(gl);
			new Label(composite4, SWT.NONE).setText(Messages.getString("preference.TMDatabasePage.contextSpi"));
			contextSpi = new Spinner(composite4, SWT.BORDER);
			new Label(composite4, SWT.NONE);
			contextSpi.setMinimum(1);
			contextSpi.setMaximum(100);
		}

		Composite matchNumberComp = new Composite(commonComp, SWT.NONE);
		new Label(matchNumberComp, SWT.NONE).setText(Messages.getString("preference.TMDatabasePage.matchNumberSpi"));
		matchNumberSpi = new Spinner(matchNumberComp, SWT.BORDER);
		GridLayout mGl = new GridLayout(2, false);
		mGl.marginHeight = 0;
		mGl.marginWidth = 0;
		matchNumberComp.setLayout(mGl);
		matchNumberSpi.setMinimum(1);
		matchNumberSpi.setMaximum(10);

		
		setLabel.computeSize();// 计算标签大小///////////////

		Group sortGroup = new Group(tparent, SWT.NONE);
		sortGroup.setLayout(new GridLayout());
		sortGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sortGroup.setText(Messages.getString("preference.TMDatabasePage.sortGroup"));

		HsImageLabel sortLabel = new HsImageLabel(Messages.getString("preference.TMDatabasePage.sortLabel"),
				Activator.getImageDescriptor("images/preference/tm/trans_display_oder_32.png"));
		Composite sortComp = sortLabel.createControl(sortGroup);

		btnDefaultDBPrecedence = new Button(sortComp, SWT.RADIO);
		btnDefaultDBPrecedence.setText(Messages.getString("preference.TMDatabasePage.btnDefaultDBPrecedence"));
		btnDefaultDBPrecedence.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnDateReverse = new Button(sortComp, SWT.RADIO);
		btnDateReverse.setText(Messages.getString("preference.TMDatabasePage.btnDateReverse"));
		btnDateReverse.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		sortLabel.computeSize();

		Group updateCompGroup = new Group(tparent, SWT.NONE);
		updateCompGroup.setLayout(new GridLayout());
		updateCompGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		updateCompGroup.setText(Messages.getString("preference.TMDatabasePage.updateCompGroup"));

		HsImageLabel updateLabel = new HsImageLabel(Messages.getString("preference.TMDatabasePage.updateLabel"),
				Activator.getImageDescriptor("images/preference/tm/tm_update_32.png"));

		Composite updateComp = updateLabel.createControl(updateCompGroup);

		btnAlwaysAdd = new Button(updateComp, SWT.RADIO);
		btnAlwaysAdd.setText(Messages.getString("preference.TMDatabasePage.btnAlwaysAdd"));
		btnAlwaysAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRepeatOverwrite = new Button(updateComp, SWT.RADIO);
		btnRepeatOverwrite.setText(Messages.getString("preference.TMDatabasePage.btnRepeatOverwrite"));
		btnRepeatOverwrite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRepeatIgnore = new Button(updateComp, SWT.RADIO);
		btnRepeatIgnore.setText(Messages.getString("preference.TMDatabasePage.btnRepeatIgnore"));
		btnRepeatIgnore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		updateLabel.computeSize();

		// boolean showDialog = preferenceStore.getBoolean(DatabaseConstants.SHOW_DIALOG_EACH_OPEN_FILE);
		int intMatch = preferenceStore.getInt(TMPreferenceConstants.MATCH_PERCENTAGE_SORT_WITH_EQUAL);
		int intUpdateTM = preferenceStore.getInt(TMPreferenceConstants.TM_UPDATE);
		setInitValues(false, intMatch, intUpdateTM);

		return parent;
	}
	
	private void setTagPenaltyEnable(){
		boolean ignoreTag = btnIsIgnoreMark.getSelection();
		tagPenaltyLbl.setEnabled(!ignoreTag);
		tagPenaltySpi.setEnabled(!ignoreTag);
	}

	@Override
	protected void performDefaults() {
		// boolean openDialog = preferenceStore.getDefaultBoolean(DatabaseConstants.SHOW_DIALOG_EACH_OPEN_FILE);
		int intMatch = preferenceStore.getDefaultInt(TMPreferenceConstants.MATCH_PERCENTAGE_SORT_WITH_EQUAL);
		int intUpdateTM = preferenceStore.getDefaultInt(TMPreferenceConstants.TM_UPDATE);
		setInitValues(true, intMatch, intUpdateTM);
		setTagPenaltyEnable();
	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}
		
		// 备注，之前的 btnCaseSensitive 是区分大小写，而后来改成忽略大小写，意思恰好相反，故应取反	--robert 2012-12-10	
		preferenceStore.setValue(TMPreferenceConstants.CASE_SENSITIVE, !btnCaseSensitive.getSelection());
		preferenceStore.setValue(TMPreferenceConstants.IGNORE_MARK, btnIsIgnoreMark.getSelection());
		if (CommonFunction.checkEdition("U")) {
			preferenceStore.setValue(TMPreferenceConstants.CONTEXT_MATCH, contextSpi.getSelection());
		}
		preferenceStore.setValue(TMPreferenceConstants.MAX_MATCH_NUMBER, matchNumberSpi.getSelection());
		preferenceStore.setValue(TMPreferenceConstants.MIN_MATCH, minMatchSpi.getSelection());
		preferenceStore.setValue(TMPreferenceConstants.TAG_PENALTY, tagPenaltySpi.getSelection());

		if (btnDefaultDBPrecedence.getSelection()) {
			preferenceStore.setValue(TMPreferenceConstants.MATCH_PERCENTAGE_SORT_WITH_EQUAL,
					TMPreferenceConstants.DEFAULT_DB_PRECEDENCE);
		} else {
			preferenceStore.setValue(TMPreferenceConstants.MATCH_PERCENTAGE_SORT_WITH_EQUAL,
					TMPreferenceConstants.DATE_REVERSE_PRECEDENCE);
		}
		if (btnAlwaysAdd.getSelection()) {
			preferenceStore.setValue(TMPreferenceConstants.TM_UPDATE, TMPreferenceConstants.TM_ALWAYS_ADD);
		} else if (btnRepeatOverwrite.getSelection()) {
			preferenceStore.setValue(TMPreferenceConstants.TM_UPDATE, TMPreferenceConstants.TM_REPEAT_OVERWRITE);
		} else if (btnRepeatIgnore.getSelection()) {
			preferenceStore.setValue(TMPreferenceConstants.TM_UPDATE, TMPreferenceConstants.TM_REPEAT_IGNORE);
		}
		return true;
	}

	/**
	 * 设置默认值
	 * @param blnIsApplyDefault
	 */
	private void setInitValues(boolean blnIsApplyDefault, int intMatch, int intUpdateTM) {
		// btnOpenDialog.setSelection(openDialog);
		if (!blnIsApplyDefault) {
			btnCaseSensitive.setSelection(!preferenceStore.getBoolean(TMPreferenceConstants.CASE_SENSITIVE));
			btnIsIgnoreMark.setSelection(preferenceStore.getBoolean(TMPreferenceConstants.IGNORE_MARK));
			if (CommonFunction.checkEdition("U")) {
				contextSpi.setSelection(preferenceStore.getInt(TMPreferenceConstants.CONTEXT_MATCH));
			}
			matchNumberSpi.setSelection(preferenceStore.getInt(TMPreferenceConstants.MAX_MATCH_NUMBER));
			minMatchSpi.setSelection(preferenceStore.getInt(TMPreferenceConstants.MIN_MATCH));
			tagPenaltySpi.setSelection(preferenceStore.getInt(TMPreferenceConstants.TAG_PENALTY));
		} else {
			btnCaseSensitive.setSelection(!preferenceStore.getDefaultBoolean(TMPreferenceConstants.CASE_SENSITIVE));
			btnIsIgnoreMark.setSelection(preferenceStore.getDefaultBoolean(TMPreferenceConstants.IGNORE_MARK));
			if (CommonFunction.checkEdition("U")) {
				contextSpi.setSelection(preferenceStore.getDefaultInt(TMPreferenceConstants.CONTEXT_MATCH));
			}
			matchNumberSpi.setSelection(preferenceStore.getDefaultInt(TMPreferenceConstants.MAX_MATCH_NUMBER));
			minMatchSpi.setSelection(preferenceStore.getDefaultInt(TMPreferenceConstants.MIN_MATCH));
			tagPenaltySpi.setSelection(preferenceStore.getDefaultInt(TMPreferenceConstants.TAG_PENALTY));
		}

		if (intMatch == TMPreferenceConstants.DEFAULT_DB_PRECEDENCE) {
			btnDefaultDBPrecedence.setSelection(true);
			btnDateReverse.setSelection(false);
		} else if (intMatch == TMPreferenceConstants.DATE_REVERSE_PRECEDENCE) {
			btnDefaultDBPrecedence.setSelection(false);
			btnDateReverse.setSelection(true);
		}

		if (intUpdateTM == TMPreferenceConstants.TM_ALWAYS_ADD) {
			btnAlwaysAdd.setSelection(true);
			btnRepeatOverwrite.setSelection(false);
			btnRepeatIgnore.setSelection(false);
		} else if (intUpdateTM == TMPreferenceConstants.TM_REPEAT_OVERWRITE) {
			btnAlwaysAdd.setSelection(false);
			btnRepeatOverwrite.setSelection(true);
			btnRepeatIgnore.setSelection(false);
		} else if (intUpdateTM == TMPreferenceConstants.TM_REPEAT_IGNORE) {
			btnAlwaysAdd.setSelection(false);
			btnRepeatOverwrite.setSelection(false);
			btnRepeatIgnore.setSelection(true);
		}
		
		setTagPenaltyEnable();
	}

	public void init(IWorkbench workbench) {

	}

}
