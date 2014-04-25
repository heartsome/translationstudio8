package net.heartsome.cat.ts.ui.qa.preference;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.resource.ImageConstant;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 品质检查的首选项设置
 * @author robert 2011-11-29
 */
public class QAPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.ui.qa.preference.QAPage";

	private IPreferenceStore preferenceStore;
	// --------------------------品质检查(针对所有品质检查项)-------------------------------------//
	private Button contextBtn;
	private Button fullMatchBtn;
	private Button lockedBtn;

	// --------------------------文本段一致性检查-------------------------------------//
	/** 相同源文不同译文，备注：默认选中 */
	private Button sameSourceBtn;
	/** 相同源文不同译文下的忽略大小写 */
	private Button srcIgnorceCaseBtn;
	/** 相同源文不同译文下的忽略标记 */
	private Button srcIgnorceTagBtn;

	/** 相同译文不同源文 */
	private Button sameTargetBtn;
	/** 相同译文不同源文下的忽略大小写 */
	private Button tarIgnorceCaseBtn;
	/** 相同译文不同源文下的忽略标记 */
	private Button tarIgnorceTagBtn;

	// 以下是目标文本段长度限制的组件
	private Button minBtn;
	private Button maxBtn;
	private Text minTxt;
	private Text maxTxt;


	/** 组件是否初始化 */
	private boolean isInit = false;
	private final static String isNumericRegex = "(^[1-9](\\d{0,2})(\\.\\d+)?$)|(^0(\\.\\d+)?$)";
	
	public QAPage() {
		setTitle(Messages.getString("qa.preference.QAInstalPage.qaInstal"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;

		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group notIncludeGroup = new Group(tparent, SWT.NONE);
		notIncludeGroup.setLayout(new GridLayout());
		notIncludeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		notIncludeGroup.setText(Messages.getString("qa.preference.QAPage.notInclude"));

		HsImageLabel notIncludeLbl = new HsImageLabel(Messages.getString("preference.QAPage.notIncludeLbl"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_QA_Page_ignore));
		Composite notIncludeCmp = notIncludeLbl.createControl(notIncludeGroup);
		notIncludeCmp.setLayout(new GridLayout());
		notIncludeCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (CommonFunction.checkEdition("U")) {
			// 上下文匹配
			contextBtn = new Button(notIncludeCmp, SWT.CHECK);
			contextBtn.setText(Messages.getString("qa.preference.QAPage.content"));
		}

		// 完全匹配的文本段
		fullMatchBtn = new Button(notIncludeCmp, SWT.CHECK);
		fullMatchBtn.setText(Messages.getString("qa.preference.QAPage.fullMatch"));

		// 已锁定的文本段
		lockedBtn = new Button(notIncludeCmp, SWT.CHECK);
		lockedBtn.setText(Messages.getString("qa.preference.QAPage.locked"));
		notIncludeLbl.computeSize();

		// 文本段一致性检查的按钮区
		addParaGroup(tparent);

		addTgtLengthGroup(tparent);

		setInitValue();
		// 初始化事件
		initListener();
		return parent;
	}

	/**
	 * 这是针对文本段一致性检查的按钮区
	 * @param tparent
	 */
	public void addParaGroup(Composite tparent) {
		Group paragraphGroup = new Group(tparent, SWT.NONE);
		paragraphGroup.setText(Messages.getString("qa.preference.QAPage.paraConsistence"));
		paragraphGroup.setLayout(new GridLayout());
		paragraphGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		HsImageLabel paraConsisLbl = new HsImageLabel(Messages.getString("preference.QAPage.paraConsisLbl"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_QA_Page_paraConsistence));
		Composite paraConsisCmp = paraConsisLbl.createControl(paragraphGroup);
		paraConsisCmp.setLayout(new GridLayout(3, false));
		paraConsisCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout cmpLayout = new GridLayout(1, false);
		cmpLayout.marginLeft = 30;
		cmpLayout.marginHeight = 0;

		// -----------------------------下面是相同源文不同译文的配置-----------------------------
		sameSourceBtn = new Button(paraConsisCmp, SWT.CHECK);
		sameSourceBtn.setText(Messages.getString("qa.preference.QAPage.sameSource"));
		GridDataFactory.fillDefaults().span(3, 1).applyTo(sameSourceBtn);

		Composite sameSourceCmp = new Composite(paraConsisCmp, SWT.NONE);
		sameSourceCmp.setLayout(cmpLayout);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(sameSourceCmp);

		srcIgnorceCaseBtn = new Button(sameSourceCmp, SWT.CHECK);
		srcIgnorceCaseBtn.setText(Messages.getString("qa.preference.QAPage.ignoreCase"));

		srcIgnorceTagBtn = new Button(sameSourceCmp, SWT.CHECK);
		srcIgnorceTagBtn.setText(Messages.getString("qa.preference.QAPage.ignoreTag"));

		// -----------------------------下面是相同译文不同源文的配置-----------------------------
		sameTargetBtn = new Button(paraConsisCmp, SWT.CHECK);
		sameTargetBtn.setText(Messages.getString("qa.preference.QAPage.sameTarget"));
		GridDataFactory.fillDefaults().span(3, 1).applyTo(sameTargetBtn);

		Composite sameTargetCmp = new Composite(paraConsisCmp, SWT.NONE);
		sameTargetCmp.setLayout(cmpLayout);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(sameTargetCmp);

		tarIgnorceCaseBtn = new Button(sameTargetCmp, SWT.CHECK);
		tarIgnorceCaseBtn.setText(Messages.getString("qa.preference.QAPage.ignoreCase"));

		tarIgnorceTagBtn = new Button(sameTargetCmp, SWT.CHECK);
		tarIgnorceTagBtn.setText(Messages.getString("qa.preference.QAPage.ignoreTag"));

		paraConsisLbl.computeSize();
	}

	/**
	 * 添加目标文本段长度限制检查
	 * @param tParent
	 */
	private void addTgtLengthGroup(Composite tParent) {
		Group group = new Group(tParent, SWT.NONE);
		group.setText(Messages.getString("preference.QAPage.group"));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout());

		HsImageLabel tgtLengthSetLbl = new HsImageLabel(Messages.getString("preference.QAPage.tgtLengthSetLbl"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_QA_Page_tgtLengthSet));
		Composite tgtLengthLblCmp = tgtLengthSetLbl.createControl(group);
		tgtLengthLblCmp.setLayout(new GridLayout(3, false));
		tgtLengthLblCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tgtLengthSetCmp = new Composite(tgtLengthLblCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).equalWidth(false).numColumns(3)
				.applyTo(tgtLengthSetCmp);
		GridDataFactory.fillDefaults().span(3, SWT.DEFAULT).grab(true, true).applyTo(tgtLengthSetCmp);

		GridData txtData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		txtData.widthHint = 200;

		minBtn = new Button(tgtLengthSetCmp, SWT.CHECK);
		minBtn.setText(Messages.getString("preference.QAPage.minBtn"));
		minTxt = new Text(tgtLengthSetCmp, SWT.BORDER);
		minTxt.setLayoutData(txtData);
		Label label = new Label(tgtLengthSetCmp, SWT.NONE);
		label.setText("%");

		maxBtn = new Button(tgtLengthSetCmp, SWT.CHECK);
		maxBtn.setText(Messages.getString("preference.QAPage.maxBtn"));
		maxTxt = new Text(tgtLengthSetCmp, SWT.BORDER);
		maxTxt.setLayoutData(txtData);
		label = new Label(tgtLengthSetCmp, SWT.NONE);
		label.setText("%");
		tgtLengthSetLbl.computeSize();

		
		
		minTxt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				validMinValue(isNumericRegex);
			}
		});
		maxTxt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				validMaxValue(isNumericRegex);
			}
		});
		
		minBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				validMinValue(isNumericRegex);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				validMinValue(isNumericRegex);
			}
		});
		maxBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				validMaxValue(isNumericRegex);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				validMaxValue(isNumericRegex);
			}
		});

	}
	
	/**
	 * 当选中　缩短按钮时，进行的提示信息
	 * @param isNumericRegex
	 */
	private boolean validMinValue(String isNumericRegex){
		if (minBtn.getSelection()) {
			String minNumer = minTxt.getText();
			if ("".equals(minNumer)) {
				minTxt.setFocus();
				minBtn.setSelection(false);
				MessageDialog.openWarning(getShell(),
						Messages.getString("dialog.TargetLengthSettingDialog.msgTitle"),
						Messages.getString("preference.QAPage.msg1"));
				return false;
			} else if (!minNumer.matches(isNumericRegex)) {
				minTxt.setFocus();
				minBtn.setSelection(false);
				MessageDialog.openWarning(getShell(),
						Messages.getString("dialog.TargetLengthSettingDialog.msgTitle"),
						Messages.getString("preference.QAPage.msg2"));
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 验证最大数字是否合格
	 * @param isNumbericRegex
	 */
	private boolean validMaxValue(String isNumbericRegex){
		if (maxBtn.getSelection()) {
			String maxNumer = maxTxt.getText();
			if ("".equals(maxNumer)) {
				maxTxt.setFocus();
				maxBtn.setSelection(false);
				MessageDialog.openWarning(getShell(),
						Messages.getString("dialog.TargetLengthSettingDialog.msgTitle"),
						Messages.getString("preference.QAPage.msg3"));
				return false;
			} else if (!maxNumer.matches(isNumbericRegex)) {
				maxTxt.setFocus();
				maxBtn.setSelection(false);
				MessageDialog.openWarning(getShell(),
						Messages.getString("dialog.TargetLengthSettingDialog.msgTitle"),
						Messages.getString("preference.QAPage.msg4"));
				return false;
			}
		}
		return true;
	}
	
	

	private void setInitValue() {
		// ----------------------------------------针对所有检查项------------------------------------//
		if (CommonFunction.checkEdition("U")) {
			// 将不包含上下文匹配(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE)的按钮设成默认选中状态，在QAInitializer类中初始化
			if (preferenceStore.getBoolean(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE)) {
				contextBtn.setSelection(true);
			} else {
				contextBtn.setSelection(false);
			}
		}

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE)) {
			fullMatchBtn.setSelection(true);
		} else {
			fullMatchBtn.setSelection(false);
		}

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_LOCKED_NOTINCLUDE)) {
			lockedBtn.setSelection(true);
		} else {
			lockedBtn.setSelection(false);
		}

		// -----------------------------------------文本段一致性检查的初始值设置-----------------------------------//
		// 将相同源文不同译文(QAConstant.QA_PREF_PARA_SAMESOURCE)设成默认,在QAInitializer类中初始化

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SAMESOURCE)) {
			sameSourceBtn.setSelection(true);
		} else {
			sameSourceBtn.setSelection(false);
		}

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SRC_IGNORCECASE)) {
			srcIgnorceCaseBtn.setSelection(true);
		} else {
			srcIgnorceCaseBtn.setSelection(false);
		}

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SRC_IGNORCETAG)) {
			srcIgnorceTagBtn.setSelection(true);
		} else {
			srcIgnorceTagBtn.setSelection(false);
		}

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_SAMETARGET)) {
			sameTargetBtn.setSelection(true);
		} else {
			sameTargetBtn.setSelection(false);
		}

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_TAR_IGNORCECASE)) {
			tarIgnorceCaseBtn.setSelection(true);
		} else {
			tarIgnorceCaseBtn.setSelection(false);
		}

		if (preferenceStore.getBoolean(QAConstant.QA_PREF_PARA_TAR_IGNORCETAG)) {
			tarIgnorceTagBtn.setSelection(true);
		} else {
			tarIgnorceTagBtn.setSelection(false);
		}

		// -------------------------目标文本段长度限制设置
		minBtn.setSelection(preferenceStore.getBoolean(QAConstant.QA_PREF_isCheckTgtMinLength));
		maxBtn.setSelection(preferenceStore.getBoolean(QAConstant.QA_PREF_isCheckTgtMaxLength));
		minTxt.setText(preferenceStore.getString(QAConstant.QA_PREF_tgtMinLength));
		maxTxt.setText(preferenceStore.getString(QAConstant.QA_PREF_tgtMaxLength));
	}

	@Override
	protected void performDefaults() {
		if (CommonFunction.checkEdition("U")) {
			// 默认情况下，不包含上下文匹配的按钮为选中状态
			contextBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE));
		}
		fullMatchBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE));
		lockedBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_LOCKED_NOTINCLUDE));

		// ----------------------文本段一致性检查的灰复默认值-----------------------------------//
		// 默认情况下，不包含上下文匹配的按钮为选中状态
		sameSourceBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_PARA_SAMESOURCE));
		srcIgnorceCaseBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_PARA_SRC_IGNORCECASE));
		srcIgnorceTagBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_PARA_SRC_IGNORCETAG));
		sameTargetBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_PARA_SAMETARGET));
		tarIgnorceCaseBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_PARA_TAR_IGNORCECASE));
		tarIgnorceTagBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_PARA_TAR_IGNORCETAG));

		// -------------------------目标文本段长度限制设置
		minBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_isCheckTgtMinLength));
		maxBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.QA_PREF_isCheckTgtMaxLength));
		minTxt.setText(preferenceStore.getDefaultString(QAConstant.QA_PREF_tgtMinLength));
		maxTxt.setText(preferenceStore.getDefaultString(QAConstant.QA_PREF_tgtMaxLength));

	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}
		
		if (!validMinValue(isNumericRegex) || !validMaxValue(isNumericRegex)) {
			return false;
		}

		if (CommonFunction.checkEdition("U")) {
			// 不包含上下文匹配的文本段
			if (contextBtn.getSelection()) {
				preferenceStore.setValue(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE, true);
			} else {
				preferenceStore.setValue(QAConstant.QA_PREF_CONTEXT_NOTINCLUDE, false);
			}
		}

		// 不包含完全匹配的文本段
		if (fullMatchBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_FULLMATCH_NOTINCLUDE, false);
		}

		// 不包含已锁定的文本段
		if (lockedBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_LOCKED_NOTINCLUDE, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_LOCKED_NOTINCLUDE, false);
		}

		// ------------------------------文本段一致性检查的确定按钮点击所触发的事件
		if (sameSourceBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SAMESOURCE, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SAMESOURCE, false);
		}

		if (srcIgnorceCaseBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SRC_IGNORCECASE, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SRC_IGNORCECASE, false);
		}

		if (srcIgnorceTagBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SRC_IGNORCETAG, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SRC_IGNORCETAG, false);
		}

		if (sameTargetBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SAMETARGET, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_SAMETARGET, false);
		}

		if (tarIgnorceCaseBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_TAR_IGNORCECASE, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_TAR_IGNORCECASE, false);
		}

		if (tarIgnorceTagBtn.getSelection()) {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_TAR_IGNORCETAG, true);
		} else {
			preferenceStore.setValue(QAConstant.QA_PREF_PARA_TAR_IGNORCETAG, false);
		}

		preferenceStore.setValue(QAConstant.QA_PREF_isCheckTgtMinLength, minBtn.getSelection());
		preferenceStore.setValue(QAConstant.QA_PREF_isCheckTgtMaxLength, maxBtn.getSelection());
		preferenceStore.setValue(QAConstant.QA_PREF_tgtMinLength, minTxt.getText());
		preferenceStore.setValue(QAConstant.QA_PREF_tgtMaxLength, maxTxt.getText());

		return true;
	}

	public void initListener() {
		// 勾选相同源文不同译文时，将属于它的两个小项“忽略标记与忽略大小写”也选中。
		sameSourceBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				srcIgnorceCaseBtn.setSelection(sameSourceBtn.getSelection());
				srcIgnorceTagBtn.setSelection(sameSourceBtn.getSelection());
			}
		});
		// 勾选相同译文不同源文时，将属于它的两个小项“忽略标记与忽略大小写”也选中。
		sameTargetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tarIgnorceCaseBtn.setSelection(sameTargetBtn.getSelection());
				tarIgnorceTagBtn.setSelection(sameTargetBtn.getSelection());
			}
		});
		srcIgnorceCaseBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (srcIgnorceCaseBtn.getSelection()) {
					sameSourceBtn.setSelection(true);
				}
			}
		});
		srcIgnorceTagBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (srcIgnorceTagBtn.getSelection()) {
					sameSourceBtn.setSelection(true);
				}
			}
		});
		tarIgnorceCaseBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (tarIgnorceCaseBtn.getSelection()) {
					sameTargetBtn.setSelection(true);
				}
			}
		});
		tarIgnorceTagBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (tarIgnorceTagBtn.getSelection()) {
					sameTargetBtn.setSelection(true);
				}
			}
		});
	}
}
